package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import de.tum.cit.fop.maze.world.*;

import java.util.List;

public class GameManager {

    // =========================
    // 🔴 SCORING
    // =========================
    private final ScoreManager scoreManager;

    private int totalHeartsCollected = 0;
    private int totalEnemiesKilled = 0;

    // =========================
    // 🌳 SKILL TREE
    // =========================
    private final SkillTree skillTree;

    // 🔴 session-only score
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
    private boolean win = false;
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

        // 🔴 ScoreManager init
        this.scoreManager = new ScoreManager();
        this.scoreManager.loadHighScore();
        // 🌳 SkillTree init
        this.skillTree = new SkillTree();

        setupTimer();
    }

    // =========================
    // UPDATE
    // =========================
    public void update(float delta) {

        // 🔴 time-based score only while active
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

        // Morph trap slow
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

    // =========================
    // GAME OVER / WIN
    // =========================
    private void gameOver() {
        if (!lose) {
            lose = true;
            canSaveOrLoad = false;

            // 🔴 finalize score on lose
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

            if (game.getIsInfiniteMode()) {
                game.addInfiniteModeScore(levelScore);

                // 🔴 FIX: reset session score for next level
                totalHeartsCollectedThisSession = 0;
                totalEnemiesKilledThisSession = 0;

                gameScreen.onInfiniteModeLevelComplete();
                return;
            }

            gameScreen.playSound("winscreen");
            gameScreen.showEndScreen("assets/images/victory.png");
        }
    }

    // =========================
    // SCORING
    // =========================
    public int calculateFinalScore() {
        int baseScore = totalHeartsCollected * 100 + totalEnemiesKilled * 50;
        baseScore *= skillTree.getScoreMultiplier();
        return baseScore;
    }

    // =========================
    // GAME LOGIC
    // =========================
    public boolean tryWin() {
        if (win) return true;
        for (Exit exit : exits) {
            if (Math.abs(player.getX() - exit.getX()) < 0.1f &&
                    Math.abs(player.getY() - exit.getY()) < 0.1f) {
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
                totalEnemiesKilledThisSession++; // 🔴 FIX

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
                totalHeartsCollectedThisSession++; // 🔴 FIX

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

    // =========================
    // UI
    // =========================
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

    // =========================
    // ACCESSORS
    // =========================
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
}