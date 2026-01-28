package de.tum.cit.fop.maze.world;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * An abstract class representing an obstacle with a certain position on the map and active status.
 */
public abstract class Obstacle extends MapElement {

    /**
     * Constructor for class {@code Obstacle}.
     *
     * @param x The x position of the obstacle on the map.
     * @param y The y position of the obstacle on the map.
     */
    public Obstacle(float x, float y) {
        super(x, y);
        active = true;
    }


    /**
     * The active status of the obstacle, meaning whether the player can interact with it.
     */
    protected boolean active;


    /**
     * Sets active to false for the obstacle.
     */
    public void deactivate() {
        active = false;
    }

    /**
     * Sets the active status of the obstacle.
     */
    public void setActive(boolean active) { // <--- Add this method
        this.active = active;
    }

    /**
     * Gets whether the obstacle is active.
     *
     * @return True if the obstacle is active, false otherwise.
     */
    public boolean isActive() {
        return active;
    }

    public abstract void render(SpriteBatch batch);

    public void update(float delta) {

    }
}