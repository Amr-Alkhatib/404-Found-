package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import java.util.ArrayList;
import java.util.List;

public class AchievementManager {

    // IDs
    public static final String ACH_NOVICE = "ach_novice";
    public static final String ACH_SPEED_RUNNER = "ach_speed_runner";
    public static final String ACH_COLLECTOR = "ach_collector";
    public static final String ACH_MILLIONAIRE = "ach_millionaire";
    public static final String ACH_SURVIVOR = "ach_survivor";

    public static class Achievement {
        public String name;
        public String description;
        public boolean unlocked;

        public Achievement(String id, String name, String description) {
            this.name = name;
            this.description = description;
            this.unlocked = SaveSystem.isAchievementUnlocked(id);
        }
    }

    public List<Achievement> getAchievements() {
        List<Achievement> list = new ArrayList<>();
        list.add(new Achievement(ACH_NOVICE, "Novice", "Complete the first level."));
        list.add(new Achievement(ACH_SPEED_RUNNER, "Speed Runner", "Finish a level in under 30s."));
        list.add(new Achievement(ACH_COLLECTOR, "Collector", "Collect 100 Hearts total."));
        list.add(new Achievement(ACH_MILLIONAIRE, "Millionaire", "Have 5000 Points."));
        list.add(new Achievement(ACH_SURVIVOR, "Survivor", "Reach Level 5 in Infinite Mode."));
        return list;
    }

    // =========================
    // EVENTS (Hier prüfen wir die Regeln)
    // =========================

    public void onLevelFinished(float timeNeeded, String mapName) {
        // 1. Novice: Irgendein Level geschafft (wir nehmen an level-1)
        if (mapName.contains("level-1")) {
            unlock(ACH_NOVICE);
        }
        // 2. Speed Runner: Unter 30 Sekunden
        if (timeNeeded <= 15.0f) {
            unlock(ACH_SPEED_RUNNER);
        }
    }

    public void onHeartsCollected(int amountInLevel) {
        // Zuerst im SaveSystem addieren
        SaveSystem.addGlobalHearts(amountInLevel);

        // 3. Collector: Prüfen ob wir jetzt 100 haben
        if (SaveSystem.getGlobalHearts() >= 150) {
            unlock(ACH_COLLECTOR);
        }
    }

    public void onScoreUpdated(int currentTotalScore) {
        // 4. Millionaire: 5000 Punkte
        if (currentTotalScore >= 10000) {
            unlock(ACH_MILLIONAIRE);
        }
    }

    public void onInfiniteLevelReached(int levelNumber) {
        // 5. Survivor: Level 5 erreicht
        if (levelNumber >= 10) {
            unlock(ACH_SURVIVOR);
        }
    }

    private void unlock(String id) {
        SaveSystem.unlockAchievement(id);
    }
}
