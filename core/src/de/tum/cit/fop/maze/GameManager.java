package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import de.tum.cit.fop.maze.world.*;
import java.util.List;

public class GameManager {
    private final ScoreManager scoreManager;
    private final AchievementManager achievementManager; // 🟢 NEU

    private int totalHeartsCollected = 0;
    private int totalEnemiesKilled = 0;

    private final SkillTree skillTree;

    private int totalHeartsCollectedThisSession = 0;
    private int totalEnemiesKilledThisSession = 0;

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
    public boolean win = false;
    private float timePlayed;
    private Label timer;

    private boolean canSaveOrLoad = true;

    public GameManager(
            GameMap gameMap,
            MazeRunnerGame game,
            GameScreen gameScreen,
            GameCharacter player,
            Hud hud,
            List<Wall> walls,
            List<Enemy> enemies,
            List<Key> keys,
            List<Trap> traps,
            Entrance entrance,
            List<Exit> exits,
            List<Heart> hearts,
            List<Boost> boosts,
            List<MorphTrap> morphTraps,
            ExitArrow exitArrow,
            int sprintKeyCode
    ) {
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

        this.scoreManager = new ScoreManager();
        this.scoreManager.loadHighScore();

        this.achievementManager = new AchievementManager();

        this.skillTree = new SkillTree();

        if (skillTree.hasHeart()) {
            player.setHeartsCollected(player.getHeartsCollected() + 1);
        }
        if (skillTree.hasSpeed()) {
            player.applySpeedBoost();
        }

        setupTimer();
    }

    public void update(float delta) {

        if (!win && !lose) {
            scoreManager.updateTimeScore(delta);
        }

        boolean isSprinting = Gdx.input.isKeyPressed(sprintKeyCode);
        player.setSprinting(isSprinting && !win && !lose);

        if (!win && !lose) {
            timePlayed += delta;
            updateTimerDisplay();

            player.update(delta, walls);
            exitArrow.update(player.getX(), player.getY(), exits);

            tryLockEntrance();
            tryUnlockExits();

            enemies.forEach(e -> e.update(delta, walls, player));
            traps.forEach(t -> t.update(delta));

            handleObstacleInteractions();
            handleKeyCollection();
            handleHeartCollection();
            handleBoostCollection();

            tryWin();
            tryLose();
        }

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
            lose = true;
            canSaveOrLoad = false;

            scoreManager.finalizeScore();

            gameScreen.playSound("losescreen");
            gameScreen.showEndScreen("assets/images/gameOver.png");
        }
    }

    public void winGame() {
        if (!win) {
            win = true;
            canSaveOrLoad = false;

            int levelScore = calculateFinalScore();

            scoreManager.addScore(levelScore);
            scoreManager.finalizeScore();

            int currentTotalScore = SaveSystem.loadTotalScore();
            int newTotalScore = currentTotalScore + levelScore;
            SaveSystem.saveTotalScore(newTotalScore);
            Gdx.app.log("GameManager", "Punkte erhalten: " + levelScore + ". Neuer Kontostand: " + newTotalScore);

            String lvlPath = (gameMap.getLevelPath() != null) ? gameMap.getLevelPath() : "unknown";
            achievementManager.onLevelFinished(timePlayed, lvlPath);

            achievementManager.onHeartsCollected(totalHeartsCollected);

            achievementManager.onScoreUpdated(newTotalScore);


            if (game.getIsInfiniteMode()) {

                game.addInfiniteModeScore(levelScore);

                totalHeartsCollectedThisSession = 0;
                totalEnemiesKilledThisSession = 0;

                game.continueInfiniteMode();

                return;
            }

            gameScreen.playSound("winscreen");
            gameScreen.showEndScreen("assets/images/victory.png");
        }
    }

    public int calculateFinalScore() {

        int collectionScore = totalHeartsCollected * 25;
        int maxTime = 60;
        int timeBonus = (int) Math.max(0, (maxTime - timePlayed) * 5);

        int survivorBonus = player.getHeartsCollected() * 50;
        int baseScore = collectionScore + timeBonus + survivorBonus;
        baseScore *= skillTree.getScoreMultiplier();

        Gdx.app.log("Score", "Collected: " + collectionScore +
                " | TimeBonus: " + timeBonus +
                " | Survivor: " + survivorBonus +
                " | TOTAL: " + baseScore);

        return baseScore;
    }

    public boolean tryWin() {
        if (win) return true;
        for (Exit exit : exits) {
            if (Math.abs(player.getX() - exit.getX()) < 0.2f &&
                    Math.abs(player.getY() - exit.getY()) < 0.2) {
                winGame();
                return true;
            }
        }

        return false;
    }

    public boolean tryLose() {
        if (lose) return true;
        if (player.getHeartsCollected() <= 0) {
            gameOver();
            return true;
        }
        return false;
    }

    private void handleObstacleInteractions() {
        for (Trap trap : traps) {
            if (trap.isActive() && GameHelper.isAtCoordinate(player.getX(), player.getY(), List.of(trap))) {
                player.loseHearts(1);
                trap.deactivate();
                gameScreen.playSound("trap");
                return;
            }
        }

        for (Enemy enemy : enemies) {
            if (enemy.isActive() && GameHelper.isAtCoordinate(player.getX(), player.getY(), List.of(enemy))) {
                enemy.deactivate();
                player.loseHearts(1);

                totalEnemiesKilled++;
                totalEnemiesKilledThisSession++;

                gameScreen.playSound("player");
                return;
            }
        }
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
                heart.collect();
                player.collectHeart();

                totalHeartsCollected++;
                totalHeartsCollectedThisSession++;

                gameScreen.playSound("heart");
                return;
            }
        }
    }

    private void handleBoostCollection() {
        for (Boost boost : boosts) {
            if (!boost.isCollected() && GameHelper.isAtCoordinate(player.getX(), player.getY(), List.of(boost))) {
                boost.collect();
                player.boostWalking();
                gameScreen.playSound("boost");
                return;
            }
        }
    }

    private void tryLockEntrance() {
        if (entrance.isUnlocked() &&
                !GameHelper.isAtCoordinate(player.getX(), player.getY(), List.of(entrance))) {
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
        Label.LabelStyle style = new Label.LabelStyle(font, Color.WHITE);
        timer = new Label("", style);
        timer.setPosition(20, Gdx.graphics.getHeight() - 60);
        hud.getStage().addActor(timer);
    }

    public ScoreManager getScoreManager() { return scoreManager; }
    public GameCharacter getPlayer() { return player; }
    public float getTimePlayed() { return timePlayed; }
    public List<Key> getKeys() { return keys; }
    public List<Exit> getExits() { return exits; }
    public List<Enemy> getEnemies() { return enemies; }
    public List<Trap> getTraps() { return traps; }
    public List<Heart> getHearts() { return hearts; }
    public List<Boost> getBoosts() { return boosts; }
    public List<MorphTrap> getMorphTraps() { return morphTraps; }

    public boolean isWin() {
        return win;
    }

    public boolean isLose() {
        return lose;
    }

    public void onMapReloaded(List<Exit> exits, List<Heart> hearts, List<Key> keys,
                              List<Trap> traps, List<Enemy> enemies,
                              List<MorphTrap> morphTraps, ExitArrow exitArrow) {
        this.exitArrow = exitArrow;
    }

    public void updateExitArrowReference(ExitArrow exitArrow) {
        this.exitArrow = exitArrow;
    }

    public void resetAfterLevelTransition() {
        win = false;
        lose = false;
        timePlayed = 0f;
    }

    private String getItemDataString(List<? extends MapElement> list) {
        StringBuilder sb = new StringBuilder();
        for (MapElement elem : list) {
            if (sb.length() > 0) sb.append(";");

            boolean isGone = false;
            if (elem instanceof Key) isGone = ((Key) elem).isCollected();
            else if (elem instanceof Heart) isGone = ((Heart) elem).isCollected();
            else if (elem instanceof Boost) isGone = ((Boost) elem).isCollected();

            sb.append(elem.getX()).append(",")
                    .append(elem.getY()).append(",")
                    .append(isGone ? "1" : "0");
        }
        return sb.toString();
    }

    private String getTrapDataStringDetailed(List<? extends MapElement> list) {
        StringBuilder sb = new StringBuilder();
        for (MapElement elem : list) {
            if (sb.length() > 0) sb.append(";");

            boolean isActive = false;
            if (elem instanceof Trap) isActive = ((Trap) elem).isActive();
            else if (elem instanceof MorphTrap) isActive = ((MorphTrap) elem).isActive();

            sb.append(elem.getX()).append(",")
                    .append(elem.getY()).append(",")
                    .append(isActive ? "1" : "0");
        }
        return sb.toString();
    }

    private String getEnemyDataString() {
        StringBuilder sb = new StringBuilder();
        for (Enemy e : enemies) {
            if (sb.length() > 0) sb.append(";");
            sb.append(e.getX()).append(",")
                    .append(e.getY()).append(",")
                    .append(e.isActive() ? "1" : "0");
        }
        return sb.toString();
    }

    public void requestSaveGameState() {
        if (player == null) return;
        String currentLevel = gameMap.getLevelPath();
        if (currentLevel == null) currentLevel = "maps/level-1.properties";

        SaveSystem.saveGame(
                player.getHeartsCollected(), player.getX(), player.getY(), currentLevel,
                this.timePlayed,
                getItemDataString(keys),
                getEnemyDataString(),
                getItemDataString(hearts),
                getItemDataString(boosts),
                getTrapDataStringDetailed(traps),
                getTrapDataStringDetailed(morphTraps),
                this.totalHeartsCollected,
                this.totalEnemiesKilled
        );
    }

    public void requestLoadGameState() {
        if (!SaveSystem.hasSaveGame()) return;
        if (player == null) return;

        var prefs = SaveSystem.getGameSave();

        player.setHeartsCollected(prefs.getInteger("hearts", 3));
        float x = prefs.getFloat("playerX", 1f);
        float y = prefs.getFloat("playerY", 1f);
        if (x < 1 && y < 1) { x = 1; y = 1; }
        player.setPosition(x, y);
        this.timePlayed = prefs.getFloat("timePlayed", 0f);
        updateTimerDisplay();

        this.totalHeartsCollected = prefs.getInteger("savedTotalHearts", 0);
        this.totalEnemiesKilled = prefs.getInteger("savedTotalEnemies", 0);

        restoreMapElements(keys, prefs.getString("keyData", ""));
        restoreMapElements(enemies, prefs.getString("enemyData", ""));
        restoreMapElements(hearts, prefs.getString("heartData", ""));
        restoreMapElements(boosts, prefs.getString("boostData", ""));
        restoreMapElements(traps, prefs.getString("trapData", ""));
        restoreMapElements(morphTraps, prefs.getString("morphTrapData", ""));

        tryUnlockExits();
        hud.update();
        Gdx.app.log("GameManager", "Spielstand geladen: Alle Positionen & Scores korrigiert!");
    }

    private void restoreMapElements(List<? extends MapElement> list, String data) {
        if (data == null || data.isEmpty()) return;

        String[] items = data.split(";");
        for (int i = 0; i < items.length && i < list.size(); i++) {
            try {
                String[] stats = items[i].split(",");
                float ex = Float.parseFloat(stats[0]);
                float ey = Float.parseFloat(stats[1]);
                boolean statusBool = stats[2].equals("1");

                MapElement elem = list.get(i);
                elem.setPosition(ex, ey);

                if (elem instanceof Enemy) {
                    if (!statusBool) ((Enemy)elem).deactivate();
                }
                else if (elem instanceof Key) {
                    if (statusBool) ((Key)elem).collect();
                }
                else if (elem instanceof Heart) {
                    if (statusBool) ((Heart)elem).collect();
                }
                else if (elem instanceof Boost) {
                    if (statusBool) ((Boost)elem).collect();
                }
                else if (elem instanceof Trap) {
                    if (!statusBool) ((Trap)elem).deactivate();
                }

            } catch (Exception e) {
                Gdx.app.error("GameManager", "Fehler bei Item Restore Index " + i);
            }
        }
    }
}