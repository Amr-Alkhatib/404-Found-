package de.tum.cit.fop.maze.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.Comparator;
import java.util.List;

/**
 * A class representing an arrow displayed at a certain position on the map with a certain degree of rotation.
 */
public class ExitArrow {

    /**
     * Constructor for class {@code ExitArrow}
     */
    public ExitArrow() {
        loadSprite();
        rotation = 0;
    }


    /**
     * The sprite to be rendered.
     */
    private Sprite sprite;

    /**
     * The rotation of the sprite.
     */
    private double rotation;


    /**
     * Sets the position of the arrow above the character and its rotation towards the nearest exit.
     *
     * @param x     The x position of the player's character.
     * @param y     The y position of the player's character.
     * @param exits The list of exits of the map.
     */
    public void update(float x, float y, List<Exit> exits) {
        sprite.setPosition(x * 32 + 10, (y + 1) * 32);

        rotation = exits.stream()
                .sorted(Comparator.comparingDouble(e ->
                        Math.sqrt(Math.pow(e.getX() - x, 2) + Math.pow(e.getY() - y, 2))
                ))
                .map(e -> Math.toDegrees(Math.atan2(e.getY() - y, e.getX() - x)))
                .findFirst().orElse(0d);

        sprite.setRotation((float) rotation);
    }


    /**
     * Loads the sprite to be rendered.
     */
    private void loadSprite() {
        Texture sheet = new Texture(Gdx.files.internal("assets/images/exit-arrow.png"));
        sprite = new Sprite(new TextureRegion(sheet, 16, 16));
        sprite.setSize(16, 16);
        sprite.setOrigin(sprite.getWidth() / 2, sprite.getHeight() / 2);
    }

    /**
     * Renders the arrow on the screen.
     *
     * @param batch The {@code SpriteBatch} used for rendering.
     */
    public void render(SpriteBatch batch) {
        sprite.draw(batch);
    }
}
