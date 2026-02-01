package de.tum.cit.fop.maze;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

public class SkillTree {

    private boolean speedUnlocked;
    private boolean heartUnlocked;
    private boolean greedUnlocked;

    public SkillTree() {
        // Beim Starten laden wir, was wir schon haben
        this.speedUnlocked = SaveSystem.isSkillUnlocked("speed");
        this.heartUnlocked = SaveSystem.isSkillUnlocked("heart");
        this.greedUnlocked = SaveSystem.isSkillUnlocked("greed");
    }

    public boolean unlockSpeed() {
        int cost = 500;
        int currentScore = SaveSystem.loadTotalScore();
        if (currentScore >= cost && !speedUnlocked) {
            SaveSystem.saveTotalScore(currentScore - cost); // Bezahlen
            speedUnlocked = true;
            save(); // Speichern
            return true; // Kauf erfolgreich
        }
        return false;
    }

    public boolean unlockHeart() {
        int cost = 1000;
        int currentScore = SaveSystem.loadTotalScore();
        if (currentScore >= cost && !heartUnlocked) {
            SaveSystem.saveTotalScore(currentScore - cost);
            heartUnlocked = true;
            save();
            return true;
        }
        return false;
    }

    public boolean unlockGreed() {
        int cost = 1500;
        int currentScore = SaveSystem.loadTotalScore();
        if (currentScore >= cost && !greedUnlocked) {
            SaveSystem.saveTotalScore(currentScore - cost);
            greedUnlocked = true;
            save();
            return true;
        }
        return false;
    }

    private void save() {
        SaveSystem.saveSkills(speedUnlocked, heartUnlocked, greedUnlocked);
    }

    // Getter für das Spiel
    public boolean hasSpeed() { return speedUnlocked; }
    public boolean hasHeart() { return heartUnlocked; }
    public boolean hasGreed() { return greedUnlocked; }

    // Hilfsmethode für den Multiplier
    public float getScoreMultiplier() {
        return greedUnlocked ? 1.5f : 1.0f;
    }
}