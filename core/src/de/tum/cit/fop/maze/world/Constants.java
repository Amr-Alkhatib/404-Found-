package de.tum.cit.fop.maze.world;

/**
 * A static class of constants essential for gameplay
 */
public final class Constants {
    private Constants() {
    }

    /**
     * How fast character walks
     */
    public static final float characterBaseWalkingSpeed = 1.7f;

    /**
     * How fast character walks when boost collected
     */
    public static final float characterBoostedWalking = 3f;

    /**
     * How fast character runs related to walking
     */
    public static final float characterRunningBoost = 1.7f;

    /**
     * How many "lives" the character has maximum
     */
    public static final int characterMaxBooks = 5;

    /**
     * How many "lives" the character starts with
     */
    public static final int characterInitialBooks = 3;

    /**
     * How long does pain or gain "last"
     */
    public static final float characterPainGainTolerance = 0.3f;

    /**
     * How long a walking boost lasts
     */
    public static final float characterBoostLast = 3f;

    /**
     * The speed at which the enemies move
     */
    public static final float enemyWalkingSpeed = 0.7f;

    /**
     * From what distance an enemy "sees" the character
     */
    public static final int enemySightRange = 2;


    /**
     * The "precision" with which wall is to be avoided.
     */
    public static final float calibratedCollision = 0.98f;
}
