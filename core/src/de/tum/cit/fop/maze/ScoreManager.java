package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

public class ScoreManager {

    private int currentScore = 0;
    private int highScore = 0;
    private boolean frozen = false;

    public void reset() {
        currentScore = 0;
        frozen = false;
    }

    public void addScore(int amount) {
        if (!frozen) {
            currentScore += amount;
        }
    }

    public void updateTimeScore(float delta) {
        if (!frozen) {
            currentScore += (int)(delta * 10); // 时间计分，示例
        }
    }

    public int getCurrentScore() {
        return currentScore;
    }

    public int getHighScore() {
        return highScore;
    }

    /** 🔴 核心：只在 Game Over / Run End 调一次 */
    public void finalizeScore() {
        frozen = true;
        if (currentScore > highScore) {
            highScore = currentScore;
            saveHighScore();
        }
    }

    private void saveHighScore() {
        Preferences prefs = Gdx.app.getPreferences("DancingLineMaze");
        prefs.putInteger("highScore", highScore);
        prefs.flush();
    }

    public void loadHighScore() {
        Preferences prefs = Gdx.app.getPreferences("DancingLineMaze");
        highScore = prefs.getInteger("highScore", 0);
    }
}
