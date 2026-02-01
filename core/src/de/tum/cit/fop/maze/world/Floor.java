package de.tum.cit.fop.maze.world;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * A class representing a floor tile with a certain position on the map.
 */
public class Floor extends MapElement {

    /**
     * Constructor for class {@code Floor}.
     *
     * @param x The x position of the floor on the map.
     * @param y The y position of the floor on the map.
     */
    public Floor(float x, float y) {
        super(x, y);
        loadTexture();
    }

    /**
     * The texture of the floor.
     */
    private static Texture texture;

    /**
     * The texture to be rendered.
     */
    private static TextureRegion floorTexture;


    /**
     * Loads the texture.
     */
    private void loadTexture() {
        if (texture == null) {
            texture = new Texture("assets/images/floor.png");
            floorTexture = new TextureRegion(texture);
        }
    }

    /**
     * Renders the floor using the given SpriteBatch.
     *
     * @param batch The SpriteBatch used for rendering.
     */
    public void render(SpriteBatch batch) {
        batch.draw(floorTexture, x * 32, y * 32, 32, 32);
    }

    /**
     * Disposes of the texture to free resources.
     */
    @Override
    public void dispose() {
        if (texture != null) {
            texture.dispose();
            texture = null;
        }
    }
}
