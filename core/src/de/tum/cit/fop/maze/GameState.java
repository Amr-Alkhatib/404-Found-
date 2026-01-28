package de.tum.cit.fop.maze;

import java.io.Serializable;
import java.util.List;

/**
 * Represents the state of the game at a given moment, used for saving and loading.
 * This class must implement Serializable to allow object serialization.
 */
public class GameState implements Serializable {
    private static final long serialVersionUID = 1L; // Version control for serialization

    // Player state
    public float playerX, playerY;
    public int currentHearts;

    // Collected items state
    public List<Float> collectedKeyPositionsX;
    public List<Float> collectedKeyPositionsY;
    // === 新增：成就系统字段 ===
    public int totalHeartsCollected;
    public int totalEnemiesKilled;
    // Exit state
    public List<Float> exitPositionsX;
    public List<Float> exitPositionsY;
    public List<Boolean> exitLockStatuses;

    // Enemy state
    public List<Float> enemyPositionsX;
    public List<Float> enemyPositionsY;
    public List<Boolean> enemyActiveStatuses;

    // Trap state
    public List<Float> trapPositionsX;
    public List<Float> trapPositionsY;
    public List<Boolean> trapActiveStatuses;

    // Heart state
    public List<Float> heartPositionsX;
    public List<Float> heartPositionsY;
    public List<Boolean> heartCollectedStatuses;

    // Boost state
    public List<Float> boostPositionsX;
    public List<Float> boostPositionsY;
    public List<Boolean> boostCollectedStatuses;

    // MorphTrap state
    public List<Float> morphTrapPositionsX;
    public List<Float> morphTrapPositionsY;
    public List<Boolean> morphTrapActiveStatuses;
    public List<Boolean> morphTrapAffectingPlayerStatuses;

    // Gameplay state
    public float timePlayed;
    public String mapFile; // Store the map file name to know which level was loaded

    /**
     * Constructor to initialize the GameState with specific values.
     * This constructor is called by GameManager.getCurrentGameState().
     *
     * @param playerX                          Player's X coordinate
     * @param playerY                          Player's Y coordinate
     * @param currentHearts                    Player's current number of hearts
     * @param collectedKeyPositionsX           X coordinates of collected keys
     * @param collectedKeyPositionsY           Y coordinates of collected keys
     * @param exitPositionsX                   X coordinates of exits
     * @param exitPositionsY                   Y coordinates of exits
     * @param exitLockStatuses                 Lock status of each exit
     * @param enemyPositionsX                  X coordinates of enemies
     * @param enemyPositionsY                  Y coordinates of enemies
     * @param enemyActiveStatuses              Active status of each enemy
     * @param trapPositionsX                   X coordinates of traps
     * @param trapPositionsY                   Y coordinates of traps
     * @param trapActiveStatuses               Active status of each trap
     * @param heartPositionsX                  X coordinates of hearts
     * @param heartPositionsY                  Y coordinates of hearts
     * @param heartCollectedStatuses           Collection status of each heart
     * @param boostPositionsX                  X coordinates of boosts
     * @param boostPositionsY                  Y coordinates of boosts
     * @param boostCollectedStatuses           Collection status of each boost
     * @param morphTrapPositionsX              X coordinates of morph traps
     * @param morphTrapPositionsY              Y coordinates of morph traps
     * @param morphTrapActiveStatuses          Active status of each morph trap
     * @param morphTrapAffectingPlayerStatuses Whether each morph trap affects the player
     * @param timePlayed                       Elapsed game time
     * @param mapFile                          Name of the map file
     * @param totalHeartsCollected
     * @param totalEnemiesKilled
     */
    public GameState(
            float playerX, float playerY, int currentHearts,
            List<Float> collectedKeyPositionsX, List<Float> collectedKeyPositionsY,
            List<Float> exitPositionsX, List<Float> exitPositionsY, List<Boolean> exitLockStatuses,
            List<Float> enemyPositionsX, List<Float> enemyPositionsY, List<Boolean> enemyActiveStatuses,
            List<Float> trapPositionsX, List<Float> trapPositionsY, List<Boolean> trapActiveStatuses,
            List<Float> heartPositionsX, List<Float> heartPositionsY, List<Boolean> heartCollectedStatuses,
            List<Float> boostPositionsX, List<Float> boostPositionsY, List<Boolean> boostCollectedStatuses,
            List<Float> morphTrapPositionsX, List<Float> morphTrapPositionsY,
            List<Boolean> morphTrapActiveStatuses, List<Boolean> morphTrapAffectingPlayerStatuses,
            float timePlayed, String mapFile, int totalHeartsCollected, int totalEnemiesKilled) {

        this.playerX = playerX;
        this.playerY = playerY;
        this.currentHearts = currentHearts;
        this.collectedKeyPositionsX = collectedKeyPositionsX;
        this.collectedKeyPositionsY = collectedKeyPositionsY;
        this.exitPositionsX = exitPositionsX;
        this.exitPositionsY = exitPositionsY;
        this.exitLockStatuses = exitLockStatuses;
        this.enemyPositionsX = enemyPositionsX;
        this.enemyPositionsY = enemyPositionsY;
        this.enemyActiveStatuses = enemyActiveStatuses;
        this.trapPositionsX = trapPositionsX;
        this.trapPositionsY = trapPositionsY;
        this.trapActiveStatuses = trapActiveStatuses;
        this.heartPositionsX = heartPositionsX;
        this.heartPositionsY = heartPositionsY;
        this.heartCollectedStatuses = heartCollectedStatuses;
        this.boostPositionsX = boostPositionsX;
        this.boostPositionsY = boostPositionsY;
        this.boostCollectedStatuses = boostCollectedStatuses;
        this.morphTrapPositionsX = morphTrapPositionsX;
        this.morphTrapPositionsY = morphTrapPositionsY;
        this.morphTrapActiveStatuses = morphTrapActiveStatuses;
        this.morphTrapAffectingPlayerStatuses = morphTrapAffectingPlayerStatuses;
        this.timePlayed = timePlayed;
        this.mapFile = mapFile;
    }

    // --- Added Getter Methods ---
    public String getMapFile() {
        return mapFile;
    }
    // 修改构造函数签名（在参数列表最后添加）
    public GameState(
            // ... 原有所有参数 ...,
            float timePlayed, String mapFile,
            // === 新增参数 ===
            int totalHeartsCollected, int totalEnemiesKilled) {

        // ... 原有赋值 ...

        this.timePlayed = timePlayed;
        this.mapFile = mapFile;

        // === 新增赋值 ===
        this.totalHeartsCollected = totalHeartsCollected;
        this.totalEnemiesKilled = totalEnemiesKilled;
    }
    // 如果需要其他字段的 getter，也可以在这里添加
    // public float getPlayerX() { return playerX; }
    // ... etc
}