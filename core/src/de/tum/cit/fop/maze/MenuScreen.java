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

public class MenuScreen implements Screen {
    private final MazeRunnerGame game;
    private final Stage stage;
    private final Music menuMusic;

    public MenuScreen(MazeRunnerGame game) {
        this.game = game;
        OrthographicCamera camera = new OrthographicCamera();
        Viewport viewport = new ScreenViewport(camera);
        stage = new Stage(viewport, game.getSpriteBatch());

        // Musik Setup
        menuMusic = Gdx.audio.newMusic(Gdx.files.internal("assets/sounds/background.mp3"));
        menuMusic.setLooping(true);
        var prefs = Gdx.app.getPreferences("MazeRunnerPrefs");
        float savedVolume = prefs.getFloat("music_volume", 0.5f);
        game.setCurrentBackgroundMusic(menuMusic);
        menuMusic.setVolume(savedVolume);
        menuMusic.play();

        setupUI();
    }

    private void setupUI() {
        // --- BUTTON STYLES ---
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

        float btnWidth = 320;
        float btnHeight = 75;
        float btnPad = 10;

        // -----------------------------------------------------------
        // 1. BACKGROUND
        // -----------------------------------------------------------
        Image backgroundImage = new Image(new Texture(Gdx.files.internal("assets/images/2.png")));
        backgroundImage.setFillParent(true);
        stage.addActor(backgroundImage);

        // -----------------------------------------------------------
        // 2. ECKE OBEN LINKS (Punkte)
        // -----------------------------------------------------------
        Table pointsTable = new Table();
        pointsTable.setFillParent(true);
        pointsTable.top().left().pad(20);

        int totalScore = SaveSystem.loadTotalScore();
        // Normale Schriftart, kein Title-Style
        Label scoreLabel = new Label("Points: " + totalScore, game.getSkin());
        scoreLabel.setFontScale(1.2f);
        pointsTable.add(scoreLabel);

        stage.addActor(pointsTable);

        // -----------------------------------------------------------
        // 3. MAIN TABLE (Titel & Zentrale Buttons)
        // -----------------------------------------------------------
        Table mainTable = new Table();
        mainTable.setFillParent(true);
        mainTable.center();

        // Buttons global hochschieben
        mainTable.padBottom(150);

        stage.addActor(mainTable);

        // -- TITEL ANIMATION --
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

        // TITEL POSITIONIERUNG
        // padTop(120): Drückt Titel runter
        // padBottom(30): Schafft Platz zwischen Titel und erstem Button
        mainTable.add(titleGroup).padTop(120).padBottom(30).row();

        // -- ZENTRALE BUTTONS --

        // 1. START GAME (Führt zur Level-Auswahl)
        TextButton startGameButton = new TextButton("Start Game", customButtonStyle);
        startGameButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SaveSystem.clearSave();
                game.goToMap(false); // Öffnet Level-Auswahl
            }
        });
        mainTable.add(startGameButton).width(btnWidth).height(btnHeight).padBottom(btnPad).row();

        // 2. Load Game
        TextButton loadGameButton = new TextButton("Load Game", customButtonStyle);
        loadGameButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (SaveSystem.hasSaveGame()) {
                    String levelToLoad = SaveSystem.getGameSave().getString("currentLevel", "maps/level-1.properties");
                    game.goToGame(levelToLoad, true);
                } else {
                    System.out.println("No save found.");
                }
            }
        });
        mainTable.add(loadGameButton).width(btnWidth).height(btnHeight).padBottom(btnPad).row();

        // (Select Section Button wurde entfernt, da jetzt in Start Game integriert)

        // 3. Settings
        TextButton settingsButton = new TextButton("Settings", customButtonStyle);
        settingsButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new SettingsScreen(game));
            }
        });
        mainTable.add(settingsButton).width(btnWidth).height(btnHeight).padBottom(btnPad).row();

        // 4. Acknowledgments
        TextButton credits = new TextButton("Acknowledgments", customButtonStyle);
        credits.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new de.tum.cit.fop.maze.AcknowledgmentScreen(game));
            }
        });
        mainTable.add(credits).width(btnWidth).height(btnHeight).padBottom(btnPad).row();

        // 5. Quit
        TextButton quit = new TextButton("Quit", customButtonStyle);
        quit.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.exit();
            }
        });
        mainTable.add(quit).width(btnWidth).height(btnHeight).padBottom(btnPad).row();


        // -----------------------------------------------------------
        // 4. ECKE UNTEN LINKS (Skill Tree & Achievements)
        // -----------------------------------------------------------
        Table leftTable = new Table();
        leftTable.setFillParent(true);
        leftTable.bottom().left().pad(20);

        TextButton skillsButton = new TextButton("Skill Tree", customButtonStyle);
        skillsButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.goToSkillTree();
            }
        });
        leftTable.add(skillsButton).width(260).height(65).padBottom(10).row();

        TextButton achButton = new TextButton("Achievements", customButtonStyle);
        achButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new AchievementsScreen(game));
            }
        });
        leftTable.add(achButton).width(260).height(65);

        stage.addActor(leftTable);


        // -----------------------------------------------------------
        // 5. ECKE UNTEN RECHTS (High Scores & Infinite Mode)
        // -----------------------------------------------------------
        Table rightTable = new Table();
        rightTable.setFillParent(true);
        rightTable.bottom().right().pad(20);

        TextButton leaderboardButton = new TextButton("High Scores", customButtonStyle);
        leaderboardButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new LeaderboardScreen(game));
            }
        });
        rightTable.add(leaderboardButton).width(260).height(65).padBottom(10).row();

        TextButton infiniteModeButton = new TextButton("Infinite Mode", customButtonStyle);
        infiniteModeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SaveSystem.clearSave();
                game.goToGame("INFINITE_MODE");
            }
        });
        rightTable.add(infiniteModeButton).width(260).height(65);

        stage.addActor(rightTable);
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
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
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