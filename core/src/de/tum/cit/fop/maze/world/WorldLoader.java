package de.tum.cit.fop.maze.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads a game world from a text-based map file.
 * Supports walls, traps, morph traps, and player start position.
 */
public class WorldLoader {

    /**
     * Loads a game world from the given map file path.
     *
     * @param mapFilePath Path to the map file (e.g., "maps/level1.txt")
     * @return A fully initialized GameWorld object.
     */
    public static GameWorld loadMap(String mapFilePath) {
        FileHandle file = Gdx.files.internal("assets/" + mapFilePath);
        if (!file.exists()) {
            throw new RuntimeException("Map file not found: " + mapFilePath);
        }

        List<String> lines = readLines(file);

        int height = lines.size();
        if (height == 0) throw new RuntimeException("Empty map file");

        String[] firstRow = lines.get(0).trim().split("\\s+");
        int width = firstRow.length;

        Array<Wall> walls = new Array<>();
        List<Obstacle> obstacles = new ArrayList<>();
        float playerStartX = -1, playerStartY = -1;

        for (int y = 0; y < height; y++) {
            String[] tokens = lines.get(y).trim().split("\\s+");
            if (tokens.length != width) {
                throw new RuntimeException("Inconsistent row length at line " + (y + 1));
            }

            for (int x = 0; x < width; x++) {
                int tileType = Integer.parseInt(tokens[x].trim());

                switch (tileType) {
                    case 1:
                        walls.add(new Wall(x, y));
                        break;
                    case 3:
                        obstacles.add(new Trap(x, y));
                        break;
                    case 6:
                        obstacles.add(new MorphTrap(x, y));
                        break;
                    case 2:
                        if (playerStartX != -1) {
                            throw new RuntimeException("Multiple player start positions!");
                        }
                        playerStartX = x;
                        playerStartY = y;
                        break;
                    case 0:
                        break;
                    default:
                        System.out.println("Warning: Unknown tile type " + tileType + " at (" + x + "," + y + ")");
                }
            }
        }

        if (playerStartX == -1) {
            throw new RuntimeException("No player start position (type 2) found in map!");
        }

        return new GameWorld(walls, obstacles, playerStartX, playerStartY, width, height);
    }

    private static List<String> readLines(FileHandle file) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.read()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read map file", e);
        }
        return lines;
    }
}