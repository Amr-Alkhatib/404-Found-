package de.tum.cit.fop.maze.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * A class representing a boost collectable on the map and in the HUD.
 */
public class Boost extends MapElement {

    /**
     * Constructor for class {@code Boost}.
     *
     * @param x The x position of the boost on the map.
     * @param y The y position of the boost on the map.
     */
    public Boost(float x, float y) {
        super(x, y);
        this.collected = false;
        loadBoostResources();
    }


    /**
     * The texture for boost.
     */
    Texture btr;

    /**
     * The texture to be rendered.
     */
    private static TextureRegion boostTexture;


    /**
     * A property signifying whether the boost has been collected.
     */
    private boolean collected;


    /**
     * Getter for the boost texture.
     *
     * @return The boost texture.
     */
    public static TextureRegion getBoostTexture() {
        return boostTexture;
    }

    /**
     * Checks if the boost has been collected.
     *
     * @return True if the boost is collected, false otherwise.
     */
    public boolean isCollected() {
        return collected;
    }

    /**
     * Marks the boost as collected.
     */
    public void collect() {
        this.collected = true;
    }


    /**
     * Loads the texture.
     */
    private void loadBoostResources() {
        btr = new Texture(Gdx.files.internal("images\\boost.png"));
        boostTexture = new TextureRegion(btr, 0, 0, 16, 16);
    }

    /**
     * Renders the boost on the screen.
     *
     * @param batch The {@code }SpriteBatch} used for rendering.
     */
    public void render(SpriteBatch batch) {
        if (!collected) {
            batch.draw(boostTexture, x * 32, y * 32, 32, 32);
        }
    }


    /**
     * Disposes of the texture resources used by the boost.
     */
    @Override
    public void dispose() {
        if (btr != null) {
            btr.dispose();
        }
        boostTexture = null;
    }
}
