package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class SkillTreeScreen implements Screen {

    private final MazeRunnerGame game;
    private final Stage stage;
    private final SkillTree skillTree;
    private Label scoreLabel;

    public SkillTreeScreen(MazeRunnerGame game) {
        this.game = game;
        this.skillTree = new SkillTree(); // Lädt aktuellen Stand
        this.stage = new Stage(new ScreenViewport(), game.getSpriteBatch());
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        setupUI();
    }

    private void setupUI() {
        stage.clear(); // Reset UI bei jedem Öffnen
        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        // Titel & Score Anzeige
        table.add(new Label("SKILL TREE", game.getSkin(), "title")).padBottom(20).row();

        int currentScore = SaveSystem.loadTotalScore();
        scoreLabel = new Label("Points available: " + currentScore, game.getSkin());
        table.add(scoreLabel).padBottom(40).row();

        // --- SPEED BUTTON ---
        String speedText = skillTree.hasSpeed() ? "Speed UNLOCKED" : "Buy Speed (500 Pts)";
        TextButton speedBtn = new TextButton(speedText, game.getSkin());
        speedBtn.setDisabled(skillTree.hasSpeed());
        speedBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (skillTree.unlockSpeed()) {
                    updateUI(speedBtn, "Speed UNLOCKED");
                }
            }
        });
        table.add(speedBtn).width(400).pad(10).row();

        // --- HEART BUTTON ---
        String heartText = skillTree.hasHeart() ? "Extra Heart UNLOCKED" : "Buy Heart (1000 Pts)";
        TextButton heartBtn = new TextButton(heartText, game.getSkin());
        heartBtn.setDisabled(skillTree.hasHeart());
        heartBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (skillTree.unlockHeart()) {
                    updateUI(heartBtn, "Extra Heart UNLOCKED");
                }
            }
        });
        table.add(heartBtn).width(400).pad(10).row();

        // --- GREED BUTTON ---
        String greedText = skillTree.hasGreed() ? "x1.5 Score UNLOCKED" : "Buy Greed (1500 Pts)";
        TextButton greedBtn = new TextButton(greedText, game.getSkin());
        greedBtn.setDisabled(skillTree.hasGreed());
        greedBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (skillTree.unlockGreed()) {
                    updateUI(greedBtn, "x1.5 Score UNLOCKED");
                }
            }
        });
        table.add(greedBtn).width(400).pad(10).row();

        // --- BACK BUTTON ---
        TextButton backBtn = new TextButton("Back to Menu", game.getSkin());
        backBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.goToMenu();
            }
        });
        table.add(backBtn).padTop(50).width(300).row();
    }

    private void updateUI(TextButton btn, String newText) {
        btn.setText(newText);
        btn.setDisabled(true);
        scoreLabel.setText("Points available: " + SaveSystem.loadTotalScore());
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() { stage.dispose(); }
}
