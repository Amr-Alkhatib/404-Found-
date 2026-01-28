package de.tum.cit.fop.maze.world;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.Comparator;
import java.util.List;

/**
 * An abstract class representing an object of the game map with a certain position.
 */
public abstract class MapElement {

    protected boolean traversable;

    /**
     * Constructor for class {@code MapElement}.
     *
     * @param x The x position of the object on the map.
     * @param y The y position of the object on the map.
     */
    public MapElement(float x, float y) {
        this.x = x;
        this.y = y;
    }


    /**
     * The x position of the object on the game map.
     */
    protected float x;

    /**
     * The y position of the object on the game map.
     */
    protected float y;

    /**
     * The direction in which the object is supposed to be moving, should it move.
     */
    protected Direction currentMovementDirection;


    /**
     * Public getter for the x position of the object on the game map.
     *
     * @return The x position of the object on the game map.
     */
    public float getX() {
        return x;
    }

    /**
     * Public getter for the y position of the object on the game map.
     *
     * @return The y position of the object on the game map.
     */
    public float getY() {
        return y;
    }

    // --- Add these two methods ---
    /**
     * Public setter for the x position of the object on the game map.
     *
     * @param x The new x position.
     */
    public void setX(float x) {
        this.x = x;
    }

    /**
     * Public setter for the y position of the object on the game map.
     *
     * @param y The new y position.
     */
    public void setY(float y) {
        this.y = y;
    }
    // --- End of added methods ---


    /**
     * Calculates the speed with which the map element should move.
     *
     * @param delta The time elapsed since the last frame.
     * @return The speed with which the map element moves.
     */
    protected float calculateSpeed(float delta) {
        return 0f;
    }

    /**
     * Makes the map element move with a determined speed in the UP direction.
     *
     * @param delta The time elapsed since the last frame.
     * @param walls The list of walls that should be avoided.
     * @return Whether the enemy has moved. False if encountered a wall.
     */
    public boolean moveUp(float delta, List<Wall> walls) {
        float speed = calculateSpeed(delta);

        for (Wall wall : walls.stream().sorted(Comparator.comparing(Wall::getY)).toList()) {
            if (wall.x - 0.98 < this.x && this.x < wall.x + 0.98
                    && this.y <= wall.y - 1 && wall.y - 1 <= this.y + speed) {
                y = wall.y - 1;
                return false;
            }
        }

        if (this.y <= GameMap.getHeight() && GameMap.getHeight() <= this.y + speed) {
            return false;
        }

        this.y += speed;
        currentMovementDirection = Direction.UP;

        return true;
    }

    /**
     * Makes the map element move with a determined speed in the DOWN direction.
     *
     * @param delta The time elapsed since the last frame.
     * @param walls The list of walls that should be avoided.
     * @return Whether the enemy has moved. False if encountered a wall.
     */
    public boolean moveDown(float delta, List<Wall> walls) {
        float speed = calculateSpeed(delta);

        for (Wall wall : walls.stream().sorted(Comparator.comparing(Wall::getY).reversed()).toList()) {
            if (wall.x - Constants.calibratedCollision < this.x && this.x < wall.x + Constants.calibratedCollision
                    && this.y >= wall.y + 1 && wall.y + 1 >= this.y - speed) {
                y = wall.y + 1;
                return false;
            }
        }

        if (this.y >= 0 && 0 >= this.y - speed) {
            return false;
        }

        y -= speed;
        currentMovementDirection = Direction.DOWN;

        return true;
    }

    /**
     * Makes the map element move with a determined speed in the LEFT direction.
     *
     * @param delta The time elapsed since the last frame.
     * @param walls The list of walls that should be avoided.
     * @return Whether the enemy has moved. False if encountered a wall.
     */
    public boolean moveLeft(float delta, List<Wall> walls) {
        float speed = calculateSpeed(delta);

        for (Wall wall : walls.stream().sorted(Comparator.comparing(Wall::getX).reversed()).toList()) {
            if (wall.y - Constants.calibratedCollision < this.y && this.y < wall.y + Constants.calibratedCollision
                    && this.x >= wall.x + 1 && wall.x + 1 >= this.x - speed) {
                x = wall.x + 1;
                return false;
            }
        }

        if (this.x >= 0 && 0 >= this.x - speed) {
            return false;
        }

        x -= speed;
        currentMovementDirection = Direction.LEFT;

        return true;
    }

    /**
     * Makes the map element move with a determined speed in the RIGHT direction.
     *
     * @param delta The time elapsed since the last frame.
     * @param walls The list of walls that should be avoided.
     * @return Whether the enemy has moved. False if encountered a wall.
     */
    public boolean moveRight(float delta, List<Wall> walls) {
        float speed = calculateSpeed(delta);

        for (Wall wall : walls.stream().sorted(Comparator.comparing(Wall::getX)).toList()) {
            if (wall.y - Constants.calibratedCollision < this.y && this.y < wall.y + Constants.calibratedCollision
                    && this.x <= wall.x - 1 && wall.x - 1 <= this.x + speed) {
                x = wall.x - 1;
                return false;
            }
        }

        if (this.x <= GameMap.getWidth() && GameMap.getWidth() <= this.x + speed) {
            return false;
        }

        x += speed;
        currentMovementDirection = Direction.RIGHT;

        return true;
    }


    /**
     * Disposes of unneeded resources.
     */
    public void dispose() {}


    public abstract void render(SpriteBatch batch);
}