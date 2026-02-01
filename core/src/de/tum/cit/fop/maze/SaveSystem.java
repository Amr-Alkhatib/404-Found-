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

    // ============================================================
    // 1. DER PERFEKTE SNAPSHOT (Mit Positionen für ALLES + Score)
    // ============================================================

    public static void saveGame(int hearts, float x, float y, String levelPath,
                                float timePlayed,
                                String keyData,       // Geändert: Speichert jetzt Positionen + Status
                                String enemyData,     // Speichert Positionen + Status
                                String heartData,     // Geändert: Positionen + Status
                                String boostData,     // Geändert: Positionen + Status
                                String trapData,      // Geändert: Positionen + Status
                                String morphTrapData, // NEU: MorphTraps
                                int totalHearts,      // NEU: Score
                                int totalEnemies) {   // NEU: Score

        Preferences prefs = Gdx.app.getPreferences(PREF_NAME_GAME);

        // Basis-Daten
        prefs.putInteger("hearts", hearts);
        prefs.putFloat("playerX", x);
        prefs.putFloat("playerY", y);
        prefs.putString("currentLevel", levelPath);
        prefs.putFloat("timePlayed", timePlayed);

        // Objekt-Daten (Positionen & Status)
        prefs.putString("keyData", keyData);
        prefs.putString("enemyData", enemyData);
        prefs.putString("heartData", heartData);
        prefs.putString("boostData", boostData);
        prefs.putString("trapData", trapData);
        prefs.putString("morphTrapData", morphTrapData);

        // Score-Daten
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

    // ============================================================
    // 2. GLOBAL SCORE & INFINITE MODE (Bleibt unverändert)
    // ============================================================

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

    // In SaveSystem.java

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
}

