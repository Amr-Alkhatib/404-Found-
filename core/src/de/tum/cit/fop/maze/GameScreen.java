package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import de.tum.cit.fop.maze.world.GameCharacter;
import de.tum.cit.fop.maze.world.GameMap;
import de.tum.cit.fop.maze.world.Hud;

public class GameScreen implements Screen {

    private boolean waitingForNextLevel = false;
    private float waitTimer = 0f;
    private static final float WAIT_BEFORE_NEXT_LEVEL = 2.0f; // 等待时长（秒）
    private final String originalMapLevel; // 新增：存储原始 mapLevel
    // ====================================

    // ... (other members remain unchanged) ...
    private final MazeRunnerGame game;
    private final OrthographicCamera camera;
    private final String mapLevel;
    private final int tileSize = 32;
    private float aspectRatio;
    private final int tilesVisibleY = 9;
    private GameMap gameMap;
    private Hud hud;
    private GameCharacter player;
    private Music backgroundMusic;
    private Sound keySound;
    private Sound heartSound;
    private Sound boostSound;
    private Sound trapSound;
    private Sound enemySound;
    private Sound playerSound;
    private Sound trapSound2;
    private Sound winScreenMusic;
    private Sound loseScreenMusic;
    private GameManager gameManager;
    private boolean isGameOver = false;
    private Texture endScreenTexture;

    // === NEW: Added flag for showing end screen and waiting for input ===
    private boolean showingEndScreen = false;
    private String endScreenImagePath = null; // Store the path of the image to show
    // ====================================


    // In the constructor, store the original map level
    public GameScreen(MazeRunnerGame game, String mapLevel, boolean ignoreSavedState, String mapLevel1) {
        this.game = game;
        this.originalMapLevel = mapLevel; // Store the original level identifier (e.g., "INFINITE_MODE")
        this.mapLevel = mapLevel1; // Store the actual map file path
        camera = new OrthographicCamera();
        camera.setToOrtho(false);

        // --- 地图加载 ---
        String actualMapFile;
        if ("INFINITE_MODE".equals(this.originalMapLevel)) {
            // Use originalMapLevel to check
            // 如果是无限模式，生成第一张地图
            actualMapFile = InfiniteMapGenerator.generateInfiniteMap(20, 15, 5, 3, 2); // 可调整大小和数量
            System.out.println("Infinite Mode: Generated initial map: " + actualMapFile);
        } else {
            actualMapFile = mapLevel1;
        }
        gameMap = new GameMap(actualMapFile);

        // --- 获取按键设置 ---
        var prefs = Gdx.app.getPreferences("MazeRunnerPrefs");
        int upKey = prefs.getInteger("key_up", Input.Keys.W);
        int downKey = prefs.getInteger("key_down", Input.Keys.S);
        int leftKey = prefs.getInteger("key_left", Input.Keys.A);
        int rightKey = prefs.getInteger("key_right", Input.Keys.D);
        int sprintKey = prefs.getInteger("key_sprint", Input.Keys.SHIFT_LEFT);

        // --- 获取起始坐标 ---
        float startX = gameMap.getPlayerStartX();
        float startY = gameMap.getPlayerStartY();
        if (startX == -1 || startY == -1) {
            startX = 0;
            startY = 0;
        }

        // --- 音频设置 ---
        setupAudio();

        // --- 初始化玩家 ---
        player = new GameCharacter(startX, startY, upKey, downKey, leftKey, rightKey);
        gameMap.setPlayer(player);

        // --- 初始化 HUD ---
        hud = new Hud(gameMap, new ScreenViewport());

        // --- 初始化 GameManager ---
        gameManager = new GameManager(
                gameMap,
                game,
                this,
                player,
                hud,
                gameMap.getWalls(),
                gameMap.getEnemies(),
                gameMap.getKeys(),
                gameMap.getTraps(),
                gameMap.getEntrance(),
                gameMap.getExits(),
                gameMap.getHearts(),
                gameMap.getBoosts(),
                gameMap.getMorphTraps(),
                gameMap.getExitArrow(),
                sprintKey // Pass the exitArrow here
        );

        // --- 相机初始设置 ---
        aspectRatio = (float) Gdx.graphics.getWidth() / (float) Gdx.graphics.getHeight();
        camera.setToOrtho(false);
        camera.viewportHeight = tileSize * tilesVisibleY;
        camera.viewportWidth = camera.viewportHeight * aspectRatio;
        camera.viewportWidth = Math.min(camera.viewportWidth, (gameMap.getWidth() - 2) * tileSize);
        camera.viewportHeight = Math.min(camera.viewportHeight, (gameMap.getHeight() - 2) * tileSize);
        updateCamera(); // Initial centering
    }


    public GameScreen(MazeRunnerGame game, String mapLevel, boolean ignoreSavedState) {
        this(game, mapLevel, ignoreSavedState, mapLevel);
    }


    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);

        if (waitingForNextLevel) {
            waitTimer -= delta;
            if (waitTimer <= 0) {
                loadNextInfiniteLevel();
            }
            return;
        }

        if (showingEndScreen) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                game.goToMenu(); // Geht zurück ins Hauptmenü
            }
        }

        else {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                pauseGame();
                return;
            }
            handleInputButtons();

            updateCamera();
            gameManager.update(delta);

            if (gameManager.tryWin()) {
                triggerEndScreen("assets/images/victory.png");
            } else if (gameManager.tryLose()) {
                triggerEndScreen("assets/images/gameOver.png");
            }
        }

        if (!showingEndScreen) {
            game.getSpriteBatch().setProjectionMatrix(camera.combined);
            renderGameWorld(delta);

            hud.update();
            hud.getStage().act(delta);
            hud.draw();
        }

        if (showingEndScreen && endScreenTexture != null) {
            game.getSpriteBatch().getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

            game.getSpriteBatch().begin();
            game.getSpriteBatch().draw(endScreenTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            game.getSpriteBatch().end();

            game.getSpriteBatch().setProjectionMatrix(camera.combined);
        }
    }

    private void handleInputButtons() {
        var prefs = Gdx.app.getPreferences("MazeRunnerPrefs");
        int volUp = prefs.getInteger("key_volume_up", Input.Keys.PLUS);
        int volDown = prefs.getInteger("key_volume_down", Input.Keys.MINUS);

        if (Gdx.input.isKeyJustPressed(volUp)) {
            backgroundMusic.setVolume(Math.min(1.0f, backgroundMusic.getVolume() + 0.1f));
        }
        if (Gdx.input.isKeyJustPressed(volDown)) {
            backgroundMusic.setVolume(Math.max(0.0f, backgroundMusic.getVolume() - 0.1f));
        }
        if (Gdx.input.isKeyPressed(Input.Keys.I)) camera.zoom = Math.max(1f, camera.zoom - 0.02f);
        if (Gdx.input.isKeyPressed(Input.Keys.O)) camera.zoom = Math.min(2f, camera.zoom + 0.02f);
    }

    private void triggerEndScreen(String path) {
        if (!showingEndScreen) {
            Gdx.app.log("GameScreen", "Show End Screen: " + path);
            showingEndScreen = true;

            if (endScreenTexture != null) endScreenTexture.dispose();
            endScreenTexture = new Texture(Gdx.files.internal(path));

            if (backgroundMusic != null) backgroundMusic.stop();
        }
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false);
        aspectRatio = (float) width / (float) height;
        camera.viewportHeight = tileSize * tilesVisibleY;
        camera.viewportWidth = camera.viewportHeight * aspectRatio;
        camera.viewportWidth = Math.min(camera.viewportWidth, (gameMap.getWidth() - 2) * tileSize);
        camera.viewportHeight = Math.min(camera.viewportHeight, (gameMap.getHeight() - 2) * tileSize);

        hud.getStage().getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        gameMap.dispose();
        hud.getStage().dispose();
        if (backgroundMusic != null) {
            backgroundMusic.stop();
            backgroundMusic.dispose();
        }
        if (keySound != null) keySound.dispose();
        if (heartSound != null) heartSound.dispose();
        if (boostSound != null) boostSound.dispose();
        if (trapSound != null) trapSound.dispose();
        if (enemySound != null) enemySound.dispose();
        if (playerSound != null) playerSound.dispose();
        if (winScreenMusic != null) winScreenMusic.dispose();
        if (loseScreenMusic != null) loseScreenMusic.dispose();
        if (trapSound2 != null) trapSound2.dispose();
    }

    @Override
    public void show() {
        // Logic when the game screen is shown, if needed
        // Initialization logic is usually done in the constructor
    }

    public void showEndScreen(String imagePath) {
        Gdx.app.log("GameScreen", "Lade End Screen: " + imagePath);

        showingEndScreen = true;


        if (endScreenTexture != null) {
            endScreenTexture.dispose();
        }
        endScreenTexture = new Texture(Gdx.files.internal(imagePath));

        if (backgroundMusic != null) {
            backgroundMusic.stop();
        }
    }


    private void setupAudio() {
        if (backgroundMusic != null) {
            backgroundMusic.stop();
            backgroundMusic.dispose();
        }
        switch (mapLevel) {
            case "maps/level-1.properties":
                backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("assets/sounds/level4.mp3"));
                break;
            case "maps/level-2.properties":
                backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("assets/sounds/level2.mp3"));
                break;
            case "maps/level-3.properties":
                backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("assets/sounds/level3.mp3"));
                break;
            default:
                backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("assets/sounds/level3.mp3"));
                break;
        }
        backgroundMusic.setVolume(0.5f); // Set initial volume
        backgroundMusic.setLooping(true);
        backgroundMusic.play();

        winScreenMusic = Gdx.audio.newSound(Gdx.files.internal("assets/sounds/gamewin.mp3"));
        loseScreenMusic = Gdx.audio.newSound(Gdx.files.internal("assets/sounds/gameover.mp3"));
        keySound = Gdx.audio.newSound(Gdx.files.internal("assets/sounds/key.mp3"));
        heartSound = Gdx.audio.newSound(Gdx.files.internal("assets/sounds/heart.mp3"));
        boostSound = Gdx.audio.newSound(Gdx.files.internal("assets/sounds/boostSpeed.mp3"));
        trapSound = Gdx.audio.newSound(Gdx.files.internal("assets/sounds/fire.mp3"));
        enemySound = Gdx.audio.newSound(Gdx.files.internal("assets/sounds/enemy.mp3"));
        playerSound = Gdx.audio.newSound(Gdx.files.internal("assets/sounds/player.mp3"));
        trapSound2 = Gdx.audio.newSound(Gdx.files.internal("assets/sounds/trap2.mp3"));
    }

    public void playSound(String soundName) {
        Sound sound = switch (soundName) {
            case "key" -> keySound;
            case "heart" -> heartSound;
            case "boost" -> boostSound;
            case "trap" -> trapSound;
            case "enemy" -> enemySound;
            case "player" -> playerSound;
            case "winscreen" -> winScreenMusic;
            case "losescreen" -> loseScreenMusic;
            case "trap2" -> trapSound2;
            default -> null;
        };
        if (sound != null) sound.play();
    }

    private void pauseGame() {
        if (backgroundMusic != null) {
            backgroundMusic.stop();
        }
        game.goToPause();
    }

    private void updateCamera() {
        // Ensure player is not null
        if (player != null) {
            camera.position.set(
                    Math.min(Math.max(player.getX() * tileSize + tileSize / 2, camera.viewportWidth / 2), gameMap.getWidth() * tileSize - camera.viewportWidth / 2), // Use gameMap.getWidth()
                    Math.min(Math.max(player.getY() * tileSize + tileSize / 2, camera.viewportHeight / 2), gameMap.getHeight() * tileSize - camera.viewportHeight / 2), // Use gameMap.getHeight()
                    0
            );
        }
        camera.update();
    }

    private void renderGameWorld(float delta) {
        game.getSpriteBatch().setProjectionMatrix(camera.combined);
        game.getSpriteBatch().begin();
        gameMap.render(game.getSpriteBatch(), delta);
        game.getSpriteBatch().end();
    }

    @Override
    public void hide() {
        // Logic when the game screen is hidden
    }

    @Override
    public void pause() {
        // Logic when the game is paused
        if (backgroundMusic != null) {
            backgroundMusic.pause();
        }
    }

    @Override
    public void resume() {
        // Logic when the game is resumed
        if (backgroundMusic != null) {
            backgroundMusic.play();
        }
    }

    // NEW: Check if in infinite mode
    private boolean isInfiniteMode() {
        return "INFINITE_MODE".equals(this.originalMapLevel);
    }

    // NEW: Called by GameManager when a level is won in infinite mode
    public void onInfiniteModeLevelComplete() {
        if (isInfiniteMode()) { // Confirm currently in infinite mode
            waitingForNextLevel = true;
            waitTimer = WAIT_BEFORE_NEXT_LEVEL;
            // Optional: Pause background music
            if (backgroundMusic != null) {
                backgroundMusic.pause();
            }
            Gdx.app.log("GameScreen", "Infinite Mode: Level complete. Waiting to load next level.");
        }
    }

    // === NEW: Getter methods added for access from other classes ===
    public Music getBackgroundMusic() {
        return backgroundMusic;
    }
    public GameManager getGameManager() {
        return gameManager;
    }
    // ====================================

    // === NEW: Method called after loading a new level in infinite mode ===
    private void loadNextInfiniteLevel() {
        Gdx.app.log("GameScreen", "Loading next infinite level...");
        System.out.println("Loading next infinite level...");

        // 生成新的地图文件路径
        String newMapFile = InfiniteMapGenerator.generateInfiniteMap(20, 15, 5, 3, 2);
        System.out.println("Generated next map: " + newMapFile);

        // 通知 GameMap 重新加载地图数据
        gameMap.reloadFrom(newMapFile);

        // 重置玩家位置到新地图的起点
        float newStartX = gameMap.getPlayerStartX();
        float newStartY = gameMap.getPlayerStartY();
        if (newStartX == -1 || newStartY == -1) {
            newStartX = 1;
            newStartY = 1;
        }
        player.setX(newStartX);
        player.setY(newStartY);

        // 更新 GameManager 中的对象引用
        gameManager.onMapReloaded(
                gameMap.getExits(),
                gameMap.getHearts(),
                gameMap.getKeys(),
                gameMap.getTraps(),
                gameMap.getEnemies(),
                gameMap.getMorphTraps(),
                gameMap.getExitArrow()
        );
        // 更新 exitArrow 引用
        gameManager.updateExitArrowReference(gameMap.getExitArrow());

        // 重置 GameManager 的状态标志（用于下一关）
        gameManager.resetAfterLevelTransition(); // Add this call to reset win/lose flags

        // 重置 GameScreen 的等待标志
        waitingForNextLevel = false;

        // Optional: Resume background music for the new level if needed
        // setupAudio(); // This would re-initialize music based on mapLevel, which might not be desired for infinite mode.
        // Or, if infinite mode has specific music:
        if (backgroundMusic != null) {
            backgroundMusic.play(); // Resume the current music track
        }

        Gdx.app.log("GameScreen", "Loaded next infinite level: " + newMapFile);
    }
    // ====================================
}