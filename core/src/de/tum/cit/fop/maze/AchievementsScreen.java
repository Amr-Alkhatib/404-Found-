package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class AchievementsScreen implements Screen {

    private final MazeRunnerGame game;
    private final Stage stage;
    private final AchievementManager manager;

    public AchievementsScreen(MazeRunnerGame game) {
        this.game = game;
        this.manager = new AchievementManager();
        this.stage = new Stage(new ScreenViewport(), game.getSpriteBatch());
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);

        Table mainTable = new Table();
        mainTable.setFillParent(true);
        stage.addActor(mainTable);

        mainTable.add(new Label("ACHIEVEMENTS", game.getSkin(), "title")).padBottom(30).row();

        Table listTable = new Table();

        for (AchievementManager.Achievement ach : manager.getAchievements()) {
            Label nameLabel = new Label(ach.name, game.getSkin());
            nameLabel.setFontScale(1.2f);

            String statusText = ach.unlocked ? "[UNLOCKED]" : "[LOCKED]";
            Label statusLabel = new Label(statusText, game.getSkin());
            statusLabel.setColor(ach.unlocked ? Color.GREEN : Color.RED);

            Label descLabel = new Label(ach.description, game.getSkin());
            descLabel.setFontScale(0.8f);
            descLabel.setColor(Color.LIGHT_GRAY);

            listTable.add(nameLabel).left().padRight(20);
            listTable.add(statusLabel).right().row();
            listTable.add(descLabel).left().colspan(2).padBottom(20).row();
        }

        ScrollPane scrollPane = new ScrollPane(listTable, game.getSkin());

        scrollPane.setFadeScrollBars(false);

        mainTable.add(scrollPane).width(700).height(400).padBottom(20).row();

        TextButton backBtn = new TextButton("Back", game.getSkin());
        backBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.goToMenu();
            }
        });
        mainTable.add(backBtn).width(200).height(50);

        stage.setScrollFocus(scrollPane);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override public void resize(int width, int height) { stage.getViewport().update(width, height, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() { stage.dispose(); }
}