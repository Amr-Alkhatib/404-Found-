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

    private GameManager gameManager;
    private Texture endScreenTexture;
    private boolean showingEndScreen = false;

    // ✅ NEU: Variable zum Merken, ob geladen werden soll
    private boolean shouldLoadSave = false;

    // ✅ FIX: Parameter umbenannt zu 'shouldLoadSave' und Logik angepasst
    public GameScreen(MazeRunnerGame game, String mapLevel, boolean shouldLoadSave) {
        this.game = game;
        this.originalMapLevel = mapLevel;
        this.mapLevel = mapLevel;
        // ✅ NEU: Wir merken uns, ob wir laden sollen
        this.shouldLoadSave = shouldLoadSave;

        camera = new OrthographicCamera();
        camera.setToOrtho(false);

        // 根据 mapLevel 决定如何加载
        if ("INFINITE_MODE".equals(mapLevel)) {
            // 对于无限模式，先创建一个临时或空的地图结构，然后立即加载第一张生成的地图
            // 我们延迟核心加载逻辑到 loadMapCore
            // 初始化 gameMap 为 null 或一个非常基础的实例，直到 loadMapCore 被调用
            this.gameMap = null; // 暂时为 null，等待 loadMapCore
            // 玩家也暂时为 null
            this.player = null;
            // 设置一些基本的 HUD 和 GameManager 占位符
            // (这里可能会比较麻烦，因为 HUD 和 GameManager 需要 gameMap)
            // 更好的方式是在 MazeRunnerGame 中生成第一张地图，然后传递给 GameScreen 构造函数
            // 但现在我们保持现有流程，构造函数后立即调用 loadMapCore
            // 假设 MazeRunnerGame.goToGame("INFINITE_MODE") 会立即导致 GameScreen 被设置，
            // 然后 MazeRunnerGame.goToNextInfiniteLevel() 会被调用。
            // 因此，GameScreen 构造函数只需初始化基本成员，真正的加载留给 loadMapCore。
            // 这里我们仍然需要调用 loadMapCore 来完成初始化。
            String initialMapFile = InfiniteMapGenerator.generateInfiniteMap(20, 20, 5, 3, 2);
            if (initialMapFile == null) {
                Gdx.app.error("GameScreen", "Failed to generate initial map in constructor. Cannot proceed.");
                // 应该有更优雅的错误处理，比如设置一个错误标志
                // 并在 render 中检查，然后跳转回菜单
                this.gameMap = new GameMap("maps/level-1.properties"); // 使用默认地图作为后备
            } else {
                loadMapCore(initialMapFile); // 完成初始化
            }
        } else {
            // 对于普通关卡，使用原有逻辑
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
            setupAudio(); // 这会为普通关卡设置音乐
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

        if (gameMap == null) { // 如果地图未初始化，显示错误或返回菜单
            Gdx.app.log("GameScreen", "GameMap is null, returning to menu.");
            game.goToMenu();
            return;
        }

        // 检查是否正在等待加载下一关
        if (waitingForNextLevel) {
            waitTimer -= delta;
            if (waitTimer <= 0) {
                waitingForNextLevel = false;
                // 在这里，理论上 loadMapCore 已经被 MazeRunnerGame.goToNextInfiniteLevel 调用
                // 所以我们只需要确保游戏状态继续即可
                // 不需要在这里再次生成或加载
                // 确保 win/lose 状态被重置（这通常在 loadMapCore 中的 GameManager 重新创建时完成）
                // 如果 GameManager 有特定的重置方法，可以在这里调用
                if (gameManager != null) {
                    gameManager.resetAfterLevelTransition(); // 确保状态重置
                }
                // 不再需要生成新地图或创建新 GameScreen
            }
            // 如果仍在等待，绘制等待界面或什么都不做，直到计时器结束
            // 这里可以选择显示一个过渡效果
        }

        // 如果仍在显示结束画面，就不渲染游戏世界
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
            return; // 仍在结束画面，直接返回
        }

        // --- 主要游戏循环 ---
        updateCamera();
        if (gameManager != null) {
            gameManager.update(delta);
        }

        // 检查胜负状态（现在应该只在 !showingEndScreen 且 !waitingForNextLevel 时检查）
        if (gameManager != null) {
            if (gameManager.isLose() && !showingEndScreen) {
                if ("INFINITE_MODE".equals(originalMapLevel)) {
                    int score = (int) gameManager.getTimePlayed();
                    game.addInfiniteModeScore(score);
                }
                showEndScreen("assets/images/gameOver.png");
            } else if (gameManager.isWin() && !showingEndScreen) {
                if ("INFINITE_MODE".equals(originalMapLevel)) {
                    // 重要：只在第一次 win 时设置 waitingForNextLevel
                    if (!waitingForNextLevel) {
                        waitingForNextLevel = true;
                        waitTimer = WAIT_BEFORE_NEXT_LEVEL;
                        if (winScreenMusic != null) winScreenMusic.play();
                    }
                    // 注意：不再在这里调用任何加载或切换屏幕的逻辑
                    // 胜利状态的处理（如加载下一关）由 MazeRunnerGame.goToNextInfiniteLevel() 触发
                } else {
                    // 普通关卡的胜利处理
                    showEndScreen("assets/images/win.png");
                }
            }
        }

        // 只有在非结束画面、非等待加载状态下才渲染游戏世界
        if (!showingEndScreen && !waitingForNextLevel) {
            game.getSpriteBatch().setProjectionMatrix(camera.combined);
            renderGameWorld(delta);
            if (hud != null) {
                hud.update();
                hud.getStage().act(delta);
                hud.draw();
            }
        }
        // --- 结束主要游戏循环 ---
    }

    // GameScreen.java 内部添加一个辅助方法
    private void loadMapCore(String mapFile) {
        if (mapFile == null) {
            Gdx.app.error("GameScreen", "Cannot load map: mapFile is null.");
            return;
        }

        // 1. 重新加载 GameMap 数据
        GameMap newMap = null;
        try {
            newMap = new GameMap(mapFile);
        } catch (Exception e) {
            Gdx.app.error("GameScreen", "Failed to load map file: " + mapFile, e);
            // 尝试返回菜单或处理错误
            game.goToMenu(); // 或者触发失败状态
            return;
        }

        // 2. 保存旧的音乐引用
        Music oldMusic = this.backgroundMusic;

        // 3. 获取输入键设置
        var prefs = Gdx.app.getPreferences("MazeRunnerPrefs");
        int upKey = prefs.getInteger("key_up", Input.Keys.W);
        int downKey = prefs.getInteger("key_down", Input.Keys.S);
        int leftKey = prefs.getInteger("key_left", Input.Keys.A);
        int rightKey = prefs.getInteger("key_right", Input.Keys.D);
        int sprintKey = prefs.getInteger("key_sprint", Input.Keys.SHIFT_LEFT);

        // 4. 重置玩家位置（通常回到入口）
        float startX = newMap.getPlayerStartX();
        float startY = newMap.getPlayerStartY();
        if (startX == -1 || startY == -1) {
            startX = 0; // 或者一个默认安全点
            startY = 0;
        }
        // 如果 player 对象不存在，则创建它；如果存在，则更新其位置
        if (this.player == null) {
            this.player = new GameCharacter(startX, startY, upKey, downKey, leftKey, rightKey);
        } else {
            this.player.setPosition(startX, startY);
            // 重置玩家状态（例如生命值、冲刺等）
            this.player.setHeartsCollected(3); // 假设初始生命值为3
            // ... 重置其他状态 ...
        }
        newMap.setPlayer(this.player); // 确保新地图知道玩家对象

        // 5. 重新设置音频
        setupAudio(); // 这会停止并替换 backgroundMusic

        // 6. 更新对新 GameMap 的引用
        this.gameMap = newMap;

        // 7. 重新创建 HUD
        this.hud = new Hud(this.gameMap, new ScreenViewport());

        // 8. 重新创建 GameManager，传入新的引用和重置的状态
        // 注意：这里 GameManager 的状态（如 win/lose/timePlayed）会被重置
        this.gameManager = new GameManager(
                this.gameMap, game, this, this.player, this.hud,
                this.gameMap.getWalls(), this.gameMap.getEnemies(), this.gameMap.getKeys(),
                this.gameMap.getTraps(), this.gameMap.getEntrance(), this.gameMap.getExits(),
                this.gameMap.getHearts(), this.gameMap.getBoosts(), this.gameMap.getMorphTraps(),
                this.gameMap.getExitArrow(), sprintKey // 重新传入 sprintKey
        );
        // 9. 重置 HUD（可选，取决于 HUD 是否依赖于 GameManager 的状态）
        this.hud.update();

        // 10. 清理旧音乐
        if (oldMusic != null && oldMusic != this.backgroundMusic) { // 确保不是同一个对象
            oldMusic.stop();
            oldMusic.dispose();
        }

        // 11. 重置结束画面和等待状态
        this.showingEndScreen = false;
        this.waitingForNextLevel = false;
        this.waitTimer = 0f;

        System.out.println("GameScreen: Reloaded map from " + mapFile);
    }

    // 添加这个公共方法
    public void reloadFromNewMap(String newMapPath) {
        loadMapCore(newMapPath);
        // 不需要重新设置 camera 视口大小等，除非尺寸改变了
        // 如果需要，可以在 loadMapCore 末尾或这里添加 updateCamera();
        updateCamera(); // 确保相机跟随玩家
        System.out.println("GameScreen: Received command to reload from " + newMapPath);
    }


    @Override
    public void resize(int i, int i1) {
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
        // Logic when the game screen is hidden
    }

    @Override
    public void dispose() {
        // Optional: Clean up resources specific to GameScreen if needed.
        // Note: Other resources like textures, music, etc., might be managed elsewhere or by LibGDX's lifecycle.
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