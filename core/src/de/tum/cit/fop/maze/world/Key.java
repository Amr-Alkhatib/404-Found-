package de.tum.cit.fop.maze.world;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * A class representing a key with a certain position on the map.
 */
public class Key extends MapElement {

    /**
     * Constructor for class {@code Key}.
     *
     * @param x The x position of the key on the map.
     * @param y The y position of the key on the map.
     */
    public Key(float x, float y) {
        super(x, y);
        collected = false;
        loadTextures();
    }


    /**
     * Texture for regular key.
     */
    private static TextureRegion normalTexture;

    /**
     * Texture for uncollected key.
     */
    private static TextureRegion greyedOutTexture;


    /**
     * A property signifying whether the key has been collected.
     */
    private boolean collected;


    /**
     * Returns the normal texture of the key.
     *
     * @return The normal texture of the key.
     */
    public static TextureRegion getKeyTexture() {
        return normalTexture;
    }

    /**
     * Returns the greyed-out texture of the key.
     *
     * @return The greyed-out texture of the key.
     */
    public static TextureRegion getGreyedOutKeyTexture() {
        return greyedOutTexture;
    }

    /**
     * Checks if the key has been collected.
     *
     * @return True if the key is collected, false otherwise.
     */
    public boolean isCollected() {
        return collected;
    }

    /**
     * Marks the key as collected.
     */
    public void collect() {
        this.collected = true;
    }


    /**
     * Loads the textures for the key. Ensures they are loaded only once.
     */
    private void loadTextures() {
        if (normalTexture == null) {
            Texture keySheet = new Texture("assets/images/key.png");
            normalTexture = new TextureRegion(keySheet, 16, 16);
        }
        if (greyedOutTexture == null) {
            Texture keySheet = new Texture("assets/images/keygrey.png");
            greyedOutTexture = new TextureRegion(keySheet, 16, 16);
        }
    }

    /**
     * Renders the key using the given {@code SpriteBatch}.
     *
     * @param batch The {@code SpriteBatch} used for rendering.
     */
    public void render(SpriteBatch batch) {
        if (!collected && normalTexture != null) {
            batch.draw(normalTexture, x * 32, y * 32, 32, 32);
        }
    }



    /**
     * Disposes of the textures.
     */
    @Override
    public void dispose() {
        if (normalTexture != null) {
            normalTexture.getTexture().dispose();
            normalTexture = null;
        }
        if (greyedOutTexture != null) {
            greyedOutTexture.getTexture().dispose();
            greyedOutTexture = null;
        }
    }
}
