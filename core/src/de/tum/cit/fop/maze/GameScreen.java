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

    private Sound keySound, heartSound, boostSound, trapSound, enemySound, playerSound, trapSound2;
    private Sound winScreenMusic, loseScreenMusic;

    private GameManager gameManager;
    private Texture endScreenTexture;
    private boolean showingEndScreen = false;

    private boolean shouldLoadSave = false;

    public GameScreen(MazeRunnerGame game, String mapLevel, boolean shouldLoadSave) {
        this.game = game;
        this.originalMapLevel = mapLevel;
        this.mapLevel = mapLevel;

        this.shouldLoadSave = shouldLoadSave;

        camera = new OrthographicCamera();
        camera.setToOrtho(false);

        if ("INFINITE_MODE".equals(mapLevel)) {

            this.gameMap = null;
            this.player = null;

            String initialMapFile = InfiniteMapGenerator.generateInfiniteMap(20, 20, 5, 3, 2);
            if (initialMapFile == null) {
                Gdx.app.error("GameScreen", "Failed to generate initial map in constructor. Cannot proceed.");
                this.gameMap = new GameMap("maps/level-1.properties");
            } else {
                loadMapCore(initialMapFile);
            }
        } else {
            this.gameMap = new GameMap(mapLevel);
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
        }
    }


    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);

        if (gameMap == null) {
            Gdx.app.log("GameScreen", "GameMap is null, returning to menu.");
            game.goToMenu();
            return;
        }

        if (waitingForNextLevel) {
            waitTimer -= delta;
            if (waitTimer <= 0) {
                waitingForNextLevel = false;
                if (gameManager != null) {
                    gameManager.resetAfterLevelTransition();
                }
            }
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
        if (gameManager != null) {
            gameManager.update(delta);
        }

        if (gameManager != null) {
            if (gameManager.isLose() && !showingEndScreen) {
                if ("INFINITE_MODE".equals(originalMapLevel)) {
                    int score = (int) gameManager.getTimePlayed();
                    game.addInfiniteModeScore(score);
                }
                showEndScreen("assets/images/gameOver.png");
            } else if (gameManager.isWin() && !showingEndScreen) {
                if ("INFINITE_MODE".equals(originalMapLevel)) {

                    if (!waitingForNextLevel) {
                        waitingForNextLevel = true;
                        waitTimer = WAIT_BEFORE_NEXT_LEVEL;
                        if (winScreenMusic != null) winScreenMusic.play();
                    }

                } else {
                    showEndScreen("assets/images/win.png");
                }
            }
        }

        if (!showingEndScreen && !waitingForNextLevel) {
            game.getSpriteBatch().setProjectionMatrix(camera.combined);
            renderGameWorld(delta);
            if (hud != null) {
                hud.update();
                hud.getStage().act(delta);
                hud.draw();
            }
        }
    }

    private void loadMapCore(String mapFile) {
        if (mapFile == null) {
            Gdx.app.error("GameScreen", "Cannot load map: mapFile is null.");
            return;
        }

        GameMap newMap = null;
        try {
            newMap = new GameMap(mapFile);
        } catch (Exception e) {
            Gdx.app.error("GameScreen", "Failed to load map file: " + mapFile, e);
            game.goToMenu();
            return;
        }

        Music oldMusic = this.backgroundMusic;

        var prefs = Gdx.app.getPreferences("MazeRunnerPrefs");
        int upKey = prefs.getInteger("key_up", Input.Keys.W);
        int downKey = prefs.getInteger("key_down", Input.Keys.S);
        int leftKey = prefs.getInteger("key_left", Input.Keys.A);
        int rightKey = prefs.getInteger("key_right", Input.Keys.D);
        int sprintKey = prefs.getInteger("key_sprint", Input.Keys.SHIFT_LEFT);

        float startX = newMap.getPlayerStartX();
        float startY = newMap.getPlayerStartY();
        if (startX == -1 || startY == -1) {
            startX = 0;
            startY = 0;
        }
        if (this.player == null) {
            this.player = new GameCharacter(startX, startY, upKey, downKey, leftKey, rightKey);
        } else {
            this.player.setPosition(startX, startY);
            this.player.setHeartsCollected(3);
        }
        newMap.setPlayer(this.player);

        setupAudio();

        this.gameMap = newMap;


        this.hud = new Hud(this.gameMap, new ScreenViewport());


        this.gameManager = new GameManager(
                this.gameMap, game, this, this.player, this.hud,
                this.gameMap.getWalls(), this.gameMap.getEnemies(), this.gameMap.getKeys(),
                this.gameMap.getTraps(), this.gameMap.getEntrance(), this.gameMap.getExits(),
                this.gameMap.getHearts(), this.gameMap.getBoosts(), this.gameMap.getMorphTraps(),
                this.gameMap.getExitArrow(), sprintKey
        );

        this.hud.update();

        if (oldMusic != null && oldMusic != this.backgroundMusic) { // 确保不是同一个对象
            oldMusic.stop();
            oldMusic.dispose();
        }

        this.showingEndScreen = false;
        this.waitingForNextLevel = false;
        this.waitTimer = 0f;

        System.out.println("GameScreen: Reloaded map from " + mapFile);
    }

    public void reloadFromNewMap(String newMapPath) {
        loadMapCore(newMapPath);
        updateCamera(); // 确保相机跟随玩家
        System.out.println("GameScreen: Received command to reload from " + newMapPath);
    }


    @Override
    public void resize(int i, int i1) {
    }

    @Override
    public void show() {
        com.badlogic.gdx.InputMultiplexer multiplexer = new com.badlogic.gdx.InputMultiplexer();
        multiplexer.addProcessor(hud.getStage());
        multiplexer.addProcessor(this);
        Gdx.input.setInputProcessor(multiplexer);

        if (shouldLoadSave) {
            Gdx.app.log("GameScreen", "Lade Spielstand aus show()...");
            gameManager.requestLoadGameState();
            shouldLoadSave = false;
        }

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
                    Math.min(Math.max(player.getX() * tileSize + tileSize / 2, camera.viewportWidth / 2),
                            gameMap.getWidth() * tileSize - camera.viewportWidth / 2),
                    Math.min(Math.max(player.getY() * tileSize + tileSize / 2, camera.viewportHeight / 2),
                            gameMap.getHeight() * tileSize - camera.viewportHeight / 2),
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
    }

    @Override
    public void dispose() {
        if (endScreenTexture != null) {
            endScreenTexture.dispose();
        }
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

    public Music getBackgroundMusic() {
        return backgroundMusic;
    }

    public GameManager getGameManager() {
        return gameManager;
    }

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