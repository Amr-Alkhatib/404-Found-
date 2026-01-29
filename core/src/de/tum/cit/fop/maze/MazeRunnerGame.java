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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * The MazeRunnerGame class represents the core of the Maze Runner game.
 * It manages screens, global resources like SpriteBatch, and the game world.
 */
public class MazeRunnerGame extends Game {

    private final NativeFileChooser fileChooser;
    private TextButton infiniteModeButton;


    private MenuScreen menuScreen;
    private GameScreen gameScreen;
    private PauseMenuScreen pauseMenuScreen;
    private SelectMapScreen selectMapScreen;

    private List<Integer> infiniteModeScores = new ArrayList<Integer>(); // 确保初始化
    private SpriteBatch spriteBatch;
    private TextureManager textureManager;
    private Skin skin;


    private GameMap gameMap;
    private Viewport viewport;


    private Music currentBackgroundMusic;


    private boolean isInfiniteMode = false;
    private int currentInfiniteLevel = 0;
    private int totalScore = 0;

    /**
     * Constructor for MazeRunnerGame.
     *
     * @param fileChooser The file chooser for the game, typically used in desktop environments.
     */


        // ✅ 修复2: 在构造函数中正确赋值


    public MazeRunnerGame(NativeFileChooser fileChooser) {
        super();
        this.fileChooser = fileChooser;
        // ✅ 确保在构造函数中初始化，但不要调用 SaveSystem
    }

    // ✅ 确保加载分数
    public void loadInfiniteModeScores() {
        this.infiniteModeScores = SaveSystem.loadInfiniteModeScores();
    }

    // 在 MazeRunnerGame.java 中确保 totalScore 被正确初始化
    // ✅ 修复：确保在构造函数中初始化

    // 在 MazeRunnerGame.java 的 create() 方法中
    @Override
    public void create() {
        spriteBatch = new SpriteBatch();
        skin = new Skin(Gdx.files.internal("craft/craftacular-ui.json"));
        viewport = new FitViewport(16 * 32, 9 * 32);
        viewport.apply();
        textureManager = new TextureManager();

        // ✅ 修复：加载总分数
        this.totalScore = SaveSystem.loadTotalScore();

        loadInfiniteModeScores();
        goToMenu();
    }
    public void addInfiniteModeScore(int score) {
        if (infiniteModeScores == null) {
            infiniteModeScores = new ArrayList<Integer>();
        }
        infiniteModeScores.add(score);
        Collections.sort(infiniteModeScores, Collections.reverseOrder());

        // ✅ 确保分数被正确保存
        SaveSystem.saveInfiniteModeScores(infiniteModeScores);
    }
    /**
     * Called when the game is created. Initializes resources, music, and sets the menu screen.
     */

    /**
     * Switches to the menu screen.
     */
    public void goToMenu() {
        if (menuScreen == null) {
            menuScreen = new MenuScreen(this);
        }
        setScreen(menuScreen);


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


    public void goToGameNew(String mapLevel) {
        if (gameScreen != null) {
            gameScreen.dispose();
        }

        gameScreen = new GameScreen(this, mapLevel, true);
        currentBackgroundMusic = gameScreen.getBackgroundMusic();
        setScreen(gameScreen);

        cleanupOtherScreens();
    }


    public void goToGame(String mapLevel) {
        if (gameScreen != null) {
            gameScreen.dispose();
        }
        gameScreen = new GameScreen(this, mapLevel, false);
        currentBackgroundMusic = gameScreen.getBackgroundMusic();
        setScreen(gameScreen);

        cleanupOtherScreens();
    }


    public void goToGame(GameState loadedState) {
        if (gameScreen != null) {
            gameScreen.dispose();
        }
        gameScreen = new GameScreen(this, loadedState.mapFile, false);
        gameScreen.getGameManager().requestLoadGameState();
        currentBackgroundMusic = gameScreen.getBackgroundMusic();
        setScreen(gameScreen);

        cleanupOtherScreens();
    }


    /**
     * Starts the infinite mode gameplay.
     * Generates the first map and creates the initial GameScreen.
     */
    public void startInfiniteMode() {
        System.out.println("MazeRunnerGame: Starting Infinite Mode...");
        this.isInfiniteMode = true;
        this.currentInfiniteLevel = 1;
        int width = 20;
        int height = 20;
        int numTraps = 10;
        int numEnemies = 2;
        int numMorphTraps = 3;
        String generatedMapPath = InfiniteMapGenerator.generateInfiniteMap(
                width, height, numTraps, numEnemies, numMorphTraps);
        if (generatedMapPath == null) {
            System.err.println("Failed to generate initial infinite map. Aborting startInfiniteMode.");
            return;
        }
        System.out.println("MazeRunnerGame: Generated first map for infinite mode: " + generatedMapPath);

        // ✅ 修复：传递 "INFINITE_MODE" 而不是 generatedMapPath
        GameScreen newGameScreen = new GameScreen(this, "INFINITE_MODE", true /* ignoreSavedState */);
        this.gameScreen = newGameScreen;
        this.setScreen(gameScreen);
        cleanupOtherScreens();
    }


    /**
     * Called by GameScreen when an infinite mode level is completed successfully.
     * Generates the next level and switches to it.
     */
    public void continueInfiniteMode() {
        if (!isInfiniteMode) {

            System.err.println("Warning: continueInfiniteMode called but not in infinite mode!");
            return;
        }

        System.out.println("MazeRunnerGame: Continuing Infinite Mode to level " + (currentInfiniteLevel + 1));

        this.currentInfiniteLevel++;


        int width = 20 + (currentInfiniteLevel / 5);
        int height = 20 + (currentInfiniteLevel / 5);
        int numTraps = 10 + currentInfiniteLevel;
        int numEnemies = 2 + (currentInfiniteLevel / 3);
        int numMorphTraps = 3 + (currentInfiniteLevel / 10);

        String generatedMapPath = InfiniteMapGenerator.generateInfiniteMap(
                width, height, numTraps, numEnemies, numMorphTraps);

        if (generatedMapPath == null) {
            System.err.println("Failed to generate subsequent infinite map for level " + currentInfiniteLevel + ". Aborting continueInfiniteMode.");
            return;
        }

        System.out.println("MazeRunnerGame: Generated next map for infinite mode (level " + currentInfiniteLevel + "): " + generatedMapPath);

        GameScreen newGameScreen = new GameScreen(this, generatedMapPath, true /* ignoreSavedState, implies infinite mode */);
        this.gameScreen = newGameScreen;
        this.setScreen(gameScreen);

        cleanupOtherScreens();
    }

    /**
     * Called by GameScreen when infinite mode ends (e.g., player loses all lives).
     * Resets the infinite mode state and returns to the menu.
     */
    public void endInfiniteMode() {
        System.out.println("MazeRunnerGame: Ending Infinite Mode.");
        this.isInfiniteMode = false;
        this.currentInfiniteLevel = 0;
        goToMenu();
    }

    /**
     * Getter for the infinite mode flag.
     * Useful for other parts of the code to check the current mode.
     */
    public boolean getIsInfiniteMode() {
        return this.isInfiniteMode;
    }
    // 在 MazeRunnerGame.java 中添加以下方法


    // 在 MazeRunnerGame.java 中添加以下方法



    /**
     * Getter for the current infinite mode level number.
     */
    public int getCurrentInfiniteLevel() {
        return this.currentInfiniteLevel;
    }

    public void goToPause() {
        if (gameScreen == null) return;

        pauseMenuScreen = new PauseMenuScreen(this, gameScreen);
        setScreen(pauseMenuScreen);

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

    public GameScreen getCurrentGameScreenInstance() {
        return gameScreen; //
        // Return the member variable

    }public int getTotalScore() {
        return this.totalScore;

    }
    public void addTotalScore(int score) {
        this.totalScore += score;
        SaveSystem.saveTotalScore(this.totalScore);
    }

    // 添加 getter 方法（必须！）
    public List<Integer> getInfiniteModeScores() {
        if (infiniteModeScores == null) {
            infiniteModeScores = new ArrayList<>();
        }
        return infiniteModeScores;
    }
}