package de.tum.cit.fop.maze.world;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.List;

/**
 * A class representing an enemy with a certain position on the game map that guards a point.
 */
public class GhostEnemy extends Enemy {

    /**
     * Constructor for class {@code GhostEnemy}.
     *
     * @param x The x position of the enemy on the map.
     * @param y The y position of the enemy on the map.
     */
    public GhostEnemy(float x, float y) {
        super(x, y);
        originX = x;
        originY = y;
        super.sightRange = Constants.enemySightRange + 1;
    }

    /**
     * The x position of the point on the map to be "guarded".
     */
    private final float originX;

    /**
     * The y position of the point on the map to be "guarded".
     */
    private final float originY;


    /**
     * Overrides the idle logic to make the enemy "guard" the origin.
     *
     * @param delta The time elapsed since the last frame.
     * @param walls The list of walls.
     */
    @Override
    protected void idle(float delta, List<Wall> walls) {
        if (originX > this.x) {
            moveRight(delta, walls);
        } else if (originX < this.x) {
            moveLeft(delta, walls);
        }
        if (originY > this.y) {
            moveUp(delta, walls);
        } else if (originY < this.y) {
            moveDown(delta, walls);
        }
    }

    /**
     * Overrides the move logic, so walls are not to be avoided.
     *
     * @param delta The time elapsed since the last frame.
     * @param walls The list of walls.
     * @return True. The enemy has been moved.
     */
    @Override
    public boolean moveUp(float delta, List<Wall> walls) {
        this.y += super.calculateSpeed(delta);
        super.currentMovementDirection = Direction.UP;
        return true;
    }

    /**
     * Overrides the move logic, so walls are not to be avoided.
     *
     * @param delta The time elapsed since the last frame.
     * @param walls The list of walls.
     * @return True. The enemy has been moved.
     */
    @Override
    public boolean moveDown(float delta, List<Wall> walls) {
        this.y -= super.calculateSpeed(delta);
        super.currentMovementDirection = Direction.DOWN;
        return true;
    }

    /**
     * Overrides the move logic, so walls are not to be avoided.
     *
     * @param delta The time elapsed since the last frame.
     * @param walls The list of walls.
     * @return True. The enemy has been moved.
     */
    @Override
    public boolean moveLeft(float delta, List<Wall> walls) {
        this.x -= super.calculateSpeed(delta);
        super.currentMovementDirection = Direction.LEFT;
        return true;
    }

    /**
     * Overrides the move logic, so walls are not to be avoided.
     *
     * @param delta The time elapsed since the last frame.
     * @param walls The list of walls.
     * @return True. The enemy has been moved.
     */
    @Override
    public boolean moveRight(float delta, List<Wall> walls) {
        this.x += super.calculateSpeed(delta);
        super.currentMovementDirection = Direction.RIGHT;
        return true;
    }

    /**
     * Renders the enemy.
     *
     * @param batch The {@code SpriteBatch} used for rendering.
     */
    @Override
    public void render(SpriteBatch batch) {
        // 在绘制前检查 currentFrame 是否为 null
        if (active && currentFrame != null) {
            batch.setColor(0, 1, 2, 1);
            batch.draw(currentFrame, x * 32, y * 32, 32, 32);
            batch.setColor(1, 1, 1, 1);
        }
    }
}