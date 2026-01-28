package de.tum.cit.fop.maze.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * A class representing an entrance with a certain position on the map.
 */
public class Entrance extends Wall {

    /**
     * Constructor for class {@code Entrance}.
     *
     * @param x The x position of the entrance on the map.
     * @param y The y position of the entrance on the map.
     */
    public Entrance(float x, float y) {
        super(x, y);
        loadTextures();
        super.traversable = true;
    }


    /**
     * The texture for when the entrance is locked.
     */
    private TextureRegion lockedTexture;

    /**
     * The texture for when the entrance is unlocked.
     */
    private TextureRegion unlockedTexture;


    /**
     * Gets whether the player can go through or stand on the entrance.
     *
     * @return True if the entrance is unlocked.
     */
    public boolean isUnlocked() {
        return super.traversable;
    }

    /**
     * Makes sure the player isn't supposed to go through anymore.
     */
    public void lock() {
        super.traversable = false;
    }


    /**
     * Loads the textures.
     */
    private void loadTextures() {
        Texture texture = new Texture(Gdx.files.internal("assets/basictiles.png"));
        lockedTexture = new TextureRegion(texture, 16 * 2, 16 * 10, 16, 16);
        unlockedTexture = new TextureRegion(texture, 16, 16 * 10, 16, 16);
    }

    /**
     * Renders the entrance using the given {@code SpriteBatch}.
     *
     * @param batch The {@code SpriteBatch} used for rendering.
     */
    public void render(SpriteBatch batch) {
        TextureRegion currentTexture = isUnlocked() ? unlockedTexture : lockedTexture;
        batch.draw(currentTexture, x * 32, y * 32, 32, 32);
    }


    /**
     * Disposes of the textures to free resources.
     */
    @Override
    public void dispose() {
        if (lockedTexture != null) lockedTexture.getTexture().dispose();
        if (unlockedTexture != null) unlockedTexture.getTexture().dispose();
    }
}
