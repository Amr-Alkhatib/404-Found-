package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import java.util.List;

public class LeaderboardScreen implements Screen {
    private final MazeRunnerGame game;
    private final Stage stage;
    private final Table table;
    private final Image background;
    private final Texture backgroundTexture; // 保存纹理引用用于安全释放

    public LeaderboardScreen(MazeRunnerGame game) {
        this.game = game;
        this.stage = new Stage(new ScreenViewport());
        this.table = new Table();
        this.table.setFillParent(true);

        // 创建并保存背景纹理
        this.backgroundTexture = new Texture(Gdx.files.internal("assets/images/2.png"));
        this.background = new Image(backgroundTexture);
        stage.addActor(background); // 背景在底层
        stage.addActor(table);      // 内容在上层

        // 标题
        Label title = new Label("LEADERBOARD - INFINITE MODE", game.getSkin(), "title");
        table.add(title).padBottom(30).row();

        // 加载分数（直接使用，无需强制转换）
        // 替换原第40-55行代码
// 加载无限模式分数列表（不是总分数！）
        List<Integer> scoresList = game.getInfiniteModeScores(); // ✅ 使用分数列表
        if (scoresList == null || scoresList.isEmpty()) { // ✅ 正确判断
            table.add(new Label("No scores yet. Play Infinite Mode!", game.getSkin())).padBottom(15).row();
        } else {
            // 显示前10名
            for (int i = 0; i < Math.min(scoresList.size(), 10); i++) {
                String rank = (i + 1) + ". ";
                String scoreText = rank + scoresList.get(i) + " Points"; // ✅ 添加单位更清晰
                Label entry = new Label(scoreText, game.getSkin(), "default");
                // 奖牌颜色
                if (i == 0) entry.setColor(1, 0.8f, 0, 1);      // Gold
                else if (i == 1) entry.setColor(0.75f, 0.75f, 0, 1); // Silver
                else if (i == 2) entry.setColor(0.8f, 0.5f, 0.2f, 1); // Bronze
                table.add(entry).padBottom(10).row();
            }
        }

        // 返回按钮
        TextButton backBtn = new TextButton("BACK TO MENU", game.getSkin());
        backBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.goToMenu();
            }
        });
        table.row().padTop(25);
        table.add(backBtn).width(250).height(50);
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
        // 确保背景充满整个视口
        background.setSize(stage.getViewport().getWorldWidth(), stage.getViewport().getWorldHeight());
        background.setPosition(0, 0);
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

    @Override
    public void dispose() {
        // ✅ 修复：直接释放纹理，无需检查 isDisposed()
        if (backgroundTexture != null) {
            backgroundTexture.dispose();
        }
        if (stage != null) {
            stage.dispose();
        }
    }
}