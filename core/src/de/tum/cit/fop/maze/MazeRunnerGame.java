package de.tum.cit.fop.maze;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
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
    public boolean IsInfiniteMode;
    private TextButton infiniteModeButton;

    private MenuScreen menuScreen;
    private GameScreen gameScreen;
    private PauseMenuScreen pauseMenuScreen;
    private SelectMapScreen selectMapScreen;

    private List<Integer> infiniteModeScores = new ArrayList<Integer>();
    private SpriteBatch spriteBatch;
    private TextureManager textureManager;
    private Skin skin;
    private Viewport viewport;
    private Music currentBackgroundMusic;

    private boolean isInfiniteMode = false;
    private int currentInfiniteLevel = 0;

    private int infiniteLevelCounter = 1;

    private int totalScore = 0;

    /**
     * Constructor for MazeRunnerGame.
     * @param fileChooser The file chooser for the game, typically used in desktop environments.
     */
    public MazeRunnerGame(NativeFileChooser fileChooser) {
        super();
        this.fileChooser = fileChooser;
    }

    public void loadInfiniteModeScores() {
        this.infiniteModeScores = SaveSystem.loadInfiniteModeScores();
    }

    @Override
    public void create() {
        spriteBatch = new SpriteBatch();
        skin = new Skin(Gdx.files.internal("craft/craftacular-ui.json"));
        viewport = new FitViewport(16 * 32, 9 * 32);
        viewport.apply();
        textureManager = new TextureManager();

        this.totalScore = SaveSystem.loadTotalScore();

        loadInfiniteModeScores();
        goToMenu();

        var prefs = Gdx.app.getPreferences("MazeRunnerPrefs");
        boolean isFullscreen = prefs.getBoolean("fullscreen", false);

        if (isFullscreen) {
            Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
        } else {
            Gdx.graphics.setWindowedMode(1280, 720);
        }
    }

    public void addInfiniteModeScore(int score) {
        if (infiniteModeScores == null) {
            infiniteModeScores = new ArrayList<Integer>();
        }
        infiniteModeScores.add(score);
        Collections.sort(infiniteModeScores, Collections.reverseOrder());
        SaveSystem.saveInfiniteModeScores(infiniteModeScores);
    }

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

    public void goToGame(String mapLevel) {
        goToGame(mapLevel, false);
    }

    public void goToGame(String mapLevel, boolean loadSave) {
        if (gameScreen != null) {
            gameScreen.dispose();
        }

        if (mapLevel.equals("INFINITE_MODE")) {
            this.isInfiniteMode = true;
            this.infiniteLevelCounter = 1;
        } else {
            this.isInfiniteMode = false;
        }

        this.gameScreen = new GameScreen(this, mapLevel, loadSave);

        this.currentBackgroundMusic = gameScreen.getBackgroundMusic();

        setScreen(gameScreen);

        cleanupOtherScreens();
    }

    public void continueInfiniteMode() {
        goToNextInfiniteLevel(); // Ruft deine bestehende Logik auf
    }

    public void goToNextInfiniteLevel() {
        if (gameScreen != null) {
            infiniteLevelCounter++;
            new AchievementManager().onInfiniteLevelReached(infiniteLevelCounter);

            String newMapFile = InfiniteMapGenerator.generateInfiniteMap(20, 20, 5, 3, 2);
            if (newMapFile != null) {
                gameScreen.reloadFromNewMap(newMapFile);
                this.currentInfiniteLevel++;
            } else {
                Gdx.app.error("MazeRunnerGame", "Failed to generate next infinite map, returning to menu.");
                goToMenu();
            }
        } else {
            Gdx.app.error("MazeRunnerGame", "No active GameScreen to load next level into.");
        }
    }

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

    public void goToSkillTree() {
        setScreen(new SkillTreeScreen(this));
    }

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

    @Override
    public void dispose() {
        textureManager.dispose();
        if (getScreen() != null) {
            getScreen().hide();
            getScreen().dispose();
        }
        spriteBatch.dispose();
        skin.dispose();

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

    public Music getCurrentBackgroundMusic() {
        return currentBackgroundMusic;
    }

    public void setCurrentBackgroundMusic(Music music) {
        this.currentBackgroundMusic = music;
    }

    public GameScreen getCurrentGameScreenInstance() {
        return gameScreen;
    }

    public int getTotalScore() {
        return this.totalScore;
    }

    public void addTotalScore(int score) {
        this.totalScore += score;
        SaveSystem.saveTotalScore(this.totalScore);
    }

    public List<Integer> getInfiniteModeScores() {
        if (infiniteModeScores == null) {
            infiniteModeScores = new ArrayList<>();
        }
        return infiniteModeScores;
    }

    public boolean getIsInfiniteMode() {
        return this.isInfiniteMode;
    }
}