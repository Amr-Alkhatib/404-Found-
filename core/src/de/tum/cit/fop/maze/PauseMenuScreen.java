package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * The PauseScreen class displays the pause menu during gameplay.
 * It allows players to continue, select a map, or return to the main menu.
 */
public class PauseMenuScreen implements Screen {

    private final MazeRunnerGame game;
    private final GameScreen gameScreen;
    private final Stage stage;
    private Music pauseMusic;

    /**
     * Constructor for the {@code PauseScreen}.
     */
    public PauseMenuScreen(MazeRunnerGame game, GameScreen gameScreen) {
        this.game = game;
        this.gameScreen = gameScreen;

        OrthographicCamera camera = new OrthographicCamera();
        Viewport viewport = new ScreenViewport(camera);
        stage = new Stage(viewport, game.getSpriteBatch());

        setUpMenu();
    }

    /**
     * Sets up the pause menu UI.
     */
    private void setUpMenu() {
        // Musik laden
        pauseMusic = Gdx.audio.newMusic(Gdx.files.internal("assets/sounds/pause.mp3"));
        pauseMusic.setLooping(true);

        var prefs = Gdx.app.getPreferences("MazeRunnerPrefs");
        float savedVolume = prefs.getFloat("music_volume", 0.5f);
        pauseMusic.setVolume(savedVolume);

        game.setCurrentBackgroundMusic(pauseMusic);
        pauseMusic.play();

        // UI Aufbauen
        Table table = new Table();
        table.setFillParent(true);
        table.center();
        stage.addActor(table);

        Image backgroundImage = new Image(new Texture(Gdx.files.internal("assets/images/2.png")));
        table.setBackground(backgroundImage.getDrawable());

        Label titleLabel = new Label("Pause Menu", game.getSkin(), "title");
        table.add(titleLabel).padBottom(80).row();

        // --- BUTTONS ---

        TextButton continueButton = new TextButton("Continue", game.getSkin());
        continueButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // Hier geht es zurück ins Spiel!
                pauseMusic.dispose();
                game.setScreen(gameScreen);
                gameScreen.resume();
            }
        });
        table.add(continueButton).width(300).pad(10).row();

        TextButton saveButton = new TextButton("Save Game", game.getSkin());
        saveButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                System.out.println("PauseMenu Save button clicked.");
                GameManager manager = gameScreen.getGameManager();
                if (manager != null) {
                    manager.requestSaveGameState();
                } else {
                    System.err.println("GameManager is null, cannot save from PauseMenu!");
                }
            }
        });
        table.add(saveButton).width(300).pad(10).row();

        TextButton selectNewMap = new TextButton("Select New Map", game.getSkin());
        selectNewMap.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.goToMap(true);
                pauseMusic.dispose();
            }
        });
        table.add(selectNewMap).width(300).pad(10).row();

        TextButton mainMenuButton = new TextButton("Exit", game.getSkin());
        mainMenuButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                pauseMusic.dispose();
                pauseMusic.stop();
                game.goToMenu();
            }
        });
        table.add(mainMenuButton).width(300).pad(10).row();
    }

    public void setPauseMusic() {
        pauseMusic.play();
    }

    @Override
    public void show() {
        // WICHTIG: Input auf Stage setzen, damit Buttons klickbar sind
        Gdx.input.setInputProcessor(stage);

        if (!pauseMusic.isPlaying()) {
            pauseMusic.play();
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // UI berechnen und zeichnen
        stage.act(delta);
        stage.draw();

        // HIER IST JETZT KEINE ESC-ABFRAGE MEHR!
        // Das Menü bleibt stabil offen, bis du "Continue" klickst.
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
        pauseMusic.stop();
    }

    @Override
    public void dispose() {
        stage.dispose();
        if (pauseMusic != null) {
            pauseMusic.stop();
            pauseMusic.dispose();
        }
    }
}