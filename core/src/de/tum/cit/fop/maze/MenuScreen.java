// File: core/src/de/tum/cit/fop/maze/MenuScreen.java
package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * The MenuScreen class is responsible for displaying the main menu of the game.
 * It implements the LibGDX Screen interface and sets up the UI components for the menu.
 */
public class MenuScreen implements Screen {
    private final MazeRunnerGame game;
    private final Stage stage;
    private final Music menuMusic;
    // 将 infiniteModeButton 声明为 final，但不在声明处初始化
    private final TextButton infiniteModeButton;

    /**
     * Constructor for {@code MenuScreen}. Sets up the camera, viewport, stage, and UI elements.
     *
     * @param game The main game class, used to access global resources and methods.
     */
    public MenuScreen(MazeRunnerGame game) {
        this.game = game;
        OrthographicCamera camera = new OrthographicCamera();
        Viewport viewport = new ScreenViewport(camera);
        stage = new Stage(viewport, game.getSpriteBatch());

        // Load and play background music
        menuMusic = Gdx.audio.newMusic(Gdx.files.internal("assets/sounds/level1.mp3")); // Adjust path as needed
        menuMusic.setLooping(true);
        menuMusic.setVolume(0.5f);
        menuMusic.play();

        // Build Main UI Table (Centered)
        Table mainTable = new Table();
        mainTable.setFillParent(true);
        stage.addActor(mainTable);

        // Background image
        Image backgroundImage = new Image(new Texture(Gdx.files.internal("assets/images/2.png"))); // Adjust path as needed
        mainTable.setBackground(backgroundImage.getDrawable());

        //Title
        float scaleFactor = 4.0f;
        Texture titleTexture = new Texture(Gdx.files.internal("assets/images/headline_menu.png")); // Adjust path as needed
        Texture dotTexture = new Texture(Gdx.files.internal("assets/images/line_red_head_left.png")); // Adjust path as needed
        Image titleImage = new Image(titleTexture);
        titleImage.setSize(titleTexture.getWidth() * scaleFactor, titleTexture.getHeight() * scaleFactor);
        Image dotImage = new Image(dotTexture);
        dotImage.setSize(dotTexture.getWidth() * scaleFactor, dotTexture.getHeight() * scaleFactor); // Fixed variable name
        Group titleGroup = new Group();
        titleGroup.setSize(titleImage.getWidth(), titleImage.getHeight());
        titleImage.setPosition(0, 0);
        float dotScale = scaleFactor * 0.7f;
        dotImage.setSize(dotTexture.getWidth() * dotScale, dotTexture.getHeight() * dotScale);
        float baseX = 55;
        float baseY = 100;
        dotImage.setPosition(baseX * scaleFactor, baseY * scaleFactor);
        dotImage.addAction(Actions.forever(
                Actions.sequence(
                        Actions.moveBy(0, 10, 1f, Interpolation.sine),
                        Actions.moveBy(0, -10, 1f, Interpolation.sine)
                )
        ));
        titleGroup.addActor(titleImage);
        titleGroup.addActor(dotImage);
        mainTable.add(titleGroup).padBottom(-80).row();

        // ===== 标签: Total Score =====
        Label totalScoreLabel = new Label("Total Score: " + game.getTotalScore(), game.getSkin());
        totalScoreLabel.setFontScale(1.5f);
        totalScoreLabel.setColor(com.badlogic.gdx.graphics.Color.WHITE);
        mainTable.add(totalScoreLabel).padBottom(30).row();

        // ... 按钮样式定义 ...
        // Buttons
        Texture buttonNormalTex = new Texture(Gdx.files.internal("assets/images/image_17.png")); // Adjust path as needed
        Texture buttonHoverTex = new Texture(Gdx.files.internal("assets/images/image_18.png")); // Adjust path as needed
        Texture buttonPressedTex = new Texture(Gdx.files.internal("assets/images/image_19.png")); // Adjust path as needed
        Drawable drawableNormal = new TextureRegionDrawable(new TextureRegion(buttonNormalTex));
        Drawable drawableHover = new TextureRegionDrawable(new TextureRegion(buttonHoverTex));
        Drawable drawablePressed = new TextureRegionDrawable(new TextureRegion(buttonPressedTex));
        TextButton.TextButtonStyle customButtonStyle = new TextButton.TextButtonStyle();
        customButtonStyle.up = drawableNormal; // normal
        customButtonStyle.over = drawableHover; // hovered
        customButtonStyle.down = drawablePressed; // pressed
        customButtonStyle.font = game.getSkin().getFont("font");

        // ===== 按钮 1: Quick Start =====
        TextButton goToGameButton = new TextButton("Quick Start", customButtonStyle);
        goToGameButton.padBottom(19);
        goToGameButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // 总是开始一个新游戏，从第一关开始
                SaveSystem.clearSave(); // <-- 清除旧存档!
                game.goToGame("maps/level-1.properties");
            }
        });
        mainTable.add(goToGameButton).width(300).height(60).pad(5).row();

        // --- Modified Load Button ---
        TextButton loadGameButton = new TextButton("Load Game", customButtonStyle);
        loadGameButton.padBottom(19);
        loadGameButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // Attempt to load the game state
                GameState loadedState = SaveSystem.loadGame();
                if (loadedState != null) {
                    // If loading was successful, use the overloaded goToGame method to load the state
                    game.goToGame(loadedState);
                } else {
                    // Optionally, show a message if no save file is found or loading fails
                    System.out.println("Failed to load game. No save file found or loading error.");
                    // Or maybe navigate to a default map if no save exists:
                    // game.goToGame("maps/level-1.properties");
                }
            }
        });
        mainTable.add(loadGameButton).width(300).height(60).pad(5).row();
        // --- End of Modified Load Button ---

        // ===== 按钮 2: Select Section =====
        TextButton selectMap = new TextButton("Select Section", customButtonStyle);
        selectMap.padBottom(19);
        selectMap.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // 总是开始一个新游戏，但会跳转到地图选择界面
                SaveSystem.clearSave(); // <-- 清除旧存档!
                // Pass 'false' to indicate this is NOT a load request from the menu
                game.goToMap(false);
            }
        });
        mainTable.add(selectMap).width(300).height(60).pad(5).row();


        // ===== 按钮 3: Settings =====
        TextButton settingsButton = new TextButton("Settings", customButtonStyle);
        settingsButton.padBottom(19);
        settingsButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // Assuming SettingsScreen exists
                 game.setScreen(new SettingsScreen(game));
            }
        });
        mainTable.add(settingsButton).width(300).height(60).pad(5).row();

        TextButton credits = new TextButton("Acknowledgments", customButtonStyle);
        credits.padBottom(19);
        credits.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // Assuming AcknowledgmentScreen exists
                 game.setScreen(new de.tum.cit.fop.maze.AcknowledgmentScreen(game));
            }
        });
        mainTable.add(credits).width(300).height(60).pad(5).row();

        TextButton quit = new TextButton("Quit", customButtonStyle);
        quit.padBottom(19);
        quit.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.exit();
            }
        });
        mainTable.add(quit).width(300).height(60).pad(5).row();

        // ===== 新增: Infinite Mode 按钮 (放置在右下角) =====
        // 在构造函数中初始化 final 的 infiniteModeButton
        infiniteModeButton = new TextButton("Infinite Mode", customButtonStyle);
        infiniteModeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // 清除旧存档
                SaveSystem.clearSave();
                // 启动无限模式：传递一个特殊标记，告诉 GameScreen 这是无限模式
                game.goToGame("INFINITE_MODE");
            }
        });

        // 为按钮设置固定大小
        infiniteModeButton.setSize(200, 50);
        infiniteModeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // 启动无限模式，生成地图并开始游戏
                SaveSystem.clearSave(); // 开始新模式前清除旧存档
                game.startInfiniteMode(); // 调用主游戏类的方法
            }
        });

        // 将按钮作为单独的Actor添加到Stage顶层，并定位到右下角
        stage.addActor(infiniteModeButton);

        // 在show()或resize()之后，或者在这里通过stage的坐标系进行定位
        // 注意：LibGDX的y轴向上为正，所以屏幕底部的y坐标接近0
        // 需要确保在viewport更新后再进行定位，这里使用了一个简单的方式
        // 或者可以在resize方法中更新位置
        updateInfiniteButtonPosition();
        // ============================


    }

    // 辅助方法：更新按钮位置
    private void updateInfiniteButtonPosition() {
        // 获取当前Stage的视口尺寸
        if (infiniteModeButton == null) return; // 添加这行安全检查
        float screenWidth = stage.getViewport().getScreenWidth();
        float screenHeight = stage.getViewport().getScreenHeight();

        // 设置按钮位置，距离右下角一定边距
        float marginX = 20; // 距离右边的边距
        float marginY = 20; // 距离底部的边距

        // 计算按钮的x, y坐标 (左下角锚点)
        float buttonX = screenWidth - infiniteModeButton.getWidth() - marginX;
        float buttonY = marginY;

        infiniteModeButton.setPosition(buttonX, buttonY);
    }


    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        // 当窗口大小改变时，重新计算按钮位置
        updateInfiniteButtonPosition();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        // 确保在Stage显示后，按钮位置也已更新
        updateInfiniteButtonPosition();
    }

    @Override
    public void hide() {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void dispose() {
        stage.dispose();
        menuMusic.dispose();
    }
}