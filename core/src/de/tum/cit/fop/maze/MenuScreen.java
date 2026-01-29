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

        menuMusic = Gdx.audio.newMusic(Gdx.files.internal("assets/sounds/background.mp3"));
        menuMusic.setLooping(true);
        var prefs = Gdx.app.getPreferences("MazeRunnerPrefs");
        float savedVolume = prefs.getFloat("music_volume", 0.5f);

        game.setCurrentBackgroundMusic(menuMusic);
        menuMusic.setVolume(savedVolume);
        menuMusic.play();


        Table mainTable = new Table();
        mainTable.setFillParent(true);
        stage.addActor(mainTable);


        Image backgroundImage = new Image(new Texture(Gdx.files.internal("assets/images/2.png")));
        mainTable.setBackground(backgroundImage.getDrawable());

        //Title
        float scaleFactor = 4.0f;
        Texture titleTexture = new Texture(Gdx.files.internal("assets/images/headline_menu.png"));
        Texture dotTexture = new Texture(Gdx.files.internal("assets/images/line_red_head_left.png"));
        Image titleImage = new Image(titleTexture);
        titleImage.setSize(titleTexture.getWidth() * scaleFactor, titleTexture.getHeight() * scaleFactor);
        Image dotImage = new Image(dotTexture);
        dotImage.setSize(dotTexture.getWidth() * scaleFactor, dotTexture.getHeight() * scaleFactor);
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

        // 在 MenuScreen.java 中确保使用正确的总分数
        Label totalScoreLabel = new Label("Total Score: " + game.getTotalScore(), game.getSkin());
        totalScoreLabel.setFontScale(1.5f);
        totalScoreLabel.setColor(com.badlogic.gdx.graphics.Color.WHITE);
        mainTable.add(totalScoreLabel).padBottom(30).row();

        Texture buttonNormalTex = new Texture(Gdx.files.internal("assets/images/image_17.png"));
        Texture buttonHoverTex = new Texture(Gdx.files.internal("assets/images/image_18.png"));
        Texture buttonPressedTex = new Texture(Gdx.files.internal("assets/images/image_19.png"));
        Drawable drawableNormal = new TextureRegionDrawable(new TextureRegion(buttonNormalTex));
        Drawable drawableHover = new TextureRegionDrawable(new TextureRegion(buttonHoverTex));
        Drawable drawablePressed = new TextureRegionDrawable(new TextureRegion(buttonPressedTex));
        TextButton.TextButtonStyle customButtonStyle = new TextButton.TextButtonStyle();
        customButtonStyle.up = drawableNormal;
        customButtonStyle.over = drawableHover;
        customButtonStyle.down = drawablePressed;
        customButtonStyle.font = game.getSkin().getFont("font");


        TextButton goToGameButton = new TextButton("Quick Start", customButtonStyle);
        goToGameButton.padBottom(19);
        goToGameButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SaveSystem.clearSave();
                game.goToGame("maps/level-1.properties");
            }
        });
        mainTable.add(goToGameButton).width(300).height(60).pad(5).row();

        TextButton loadGameButton = new TextButton("Load Game", customButtonStyle);
        loadGameButton.padBottom(19);
        loadGameButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                GameState loadedState = SaveSystem.loadGame();
                if (loadedState != null) {
                    game.goToGame(loadedState);
                } else {
                    System.out.println("Failed to load game. No save file found or loading error.");
                }
            }
        });
        mainTable.add(loadGameButton).width(300).height(60).pad(5).row();


        TextButton selectMap = new TextButton("Select Section", customButtonStyle);
        selectMap.padBottom(19);
        selectMap.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SaveSystem.clearSave();
                game.goToMap(false);
            }
        });
        mainTable.add(selectMap).width(300).height(60).pad(5).row();


        TextButton settingsButton = new TextButton("Settings", customButtonStyle);
        settingsButton.padBottom(19);
        settingsButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                 game.setScreen(new SettingsScreen(game));
            }
        });
        mainTable.add(settingsButton).width(300).height(60).pad(5).row();
        TextButton leaderboardButton = new TextButton("High Scores", customButtonStyle);
        leaderboardButton.padBottom(19);
        leaderboardButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new LeaderboardScreen(game));
            }
        });
        mainTable.add(leaderboardButton).width(300).height(60).pad(5).row();
// --- 结束添加 ---
        TextButton credits = new TextButton("Acknowledgments", customButtonStyle);
        credits.padBottom(19);
        credits.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
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


        infiniteModeButton = new TextButton("Infinite Mode", customButtonStyle);
        infiniteModeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {

                SaveSystem.clearSave();

                game.goToGame("INFINITE_MODE");
            }
        });


        infiniteModeButton.setSize(200, 50);
        infiniteModeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {

                SaveSystem.clearSave();
                game.startInfiniteMode();
            }
        });

        stage.addActor(infiniteModeButton);

        updateInfiniteButtonPosition();
    }

    private void updateInfiniteButtonPosition() {

        if (infiniteModeButton == null) return;
        float screenWidth = stage.getViewport().getScreenWidth();
        float screenHeight = stage.getViewport().getScreenHeight();


        float marginX = 20;
        float marginY = 20;


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
        updateInfiniteButtonPosition();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
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