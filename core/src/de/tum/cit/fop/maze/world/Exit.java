package de.tum.cit.fop.maze.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * A class representing an exit with a certain position on the map.
 */
public class Exit extends Wall {

    /**
     * Constructor for class {@code Exit}.
     *
     * @param x The x position of the exit on the map.
     * @param y The y position of the exit on the map.
     */
    public Exit(float x, float y) {
        super(x, y);
        loadTextures();
        super.traversable = false;
    }

    public void lock() {
        super.traversable = false;
    }
    /**
     * The texture for when the exit is locked.
     */
    private TextureRegion lockedTexture;

    /**
     * The texture for when the exit is unlocked.
     */
    private TextureRegion unlockedTexture;


    /**
     * Gets whether the exit is locked.
     * @return True if the exit is locked.
     */
    public boolean isLocked() {
        return !traversable;
    }

    /**
     * Makes sure the exit is unlocked.
     */
    public void unlock() {
        super.traversable = true;
    }


    /**
     * Loads the textures.
     */
    private void loadTextures() {
        Texture texture = new Texture(Gdx.files.internal("assets/things.png"));
        lockedTexture = new TextureRegion(texture, 0, 0, 16, 16);
        unlockedTexture = new TextureRegion(texture, 0, 16 * 3, 16, 16);
    }

    /**
     * Renders the exit using the given {@code SpriteBatch}.
     *
     * @param batch The {@code SpriteBatch} used for rendering.
     */
    public void render(SpriteBatch batch) {
        TextureRegion currentTexture = isLocked() ? lockedTexture : unlockedTexture;
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
