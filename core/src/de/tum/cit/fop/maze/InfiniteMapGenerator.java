// File: core/src/de/tum/cit/fop/maze/InfiniteMapGenerator.java

package de.tum.cit.fop.maze;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    private static final int CELL_WALL = 0;   // Value for walls (this becomes 'null' in output)
    private static final int CELL_START = 1;  // Value for Start
    private static final int CELL_EXIT = 2;   // Value for Exit
    private static final int CELL_TRAP = 4;   // Value for Trap
    private static final int CELL_ENEMY = 5;  // Value for Enemy
    private static final int CELL_MORPH_TRAP = 6; // Value for Morph Trap


    /**
     * Generates an infinite map string in the required format and saves it to a file.
     * Uses probabilistic generation for internal areas to create maze-like structures.
     * Walls (0) are placed randomly inside AND boundaries are always walls (0).
     * Other entities (1, 2, 4, 5, 6) overwrite any existing value (including 0 or default).
     * Ensures Start (1), Exit (2), and one Trap (4) are placed internally and far apart.
     * Outputs only non-zero (non-wall) cells to represent 'null' (passable) ground.
     * Includes a basic lock to prevent immediate consecutive calls.
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
        System.out.println("DEBUG: generateInfiniteMap called with width=" + width + ", height=" + height); // Keep debug print

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
            // Initialize the entire grid with 0 (wall).
            int[][] mapGrid = new int[width][height]; // Default initialization is 0 for all elements

            // 1. Ensure borders (value 0) are set - Top and Bottom
            for (int x = 0; x < width; x++) {
                mapGrid[x][0] = CELL_WALL; // Top border
                mapGrid[x][height - 1] = CELL_WALL; // Bottom border
            }

            // Add borders (value 0) - Left and Right (excluding corners already added if needed, but setting anyway is fine)
            for (int y = 0; y < height; y++) {
                mapGrid[0][y] = CELL_WALL; // Left border
                mapGrid[width - 1][y] = CELL_WALL; // Right border
            }

            // 2. Probabilistically place internal non-walls (i.e., set them to a value that represents passable ground, which is 'null' when outputting)
            // We will set internal cells to 9 (or any non-zero, non-entity value) initially if they are not walls.
            // Then, during output, we only write non-zero entity values (1, 2, 4, 5, 6).
            // This means cells remaining as 0 (wall) or 9 (ground) will be 'null' in the output.
            // Let's use 0 for walls and keep other entity values as is.
            // So, we probabilistically *keep* internal cells as 0 (wall) or let them remain 0 (still wall) or change them to... wait.
            // NO. The default is 0 (wall). We want to make some of them *not* walls (i.e., passable, represented by 'null').
            // So, we iterate internal cells. If random < prob, keep as 0 (wall). Otherwise, set to a temporary "ground" value (e.g., 9).
            // Actually, no need for a temp value. We just need to decide which internal cells become walls (0) and which stay as... what?
            // The output logic says: if mapGrid[x][y] != 0, write it. This means 0 becomes 'null'.
            // So, to make a cell passable ('null'), we LEAVE it as 0.
            // To make a cell a wall, we SET it to 0.
            // Wait, that's backwards. Let me re-read.
            // "墙为0，路为null"
            // Game logic parses the file. Unmentioned coordinates are 'null' -> passable.
            // Coordinates mentioned like "x,y=0" mean that specific cell is a wall.
            // So, to create a passable area, we do NOT put an entry for that x,y in the file.
            // To create a wall, we DO put an entry like "x,y=0".
            // Therefore, in our internal grid (mapGrid):
            // - Boundary cells should end up as 0, so they get written as "x,y=0" -> wall.
            // - Internal cells that should be walls need to be set to 0, so they get written as "x,y=0" -> wall.
            // - Internal cells that should be passable (null) must REMAIN 0 initially, but then be skipped during output.
            // - Entities (1, 2, 4, 5, 6) go into mapGrid, and get written.
            // So, default int array value is 0. Boundaries -> set to 0 (already default, but set anyway for clarity).
            // Internal cells -> If random < WALL_PROBABILITY -> set to 0 (to be written as wall). Else -> leave as 0 (to be skipped, representing null/passable).
            // No, this is wrong again. If I leave internal cells as 0, they will be written because the output loop checks `if (mapGrid[x][y] != 0)`.
            // Ah! I see the confusion. The *output condition* `if (mapGrid[x][y] != 0)` means only NON-ZERO values are written.
            // So, if the *internal* grid has a 0, it represents a wall *internally*, but it will NOT be written to the file because `0 != 0` is false.
            // This means internally 0 means wall, but outputting 0 makes it null!
            // NO! The requirement is "墙为0". This means in the *output file*, a wall is represented by `x,y=0`.
            // So, internally, a wall must also be 0, AND it must be written to the file.
            // The output condition `if (mapGrid[x][y] != 0)` implies that internally, 0 means something that should NOT be written (i.e., passable/null).
            // But the requirement "墙为0" contradicts this unless the game engine interprets the *presence* of `x,y=0` differently.
            // Let's assume the output logic is fixed: ONLY non-zero values are written to the file.
            // Then, for "路为null", internal value for passable ground must be 0 (so it's not written).
            // For "墙为0", internal value for wall must be 0, but then it won't be written!
            // This is a contradiction in the output format description vs. the requirement "墙为0".
            // UNLESS: The game engine specifically looks for `x,y=0` and treats *that specific assignment* as a wall, while absence means passable.
            // Yes, that seems to be the case based on the context and your statement.
            // So:
            // Internal Grid Value | Output File Entry | Game Interpretation
            //         0           |    NOT WRITTEN    |      PASSABLE (null)
            //         0           |   x,y=0 WRITTEN   |       WALL (!)
            // Internal grid value 0 is ambiguous. We need a way to distinguish "internal ground (don't write)" from "internal wall (write as 0)".
            // Option 1: Change output logic to write ALL cells, interpreting 0 as wall, non-0 as entity, maybe -1 as passable ground.
            // Option 2: Keep output logic, make internal ground != 0, make internal wall = 0, make entities != 0 and != internal ground val.
            // Option 2 seems less disruptive if output logic is fixed elsewhere.
            // Let's use a different internal value for passable ground. How about -1? And keep wall as 0.
            // Internal Ground = -1 -> Output: NOT written -> Game: null (passable)
            // Internal Wall   =  0 -> Output: x,y=0 written -> Game: 0 (wall)
            // Internal Entity =  1,2,4,5,6 -> Output: x,y=1,2,4,5,6 written -> Game: 1,2,4,5,6 (entity)
            // This fits! Let's initialize grid with -1 (ground) and then set walls (0).

            // Re-initialize the grid with -1 (representing passable ground internally)
            int GROUND_VALUE = -1; // Internal value for passable ground (will not be written to file)
            mapGrid = new int[width][height]; // Default is 0, need to set to -1 first
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    mapGrid[x][y] = GROUND_VALUE;
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
                    // Otherwise, leave as GROUND_VALUE (-1), which will not be written (becomes 'null')
                    if (random.nextDouble() < WALL_PROBABILITY) {
                        mapGrid[x][y] = CELL_WALL; // Place a wall (0)
                    }
                    // Else, remains GROUND_VALUE (-1), will be skipped in output -> null/passable
                }
            }

            // 3. --- PLACE MANDATORY POINTS (1, 2, 4) FAR APART ---
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
                            manhattanDistance(exit_x, exit_y, trap_x, trap_y) < minDistance  // Ensure distance from exit
            );

            // Add mandatory points to map grid (overwrites anything underneath)
            mapGrid[start_x][start_y] = CELL_START;
            mapGrid[exit_x][exit_y] = CELL_EXIT;
            mapGrid[trap_x][trap_y] = CELL_TRAP;

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
            // Only write cells that have a value different from GROUND_VALUE (-1).
            // This means cells with value GROUND_VALUE (-1) (open ground) will NOT appear in the file,
            // effectively making them 'null' or 'undefined' entries when parsed by the game engine.
            // Cells with value CELL_WALL (0) WILL appear in the file as "x,y=0", interpreted as walls.
            // Cells with other entity values (1,2,4,5,6) WILL appear in the file.
            StringBuilder mapBuilder = new StringBuilder();
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    // Only add to the string if the value is NOT the ground value (-1)
                    if (mapGrid[x][y] != GROUND_VALUE) {
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