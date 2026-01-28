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
    private final boolean calledFromPause; // Store the flag passed from goToMap
    private final Stage stage; // Store the stage for rendering and input processing

    // Constants for map file paths (optional, but good practice)
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
        this.calledFromPause = calledFromPause; // Store the flag indicating origin

        // --- Stage Setup ---
        var camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight()); // Optional: Set initial size
        camera.zoom = 1.0f;
        Viewport viewport = new ScreenViewport(camera);
        stage = new Stage(viewport, game.getSpriteBatch());

        // --- UI Table Setup ---
        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        // --- Background ---
        Image backgroundImage = new Image(new Texture(Gdx.files.internal("assets/images/2.png")));
        table.setBackground(backgroundImage.getDrawable());

        // --- Title ---
        table.add(new Label("Select Section", game.getSkin(), "title")).padBottom(80).row();

        // --- Buttons ---
        TextButton level1 = new TextButton("1", game.getSkin());
        TextButton level2 = new TextButton("2", game.getSkin());
        TextButton level3 = new TextButton("2", game.getSkin());
        TextButton level4 = new TextButton("3", game.getSkin());
        TextButton level5 = new TextButton("4", game.getSkin());
        TextButton back = new TextButton("Back", game.getSkin());

        // --- Add Buttons to Table ---
        table.add(level1).width(400).pad(10).row();
        table.add(level2).width(400).pad(10).row();
        table.add(level3).width(400).pad(10).row();
        table.add(level4).width(400).pad(10).row();
        table.add(level5).width(400).pad(10).row();
        table.add(back).width(400).pad(10).row();

        // --- Button Listeners ---
        // Each listener checks if it was called from pause and attempts to load the save.
        // If no save exists for that map or loading fails, it starts a new game for that map.

        level1.addListener(createLevelButtonListener(MAP_LEVEL_1));
        level2.addListener(createLevelButtonListener(MAP_LEVEL_2));
        level3.addListener(createLevelButtonListener(MAP_LEVEL_3));
        level4.addListener(createLevelButtonListener(MAP_LEVEL_4));
        level5.addListener(createLevelButtonListener(MAP_LEVEL_5));

        // Back button logic remains unchanged
        back.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (calledFromPause) {
                    // Return to the pause menu if coming from there
                    game.goToPause();
                    return;
                }
                // Otherwise, return to the main menu
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
                    // Attempt to load the game state when coming from pause menu
                    GameState loadedState = SaveSystem.loadGame(); // Assumes SaveSystem is accessible

                    if (loadedState != null) {
                        // Check if the loaded save state matches the selected map
                        if (mapFile.equals(loadedState.getMapFile())) { // Use getter method from GameState
                            System.out.println("Loading saved game for: " + mapFile);
                            game.goToGame(loadedState); // Load the existing game state
                        } else {
                            // Map mismatch - perhaps save was for a different level
                            System.out.println("Saved game is for a different map (" +
                                    loadedState.getMapFile() + "). Starting new game for: " + mapFile);
                            game.goToGame(mapFile); // Start new game for the selected map
                        }
                    } else {
                        // No save file found
                        System.out.println("No save file found. Starting new game for: " + mapFile);
                        game.goToGame(mapFile); // Start new game for the selected map
                    }
                } else {
                    // Always start a new game when coming from the main menu
                    System.out.println("Starting new game from main menu for: " + mapFile);
                    game.goToGame(mapFile); // Start new game for the selected map
                }
            }
        };
    }


    @Override
    public void render(float delta) {
        // Clear the screen
        Gdx.gl.glClearColor(0, 0, 0, 1); // Optional: Set clear color if needed, or rely on background image
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Update and draw the stage
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
        // Set the stage as the input processor
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void hide() {
        // Input processor might be set elsewhere when hiding, e.g., to null or another stage
    }

    @Override
    public void pause() {
        // Handle pause events if necessary
    }

    @Override
    public void resume() {
        // Handle resume events if necessary
    }
}