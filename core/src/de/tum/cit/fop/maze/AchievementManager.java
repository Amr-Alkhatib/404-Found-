package de.tum.cit.fop.maze;

import java.util.ArrayList;
import java.util.List;

public class AchievementManager {
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
        list.add(new Achievement(ACH_SPEED_RUNNER, "Speed Runner", "Finish a level in under 15s."));
        list.add(new Achievement(ACH_COLLECTOR, "Collector", "Collect 150 Hearts total."));
        list.add(new Achievement(ACH_MILLIONAIRE, "Millionaire", "Have 10000 Points."));
        list.add(new Achievement(ACH_SURVIVOR, "Survivor", "Reach Level 10 in Infinite Mode."));
        return list;
    }

    public void onLevelFinished(float timeNeeded, String mapName) {
        if (mapName.contains("level-1")) {
            unlock(ACH_NOVICE);
        }
        if (timeNeeded <= 15.0f) {
            unlock(ACH_SPEED_RUNNER);
        }
    }

    public void onHeartsCollected(int amountInLevel) {
        SaveSystem.addGlobalHearts(amountInLevel);

        if (SaveSystem.getGlobalHearts() >= 150) {
            unlock(ACH_COLLECTOR);
        }
    }

    public void onScoreUpdated(int currentTotalScore) {
        if (currentTotalScore >= 10000) {
            unlock(ACH_MILLIONAIRE);
        }
    }

    public void onInfiniteLevelReached(int levelNumber) {

        if (levelNumber >= 10) {
            unlock(ACH_SURVIVOR);
        }
    }

    private void unlock(String id) {
        SaveSystem.unlockAchievement(id);
    }
}
