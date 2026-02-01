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
    private final GameMap gameMap;
    private final Stage stage;

    private Table topLeftTable;
    private Table topRightTable;

    private Image keyImage;
    private Image boostImage;
    private final List<Image> bookImages = new ArrayList<>();


    private final TextureRegionDrawable keyNormalDrawable;
    private final TextureRegionDrawable keyGreyedDrawable;
    private final TextureRegionDrawable bookFilledDrawable;
    private final TextureRegionDrawable bookEmptyDrawable;
    private final TextureRegionDrawable boostDrawable;

    private static TextureRegionDrawable createErrorDrawable(String name) {
        Pixmap pixmap = new Pixmap(16, 16, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.RED);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        System.err.println("ERROR: Failed to load texture for '" + name + "', using red error texture instead.");
        return new TextureRegionDrawable(new TextureRegion(texture));
    }

    public Hud(GameMap gameMap, Viewport viewport) {
        this.gameMap = gameMap;
        this.stage = new Stage(viewport);

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

        TextureRegion boostTexture = Boost.getBoostTexture();
        if (boostTexture == null) {
            boostDrawable = createErrorDrawable("Boost");
        } else {
            boostDrawable = new TextureRegionDrawable(boostTexture);
        }

        setUpHud();
    }

    private void setUpHud() {
        topLeftTable = new Table();
        topLeftTable.top().left();
        topLeftTable.setFillParent(true);
        topLeftTable.pad(20);
        stage.addActor(topLeftTable);

        topRightTable = new Table();
        topRightTable.top().right();
        topRightTable.setFillParent(true);
        topRightTable.pad(40);
        stage.addActor(topRightTable);

        keyImage = new Image(keyGreyedDrawable);
        keyImage.setScale(4);
        topRightTable.add(keyImage).padRight(60);

        boostImage = new Image(boostDrawable);
        boostImage.setScale(4);
        boostImage.setVisible(false);
        topRightTable.add(boostImage).padRight(50);

        int initialBooks = gameMap.getPlayer().getHeartsCollected();
        for (int i = 0; i < Constants.characterMaxBooks; i++) {
            boolean filled = (i < initialBooks);
            Image bookImage = new Image(filled ? bookFilledDrawable : bookEmptyDrawable);
            bookImage.setScale(4);
            bookImages.add(bookImage);

            if (i == Constants.characterMaxBooks - 1) {
                topRightTable.add(bookImage).padLeft(15);
            } else {
                topRightTable.add(bookImage).padLeft(15).padRight(15);
            }
        }
    }

    public void update() {
        boostImage.setVisible(gameMap.getPlayer().isBoosted());

        boolean hasKey = false;
        List<Key> keys = gameMap.getKeys();
        if (keys != null) {
            for (Key key : keys) {
                if (key != null && key.isCollected()) {
                    hasKey = true;
                    break;
                }
            }
        }
        keyImage.setDrawable(hasKey ? keyNormalDrawable : keyGreyedDrawable);

        int booksCollected = gameMap.getPlayer().getHeartsCollected();
        for (int i = 0; i < bookImages.size(); i++) {
            Image bookImage = bookImages.get(i);
            if (i < Constants.characterMaxBooks) {
                bookImage.setDrawable(
                        i < booksCollected ? bookFilledDrawable : bookEmptyDrawable
                );
            }
        }
    }

    public void animateKeyCollection() {
        keyImage.clearActions();
        keyImage.setScale(4);
        keyImage.addAction(Actions.sequence(
                Actions.scaleTo(6, 6, 0.3f),
                Actions.scaleTo(4, 4, 0.3f)
        ));
    }

    public Stage getStage() {
        return stage;
    }

    public void draw() {
        stage.draw();
    }
}