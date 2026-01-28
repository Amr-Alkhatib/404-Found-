package de.tum.cit.fop.maze.world;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GameHelper class providing static methods for handling map data and collisions in the maze world.
 */
public class GameHelper {

    /**
     * Reads a map file and constructs a map representation from it.
     *
     * @param filePath The path of the file to be read.
     * @return A map where each key is a list of two integers representing coordinates, and the value is an integer associated with these coordinates.
     */
    public static Map<List<Integer>, Integer> loadLevelData(String filePath) {
        Map<List<Integer>, Integer> map = new HashMap<>();
        try {
            Path path = Paths.get(filePath);
            List<String> lines = Files.readAllLines(path);
            for (String line : lines) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                if (line.contains("=")) {
                    String[] parts = line.split("=");
                    if (parts.length == 2) {
                        String[] xy = parts[0].split(",");
                        try {
                            if (xy.length != 2) {
                                System.err.println("Invalid coordinates in line: " + line);
                                continue;
                            }
                            int x = Integer.parseInt(xy[0].trim());
                            int y = Integer.parseInt(xy[1].trim());
                            int value = Integer.parseInt(parts[1].trim());
                            map.put(Arrays.asList(x, y), value);
                        } catch (NumberFormatException e) {
                            System.err.println("Invalid coordinates or value in line: " + line);
                        }
                    } else {
                        System.err.println("Malformed line: " + line);
                    }
                } else {
                    System.err.println("Line missing '=': " + line);
                }
            }
        } catch (IOException ignored) {
        }
        return map;
    }


    /**
     * Checks if the specified coordinates correspond to a specified game element in the maze.
     *
     * @param x        The x-coordinate to check.
     * @param y        The y-coordinate to check.
     * @param elements A list of game elements to compare against.
     * @return True if the coordinates correspond to one of the elements, false otherwise.
     */
    public static boolean isAtCoordinate(float x, float y, List<? extends MapElement> elements) {
        final float tolerance = 0.5f;
        for (MapElement element : elements) {
            if (Math.abs(x - element.getX()) < tolerance && Math.abs(y - element.getY()) < tolerance) {
                return true;
            }
        }
        return false;
    }
}