package de.tum.cit.fop.maze;

public class SkillTree {

    // ===== Skills =====
    private boolean extraHeartUnlocked = false;
    private boolean scoreBoostUnlocked = false;

    // ===== Unlock logic =====
    public void unlockExtraHeart() {
        extraHeartUnlocked = true;
    }

    public void unlockScoreBoost() {
        scoreBoostUnlocked = true;
    }

    // ===== Query =====
    public boolean hasExtraHeart() {
        return extraHeartUnlocked;
    }

    public boolean hasScoreBoost() {
        return scoreBoostUnlocked;
    }

    // ===== Score modifier =====
    public float getScoreMultiplier() {
        return scoreBoostUnlocked ? 1.2f : 1.0f;
    }
}