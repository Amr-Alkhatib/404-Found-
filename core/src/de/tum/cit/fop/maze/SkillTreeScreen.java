package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color; // WICHTIG: Import für die Farben
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

        // Titel
        table.add(new Label("SKILL TREE", game.getSkin(), "title")).padBottom(20).row();

        // Score Anzeige
        int currentScore = SaveSystem.loadTotalScore();
        scoreLabel = new Label("Points available: " + currentScore, game.getSkin());
        table.add(scoreLabel).padBottom(40).row();

        // --- SPEED BUTTON ---
        TextButton speedBtn = new TextButton("", game.getSkin());
        // Sofort prüfen: Ist es schon gekauft? Farbe setzen!
        updateButtonVisuals(speedBtn, skillTree.hasSpeed(), "Speed Boost", 1500);

        speedBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (skillTree.unlockSpeed()) {
                    updateButtonVisuals(speedBtn, true, "Speed Boost", 1500);
                    updateScoreLabel(); // Score oben aktualisieren
                }
            }
        });
        table.add(speedBtn).width(400).pad(10).row();

        // --- HEART BUTTON ---
        TextButton heartBtn = new TextButton("", game.getSkin());
        updateButtonVisuals(heartBtn, skillTree.hasHeart(), "Extra Heart", 3000);

        heartBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (skillTree.unlockHeart()) {
                    updateButtonVisuals(heartBtn, true, "Extra Heart", 3000);
                    updateScoreLabel();
                }
            }
        });
        table.add(heartBtn).width(400).pad(10).row();

        // --- GREED BUTTON ---
        TextButton greedBtn = new TextButton("", game.getSkin());
        updateButtonVisuals(greedBtn, skillTree.hasGreed(), "Greed (Score x1.5)", 5000);

        greedBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (skillTree.unlockGreed()) {
                    updateButtonVisuals(greedBtn, true, "Greed (Score x1.5)", 5000);
                    updateScoreLabel();
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

    /**
     * Diese Methode kümmert sich um Farben und Text:
     * - Grün/Active: Wenn schon gekauft.
     * - Rot: Wenn zu teuer.
     * - Weiß: Wenn kaufbar.
     */
    private void updateButtonVisuals(TextButton btn, boolean isUnlocked, String skillName, int cost) {
        if (isUnlocked) {
            btn.setText(skillName + " [ACTIVE]");
            btn.getLabel().setColor(Color.GREEN);
            btn.setDisabled(true); // Knopf deaktivieren, da schon gekauft
        } else {
            btn.setText("Buy " + skillName + " (" + cost + " Pts)");

            int currentScore = SaveSystem.loadTotalScore();
            if (currentScore >= cost) {
                btn.getLabel().setColor(Color.WHITE); // Genug Geld -> Weiß
                btn.setDisabled(false);
            } else {
                btn.getLabel().setColor(Color.RED);   // Zu arm -> Rot
                // Wir lassen ihn aktiv, damit man klicken kann (passiert aber nichts, außer "Fail")
                // oder du kannst btn.setDisabled(true) machen, wenn man gar nicht klicken können soll.
                btn.setDisabled(false);
            }
        }
    }

    private void updateScoreLabel() {
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
