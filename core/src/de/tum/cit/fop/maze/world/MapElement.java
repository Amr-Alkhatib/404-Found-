package de.tum.cit.fop.maze.world;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import java.util.List;

/**
 * An abstract class representing an object of the game map with a certain position.
 */
public abstract class MapElement {

    protected boolean traversable;

    // --- HITBOX EINSTELLUNGEN ---

    // 1. Wie breit bin ich, um durch Löcher zu passen? (Seitliche Schulterfreiheit)
    // 0.75f = 24 Pixel breit (passt zur optischen Größe von scale 1.5)
    private final float collisionOffset = 0.87f;

    // 2. Wie nah darf ich an eine Wand herangehen? (Abstand zur Wand)
    // 0.125f = 4 Pixel. Das entspricht genau dem Rand, den wir beim Malen freigelassen haben.
    // Wir ziehen das ab, damit die Figur die Wand optisch BERÜHREN kann.
    private final float BOX_PADDING = 0.125f;

    public MapElement(float x, float y) {
        this.x = x;
        this.y = y;
    }

    protected float x;
    protected float y;
    protected Direction currentMovementDirection;

    public float getX() { return x; }
    public float getY() { return y; }
    public void setX(float x) { this.x = x; }
    public void setY(float y) { this.y = y; }

    protected float calculateSpeed(float delta) {
        return 0f;
    }

    /**
     * Move UP.
     */
    public boolean moveUp(float delta, List<Wall> walls) {
        float speed = calculateSpeed(delta);
        float nextY = this.y + speed;

        for (Wall wall : walls) {
            // Passt meine Breite in den Gang?
            boolean xOverlap = (this.x > wall.x - collisionOffset) && (this.x < wall.x + collisionOffset);

            // Stoppen, wenn Wand über mir ist (wall.y > this.y)
            // Wir stoppen bei "Wandposition - 1 + Padding", also hauteng dran.
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

            // Stoppen bei "Wandposition + 1 - Padding"
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

    /**
     * Move LEFT.
     */
    public boolean moveLeft(float delta, List<Wall> walls) {
        float speed = calculateSpeed(delta);
        float nextX = this.x - speed;

        for (Wall wall : walls) {
            boolean yOverlap = (this.y > wall.y - collisionOffset) && (this.y < wall.y + collisionOffset);

            // Stoppen bei "Wandposition + 1 - Padding"
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

    /**
     * Move RIGHT.
     */
    public boolean moveRight(float delta, List<Wall> walls) {
        float speed = calculateSpeed(delta);
        float nextX = this.x + speed;

        for (Wall wall : walls) {
            boolean yOverlap = (this.y > wall.y - collisionOffset) && (this.y < wall.y + collisionOffset);

            // Stoppen bei "Wandposition - 1 + Padding"
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

    public void dispose() {}

    public abstract void render(SpriteBatch batch);
}