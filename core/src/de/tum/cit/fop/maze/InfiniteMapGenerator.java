package de.tum.cit.fop.maze;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

public class InfiniteMapGenerator {
    private static final Random random = new Random();
    private static final String MAPS_DIR = "maps/";

    private static boolean isGenerating = false;

    private static final double WALL_PROBABILITY = 0.28; // Adjusted probability

    private static final int CELL_WALL = 0;
    private static final int CELL_START = 1;
    private static final int CELL_EXIT = 2;
    private static final int CELL_TRAP = 4;
    private static final int CELL_ENEMY = 5;
    private static final int CELL_MORPH_TRAP = 6;

    private static final int CELL_GROUND_INTERNAL = -1;

    /**
     * Generates an infinite map string in the required format and saves it to a file.
     * Uses probabilistic generation for internal areas to create maze-like structures.
     * Walls (0) are placed randomly inside AND boundaries are always walls (0).
     * Other entities (1, 2, 4, 5, 6) overwrite any existing value (including 0 or default).
     * Ensures Start (1), Exit (2), and one Trap (4) are placed internally and far apart.
     * Outputs only non-zero (non-wall) cells to represent 'null' (passable) ground.
     * Includes a basic lock to prevent immediate consecutive calls.
     * Adds connectivity verification between Start and Exit.
     *
     * @param width The width of the map grid.
     * @param height The height of the map grid.
     * @param numExtraTraps Number of additional trap cells (value 4) to place (beyond the mandatory one).
     * @param numEnemies Number of enemy cells (value 5) to place.
     * @param numMorphTraps Number of morph trap cells (value 6) to place.
     * @return The path to the file containing the map string, or null on error.
     */
    public static String generateInfiniteMap(int width, int height, int numExtraTraps, int numEnemies, int numMorphTraps) {
        if (isGenerating) {
            System.err.println("WARNING: generateInfiniteMap called while another generation is in progress. Skipping.");
            return null;
        }
        isGenerating = true;
        System.out.println("DEBUG: generateInfiniteMap called with width=" + width + ", height=" + height);

        try {
            Path mapsDirPath = Paths.get(MAPS_DIR);
            try {
                Files.createDirectories(mapsDirPath);
            } catch (IOException e) {
                System.err.println("Failed to create maps directory: " + MAPS_DIR);
                e.printStackTrace();
                return null;
            }

            int[][] mapGrid = new int[width][height];
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    mapGrid[x][y] = CELL_GROUND_INTERNAL;
                }
            }

            for (int x = 0; x < width; x++) {
                mapGrid[x][0] = CELL_WALL;
                mapGrid[x][height - 1] = CELL_WALL;
            }

            for (int y = 0; y < height; y++) {
                mapGrid[0][y] = CELL_WALL;
                mapGrid[width - 1][y] = CELL_WALL;
            }

            for (int x = 1; x < width - 1; x++) {
                for (int y = 1; y < height - 1; y++) {
                    if (random.nextDouble() < WALL_PROBABILITY) {
                        mapGrid[x][y] = CELL_WALL;
                    }
                }
            }

            int minDistance = Math.max(width, height) / 2;
            int start_x = -1, start_y = -1;
            int exit_x = -1, exit_y = -1;
            int trap_x = -1, trap_y = -1;

            do {
                start_x = random.nextInt(width - 2) + 1;
                start_y = random.nextInt(height - 2) + 1;
            } while (start_x <= 0 || start_x >= width - 1 || start_y <= 0 || start_y >= height - 1);

            // Find suitable location for Exit (2) - Must be far from Start, avoid borders
            do {
                exit_x = random.nextInt(width - 2) + 1;
                exit_y = random.nextInt(height - 2) + 1;
            } while (
                    exit_x <= 0 || exit_x >= width - 1 || exit_y <= 0 || exit_y >= height - 1 ||
                            manhattanDistance(start_x, start_y, exit_x, exit_y) < minDistance
            );

            do {
                trap_x = random.nextInt(width - 2) + 1;
                trap_y = random.nextInt(height - 2) + 1;
            } while (
                    trap_x <= 0 || trap_x >= width - 1 || trap_y <= 0 || trap_y >= height - 1 ||
                            manhattanDistance(start_x, start_y, trap_x, trap_y) < minDistance ||
                            manhattanDistance(exit_x, exit_y, trap_x, trap_y) < minDistance
            );

            mapGrid[start_x][start_y] = CELL_START;
            mapGrid[exit_x][exit_y] = CELL_EXIT;
            mapGrid[trap_x][trap_y] = CELL_TRAP;

            int originalExitValue = mapGrid[exit_x][exit_y];
            mapGrid[exit_x][exit_y] = CELL_GROUND_INTERNAL;

            boolean pathExists = hasPathBetween(mapGrid, start_x, start_y, exit_x, exit_y, width, height);

            mapGrid[exit_x][exit_y] = originalExitValue;

            if (!pathExists) {
                System.out.println("DEBUG: No direct path found from Start to Exit. Attempting minor adjustments...");

                int attemptX = start_x, attemptY = start_y;
                boolean connected = false;
                while (attemptX != exit_x || attemptY != exit_y) {
                    int dirX = Integer.compare(exit_x, attemptX);
                    int dirY = Integer.compare(exit_y, attemptY);

                    int nextX = attemptX + (dirX != 0 ? dirX : (random.nextBoolean() ? 1 : -1));
                    int nextY = attemptY + (dirY != 0 ? dirY : (random.nextBoolean() ? 1 : -1));

                    if (nextX > 0 && nextX < width - 1 && nextY > 0 && nextY < height - 1) {
                        if (mapGrid[nextX][nextY] == CELL_WALL) {
                            mapGrid[nextX][nextY] = CELL_GROUND_INTERNAL;
                            System.out.println("DEBUG: Carved path segment at (" + nextX + "," + nextY + ")");
                        }
                        attemptX = nextX;
                        attemptY = nextY;
                        if (attemptX == exit_x && attemptY == exit_y) {
                            connected = true;
                            break;
                        }
                    } else {
                        int[] dirs = {-1, 0, 1};
                        boolean carved = false;
                        for (int dx : dirs) {
                            for (int dy : dirs) {
                                if (dx == 0 && dy == 0) continue;
                                int nx = attemptX + dx;
                                int ny = attemptY + dy;
                                if (nx > 0 && nx < width - 1 && ny > 0 && ny < height - 1 && mapGrid[nx][ny] == CELL_WALL) {
                                    mapGrid[nx][ny] = CELL_GROUND_INTERNAL;
                                    System.out.println("DEBUG: Carved fallback path segment at (" + nx + "," + ny + ")");
                                    attemptX = nx;
                                    attemptY = ny;
                                    carved = true;
                                    break;
                                }
                            }
                            if (carved) break;
                        }
                        if (!carved) {
                            if (attemptX < exit_x) attemptX++;
                            else if (attemptX > exit_x) attemptX--;
                            if (attemptY < exit_y) attemptY++;
                            else if (attemptY > exit_y) attemptY--;
                        }
                        if (attemptX == exit_x && attemptY == exit_y) {
                            connected = true;
                            break;
                        }
                    }
                }

                if (connected) {
                    System.out.println("DEBUG: Basic connection made, checking path again...");
                    mapGrid[exit_x][exit_y] = CELL_GROUND_INTERNAL;
                    pathExists = hasPathBetween(mapGrid, start_x, start_y, exit_x, exit_y, width, height);

                    mapGrid[exit_x][exit_y] = originalExitValue;

                    if (!pathExists) {
                        System.out.println("WARNING: Even after adjustment, no path found. Generated map might be invalid.");

                    } else {
                        System.out.println("DEBUG: Path found after adjustment.");
                    }
                } else {
                    System.out.println("WARNING: Could not connect Start and Exit after adjustment attempts.");
                }
            } else {
                System.out.println("DEBUG: Initial path found between Start and Exit.");
            }

            for (int i = 0; i < numExtraTraps; i++) {
                int trapX, trapY;
                do {
                    trapX = random.nextInt(width - 2) + 1;
                    trapY = random.nextInt(height - 2) + 1;
                } while (trapX <= 0 || trapX >= width - 1 || trapY <= 0 || trapY >= height - 1);
                mapGrid[trapX][trapY] = CELL_TRAP;
            }

            for (int i = 0; i < numEnemies; i++) {
                int enemyX, enemyY;
                do {
                    enemyX = random.nextInt(width - 2) + 1;
                    enemyY = random.nextInt(height - 2) + 1;
                } while (enemyX <= 0 || enemyX >= width - 1 || enemyY <= 0 || enemyY >= height - 1); // Ensure internal
                mapGrid[enemyX][enemyY] = CELL_ENEMY;
            }

            for (int i = 0; i < numMorphTraps; i++) {
                int morphTrapX, morphTrapY;
                do {
                    morphTrapX = random.nextInt(width - 2) + 1;
                    morphTrapY = random.nextInt(height - 2) + 1;
                } while (morphTrapX <= 0 || morphTrapX >= width - 1 || morphTrapY <= 0 || morphTrapY >= height - 1);
                mapGrid[morphTrapX][morphTrapY] = CELL_MORPH_TRAP;
            }

            StringBuilder mapBuilder = new StringBuilder();
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {

                    if (mapGrid[x][y] != CELL_GROUND_INTERNAL) {
                        mapBuilder.append(x).append(",").append(y).append("=").append(mapGrid[x][y]).append("\n");
                    }
                }
            }


            String fileName = "infinite_map_" + System.currentTimeMillis() + "_" + width + "x" + height + ".properties";
            Path filePath = mapsDirPath.resolve(fileName);
            try (FileWriter writer = new FileWriter(filePath.toFile())) {
                writer.write(mapBuilder.toString());
                System.out.println("Generated infinite map file saved to: " + filePath.toString());
                return filePath.toString();
            } catch (IOException e) {
                System.err.println("Error writing generated map to file: " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        } finally {
            isGenerating = false;
        }
    }

    /**
     * Calculates the Manhattan distance between two points.
     * @param x1 X coordinate of point 1
     * @param y1 Y coordinate of point 1
     * @param x2 X coordinate of point 2
     * @param y2 Y coordinate of point 2
     * @return The Manhattan distance.
     */
    private static int manhattanDistance(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }

    /**
     * Checks if there is a path between two points using Breadth-First Search (BFS).
     * Considers cells with value CELL_GROUND_INTERNAL (-1) and CELL_START (1), CELL_EXIT (2), etc. as passable.
     * Considers CELL_WALL_INTERNAL (0) as impassable.
     *
     * @param mapGrid The internal representation of the map grid.
     * @param startX X coordinate of the start point.
     * @param startY Y coordinate of the start point.
     * @param targetX X coordinate of the target point.
     * @param targetY Y coordinate of the target point.
     * @param width Width of the map grid.
     * @param height Height of the map grid.
     * @return True if a path exists, false otherwise.
     */
    private static boolean hasPathBetween(int[][] mapGrid, int startX, int startY, int targetX, int targetY, int width, int height) {

        if (startX < 0 || startX >= width || startY < 0 || startY >= height ||
                targetX < 0 || targetX >= width || targetY < 0 || targetY >= height) {
            return false;
        }

        if (mapGrid[startX][startY] == CELL_WALL || mapGrid[targetX][targetY] == CELL_WALL) {

            if (mapGrid[startX][startY] == CELL_WALL) {
                return false;
            }

            if (mapGrid[targetX][targetY] == CELL_WALL) {
                return false;
            }
        }

        boolean[][] visited = new boolean[width][height];
        Queue<int[]> queue = new LinkedList<>();
        queue.offer(new int[]{startX, startY});
        visited[startX][startY] = true;

        int[] dx = {-1, 1, 0, 0};
        int[] dy = {0, 0, -1, 1};

        while (!queue.isEmpty()) {
            int[] current = queue.poll();
            int cx = current[0];
            int cy = current[1];

            if (cx == targetX && cy == targetY) {
                return true;
            }

            for (int i = 0; i < 4; i++) {
                int nx = cx + dx[i];
                int ny = cy + dy[i];

                if (nx >= 0 && nx < width && ny >= 0 && ny < height &&
                        !visited[nx][ny] && mapGrid[nx][ny] != CELL_WALL) {
                    visited[nx][ny] = true;
                    queue.offer(new int[]{nx, ny});
                }
            }
        }
        return false;
    }
}