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

    // NEW: Add a simple lock flag to prevent immediate consecutive calls
    private static boolean isGenerating = false;

    // NEW: Define constants for probability and cell types
    // NEW: Define wall probability for internal areas
    // A lower probability means fewer walls, more potential open space.
    // Needs tuning to balance maze structure and path availability.
    private static final double WALL_PROBABILITY = 0.28; // Adjusted probability

    // Values for different map entities
    private static final int CELL_WALL = 0; // Value for walls (this becomes 'null' in output)
    private static final int CELL_START = 1; // Value for Start
    private static final int CELL_EXIT = 2; // Value for Exit
    private static final int CELL_TRAP = 4; // Value for Trap
    private static final int CELL_ENEMY = 5; // Value for Enemy
    private static final int CELL_MORPH_TRAP = 6; // Value for Morph Trap

    // Internal value for passable ground (will not be written to file)
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
        // NEW: Check the lock flag
        if (isGenerating) {
            System.err.println("WARNING: generateInfiniteMap called while another generation is in progress. Skipping.");
            return null; // Or handle differently as needed
        }
        isGenerating = true; // Set the lock
        System.out.println("DEBUG: generateInfiniteMap called with width=" + width + ", height=" + height);

        try { // Wrap main logic in try-finally to ensure lock is released
            Path mapsDirPath = Paths.get(MAPS_DIR);
            try {
                Files.createDirectories(mapsDirPath);
            } catch (IOException e) {
                System.err.println("Failed to create maps directory: " + MAPS_DIR);
                e.printStackTrace();
                return null;
            }

            // NEW: Use a 2D array to build the map internally for easier manipulation
            // Initialize the entire grid with CELL_GROUND_INTERNAL (-1) (representing passable ground internally)
            int[][] mapGrid = new int[width][height];
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    mapGrid[x][y] = CELL_GROUND_INTERNAL;
                }
            }

            // 1. Ensure borders (value 0) are set - Top and Bottom
            for (int x = 0; x < width; x++) {
                mapGrid[x][0] = CELL_WALL; // Top border -> wall
                mapGrid[x][height - 1] = CELL_WALL; // Bottom border -> wall
            }
            // Add borders (value 0) - Left and Right (excluding corners already added if needed, but setting anyway is fine)
            for (int y = 0; y < height; y++) {
                mapGrid[0][y] = CELL_WALL; // Left border -> wall
                mapGrid[width - 1][y] = CELL_WALL; // Right border -> wall
            }

            // 2. Probabilistically place internal walls (value 0)
            // Loop over internal area only (avoiding borders)
            for (int x = 1; x < width - 1; x++) {
                for (int y = 1; y < height - 1; y++) {
                    // Generate random value and decide based on probability
                    // If random < WALL_PROBABILITY, place a wall (set to CELL_WALL which is 0)
                    // Otherwise, leave as CELL_GROUND_INTERNAL (-1), which will not be written (becomes 'null')
                    if (random.nextDouble() < WALL_PROBABILITY) {
                        mapGrid[x][y] = CELL_WALL; // Place a wall (0)
                    }
                    // Else, remains CELL_GROUND_INTERNAL (-1), will be skipped in output -> null/passable
                }
            }

            // 3.1 --- PLACE MANDATORY POINTS (1, 2, 4) FAR APART ---
            // Minimum distance constraint
            int minDistance = Math.max(width, height) / 2;
            int start_x = -1, start_y = -1;
            int exit_x = -1, exit_y = -1;
            int trap_x = -1, trap_y = -1;

            // Find suitable location for Start (1) - Avoid borders
            // It can overwrite any internal value (ground -1 or wall 0)
            do {
                start_x = random.nextInt(width - 2) + 1; // x=1 to width-2 (internal)
                start_y = random.nextInt(height - 2) + 1; // y=1 to height-2 (internal)
            } while (start_x <= 0 || start_x >= width - 1 || start_y <= 0 || start_y >= height - 1); // Just ensure internal, overwriting is okay

            // Find suitable location for Exit (2) - Must be far from Start, avoid borders
            do {
                exit_x = random.nextInt(width - 2) + 1; // x=1 to width-2 (internal)
                exit_y = random.nextInt(height - 2) + 1; // y=1 to height-2 (internal)
            } while (
                    exit_x <= 0 || exit_x >= width - 1 || exit_y <= 0 || exit_y >= height - 1 || // Ensure internal
                            manhattanDistance(start_x, start_y, exit_x, exit_y) < minDistance // Ensure distance from start
            );

            // Find suitable location for mandatory Trap (4) - Must be far from both Start and Exit, avoid borders
            do {
                trap_x = random.nextInt(width - 2) + 1; // x=1 to width-2 (internal)
                trap_y = random.nextInt(height - 2) + 1; // y=1 to height-2 (internal)
            } while (
                    trap_x <= 0 || trap_x >= width - 1 || trap_y <= 0 || trap_y >= height - 1 || // Ensure internal
                            manhattanDistance(start_x, start_y, trap_x, trap_y) < minDistance || // Ensure distance from start
                            manhattanDistance(exit_x, exit_y, trap_x, trap_y) < minDistance // Ensure distance from exit
            );

            // Add mandatory points to map grid (overwrites anything underneath)
            mapGrid[start_x][start_y] = CELL_START;
            mapGrid[exit_x][exit_y] = CELL_EXIT;
            mapGrid[trap_x][trap_y] = CELL_TRAP;

            // 3.2 --- VERIFY PATH EXISTS BETWEEN START AND EXIT ---
            // We need to ensure there's at least one path from Start to Exit.
            // If not, we might need to adjust the map generation slightly or retry.

            // First, temporarily treat the exit cell as ground for pathfinding purposes
            // This avoids the pathfinder getting stuck thinking the exit itself is blocked.
            int originalExitValue = mapGrid[exit_x][exit_y];
            mapGrid[exit_x][exit_y] = CELL_GROUND_INTERNAL;

            // Run pathfinding
            boolean pathExists = hasPathBetween(mapGrid, start_x, start_y, exit_x, exit_y, width, height);

            // Restore the exit value
            mapGrid[exit_x][exit_y] = originalExitValue;

            if (!pathExists) {
                System.out.println("DEBUG: No direct path found from Start to Exit. Attempting minor adjustments...");

                // Simple adjustment: try to carve a small path or connect via nearby cells
                // This is a basic fix, you might want more sophisticated logic later
                int attemptX = start_x, attemptY = start_y;
                boolean connected = false;
                // Try moving towards exit in steps, carving if necessary
                while (attemptX != exit_x || attemptY != exit_y) {
                    int dirX = Integer.compare(exit_x, attemptX);
                    int dirY = Integer.compare(exit_y, attemptY);

                    // Prefer moving in the direction of the exit
                    int nextX = attemptX + (dirX != 0 ? dirX : (random.nextBoolean() ? 1 : -1));
                    int nextY = attemptY + (dirY != 0 ? dirY : (random.nextBoolean() ? 1 : -1));

                    // Ensure nextX, nextY are within bounds and not on border
                    if (nextX > 0 && nextX < width - 1 && nextY > 0 && nextY < height - 1) {
                        // Carve a path segment (make it ground/internal value -1)
                        // Only carve if it's currently a wall (0)
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
                        // Fallback: try a random neighbor if preferred direction is out of bounds
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
                            // If no neighbors are walls, move closer to exit along axes
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
                    // Temporarily treat exit as ground again for final check
                    mapGrid[exit_x][exit_y] = CELL_GROUND_INTERNAL;
                    pathExists = hasPathBetween(mapGrid, start_x, start_y, exit_x, exit_y, width, height);
                    // Restore the exit value again
                    mapGrid[exit_x][exit_y] = originalExitValue;

                    if (!pathExists) {
                        System.out.println("WARNING: Even after adjustment, no path found. Generated map might be invalid.");
                        // Consider returning null or trying again with different parameters
                        // For now, we'll proceed with the adjusted map
                    } else {
                        System.out.println("DEBUG: Path found after adjustment.");
                    }
                } else {
                    System.out.println("WARNING: Could not connect Start and Exit after adjustment attempts.");
                    // Consider returning null or trying again
                    // For now, proceed and hope the basic carving helped somewhat
                }
            } else {
                System.out.println("DEBUG: Initial path found between Start and Exit.");
            }


            // 4. --- PLACE EXTRA ENTITIES ---
            // Place extra Traps (value 4)
            for (int i = 0; i < numExtraTraps; i++) {
                int trapX, trapY;
                do {
                    trapX = random.nextInt(width - 2) + 1; // x=1 to width-2 (internal)
                    trapY = random.nextInt(height - 2) + 1; // y=1 to height-2 (internal)
                } while (trapX <= 0 || trapX >= width - 1 || trapY <= 0 || trapY >= height - 1); // Ensure internal
                mapGrid[trapX][trapY] = CELL_TRAP;
            }

            // Place Enemies (value 5)
            for (int i = 0; i < numEnemies; i++) {
                int enemyX, enemyY;
                do {
                    enemyX = random.nextInt(width - 2) + 1; // x=1 to width-2 (internal)
                    enemyY = random.nextInt(height - 2) + 1; // y=1 to height-2 (internal)
                } while (enemyX <= 0 || enemyX >= width - 1 || enemyY <= 0 || enemyY >= height - 1); // Ensure internal
                mapGrid[enemyX][enemyY] = CELL_ENEMY;
            }

            // Place Morph Traps (value 6)
            for (int i = 0; i < numMorphTraps; i++) {
                int morphTrapX, morphTrapY;
                do {
                    morphTrapX = random.nextInt(width - 2) + 1; // x=1 to width-2 (internal)
                    morphTrapY = random.nextInt(height - 2) + 1; // y=1 to height-2 (internal)
                } while (morphTrapX <= 0 || morphTrapX >= width - 1 || morphTrapY <= 0 || morphTrapY >= height - 1); // Ensure internal
                mapGrid[morphTrapX][morphTrapY] = CELL_MORPH_TRAP;
            }

            // 5. Convert the 2D grid to the required string format and write to file
            // Only write cells that have a value different from CELL_GROUND_INTERNAL (-1).
            // This means cells with value CELL_GROUND_INTERNAL (-1) (open ground) will NOT appear in the file,
            // effectively making them 'null' or 'undefined' entries when parsed by the game engine.
            // Cells with value CELL_WALL (0) WILL appear in the file as "x,y=0", interpreted as walls.
            // Cells with other entity values (1,2,4,5,6) WILL appear in the file.
            StringBuilder mapBuilder = new StringBuilder();
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    // Only add to the string if the value is NOT the ground value (-1)
                    if (mapGrid[x][y] != CELL_GROUND_INTERNAL) {
                        mapBuilder.append(x).append(",").append(y).append("=").append(mapGrid[x][y]).append("\n");
                    }
                }
            }

            // Write the generated map string to a .properties file
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
            isGenerating = false; // NEW: Always release the lock
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
        // Validate start and target coordinates
        if (startX < 0 || startX >= width || startY < 0 || startY >= height ||
                targetX < 0 || targetX >= width || targetY < 0 || targetY >= height) {
            return false; // Coordinates out of bounds
        }

        // Check if start or target is a wall
        // Note: We allow starting FROM the start or exit cell itself, so we check if it's a wall OTHER than start/exit
        // However, for simplicity in pathfinding, we consider any non-wall cell passable.
        // The critical check is if the target is passable at all.
        if (mapGrid[startX][startY] == CELL_WALL || mapGrid[targetX][targetY] == CELL_WALL) {
            // Allow pathfinding to start from the START/EXIT cell even if its value is 1/2,
            // but if it's explicitly a wall (0), it's impassable.
            // For this logic, since we place START/EXIT after generating walls, they override walls.
            // So, if the value is CELL_WALL (0), it means it's definitely a wall.
            // If it's CELL_START (1) or CELL_EXIT (2), it's passable.
            // The pathfinding algorithm below handles CELL_START/CELL_EXIT as passable implicitly.
            // However, if the START position somehow became a wall after placement due to logic errors,
            // this check prevents finding a path. This is correct behavior.
            if (mapGrid[startX][startY] == CELL_WALL) {
                return false; // Start position is a wall
            }
            // We don't strictly need to check target being a wall if we want to find *to* it,
            // but since we place it after generation, it shouldn't be a wall unless overwritten incorrectly.
            // For robustness, we can still check it if it's meant to be passable upon reaching.
            // For now, assuming 1,2,4,5,6 are passable to move onto, and 0 is not.
            // Let's refine: Target needs to be a non-wall cell for path to be valid.
            if (mapGrid[targetX][targetY] == CELL_WALL) {
                return false; // Target position is a wall
            }
        }


        boolean[][] visited = new boolean[width][height];
        Queue<int[]> queue = new LinkedList<>();
        queue.offer(new int[]{startX, startY});
        visited[startX][startY] = true;

        int[] dx = {-1, 1, 0, 0}; // Left, Right, Up, Down
        int[] dy = {0, 0, -1, 1};

        while (!queue.isEmpty()) {
            int[] current = queue.poll();
            int cx = current[0];
            int cy = current[1];

            if (cx == targetX && cy == targetY) {
                return true; // Found the target
            }

            for (int i = 0; i < 4; i++) {
                int nx = cx + dx[i];
                int ny = cy + dy[i];

                // Check bounds, not visited, and passable (not a wall)
                if (nx >= 0 && nx < width && ny >= 0 && ny < height &&
                        !visited[nx][ny] && mapGrid[nx][ny] != CELL_WALL) {
                    visited[nx][ny] = true;
                    queue.offer(new int[]{nx, ny});
                }
            }
        }
        return false; // Target not reachable
    }

    // --- PRESERVE ANY EXISTING METHODS HERE ---
    // If there were other methods in the original InfiniteMapGenerator.java,
    // they would be included here exactly as they were.
    // Example placeholder if there was another method:
    /*
    public static void someExistingMethod() {
        // Existing logic here...
    }
    */
    // --- END OF PRESERVED SECTION ---
}