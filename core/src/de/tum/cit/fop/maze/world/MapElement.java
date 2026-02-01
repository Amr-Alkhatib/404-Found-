package de.tum.cit.fop.maze.world;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import java.util.List;

/**
 * An abstract class representing an object of the game map with a certain position.
 */
public abstract class MapElement {

    protected boolean traversable;

    private final float collisionOffset = 0.87f;

    private final float BOX_PADDING = 0.125f;

    public MapElement(float x, float y) {
        this.x = x;
        this.y = y;
    }

    protected float x;
    protected float y;
    protected Direction currentMovementDirection;

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    protected float calculateSpeed(float delta) {
        return 0f;
    }

    public boolean moveUp(float delta, List<Wall> walls) {
        float speed = calculateSpeed(delta);
        float nextY = this.y + speed;

        for (Wall wall : walls) {
            boolean xOverlap = (this.x > wall.x - collisionOffset) && (this.x < wall.x + collisionOffset);

            if (xOverlap && wall.y > this.y && nextY >= wall.y - 1 + BOX_PADDING) {
                y = wall.y - 1 + BOX_PADDING;
                return false;
            }
        }

        if (this.y <= GameMap.getHeight() && GameMap.getHeight() <= nextY) {
            return false;
        }

        this.y += speed;
        currentMovementDirection = Direction.UP;
        return true;
    }

    /**
     * Move DOWN.
     */
    public boolean moveDown(float delta, List<Wall> walls) {
        float speed = calculateSpeed(delta);
        float nextY = this.y - speed;

        for (Wall wall : walls) {
            boolean xOverlap = (this.x > wall.x - collisionOffset) && (this.x < wall.x + collisionOffset);

            if (xOverlap && wall.y < this.y && nextY <= wall.y + 1 - BOX_PADDING) {
                y = wall.y + 1 - BOX_PADDING;
                return false;
            }
        }

        if (this.y >= 0 && 0 >= nextY) {
            return false;
        }

        y -= speed;
        currentMovementDirection = Direction.DOWN;
        return true;
    }

    public boolean moveLeft(float delta, List<Wall> walls) {
        float speed = calculateSpeed(delta);
        float nextX = this.x - speed;

        for (Wall wall : walls) {
            boolean yOverlap = (this.y > wall.y - collisionOffset) && (this.y < wall.y + collisionOffset);

            if (yOverlap && wall.x < this.x && nextX <= wall.x + 1 - BOX_PADDING) {
                x = wall.x + 1 - BOX_PADDING;
                return false;
            }
        }

        if (this.x >= 0 && 0 >= nextX) {
            return false;
        }

        x -= speed;
        currentMovementDirection = Direction.LEFT;
        return true;
    }

    public boolean moveRight(float delta, List<Wall> walls) {
        float speed = calculateSpeed(delta);
        float nextX = this.x + speed;

        for (Wall wall : walls) {
            boolean yOverlap = (this.y > wall.y - collisionOffset) && (this.y < wall.y + collisionOffset);

            if (yOverlap && wall.x > this.x && nextX >= wall.x - 1 + BOX_PADDING) {
                x = wall.x - 1 + BOX_PADDING;
                return false;
            }
        }

        if (this.x <= GameMap.getWidth() && GameMap.getWidth() <= nextX) {
            return false;
        }

        x += speed;
        currentMovementDirection = Direction.RIGHT;
        return true;
    }

    public void dispose() {
    }

    public abstract void render(SpriteBatch batch);

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }
}