// File: core/src/de/tum/cit/fop/maze/MazeRunnerGame.java
package de.tum.cit.fop.maze;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import de.tum.cit.fop.maze.world.GameMap;
import de.tum.cit.fop.maze.world.TextureManager;
import games.spooky.gdx.nativefilechooser.NativeFileChooser;

/**
 * The MazeRunnerGame class represents the core of the Maze Runner game.
 * It manages screens, global resources like SpriteBatch, and the game world.
 */
public class MazeRunnerGame extends Game {

    private final NativeFileChooser fileChooser;
    private TextButton infiniteModeButton;
    // Screens
    private MenuScreen menuScreen;
    private GameScreen gameScreen;
    private PauseMenuScreen pauseMenuScreen;
    private SelectMapScreen selectMapScreen;

    // Global resources
    private SpriteBatch spriteBatch;
    private TextureManager textureManager;
    private Skin skin;

    // Game map and viewport
    private GameMap gameMap;
    private Viewport viewport;

    // Background music reference for volume control
    private Music currentBackgroundMusic;

    // === NEW: Added infinite mode tracking variables ===
    private boolean isInfiniteMode = false; // Track if currently in infinite mode
    private int currentInfiniteLevel = 0;   // Track the current level number in infinite mode
    // ================================================

    /**
     * Constructor for MazeRunnerGame.
     *
     * @param fileChooser The file chooser for the game, typically used in desktop environments.
     */
    private int totalScore = 0;
    public void addToTotalScore(int points) {
        totalScore += points;
        SaveSystem.saveTotalScore(totalScore); // 持久化
    }
    public int getTotalScore() { return totalScore; }

    public MazeRunnerGame(NativeFileChooser fileChooser) {
        super();
        this.fileChooser = fileChooser;
    }

    /**
     * Called when the game is created. Initializes resources, music, and sets the menu screen.
     */
    @Override
    public void create() {
        spriteBatch = new SpriteBatch();
        skin = new Skin(Gdx.files.internal("craft/craftacular-ui.json"));
        viewport = new FitViewport(16 * 32, 9 * 32);
        viewport.apply();
        textureManager = new TextureManager();

        goToMenu();
    }

    // REMOVED the old goToGameFromInfiniteMode method that had incorrect logic.
    // It's now handled by passing the map file path directly to the GameScreen constructor.

    /**
     * Switches to the menu screen.
     */
    public void goToMenu() {
        if (menuScreen == null) {
            menuScreen = new MenuScreen(this);
        }
        setScreen(menuScreen);

        // Clean up other screens
        if (gameScreen != null) {
            gameScreen.dispose();
            gameScreen = null;
        }
        if (pauseMenuScreen != null) {
            pauseMenuScreen.dispose();
            pauseMenuScreen = null;
        }
        if (selectMapScreen != null) {
            selectMapScreen.dispose();
            selectMapScreen = null;
        }
    }

    // In MazeRunnerGame.java
    // Modify goToGameNew as an example (apply similar logic to goToGame methods):
    public void goToGameNew(String mapLevel) {
        if (gameScreen != null) { // Use the existing gameScreen member variable
            gameScreen.dispose();
        }
        // Create new game screen and assign it to the member variable
        gameScreen = new GameScreen(this, mapLevel, true); // Pass 'true' to ignore saved state
        currentBackgroundMusic = gameScreen.getBackgroundMusic();
        setScreen(gameScreen); // This sets the LibGDX Game.currentScreen

        cleanupOtherScreens();
    }

    // Remember to update 'gameScreen' member variable in ALL goToGame methods:
    // goToGame(String)
    // goToGame(GameState)
    // goToGameNew(String)
    // Example for goToGame(String):
    public void goToGame(String mapLevel) {
        if (gameScreen != null) {
            gameScreen.dispose();
        }
        gameScreen = new GameScreen(this, mapLevel, false); // Pass 'false' to allow loading saved state
        currentBackgroundMusic = gameScreen.getBackgroundMusic();
        setScreen(gameScreen);

        cleanupOtherScreens();
    }

    // Example for goToGame(GameState):
    public void goToGame(GameState loadedState) {
        if (gameScreen != null) {
            gameScreen.dispose();
        }
        gameScreen = new GameScreen(this, loadedState.mapFile, false); // allow load, but we'll override
        // Restore the exact saved state - this happens *after* gameScreen creation
        gameScreen.getGameManager().requestLoadGameState(loadedState);
        currentBackgroundMusic = gameScreen.getBackgroundMusic();
        setScreen(gameScreen);

        cleanupOtherScreens();
    }

    // === 原有方法：用于底层加载（包括从 GameState 加载时的初始创建）===


    /**
     * Starts the infinite mode gameplay.
     * Generates the first map and creates the initial GameScreen.
     */
    public void startInfiniteMode() {
        System.out.println("MazeRunnerGame: Starting Infinite Mode...");
        // Set the flag
        this.isInfiniteMode = true;
        // Reset level counter
        this.currentInfiniteLevel = 1; // Or 0, depending on your preference

        // Define initial map parameters (these could be configurable)
        int width = 20; // Adjusted to match your example (20x20 grid)
        int height = 20;
        int numTraps = 10; // Adjusted number of elements
        int numEnemies = 2;
        int numMorphTraps = 3;

        // Generate the first map file path
        String generatedMapPath = InfiniteMapGenerator.generateInfiniteMap(
                width, height, numTraps, numEnemies, numMorphTraps);

        if (generatedMapPath == null) {
            System.err.println("Failed to generate initial infinite map. Aborting startInfiniteMode.");
            return; // Or go back to menu?
        }

        System.out.println("MazeRunnerGame: Generated first map for infinite mode: " + generatedMapPath);

        // Create the first GameScreen for infinite mode
        // Pass the path to the generated map file
        GameScreen newGameScreen = new GameScreen(this, generatedMapPath, true /* ignoreSavedState, also implies infinite mode if needed internally */);
        this.gameScreen = newGameScreen; // Assign to member variable
        this.setScreen(gameScreen);      // Set the LibGDX current screen

        // Clean up other screens (if any were active before)
        cleanupOtherScreens();
    }

    /**
     * Called by GameScreen when an infinite mode level is completed successfully.
     * Generates the next level and switches to it.
     */
    public void continueInfiniteMode() {
        if (!isInfiniteMode) {
            // Should not happen if called correctly
            System.err.println("Warning: continueInfiniteMode called but not in infinite mode!");
            return;
        }

        System.out.println("MazeRunnerGame: Continuing Infinite Mode to level " + (currentInfiniteLevel + 1));

        // Increment level counter
        this.currentInfiniteLevel++;

        // Define map parameters that might scale with level (difficulty scaling)
        int width = 20 + (currentInfiniteLevel / 5); // Increase size every 5 levels
        int height = 20 + (currentInfiniteLevel / 5);
        int numTraps = 10 + currentInfiniteLevel; // Increase traps linearly
        int numEnemies = 2 + (currentInfiniteLevel / 3); // Increase enemies every 3 levels
        int numMorphTraps = 3 + (currentInfiniteLevel / 10); // Increase morph traps slowly

        // Generate the next map file path
        String generatedMapPath = InfiniteMapGenerator.generateInfiniteMap(
                width, height, numTraps, numEnemies, numMorphTraps);

        if (generatedMapPath == null) {
            System.err.println("Failed to generate subsequent infinite map for level " + currentInfiniteLevel + ". Aborting continueInfiniteMode.");
            // Decide: end game? retry? For now, just return.
            return;
        }

        System.out.println("MazeRunnerGame: Generated next map for infinite mode (level " + currentInfiniteLevel + "): " + generatedMapPath);

        // Create the next GameScreen for infinite mode
        GameScreen newGameScreen = new GameScreen(this, generatedMapPath, true /* ignoreSavedState, implies infinite mode */);
        this.gameScreen = newGameScreen; // Assign to member variable
        this.setScreen(gameScreen);      // Set the LibGDX current screen

        // Clean up the previous game screen if necessary (cleanupOtherScreens handles this)
        cleanupOtherScreens();

        // Potentially update total score based on level completion here,
        // or let GameScreen handle scoring per level and accumulate.
        // Example: addToTotalScore(calculateLevelScore(currentInfiniteLevel));
    }

    /**
     * Called by GameScreen when infinite mode ends (e.g., player loses all lives).
     * Resets the infinite mode state and returns to the menu.
     */
    public void endInfiniteMode() {
        System.out.println("MazeRunnerGame: Ending Infinite Mode.");
        this.isInfiniteMode = false;
        this.currentInfiniteLevel = 0; // Reset level counter
        // Optionally, save the final total score here if not already saved per level/game over
        goToMenu(); // Go back to the main menu
    }

    /**
     * Getter for the infinite mode flag.
     * Useful for other parts of the code to check the current mode.
     */
    public boolean getIsInfiniteMode() {
        return this.isInfiniteMode;
    }

    /**
     * Getter for the current infinite mode level number.
     */
    public int getCurrentInfiniteLevel() {
        return this.currentInfiniteLevel;
    }

    public void goToPause() {
        if (gameScreen == null) return; // Safety check

        pauseMenuScreen = new PauseMenuScreen(this, gameScreen);
        setScreen(pauseMenuScreen);

        // No need to dispose gameScreen — it's paused, not closed
        if (menuScreen != null) {
            menuScreen.dispose();
            menuScreen = null;
        }
        if (selectMapScreen != null) {
            selectMapScreen.dispose();
            selectMapScreen = null;
        }
    }

    /**
     * Switches to {@code SelectMapScreen}.
     *
     * @param calledFromPause Whether it's opened from the pause menu.
     */
    public void goToMap(boolean calledFromPause) {
        selectMapScreen = new SelectMapScreen(this, calledFromPause);
        setScreen(selectMapScreen);

        if (calledFromPause && pauseMenuScreen != null) {
            pauseMenuScreen.setPauseMusic();
        }

        // Clean up others if needed
        if (menuScreen != null) {
            menuScreen.dispose();
            menuScreen = null;
        }
    }

    /**
     * Helper method to clean up non-active screens.
     */
    private void cleanupOtherScreens() {
        if (menuScreen != null) {
            menuScreen.dispose();
            menuScreen = null;
        }
        if (pauseMenuScreen != null) {
            pauseMenuScreen.dispose();
            pauseMenuScreen = null;
        }
        if (selectMapScreen != null) {
            selectMapScreen.dispose();
            selectMapScreen = null;
        }
    }

    /**
     * Cleans up resources when the game is disposed.
     */
    @Override
    public void dispose() {
        if (getScreen() != null) {
            getScreen().hide();
            getScreen().dispose();
        }
        spriteBatch.dispose();
        skin.dispose();
        textureManager.dispose();
        if (currentBackgroundMusic != null) {
            currentBackgroundMusic.dispose();
        }
    }

    // --- Getters ---

    public SpriteBatch getSpriteBatch() {
        return spriteBatch;
    }

    public Skin getSkin() {
        return skin;
    }

    public Viewport getViewport() {
        return viewport;
    }

    public NativeFileChooser getFileChooser() {
        return fileChooser;
    }

    /**
     * Returns the currently active background music (e.g., from GameScreen).
     * May be null if no game is running.
     */
    public Music getCurrentBackgroundMusic() {
        return currentBackgroundMusic;
    }

    // NEW: Getter method for the current GameScreen instance
    public GameScreen getCurrentGameScreenInstance() {
        return gameScreen; // Return the member variable
    }
}