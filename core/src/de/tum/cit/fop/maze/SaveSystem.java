package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SaveSystem {

    private static final String PREF_NAME_GAME = "MazeRunnerSave";
    private static final String PREF_NAME_SCORES = "InfiniteModeScores";
    private static final String PREF_NAME_GLOBAL = "MazeRunnerPrefs";

    public static void saveGame(int hearts, float x, float y, String levelPath,
                                float timePlayed,
                                String keyData,
                                String enemyData,
                                String heartData,
                                String boostData,
                                String trapData,
                                String morphTrapData,
                                int totalHearts,
                                int totalEnemies) {

        Preferences prefs = Gdx.app.getPreferences(PREF_NAME_GAME);

        prefs.putInteger("hearts", hearts);
        prefs.putFloat("playerX", x);
        prefs.putFloat("playerY", y);
        prefs.putString("currentLevel", levelPath);
        prefs.putFloat("timePlayed", timePlayed);

        prefs.putString("keyData", keyData);
        prefs.putString("enemyData", enemyData);
        prefs.putString("heartData", heartData);
        prefs.putString("boostData", boostData);
        prefs.putString("trapData", trapData);
        prefs.putString("morphTrapData", morphTrapData);

        prefs.putInteger("savedTotalHearts", totalHearts);
        prefs.putInteger("savedTotalEnemies", totalEnemies);

        prefs.flush();
        Gdx.app.log("SaveSystem", "Snapshot mit exakten Positionen gespeichert!");
    }

    public static Preferences getGameSave() {
        return Gdx.app.getPreferences(PREF_NAME_GAME);
    }

    public static boolean hasSaveGame() {
        return getGameSave().contains("hearts");
    }

    public static void clearSave() {
        getGameSave().clear();
        getGameSave().flush();
    }

    public static void saveTotalScore(int score) {
        Gdx.app.getPreferences(PREF_NAME_GLOBAL).putInteger("total_score", score).flush();
    }

    public static int loadTotalScore() {
        return Gdx.app.getPreferences(PREF_NAME_GLOBAL).getInteger("total_score", 0);
    }

    public static void saveInfiniteModeScores(List<Integer> scores) {
        Preferences prefs = Gdx.app.getPreferences(PREF_NAME_SCORES);
        for (int i = 0; i < scores.size(); i++) {
            prefs.putInteger("score_" + i, scores.get(i));
        }
        for (int i = scores.size(); i < 10; i++) {
            if (prefs.contains("score_" + i)) {
                prefs.remove("score_" + i);
            }
        }
        prefs.flush();
    }

    public static List<Integer> loadInfiniteModeScores() {
        Preferences prefs = Gdx.app.getPreferences(PREF_NAME_SCORES);
        List<Integer> scores = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            if (prefs.contains("score_" + i)) {
                scores.add(prefs.getInteger("score_" + i));
            }
        }
        Collections.sort(scores, Collections.reverseOrder());
        return scores;
    }

    public static void saveSkills(boolean speed, boolean heart, boolean greed) {
        Preferences prefs = Gdx.app.getPreferences("MazeRunnerSkills");
        prefs.putBoolean("skill_speed", speed);
        prefs.putBoolean("skill_heart", heart);
        prefs.putBoolean("skill_greed", greed);
        prefs.flush();
    }

    public static boolean isSkillUnlocked(String skillName) {
        return Gdx.app.getPreferences("MazeRunnerSkills").getBoolean("skill_" + skillName, false);
    }

    public static void unlockAchievement(String id) {
        Preferences prefs = Gdx.app.getPreferences("MazeRunnerAchievements");
        if (!prefs.getBoolean(id, false)) {
            prefs.putBoolean(id, true);
            prefs.flush();
            Gdx.app.log("Achievement", "UNLOCKED: " + id);
        }
    }

    public static boolean isAchievementUnlocked(String id) {
        return Gdx.app.getPreferences("MazeRunnerAchievements").getBoolean(id, false);
    }

    public static void addGlobalHearts(int amount) {
        Preferences prefs = Gdx.app.getPreferences("MazeRunnerStats");
        int current = prefs.getInteger("total_hearts_collected", 0);
        prefs.putInteger("total_hearts_collected", current + amount);
        prefs.flush();
    }

    public static int getGlobalHearts() {
        return Gdx.app.getPreferences("MazeRunnerStats").getInteger("total_hearts_collected", 0);
    }
}

