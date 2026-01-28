package de.tum.cit.fop.maze.world;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;

import java.util.List;

public class GameWorld {

    private Array<Wall> walls;
    private List<Obstacle> obstacles;
    private float playerStartX, playerStartY;
    private int width, height;

    public GameWorld(Array<Wall> walls, List<Obstacle> obstacles,
                     float playerStartX, float playerStartY,
                     int width, int height) {
        this.walls = walls;
        this.obstacles = obstacles;
        this.playerStartX = playerStartX;
        this.playerStartY = playerStartY;
        this.width = width;
        this.height = height;
    }

    public void update(float delta) {
        for (Obstacle obs : obstacles) {
            obs.update(delta);
        }
    }

    public void render(SpriteBatch batch) {
        for (Wall wall : walls) {
            wall.render(batch);
        }
        for (Obstacle obs : obstacles) {
            obs.render(batch);
        }
    }

    public void dispose() {
        for (Obstacle obs : obstacles) {
            obs.dispose();
        }
        // Wall usually doesn't need dispose if using shared texture
    }

    // Getters
    public Array<Wall> getWalls() { return walls; }
    public List<Obstacle> getObstacles() { return obstacles; }
    public float getPlayerStartX() { return playerStartX; }
    public float getPlayerStartY() { return playerStartY; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}