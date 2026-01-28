package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
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
 * The SelectMapScreen class allows the player to select a map/level to play.
 * It implements the LibGDX Screen interface and provides UI for map selection.
 */
public class SelectMapScreen implements Screen {

    private final MazeRunnerGame game;
    private final boolean calledFromPause;
    private final Stage stage;

    private static final String MAP_LEVEL_1 = "maps/level-1.properties";
    private static final String MAP_LEVEL_2 = "maps/level-2.properties";
    private static final String MAP_LEVEL_3 = "maps/level-3.properties";
    private static final String MAP_LEVEL_4 = "maps/level-4.properties";
    private static final String MAP_LEVEL_5 = "maps/level-5.properties";

    /**
     * Constructor for {@code SelectMapScreen}.
     *
     * @param game The main game class, used to access global resources and methods.
     * @param calledFromPause Whether this screen was opened from the pause menu.
     */
    public SelectMapScreen(MazeRunnerGame game, boolean calledFromPause) {
        this.game = game;
        this.calledFromPause = calledFromPause;


        var camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.zoom = 1.0f;
        Viewport viewport = new ScreenViewport(camera);
        stage = new Stage(viewport, game.getSpriteBatch());


        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);


        Image backgroundImage = new Image(new Texture(Gdx.files.internal("assets/images/2.png")));
        table.setBackground(backgroundImage.getDrawable());


        table.add(new Label("Select Section", game.getSkin(), "title")).padBottom(80).row();


        TextButton level1 = new TextButton("1", game.getSkin());
        TextButton level2 = new TextButton("2", game.getSkin());
        TextButton level3 = new TextButton("2", game.getSkin());
        TextButton level4 = new TextButton("3", game.getSkin());
        TextButton level5 = new TextButton("4", game.getSkin());
        TextButton back = new TextButton("Back", game.getSkin());


        table.add(level1).width(400).pad(10).row();
        table.add(level2).width(400).pad(10).row();
        table.add(level3).width(400).pad(10).row();
        table.add(level4).width(400).pad(10).row();
        table.add(level5).width(400).pad(10).row();
        table.add(back).width(400).pad(10).row();


        level1.addListener(createLevelButtonListener(MAP_LEVEL_1));
        level2.addListener(createLevelButtonListener(MAP_LEVEL_2));
        level3.addListener(createLevelButtonListener(MAP_LEVEL_3));
        level4.addListener(createLevelButtonListener(MAP_LEVEL_4));
        level5.addListener(createLevelButtonListener(MAP_LEVEL_5));


        back.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (calledFromPause) {

                    game.goToPause();
                    return;
                }
                game.goToMenu();
            }
        });
    }

    /**
     * Helper method to create a ChangeListener for a specific level.
     * This avoids code duplication in the constructor.
     *
     * @param mapFile The path to the map file associated with the button.
     * @return A ChangeListener that handles the button click logic.
     */
    private ChangeListener createLevelButtonListener(String mapFile) {
        return new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (calledFromPause) {
                    GameState loadedState = SaveSystem.loadGame();

                    if (loadedState != null) {
                        if (mapFile.equals(loadedState.getMapFile())) {
                            System.out.println("Loading saved game for: " + mapFile);
                            game.goToGame(loadedState);
                        } else {
                            System.out.println("Saved game is for a different map (" +
                                    loadedState.getMapFile() + "). Starting new game for: " + mapFile);
                            game.goToGame(mapFile);
                        }
                    } else {
                        System.out.println("No save file found. Starting new game for: " + mapFile);
                        game.goToGame(mapFile);
                    }
                } else {
                    System.out.println("Starting new game from main menu for: " + mapFile);
                    game.goToGame(mapFile);
                }
            }
        };
    }


    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

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
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void hide() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }
}