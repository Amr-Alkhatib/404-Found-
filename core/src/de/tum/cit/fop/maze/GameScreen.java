package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor; // ✅ WICHTIG
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

// ✅ FIX: "implements InputProcessor" hinzugefügt, damit ESC erkannt wird
public class GameScreen implements Screen, InputProcessor {

    private boolean waitingForNextLevel = false;
    private float waitTimer = 0f;
    private static final float WAIT_BEFORE_NEXT_LEVEL = 2.0f;
    private final String originalMapLevel;

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

    // Sounds
    private Sound keySound, heartSound, boostSound, trapSound, enemySound, playerSound, trapSound2;
    private Sound winScreenMusic, loseScreenMusic;

    private final GameManager gameManager;
    private Texture endScreenTexture;
    private boolean showingEndScreen = false;

    // ✅ NEU: Variable zum Merken, ob geladen werden soll
    private boolean shouldLoadSave = false;

    // ✅ FIX: Parameter umbenannt zu 'shouldLoadSave' und Logik angepasst
    public GameScreen(MazeRunnerGame game, String mapLevel, boolean shouldLoadSave, String mapLevel1) {
        this.game = game;
        this.originalMapLevel = mapLevel;
        this.mapLevel = mapLevel1;

        // ✅ NEU: Wir merken uns, ob wir laden sollen
        this.shouldLoadSave = shouldLoadSave;

        camera = new OrthographicCamera();
        camera.setToOrtho(false);

        String actualMapFile;
        if ("INFINITE_MODE".equals(this.originalMapLevel)) {
            actualMapFile = InfiniteMapGenerator.generateInfiniteMap(20, 15, 5, 3, 2);
            System.out.println("Infinite Mode: Generated initial map: " + actualMapFile);
        } else {
            actualMapFile = mapLevel1;
        }
        gameMap = new GameMap(actualMapFile);

        var prefs = Gdx.app.getPreferences("MazeRunnerPrefs");
        int upKey = prefs.getInteger("key_up", Input.Keys.W);
        int downKey = prefs.getInteger("key_down", Input.Keys.S);
        int leftKey = prefs.getInteger("key_left", Input.Keys.A);
        int rightKey = prefs.getInteger("key_right", Input.Keys.D);
        int sprintKey = prefs.getInteger("key_sprint", Input.Keys.SHIFT_LEFT);

        float startX = gameMap.getPlayerStartX();
        float startY = gameMap.getPlayerStartY();
        if (startX == -1 || startY == -1) {
            startX = 0;
            startY = 0;
        }

        setupAudio();

        player = new GameCharacter(startX, startY, upKey, downKey, leftKey, rightKey);
        gameMap.setPlayer(player);

        hud = new Hud(gameMap, new ScreenViewport());

        gameManager = new GameManager(
                gameMap, game, this, player, hud,
                gameMap.getWalls(), gameMap.getEnemies(), gameMap.getKeys(),
                gameMap.getTraps(), gameMap.getEntrance(), gameMap.getExits(),
                gameMap.getHearts(), gameMap.getBoosts(), gameMap.getMorphTraps(),
                gameMap.getExitArrow(), sprintKey
        );

        aspectRatio = (float) Gdx.graphics.getWidth() / (float) Gdx.graphics.getHeight();
        camera.setToOrtho(false);
        camera.viewportHeight = tileSize * tilesVisibleY;
        camera.viewportWidth = camera.viewportHeight * aspectRatio;
        updateCamera();

        // HINWEIS: Hier KEIN Laden mehr! Das passiert jetzt in show().
    }

    public GameScreen(MazeRunnerGame game, String mapLevel, boolean shouldLoadSave) {
        this(game, mapLevel, shouldLoadSave, mapLevel);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);

        if (waitingForNextLevel) {
            waitTimer -= delta;
            if (waitTimer <= 0) {
                waitingForNextLevel = false;
                loadNextInfiniteLevel();
            }
            if (!showingEndScreen) {
                game.getSpriteBatch().setProjectionMatrix(camera.combined);
                renderGameWorld(delta);
                hud.update();
                hud.getStage().act(delta);
                hud.draw();
            }
            return;
        }

        if (showingEndScreen) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                game.goToMenu();
            }
            if (endScreenTexture != null) {
                game.getSpriteBatch().getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
                game.getSpriteBatch().begin();
                game.getSpriteBatch().draw(endScreenTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
                game.getSpriteBatch().end();
                game.getSpriteBatch().setProjectionMatrix(camera.combined);
            }
            return;
        }

        updateCamera();
        gameManager.update(delta);

        if (gameManager.isLose() && !showingEndScreen) {
            if ("INFINITE_MODE".equals(originalMapLevel)) {
                int score = (int) gameManager.getTimePlayed();
                game.addInfiniteModeScore(score);
            }
            showEndScreen("assets/images/gameOver.png");
        } else if (gameManager.isWin() && !showingEndScreen) {
            if ("INFINITE_MODE".equals(originalMapLevel)) {
                waitingForNextLevel = true;
                waitTimer = WAIT_BEFORE_NEXT_LEVEL;
                if (winScreenMusic != null) winScreenMusic.play();
            } else {
                showEndScreen("assets/images/win.png");
            }
        }

        if (!showingEndScreen && !waitingForNextLevel) {
            game.getSpriteBatch().setProjectionMatrix(camera.combined);
            renderGameWorld(delta);
            hud.update();
            hud.getStage().act(delta);
            hud.draw();
        }
    }

    @Override
    public void resize(int width, int height) {
        aspectRatio = (float) width / (float) height;
        camera.viewportHeight = tileSize * tilesVisibleY;
        camera.viewportWidth = camera.viewportHeight * aspectRatio;
        camera.update();
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
        // 1. Multiplexer erstellen (Kombiniert HUD und Spiel-Input)
        com.badlogic.gdx.InputMultiplexer multiplexer = new com.badlogic.gdx.InputMultiplexer();

        // Erst das HUD (damit Buttons Klicks abfangen können)
        multiplexer.addProcessor(hud.getStage());

        // Dann das Spiel (für ESC und WASD)
        multiplexer.addProcessor(this);

        // Den Multiplexer als Chef setzen
        Gdx.input.setInputProcessor(multiplexer);

        // 2. Spielstand laden
        if (shouldLoadSave) {
            Gdx.app.log("GameScreen", "Lade Spielstand aus show()...");
            gameManager.requestLoadGameState();
            shouldLoadSave = false;
        }

        // 3. Musik
        if (backgroundMusic != null) {
            game.setCurrentBackgroundMusic(backgroundMusic);
            var prefs = Gdx.app.getPreferences("MazeRunnerPrefs");
            backgroundMusic.setVolume(prefs.getFloat("music_volume", 0.5f));
            if (!backgroundMusic.isPlaying()) {
                backgroundMusic.play();
            }
        }
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
                backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("assets/sounds/level1.mp3"));
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
        var prefs = Gdx.app.getPreferences("MazeRunnerPrefs");
        float savedMusicVolume = prefs.getFloat("music_volume", 0.5f);

        backgroundMusic.setVolume(savedMusicVolume);
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

        if (sound != null) {
            var prefs = Gdx.app.getPreferences("MazeRunnerPrefs");
            float sfxVolume = prefs.getFloat("sfx_volume", 0.5f);
            sound.play(sfxVolume);
        }
    }

    private void pauseGame() {
        if (backgroundMusic != null) {
            backgroundMusic.stop();
        }
        game.goToPause();
    }

    private void updateCamera() {
        if (player != null) {
            camera.position.set(
                    Math.min(Math.max(player.getX() * tileSize + tileSize / 2, camera.viewportWidth / 2), gameMap.getWidth() * tileSize - camera.viewportWidth / 2),
                    Math.min(Math.max(player.getY() * tileSize + tileSize / 2, camera.viewportHeight / 2), gameMap.getHeight() * tileSize - camera.viewportHeight / 2),
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
        if (backgroundMusic != null) {
            backgroundMusic.pause();
        }
    }

    @Override
    public void resume() {
        if (backgroundMusic != null) {
            backgroundMusic.play();
        }
    }

    private boolean isInfiniteMode() {
        return "INFINITE_MODE".equals(this.originalMapLevel);
    }

    public void onInfiniteModeLevelComplete() {
        if (isInfiniteMode()) {
            waitingForNextLevel = true;
            waitTimer = WAIT_BEFORE_NEXT_LEVEL;

            if (backgroundMusic != null) {
                backgroundMusic.pause();
            }
            Gdx.app.log("GameScreen", "Infinite Mode: Level complete. Waiting to load next level.");
        }
    }

    public Music getBackgroundMusic() {
        return backgroundMusic;
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    private void loadNextInfiniteLevel() {
        Gdx.app.log("GameScreen", "Loading next infinite level...");
        String newMapFile = InfiniteMapGenerator.generateInfiniteMap(20, 15, 5, 3, 2);
        if (newMapFile == null) {
            Gdx.app.error("GameScreen", "Failed to generate next map for infinite mode");
            return;
        }
        gameMap.reloadFrom(newMapFile);
        float newStartX = gameMap.getPlayerStartX();
        float newStartY = gameMap.getPlayerStartY();
        if (newStartX == -1 || newStartY == -1) {
            newStartX = 1;
            newStartY = 1;
        }
        player.setX(newStartX);
        player.setY(newStartY);
        gameManager.onMapReloaded(
                gameMap.getExits(), gameMap.getHearts(), gameMap.getKeys(),
                gameMap.getTraps(), gameMap.getEnemies(), gameMap.getMorphTraps(),
                gameMap.getExitArrow()
        );
        gameManager.updateExitArrowReference(gameMap.getExitArrow());
        gameManager.resetAfterLevelTransition();

        showingEndScreen = false;
        if (endScreenTexture != null) {
            endScreenTexture.dispose();
            endScreenTexture = null;
        }
        waitingForNextLevel = false;
        if (backgroundMusic != null) {
            backgroundMusic.play();
        }
        Gdx.app.log("GameScreen", "Next level loaded successfully! Exiting end screen.");
    }

    // ==========================================================
    // ✅ NEU: InputProcessor Methoden (Erforderlich für ESC!)
    // ==========================================================

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.ESCAPE) {
            System.out.println("ESC wurde gedrückt! Pausiere...");
            pauseGame();
            return true;
        }
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return false;
    }
}