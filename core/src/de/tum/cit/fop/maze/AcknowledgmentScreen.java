package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/**
 * The AcknowledgmentScreen class displays acknowledgments for the game.
 */
public class AcknowledgmentScreen implements Screen {

    /**
     * Constructor for {@code AcknowledgmentScreen}.
     *
     * @param game The main game class, used to access global resources and methods.
     */
    public AcknowledgmentScreen(MazeRunnerGame game) {
        this.game = game;

        stage = new Stage(new ScreenViewport());

        backgroundTexture = new Texture(Gdx.files.internal("assets/images/2.png"));

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        Label titleLabel = new Label("Acknowledgments", game.getSkin(), "title");
        table.add(titleLabel).padBottom(50).row();

        Label acknowledgmentText = setAcknowledgementsText(game);
        table.add(acknowledgmentText).width(600).padBottom(50).row();

        TextButton backButton = new TextButton("Back to Menu", game.getSkin());
        table.add(backButton).width(300).pad(10);

        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.goToMenu();
            }
        });
    }

    /**
     * The stage to be rendered.
     */
    private final Stage stage;

    /**
     * The background image.
     */
    private final Texture backgroundTexture;

    /**
     * Reference for the current running game.
     */
    private final MazeRunnerGame game;

    /**
     * Sets the main text to be displayed.
     * @param game The current game running.
     * @return a Label with text.
     */
    private static Label setAcknowledgementsText(MazeRunnerGame game) {
        Label acknowledgmentText = new Label(
                "Maze Runner\n\n" +
                        "Team: 404 Found!\n\n" +
                        "Developed by:\n" +
                        "Huaijing Hou, Amr Alkhatib\n\n" +
                        "Graphics:\n" +
                        "Samuel\n\n" +
                        "Music:\n" +
                        "Free Music Archive (https://pixabay.com/)\n\n" +
                        "Patch & Debugging:\n" +
                        "Cheng Cheng\n\n" +
                        "Powered by:\n" +
                        "LibGDX\n\n" +
                        "Special Thanks to:\n" +
                        "All players and testers!",
                game.getSkin(), "default"
        );
        acknowledgmentText.setWrap(true);
        acknowledgmentText.setAlignment(Align.center);
        return acknowledgmentText;
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.getSpriteBatch().begin();
        game.getSpriteBatch().draw(backgroundTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        game.getSpriteBatch().end();

        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
        backgroundTexture.dispose();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}
}