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
    // 1. DER PERFEKTE SNAPSHOT
    // ============================================================

    public static void saveGame(int hearts, float x, float y, String levelPath,
                                float timePlayed,
                                String collectedKeys,     // Welche Schlüssel weg sind
                                String enemyData,         // Wo die Gegner stehen & ob sie leben
                                String collectedHearts,   // Welche Herzen weg sind
                                String collectedBoosts,   // Welche Boosts weg sind
                                String trapStatus) {      // Welche Fallen aktiv sind

        Preferences prefs = Gdx.app.getPreferences(PREF_NAME_GAME);

        // Basis
        prefs.putInteger("hearts", hearts);
        prefs.putFloat("playerX", x);
        prefs.putFloat("playerY", y);
        prefs.putString("currentLevel", levelPath);
        prefs.putFloat("timePlayed", timePlayed);

        // Die Listen-Daten
        prefs.putString("collectedKeys", collectedKeys);
        prefs.putString("enemyData", enemyData);
        prefs.putString("collectedHearts", collectedHearts);
        prefs.putString("collectedBoosts", collectedBoosts);
        prefs.putString("trapStatus", trapStatus);

        prefs.flush();
        Gdx.app.log("SaveSystem", "Snapshot gespeichert!");
    }

    public static Preferences getGameSave() {
        return Gdx.app.getPreferences(PREF_NAME_GAME);
    }

    public static boolean hasSaveGame() {
        return getGameSave().contains("hearts");
    }

    // ... (Der Rest für InfiniteMode/TotalScore bleibt gleich wie vorher) ...
    public static void saveTotalScore(int score) {
        Gdx.app.getPreferences(PREF_NAME_GLOBAL).putInteger("total_score", score).flush();
    }
    public static int loadTotalScore() {
        return Gdx.app.getPreferences(PREF_NAME_GLOBAL).getInteger("total_score", 0);
    }
    public static void saveInfiniteModeScores(List<Integer> scores) {
        Preferences prefs = Gdx.app.getPreferences(PREF_NAME_SCORES);
        for (int i = 0; i < scores.size(); i++) prefs.putInteger("score_" + i, scores.get(i));
        for (int i = scores.size(); i < 10; i++) if (prefs.contains("score_" + i)) prefs.remove("score_" + i);
        prefs.flush();
    }
    public static List<Integer> loadInfiniteModeScores() {
        Preferences prefs = Gdx.app.getPreferences(PREF_NAME_SCORES);
        List<Integer> scores = new ArrayList<>();
        for (int i = 0; i < 10; i++) if (prefs.contains("score_" + i)) scores.add(prefs.getInteger("score_" + i));
        Collections.sort(scores, Collections.reverseOrder());
        return scores;
    }
    public static void clearSave() {
        getGameSave().clear();
        getGameSave().flush();
    }
}