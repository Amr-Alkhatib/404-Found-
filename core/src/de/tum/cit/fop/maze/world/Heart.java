package de.tum.cit.fop.maze.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

/**
 * A class representing a heart collectable on the map and in the HUD.
 */
public class Heart extends MapElement {

    /**
     * The sheet from which the heart texture is loaded (shared among all instances).
     */
    private static Texture heartSheet;

    /**
     * The texture of a closed heart (shared).
     */
    private static TextureRegion staticHeartTexture;

    /**
     * The texture of an open heart (shared).
     */
    private static TextureRegion emptyHeartTexture;

    /**
     * An animation for the heart (per instance, but frames are shared).
     */
    private Animation<TextureRegion> animatedHeart;

    /**
     * A property signifying whether the heart has been collected.
     */
    private boolean collected;

    /**
     * Constructor for class {@code Heart}.
     *
     * @param x The x position of the heart on the map.
     * @param y The y position of the heart on the map.
     */
    public Heart(float x, float y) {
        super(x, y);
        this.collected = false;
        loadHeartResources();
    }

    /**
     * Gets the texture of a closed heart.
     *
     * @return The texture of a closed heart.
     */
    public static TextureRegion getStaticHeartTexture() {
        return staticHeartTexture;
    }

    /**
     * Gets the texture of an open heart.
     *
     * @return The texture of an open heart.
     */
    public static TextureRegion getEmptyHeartTexture() {
        return emptyHeartTexture;
    }

    /**
     * Checks if the heart has been collected.
     *
     * @return True if the heart is collected, false otherwise.
     */
    public boolean isCollected() {
        return collected;
    }

    /**
     * Marks the heart as collected.
     */
    public void collect() {
        this.collected = true;
    }

    /**
     * Loads the textures and animation for the Heart. Ensures resources are loaded only once.
     */
    private void loadHeartResources() {
        if (heartSheet == null) {
            heartSheet = new Texture(Gdx.files.internal("objects.png"));
        }
        if (staticHeartTexture == null) {
            staticHeartTexture = new TextureRegion(heartSheet, 4 * 16, 8 * 16, 16, 16);
        }
        if (emptyHeartTexture == null) {
            emptyHeartTexture = new TextureRegion(heartSheet, 8 * 16, 0, 16, 16);
        }

        if (animatedHeart == null) {
            Array<TextureRegion> heartFrames = new Array<>(TextureRegion.class);
            heartFrames.add(staticHeartTexture);
            heartFrames.add(new TextureRegion(heartSheet, 6 * 16, 0, 16, 16));
            animatedHeart = new Animation<>(0.2f, heartFrames);
            animatedHeart.setPlayMode(Animation.PlayMode.LOOP);
        }
    }

    /**
     * Renders the heart on the screen if not collected.
     *
     * @param batch The SpriteBatch used for rendering.
     * @param delta The time elapsed since the last frame, used for animation.
     */
    public void render(SpriteBatch batch, float delta) {
        if (!collected && animatedHeart != null) {
            batch.draw(animatedHeart.getKeyFrame(delta, true), x * 32, y * 32, 32, 32);
        }
    }

    /**
     * Disposes of the texture resources used by the heart (only once, via static reference).
     */
    @Override
    public void dispose() {
    }

    /**
     * Legacy render method (override from MapElement). You can call render(batch, delta) elsewhere.
     */
    @Override
    public void render(SpriteBatch batch) {
    }

    /**
     * Static method to dispose shared resources (call once when game shuts down).
     */
    public static void disposeResources() {
        if (heartSheet != null) {
            heartSheet.dispose();
            heartSheet = null;
        }
        staticHeartTexture = null;
        emptyHeartTexture = null;
    }
}