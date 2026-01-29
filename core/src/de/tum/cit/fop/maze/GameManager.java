package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import de.tum.cit.fop.maze.world.*;

import java.util.ArrayList;
import java.util.List;

public class GameManager {
    private boolean isInfiniteMode = false;
    private int totalHeartsCollected = 0;
    private int totalEnemiesKilled = 0;
    // 在 GameManager 类中添加两个变量
    private int totalHeartsCollectedThisSession;
    private int totalEnemiesKilledThisSession;
    private final GameMap gameMap;
    private final GameScreen gameScreen;
    private final MazeRunnerGame game;
    private final List<Wall> walls;
    private final Entrance entrance;
    private final List<Exit> exits;
    private final GameCharacter player;
    private final List<Trap> traps;
    private final List<Enemy> enemies;
    private final List<Key> keys;
    private final List<Heart> hearts;
    private final List<Boost> boosts;
    private final Hud hud;
    private ExitArrow exitArrow;
    private final int sprintKeyCode;
    private final List<MorphTrap> morphTraps;

    private boolean lose = false;
    private boolean win = false;
    private float timePlayed;
    private Label timer;

    private boolean canSaveOrLoad = true;

    public GameManager(GameMap gameMap, MazeRunnerGame game, GameScreen gameScreen, GameCharacter player, Hud hud, List<Wall> walls, List<Enemy> enemies, List<Key> keys, List<Trap> traps, Entrance entrance, List<Exit> exits, List<Heart> hearts, List<Boost> boosts, List<MorphTrap> morphTraps, ExitArrow exitArrow, int sprintKeyCode) {
        this.gameMap = gameMap;
        this.game = game;
        this.gameScreen = gameScreen;
        this.player = player;
        this.hud = hud;
        this.walls = walls;
        this.enemies = enemies;
        this.keys = keys;
        this.hearts = hearts;
        this.traps = traps;
        this.entrance = entrance;
        this.exits = exits;
        this.boosts = boosts;
        this.exitArrow = exitArrow;
        this.sprintKeyCode = sprintKeyCode;
        this.morphTraps = morphTraps;
        this.timePlayed = 0f;
        this.isInfiniteMode = "INFINITE_MODE".equals(gameMap.getMapFile());
        setupTimer();

        GameState loadedState = SaveSystem.loadGame();
        if (loadedState != null) {
            applyLoadedState(loadedState);
        }
    }

    public GameState getCurrentGameState() {
        List<Float> collectedKeyPositionsX = new ArrayList<>();
        List<Float> collectedKeyPositionsY = new ArrayList<>();
        for (Key key : this.keys) {
            if (key.isCollected()) {
                collectedKeyPositionsX.add(key.getX());
                collectedKeyPositionsY.add(key.getY());
            }
        }

        List<Float> exitPositionsX = new ArrayList<>();
        List<Float> exitPositionsY = new ArrayList<>();
        List<Boolean> exitLockStatuses = new ArrayList<>();
        for (Exit exit : this.exits) {
            exitPositionsX.add(exit.getX());
            exitPositionsY.add(exit.getY());
            exitLockStatuses.add(exit.isLocked());
        }

        List<Float> enemyPositionsX = new ArrayList<>();
        List<Float> enemyPositionsY = new ArrayList<>();
        List<Boolean> enemyActiveStatuses = new ArrayList<>();
        for (Enemy enemy : this.enemies) {
            enemyPositionsX.add(enemy.getX());
            enemyPositionsY.add(enemy.getY());
            enemyActiveStatuses.add(enemy.isActive());
        }

        List<Float> trapPositionsX = new ArrayList<>();
        List<Float> trapPositionsY = new ArrayList<>();
        List<Boolean> trapActiveStatuses = new ArrayList<>();
        for (Trap trap : this.traps) {
            trapPositionsX.add(trap.getX());
            trapPositionsY.add(trap.getY());
            trapActiveStatuses.add(trap.isActive());
        }

        List<Float> heartPositionsX = new ArrayList<>();
        List<Float> heartPositionsY = new ArrayList<>();
        List<Boolean> heartCollectedStatuses = new ArrayList<>();
        for (Heart heart : this.hearts) {
            heartPositionsX.add(heart.getX());
            heartPositionsY.add(heart.getY());
            heartCollectedStatuses.add(heart.isCollected());
        }

        List<Float> boostPositionsX = new ArrayList<>();
        List<Float> boostPositionsY = new ArrayList<>();
        List<Boolean> boostCollectedStatuses = new ArrayList<>();
        for (Boost boost : this.boosts) {
            boostPositionsX.add(boost.getX());
            boostPositionsY.add(boost.getY());
            boostCollectedStatuses.add(boost.isCollected());
        }

        List<Float> morphTrapPositionsX = new ArrayList<>();
        List<Float> morphTrapPositionsY = new ArrayList<>();
        List<Boolean> morphTrapActiveStatuses = new ArrayList<>();
        List<Boolean> morphTrapAffectingPlayerStatuses = new ArrayList<>();
        for (MorphTrap trap : this.morphTraps) {
            morphTrapPositionsX.add(trap.getX());
            morphTrapPositionsY.add(trap.getY());
            morphTrapActiveStatuses.add(trap.isActive());
            morphTrapAffectingPlayerStatuses.add(trap.isAffectingPlayer());
        }

        return new GameState(
                this.player.getX(),
                this.player.getY(),
                this.player.getHeartsCollected(),
                collectedKeyPositionsX,
                collectedKeyPositionsY,
                exitPositionsX,
                exitPositionsY,
                exitLockStatuses,
                enemyPositionsX,
                enemyPositionsY,
                enemyActiveStatuses,
                trapPositionsX,
                trapPositionsY,
                trapActiveStatuses,
                heartPositionsX,
                heartPositionsY,
                heartCollectedStatuses,
                boostPositionsX,
                boostPositionsY,
                boostCollectedStatuses,
                morphTrapPositionsX,
                morphTrapPositionsY,
                morphTrapActiveStatuses,
                morphTrapAffectingPlayerStatuses,
                this.getTimePlayed(),
                this.gameMap.getMapFile(),
                this.totalHeartsCollected,
                this.totalEnemiesKilled
        );
    }


    private void applyLoadedState(GameState state) {
        if (!this.gameMap.getLevelPath().equals(state.mapFile)) {
            Gdx.app.log("GameManager", "Saved state map (" + state.mapFile + ") differs from current map (" + this.gameMap.getLevelPath() + "). Reloading map via GameScreen.");
            Gdx.app.error("GameManager", "WARNING: Attempting to apply state from a different map! Current map: " + this.gameMap.getLevelPath() + ", State map: " + state.mapFile + ". Ensure GameScreen handles map reload before calling applyLoadedState.");
        }
        player.setX(state.playerX);
        player.setY(state.playerY);


        try {
            this.totalHeartsCollected = state.totalHeartsCollected;
            this.totalEnemiesKilled = state.totalEnemiesKilled;
        } catch (Exception e) {
            Gdx.app.log("GameManager", "Old save detected: achievement counters not loaded.");
        }

        for (Key key : keys) {
            boolean foundInCollected = false;
            for (int i = 0; i < state.collectedKeyPositionsX.size(); i++) {
                if (Math.abs(key.getX() - state.collectedKeyPositionsX.get(i)) < 0.1f &&
                        Math.abs(key.getY() - state.collectedKeyPositionsY.get(i)) < 0.1f) {
                    key.collect();
                    foundInCollected = true;
                    break;
                }
            }
        }


        for (int i = 0; i < state.exitPositionsX.size(); i++) {
            float x = state.exitPositionsX.get(i);
            float y = state.exitPositionsY.get(i);
            boolean isLocked = state.exitLockStatuses.get(i);
            for (Exit exit : exits) {
                if (Math.abs(exit.getX() - x) < 0.1f && Math.abs(exit.getY() - y) < 0.1f) {
                    if (isLocked) {
                        exit.lock();
                        if (!walls.contains(exit)) {
                            walls.add(exit);
                        }
                    } else {
                        exit.unlock();
                        walls.remove(exit);
                    }
                    break;
                }
            }
        }


        for (int i = 0; i < state.enemyPositionsX.size(); i++) {
            float x = state.enemyPositionsX.get(i);
            float y = state.enemyPositionsY.get(i);
            boolean isActive = state.enemyActiveStatuses.get(i);
            for (Enemy enemy : enemies) {
                if (Math.abs(enemy.getX() - x) < 0.1f && Math.abs(enemy.getY() - y) < 0.1f) {
                    if (isActive) {
                        enemy.activate();
                    } else {
                        enemy.deactivate();
                    }
                    break;
                }
            }
        }


        for (int i = 0; i < state.trapPositionsX.size(); i++) {
            float x = state.trapPositionsX.get(i);
            float y = state.trapPositionsY.get(i);
            boolean isActive = state.trapActiveStatuses.get(i);
            for (Trap trap : traps) {
                if (Math.abs(trap.getX() - x) < 0.1f && Math.abs(trap.getY() - y) < 0.1f) {
                    if (isActive) {
                        trap.activate();
                    } else {
                        trap.deactivate();
                    }
                    break;
                }
            }
        }


        for (int i = 0; i < state.heartPositionsX.size(); i++) {
            float x = state.heartPositionsX.get(i);
            float y = state.heartPositionsY.get(i);
            boolean isCollected = state.heartCollectedStatuses.get(i);
            for (Heart heart : hearts) {
                if (Math.abs(heart.getX() - x) < 0.1f && Math.abs(heart.getY() - y) < 0.1f) {
                    if (isCollected) {
                        heart.collect();
                    }

                    break;
                }
            }
        }


        for (int i = 0; i < state.boostPositionsX.size(); i++) {
            float x = state.boostPositionsX.get(i);
            float y = state.boostPositionsY.get(i);
            boolean isCollected = state.boostCollectedStatuses.get(i);
            for (Boost boost : boosts) {
                if (Math.abs(boost.getX() - x) < 0.1f && Math.abs(boost.getY() - y) < 0.1f) {
                    if (isCollected) {
                        boost.collect();
                    }

                    break;
                }
            }
        }


        for (int i = 0; i < state.morphTrapPositionsX.size(); i++) {
            float x = state.morphTrapPositionsX.get(i);
            float y = state.morphTrapPositionsY.get(i);
            boolean isActive = state.morphTrapActiveStatuses.get(i);
            boolean isAffectingPlayer = state.morphTrapAffectingPlayerStatuses.get(i);
            for (MorphTrap trap : morphTraps) {
                if (Math.abs(trap.getX() - x) < 0.1f && Math.abs(trap.getY() - y) < 0.1f) {
                    if (isActive) {
                        trap.activate();
                    } else {
                        trap.deactivate();
                    }
                    trap.setAffectingPlayer(isAffectingPlayer);
                    break;
                }
            }
        }

        this.timePlayed = state.timePlayed;
        updateTimerDisplay();

        Gdx.app.log("GameManager", "Applied loaded game state from file: " + state.mapFile);
    }


    public void update(float delta) {
        if (canSaveOrLoad) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.F5)) {
                SaveSystem.saveGame(getCurrentGameState());
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.F9)) {
                GameState loadedState = SaveSystem.loadGame();
                if (loadedState != null) {
                    applyLoadedState(loadedState);
                }
            }
        }

        boolean isSprinting = Gdx.input.isKeyPressed(sprintKeyCode);
        player.setSprinting(isSprinting && !win && !lose);

        if (!win && !lose) {
            timePlayed += delta;
            updateTimerDisplay();
        }

        if (!win && !lose) {
            player.update(delta, walls);
            exitArrow.update(player.getX(), player.getY(), exits);
            tryLockEntrance();
            tryUnlockExits();
            tryWin();
            tryLose();
            enemies.forEach(e -> e.update(delta, walls, player));
            traps.forEach(t -> t.update(delta));
            handleObstacleInteractions();
            handleKeyCollection();
            handleHeartCollection();
            handleBoostCollection();
        }
        handleObstacleInteractions();  // 保证生命值更新在失败检测前

        // ✅ 【关键修复】添加日志，确认失败检测
        boolean winResult = tryWin();
        boolean loseResult = tryLose();
        System.out.println("Win check: " + winResult + ", Lose check: " + loseResult);
        player.update(delta, walls);
        exitArrow.update(player.getX(), player.getY(), exits);
        tryLockEntrance();
        tryUnlockExits();

        enemies.forEach(e -> e.update(delta, walls, player));
        traps.forEach(t -> t.update(delta));
        handleKeyCollection();
        handleHeartCollection();
        handleBoostCollection();

        boolean onMorphTrap = false;
        float px = player.getX();
        float py = player.getY();
        for (MorphTrap trap : morphTraps) {
            if (!trap.isActive()) continue;
            if (Math.abs(px - trap.getX()) < 0.5f && Math.abs(py - trap.getY()) < 0.5f) {
                if (!trap.isAffectingPlayer()) {
                    trap.trigger();
                    gameScreen.playSound("trap2");
                }
                onMorphTrap = true;
                break;
            }
        }
        player.setSpeedMultiplier(onMorphTrap ? 0.5f : 1.0f);
    }

    private void gameOver() {
        if (!lose) {
            gameScreen.playSound("losescreen");

            lose = true;
            canSaveOrLoad = false;

            gameScreen.showEndScreen("assets/images/gameOver.png");
        }
    }

    // 在 GameManager 类中添加
    public boolean isInfiniteMode() {
        return game.getIsInfiniteMode();
    }

    public void winGame() {
        if (!win) {
            gameScreen.playSound("winscreen");
            win = true;
            canSaveOrLoad = false;
            int levelScore = calculateFinalScore();

            // ✅ 修复: 确保调用正确的方法
            if (isInfiniteMode()) {
                game.addInfiniteModeScore(levelScore);
                gameScreen.onInfiniteModeLevelComplete();
                return;
            }

            Gdx.app.log("GameManager", "Level completed! Score added: " + levelScore + " (Hearts: " + totalHeartsCollectedThisSession + ", Enemies: " + totalEnemiesKilledThisSession + ")");
            gameScreen.showEndScreen("assets/images/victory.png");
        }
    }

    /**
     * 当 GameMap 重新加载后，GameManager 需要更新其持有的实体列表内容。
     * 使用 clear() 和 addAll() 来修改 final List 的内容，而不是引用。
     */
    public void onMapReloaded(
            List<Exit> exits,
            List<Heart> hearts,
            List<Key> keys,
            List<Trap> traps,
            List<Enemy> enemies,
            List<MorphTrap> morphTraps,
            ExitArrow newExitArrow // Add this parameter
    ) {

        this.exits.clear();
        this.exits.addAll(exits);

        this.hearts.clear();
        this.hearts.addAll(hearts);

        this.keys.clear();
        this.keys.addAll(keys);

        this.traps.clear();
        this.traps.addAll(traps);

        this.enemies.clear();
        this.enemies.addAll(enemies);

        this.morphTraps.clear();
        this.morphTraps.addAll(morphTraps);

        this.exitArrow = newExitArrow;

        Gdx.app.log("GameManager", "Updated entity list contents and exitArrow after map reload.");
    }


    public void resetAfterLevelTransition() {
        this.win = false;
        this.lose = false;
        this.canSaveOrLoad = true;
        this.timePlayed = 0f;

        Gdx.app.log("GameManager", "Reset flags after level transition.");
    }

    private void handleObstacleInteractions() {
        for (Trap trap : traps) {
            if (trap.isActive() && GameHelper.isAtCoordinate(player.getX(), player.getY(), List.of(trap))) {
                player.loseHearts(1);
                trap.deactivate();
                gameScreen.playSound("trap");
                gameScreen.playSound("player");
                return;
            }
        }
        for (Enemy enemy : enemies) {
            if (enemy.isActive() && GameHelper.isAtCoordinate(player.getX(), player.getY(), List.of(enemy))) {
                totalEnemiesKilled++;
                player.loseHearts(1);
                enemy.deactivate();
                gameScreen.playSound("player");
                return;
            }
        }
        for (Enemy enemy : enemies) {
            if (enemy.isActive() && GameHelper.isAtCoordinate(player.getX(), player.getY(), List.of(enemy))) {
                player.loseHearts(1);
                enemy.deactivate();
                gameScreen.playSound("player");
                return;
            }
        }
    }

    public int calculateFinalScore() {
        return totalHeartsCollected * 100 + totalEnemiesKilled * 50;
    }

    public int getTotalHeartsCollected() {
        return totalHeartsCollected;
    }

    public int getTotalEnemiesKilled() {
        return totalEnemiesKilled;
    }

    private void handleKeyCollection() {
        for (Key key : keys) {
            if (!key.isCollected() && GameHelper.isAtCoordinate(player.getX(), player.getY(), List.of(key))) {
                key.collect();
                gameScreen.playSound("key");
                hud.animateKeyCollection();
                return;
            }
        }
    }

    private void handleHeartCollection() {
        for (Heart heart : hearts) {
            if (!heart.isCollected() && GameHelper.isAtCoordinate(player.getX(), player.getY(), List.of(heart))) {
                player.collectHeart();
                heart.collect();
                gameScreen.playSound("heart");
                totalHeartsCollected++;
                return;
            }
        }
    }

    private void handleBoostCollection() {
        for (Boost boost : boosts) {
            if (!boost.isCollected() && GameHelper.isAtCoordinate(player.getX(), player.getY(), List.of(boost))) {
                player.boostWalking();
                boost.collect();
                gameScreen.playSound("boost");
                return;
            }
        }
    }

    private void tryLockEntrance() {
        if (entrance.isUnlocked() && !GameHelper.isAtCoordinate(player.getX(), player.getY(), List.of(entrance))) {
            entrance.lock();
            walls.add(entrance);
        }
    }

    private void tryUnlockExits() {
        for (Key key : keys) {
            if (key.isCollected()) {
                for (Exit exit : exits) {
                    if (exit.isLocked()) {
                        exit.unlock();
                        walls.remove(exit);
                    }
                }
                return;
            }
        }
    }

    public boolean tryWin() {
        if (win) return true;

        // ✅ 修复：正确检查玩家是否到达了出口
        for (Exit exit : exits) {
            if (Math.abs(player.getX() - exit.getX()) < 0.1f &&
                    Math.abs(player.getY() - exit.getY()) < 0.1f) {
                win = true;
                return true;
            }
        }

        return false;
    }
    public boolean isLose() {
        return lose;
    }

    public boolean isWin() {
        return win;
    }
    // 在 GameManager.java 中添加 resetAfterLevelTransition 方法

    public boolean tryLose() {
        // ✅ 添加关键日志，确认失败检测
        System.out.println("Lose check: Hearts = " + player.getHeartsCollected() +
                " (Max Hearts: " + player.getMaxHearts() + ")");

        if (lose) return true;

        // ✅ 确保检查的是当前生命值
        if (player.getHeartsCollected() <= 0) {
            lose = true;
            System.out.println("Lose triggered! Hearts = " + player.getHeartsCollected());
            return true;
        }
        return false;
    }

    private String formatTime(float seconds) {
        int minutes = (int) seconds / 60;
        int secs = (int) seconds % 60;
        return String.format("Time Played: %d:%02d min", minutes, secs);
    }

    private void updateTimerDisplay() {
        if (timer != null) {
            timer.setText(formatTime(timePlayed));
        }
    }

    private void setupTimer() {
        BitmapFont font = new BitmapFont();
        font.getData().setScale(2.0f);
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = font;
        labelStyle.fontColor = Color.WHITE;
        timer = new Label("", labelStyle);
        timer.setPosition(20, Gdx.graphics.getHeight() - 60);
        hud.getStage().addActor(timer);
    }


    public GameCharacter getPlayer() {
        return player;
    }

    public float getTimePlayed() {
        return timePlayed;
    }

    public List<Key> getKeys() {
        return keys;
    }

    public List<Exit> getExits() {
        return exits;
    }

    public List<Enemy> getEnemies() {
        return enemies;
    }

    public List<Trap> getTraps() {
        return traps;
    }

    public List<Heart> getHearts() {
        return hearts;
    }

    public List<Boost> getBoosts() {
        return boosts;
    }

    public List<MorphTrap> getMorphTraps() {
        return morphTraps;
    }


    public void requestSaveGameState() {
        if (!canSaveOrLoad) {
            Gdx.app.log("GameManager", "Save/Load disabled (e.g., during win/lose).");
            return;
        }

        SaveSystem.saveGame(getCurrentGameState());
    }

    public void requestLoadGameState(GameState loadedState) {
        if (!canSaveOrLoad) {
            Gdx.app.log("GameManager", "Save/Load disabled (e.g., during win/lose).");
            return;
        }
    }
    public void updateExitArrowReference(ExitArrow newExitArrow) {
        this.exitArrow = newExitArrow;
    }
}