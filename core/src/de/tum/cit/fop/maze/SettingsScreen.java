package de.tum.cit.fop.maze;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/**
 * Settings screen for adjusting game options like volume, fullscreen mode, and key bindings.
 */
public class SettingsScreen implements Screen {

    private final MazeRunnerGame game;
    private Stage stage;
    private Texture backgroundTexture;


    private Slider musicSlider;
    private Slider sfxSlider;
    private CheckBox fullscreenCheckBox;

    private Label upKeyLabel;
    private Label downKeyLabel;
    private Label leftKeyLabel;
    private Label rightKeyLabel;
    private Label actionKeyLabel;
    private Label sprintKeyLabel;

    // Preferences keys
    private static final String PREF_MUSIC_VOLUME = "music_volume";
    private static final String PREF_SFX_VOLUME = "sfx_volume";
    private static final String PREF_FULLSCREEN = "fullscreen";
    private static final String PREF_KEY_UP = "key_up";
    private static final String PREF_KEY_DOWN = "key_down";
    private static final String PREF_KEY_LEFT = "key_left";
    private static final String PREF_KEY_RIGHT = "key_right";
    private static final String PREF_KEY_ACTION = "key_action";
    private static final String PREF_KEY_SPRINT = "key_sprint";


    private String currentBinding = null;


    private InputMultiplexer inputMultiplexer;

    public SettingsScreen(MazeRunnerGame game) {
        this.game = game;
        this.stage = new Stage(new ScreenViewport());
        this.backgroundTexture = new Texture(Gdx.files.internal("assets/images/2.png"));

        var prefs = Gdx.app.getPreferences("MazeRunnerPrefs");
        float savedMusic = prefs.getFloat(PREF_MUSIC_VOLUME, 0.7f);
        float savedSfx = prefs.getFloat(PREF_SFX_VOLUME, 0.5f);
        boolean savedFullscreen = prefs.getBoolean(PREF_FULLSCREEN, false);

        int keyUp = prefs.getInteger(PREF_KEY_UP, Input.Keys.W);
        int keyDown = prefs.getInteger(PREF_KEY_DOWN, Input.Keys.S);
        int keyLeft = prefs.getInteger(PREF_KEY_LEFT, Input.Keys.A);
        int keyRight = prefs.getInteger(PREF_KEY_RIGHT, Input.Keys.D);
        int keyAction = prefs.getInteger(PREF_KEY_ACTION, Input.Keys.SPACE);
        int keySprint = prefs.getInteger(PREF_KEY_SPRINT, Input.Keys.SHIFT_LEFT); // ← 默认为左 Shift


        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        table.add(new Label("Settings", game.getSkin(), "title")).padBottom(40).row();


        table.add(new Label("Music Volume:", game.getSkin())).left().padRight(20);
        musicSlider = new Slider(0f, 1f, 0.01f, false, game.getSkin());
        musicSlider.setValue(savedMusic);
        musicSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (game.getCurrentBackgroundMusic() != null) {
                    game.getCurrentBackgroundMusic().setVolume(musicSlider.getValue());
                }
            }
        });
        table.add(musicSlider).width(200).padBottom(15).row();


        table.add(new Label("Sound Effects:", game.getSkin())).left().padRight(20);
        sfxSlider = new Slider(0f, 1f, 0.01f, false, game.getSkin());
        sfxSlider.setValue(savedSfx);
        table.add(sfxSlider).width(200).padBottom(25).row();


        fullscreenCheckBox = new CheckBox("Fullscreen Mode", game.getSkin());
        fullscreenCheckBox.setChecked(savedFullscreen);
        table.add(fullscreenCheckBox).colspan(2).left().padBottom(30).row();


        table.add(new Label("Key Bindings:", game.getSkin(), "bold")).colspan(2).left().padTop(20).padBottom(15).row();


        table.add(new Label("Move Up:", game.getSkin())).left().padRight(20);
        upKeyLabel = createKeyLabel(keyUp);
        table.add(upKeyLabel).left().padBottom(8).row();


        table.add(new Label("Move Down:", game.getSkin())).left().padRight(20);
        downKeyLabel = createKeyLabel(keyDown);
        table.add(downKeyLabel).left().padBottom(8).row();


        table.add(new Label("Move Left:", game.getSkin())).left().padRight(20);
        leftKeyLabel = createKeyLabel(keyLeft);
        table.add(leftKeyLabel).left().padBottom(8).row();

        table.add(new Label("Move Right:", game.getSkin())).left().padRight(20);
        rightKeyLabel = createKeyLabel(keyRight);
        table.add(rightKeyLabel).left().padBottom(8).row();

        table.add(new Label("Action:", game.getSkin())).left().padRight(20);
        actionKeyLabel = createKeyLabel(keyAction);
        table.add(actionKeyLabel).left().padBottom(8).row();

        table.add(new Label("Sprint:", game.getSkin())).left().padRight(20);
        sprintKeyLabel = createKeyLabel(keySprint);
        table.add(sprintKeyLabel).left().padBottom(20).row();

        TextButton backButton = new TextButton("Back to Menu", game.getSkin());
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                saveAndApplySettings();
                game.goToMenu();
            }
        });
        table.add(backButton).colspan(2).padTop(20);
    }

    private Label createKeyLabel(int keycode) {
        Label label = new Label(Input.Keys.toString(keycode), game.getSkin());
        label.setTouchable(Touchable.enabled);
        label.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                currentBinding = getLabelName(label);
                label.setText("Press a key...");
            }
        });
        return label;
    }

    private String getLabelName(Label label) {
        if (label == upKeyLabel) return "up";
        if (label == downKeyLabel) return "down";
        if (label == leftKeyLabel) return "left";
        if (label == rightKeyLabel) return "right";
        if (label == actionKeyLabel) return "action";
        if (label == sprintKeyLabel) return "sprint"; // ← 新增
        return "";
    }

    private void saveAndApplySettings() {
        var prefs = Gdx.app.getPreferences("MazeRunnerPrefs");
        prefs.putFloat(PREF_MUSIC_VOLUME, musicSlider.getValue());
        prefs.putFloat(PREF_SFX_VOLUME, sfxSlider.getValue());
        prefs.putBoolean(PREF_FULLSCREEN, fullscreenCheckBox.isChecked());

        if (fullscreenCheckBox.isChecked()) {
            Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
        } else {
            Gdx.graphics.setWindowedMode(1280, 720);
        }

        if (game.getCurrentBackgroundMusic() != null) {
            game.getCurrentBackgroundMusic().setVolume(musicSlider.getValue());
        }

        prefs.flush();
    }

    @Override
    public void show() {
        InputAdapter keyBindingListener = new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (currentBinding != null) {
                    var prefs = Gdx.app.getPreferences("MazeRunnerPrefs");
                    switch (currentBinding) {
                        case "up" -> {
                            prefs.putInteger(PREF_KEY_UP, keycode);
                            upKeyLabel.setText(Input.Keys.toString(keycode));
                        }
                        case "down" -> {
                            prefs.putInteger(PREF_KEY_DOWN, keycode);
                            downKeyLabel.setText(Input.Keys.toString(keycode));
                        }
                        case "left" -> {
                            prefs.putInteger(PREF_KEY_LEFT, keycode);
                            leftKeyLabel.setText(Input.Keys.toString(keycode));
                        }
                        case "right" -> {
                            prefs.putInteger(PREF_KEY_RIGHT, keycode);
                            rightKeyLabel.setText(Input.Keys.toString(keycode));
                        }
                        case "action" -> {
                            prefs.putInteger(PREF_KEY_ACTION, keycode);
                            actionKeyLabel.setText(Input.Keys.toString(keycode));
                        }
                        case "sprint" -> { // ← 新增
                            prefs.putInteger(PREF_KEY_SPRINT, keycode);
                            sprintKeyLabel.setText(Input.Keys.toString(keycode));
                        }
                    }
                    prefs.flush();
                    currentBinding = null;
                    return true;
                }
                return false;
            }
        };

        inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(keyBindingListener);
        inputMultiplexer.addProcessor(stage);

        Gdx.input.setInputProcessor(inputMultiplexer);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.getSpriteBatch().begin();
        game.getSpriteBatch().draw(backgroundTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        game.getSpriteBatch().end();

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        if (stage != null) stage.dispose();
        if (backgroundTexture != null) backgroundTexture.dispose();
    }
}