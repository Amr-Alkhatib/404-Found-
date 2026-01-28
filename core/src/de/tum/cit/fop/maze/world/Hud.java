package de.tum.cit.fop.maze.world;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.ArrayList;
import java.util.List;

/**
 * Heads-Up Display (HUD) for showing player status.
 * Displays time (top-left) and resources (top-right).
 */
public class Hud {

    // Core References
    private final GameMap gameMap;
    private final Stage stage;

    // Tables
    private Table topLeftTable;   // Reserved for timer (added by GameManager)
    private Table topRightTable;  // Key / Boost / Hearts

    // UI Elements
    private Image keyImage;
    private Image boostImage;
    private final List<Image> bookImages = new ArrayList<>();

    // Drawables
    private final TextureRegionDrawable keyNormalDrawable;
    private final TextureRegionDrawable keyGreyedDrawable;
    private final TextureRegionDrawable bookFilledDrawable;
    private final TextureRegionDrawable bookEmptyDrawable;
    private final TextureRegionDrawable boostDrawable;

    // NEW: Helper method to create a simple error texture (red square)
    private static TextureRegionDrawable createErrorDrawable(String name) {
        Pixmap pixmap = new Pixmap(16, 16, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.RED);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose(); // Free memory
        System.err.println("ERROR: Failed to load texture for '" + name + "', using red error texture instead.");
        return new TextureRegionDrawable(new TextureRegion(texture));
    }

    // Constructor
    public Hud(GameMap gameMap, Viewport viewport) {
        this.gameMap = gameMap;
        this.stage = new Stage(viewport);

        // NEW: Load textures and check for null, assign drawables
        // Key textures
        TextureRegion keyNormalTexture = Key.getKeyTexture();
        if (keyNormalTexture == null) {
            keyNormalDrawable = createErrorDrawable("Key Normal");
        } else {
            keyNormalDrawable = new TextureRegionDrawable(keyNormalTexture);
        }

        TextureRegion keyGreyedTexture = Key.getGreyedOutKeyTexture();
        if (keyGreyedTexture == null) {
            keyGreyedDrawable = createErrorDrawable("Key Greyed Out");
        } else {
            keyGreyedDrawable = new TextureRegionDrawable(keyGreyedTexture);
        }

        // Heart textures
        TextureRegion bookFilledTexture = Heart.getStaticHeartTexture();
        if (bookFilledTexture == null) {
            bookFilledDrawable = createErrorDrawable("Heart Filled");
        } else {
            bookFilledDrawable = new TextureRegionDrawable(bookFilledTexture);
        }

        TextureRegion bookEmptyTexture = Heart.getEmptyHeartTexture();
        if (bookEmptyTexture == null) {
            bookEmptyDrawable = createErrorDrawable("Heart Empty");
        } else {
            bookEmptyDrawable = new TextureRegionDrawable(bookEmptyTexture);
        }

        // Boost texture
        TextureRegion boostTexture = Boost.getBoostTexture();
        if (boostTexture == null) {
            boostDrawable = createErrorDrawable("Boost");
        } else {
            boostDrawable = new TextureRegionDrawable(boostTexture);
        }

        setUpHud();
    }

    // Setup HUD layout
    private void setUpHud() {
        // ===== Top Left: Timer placeholder =====
        topLeftTable = new Table();
        topLeftTable.top().left();
        topLeftTable.setFillParent(true);
        topLeftTable.pad(20);
        stage.addActor(topLeftTable);

        // ===== Top Right: Status icons =====
        topRightTable = new Table();
        topRightTable.top().right();
        topRightTable.setFillParent(true);
        topRightTable.pad(40); // Overall padding from screen edge
        stage.addActor(topRightTable);

        // Key icon - Initialize with greyed drawable
        keyImage = new Image(keyGreyedDrawable); // Now guaranteed to be non-null
        keyImage.setScale(4);
        topRightTable.add(keyImage).padRight(60); // Push hearts farther right

        // Boost icon - Initialize with boost drawable and hide it initially
        boostImage = new Image(boostDrawable); // Now guaranteed to be non-null
        boostImage.setScale(4);
        boostImage.setVisible(false); // NEW: Hide boost icon initially
        topRightTable.add(boostImage).padRight(50); // Extra space before hearts

        // Hearts (Books) — spaced out
        int initialBooks = gameMap.getPlayer().getHeartsCollected();
        for (int i = 0; i < Constants.characterMaxBooks; i++) {
            boolean filled = (i < initialBooks);
            // Use the correct drawable based on loading result
            Image bookImage = new Image(filled ? bookFilledDrawable : bookEmptyDrawable);
            bookImage.setScale(4);
            bookImages.add(bookImage);

            if (i == Constants.characterMaxBooks - 1) {
                // Last heart: no right padding
                topRightTable.add(bookImage).padLeft(15);
            } else {
                // Others: add spacing between hearts
                topRightTable.add(bookImage).padLeft(15).padRight(15);
            }
        }
    }

    // Update HUD based on game state
    public void update() {
        // Boost visibility - Use setVisible instead of setting Drawable to null (CORRECTED)
        boostImage.setVisible(gameMap.getPlayer().isBoosted());

        // Key visibility - Iterate through keys safely
        boolean hasKey = false;
        List<Key> keys = gameMap.getKeys(); // Get the list once
        if (keys != null) { // Check if list is not null
            for (Key key : keys) {
                if (key != null && key.isCollected()) { // Check if key object is not null
                    hasKey = true;
                    break;
                }
            }
        }
        // Set the appropriate drawable based on whether a key was found
        keyImage.setDrawable(hasKey ? keyNormalDrawable : keyGreyedDrawable);

        // Hearts (Books) - Update based on collected count
        int booksCollected = gameMap.getPlayer().getHeartsCollected();
        for (int i = 0; i < bookImages.size(); i++) {
            Image bookImage = bookImages.get(i);
            // Ensure we don't access beyond the initial size if characterMaxBooks changed unexpectedly
            if (i < Constants.characterMaxBooks) {
                bookImage.setDrawable(
                        i < booksCollected ? bookFilledDrawable : bookEmptyDrawable
                );
            }
        }
    }

    // Animate key collection
    public void animateKeyCollection() {
        keyImage.clearActions();
        keyImage.setScale(4);
        keyImage.addAction(Actions.sequence(
                Actions.scaleTo(6, 6, 0.3f),
                Actions.scaleTo(4, 4, 0.3f)
        ));
    }

    // Accessors
    public Stage getStage() {
        return stage;
    }

    public void draw() {
        stage.draw(); // This should now be safe if all drawables are initialized
    }
}