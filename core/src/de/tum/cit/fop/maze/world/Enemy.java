package de.tum.cit.fop.maze.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.List;

/**
 * A class representing an enemy with a certain position on the game map that moves around.
 */
public class Enemy extends Obstacle {

    /**
     * Constructor for class {@code Enemy}.
     *
     * @param x The x position of the enemy on the map.
     * @param y The y position of the enemy on the map.
     */
    public Enemy(float x, float y) {
        super(x, y);
        chooseRandomDirection();
        loadTexturesAndAnimations();
        stateTime = 0f;
    }

    public void activate() {
        this.active = true; // 假设 'active' 字段用于表示敌人的活动状态
    }
    /**
     * The texture from which frames and animations are loaded.
     */
    private static Texture enemyTexture;

    /**
     * The frame to be rendered.
     */
    protected TextureRegion currentFrame;

    /**
     * Animation for walking up.
     */
    private static Animation<TextureRegion> walkUpAnimation;

    /**
     * Animation for walking down.
     */
    private static Animation<TextureRegion> walkDownAnimation;

    /**
     * Animation for walking left.
     */
    private static Animation<TextureRegion> walkLeftAnimation;

    /**
     * Animation for walking right.
     */
    private static Animation<TextureRegion> walkRightAnimation;

    /**
     * A property signifying the enemy's animation state.
     */
    private float stateTime;


    /**
     * The speed with which the enemy moves.
     */
    protected float walkingSpeed = Constants.enemyWalkingSpeed;

    /**
     * A property signifying from what distance the enemy detects the player.
     */
    protected int sightRange = Constants.enemySightRange;

    /**
     * A property used to determine whether the sound of the chase behavior beginning has been played.
     */
    private boolean proximitySoundPlayed = false;


    /**
     * Chooses a movement direction for the enemy at random.
     */
    private void chooseRandomDirection() {
        double random = Math.random();
        if (random < 0.25) {
            currentMovementDirection = Direction.UP;
        } else if (random < 0.5) {
            currentMovementDirection = Direction.DOWN;
        } else if (random < 0.75) {
            currentMovementDirection = Direction.LEFT;
        } else {
            currentMovementDirection = Direction.RIGHT;
        }
    }

    /**
     * Calculates the speed with which the enemy should move.
     *
     * @param delta The time elapsed since the last frame.
     * @return The speed with which the enemy moves.
     */
    @Override
    protected float calculateSpeed(float delta) {
        return delta * walkingSpeed;
    }

    /**
     * The enemy chases the target if within sight range; otherwise, it idles.
     *
     * @param delta  The time elapsed since the last frame.
     * @param walls  The list of walls that should be avoided.
     * @param player The player to be chased.
     */
    protected void tryChase(float delta, List<Wall> walls, GameCharacter player) {
        if (!active) return;

        if (Math.abs(x - player.getX()) > sightRange
                || Math.abs(y - player.getY()) > sightRange) {
            idle(delta, walls);
            return;
        }

        if (!proximitySoundPlayed) {
            com.badlogic.gdx.audio.Sound sound = Gdx.audio.newSound(Gdx.files.internal("assets/sounds/enemy.mp3"));

            float sfxVolume = Gdx.app.getPreferences("MazeRunnerPrefs").getFloat("sfx_volume", 0.5f);

            sound.play(sfxVolume);

            proximitySoundPlayed = true;
        }

        if (player.getX() > this.x) {
            moveRight(delta, walls);
        } else if (player.getX() < this.x) {
            moveLeft(delta, walls);
        }
        if (player.getY() > this.y) {
            moveUp(delta, walls);
        } else if (player.getY() < this.y) {
            moveDown(delta, walls);
        }
    }

    /**
     * The enemy moves randomly in the idle state.
     *
     * @param delta The time elapsed since the last frame.
     * @param walls The list of walls that should be avoided.
     */
    protected void idle(float delta, List<Wall> walls) {
        proximitySoundPlayed = false;
        boolean moved = switch (currentMovementDirection) {
            case UP -> moveUp(delta, walls);
            case DOWN -> moveDown(delta, walls);
            case LEFT -> moveLeft(delta, walls);
            case RIGHT -> moveRight(delta, walls);
        };

        if (moved) return;

        chooseRandomDirection();
    }


    /**
     * Updates the enemy's behavior and animation frame.
     *
     * @param delta  The time elapsed since the last frame.
     * @param walls  The list of walls that should be avoided.
     * @param player The player to be chased.
     */
    public void update(float delta, List<Wall> walls, GameCharacter player) {
        if (!super.active) return; // Skip updates if the enemy is inactive
        stateTime += delta;
        tryChase(delta, walls, player);
        updateAnimation(); // This will update currentFrame based on direction and stateTime
    }

    /**
     * Updates the animation frame based on the enemy's movement direction.
     */
    private void updateAnimation() {
        // Ensure currentFrame is always assigned, even if animation is temporarily null
        this.currentFrame = switch (currentMovementDirection) {
            case UP -> walkUpAnimation != null ? walkUpAnimation.getKeyFrame(stateTime) : this.currentFrame; // Keep previous or fallback
            case DOWN -> walkDownAnimation != null ? walkDownAnimation.getKeyFrame(stateTime) : this.currentFrame;
            case LEFT -> walkLeftAnimation != null ? walkLeftAnimation.getKeyFrame(stateTime) : this.currentFrame;
            case RIGHT -> walkRightAnimation != null ? walkRightAnimation.getKeyFrame(stateTime) : this.currentFrame;
        };
        // Fallback: If all animations were null somehow, currentFrame should already have the initial frame set in the constructor.
    }


    /**
     * Loads textures and animations for the enemy.
     */
    private static void loadTexturesAndAnimations() {
        if (enemyTexture == null) {
            // ✅ 从 TextureManager 获取，而不是自己 new
            enemyTexture = TextureManager.mobsTexture;

            int animationFrames = 3;
            int frameWidth = enemyTexture.getWidth() / animationFrames;
            int frameHeight = enemyTexture.getHeight() / 4;


            int characterRow = 0;

            TextureRegion[] walkDownFrames = new TextureRegion[animationFrames];
            TextureRegion[] walkLeftFrames = new TextureRegion[animationFrames];
            TextureRegion[] walkRightFrames = new TextureRegion[animationFrames];
            TextureRegion[] walkUpFrames = new TextureRegion[animationFrames];

            for (int i = 0; i < animationFrames; i++) {
                walkDownFrames[i] = new TextureRegion(enemyTexture, i * frameWidth, characterRow * frameHeight, frameWidth, frameHeight);
                walkLeftFrames[i] = new TextureRegion(enemyTexture, i * frameWidth, (characterRow + 1) * frameHeight, frameWidth, frameHeight);
                walkRightFrames[i] = new TextureRegion(enemyTexture, i * frameWidth, (characterRow + 2) * frameHeight, frameWidth, frameHeight);
                walkUpFrames[i] = new TextureRegion(enemyTexture, i * frameWidth, (characterRow + 3) * frameHeight, frameWidth, frameHeight);
            }

            walkDownAnimation = new Animation<>(0.2f, walkDownFrames);
            walkLeftAnimation = new Animation<>(0.2f, walkLeftFrames);
            walkRightAnimation = new Animation<>(0.2f, walkRightFrames);
            walkUpAnimation = new Animation<>(0.2f, walkUpFrames);

            walkDownAnimation.setPlayMode(Animation.PlayMode.LOOP);
            walkLeftAnimation.setPlayMode(Animation.PlayMode.LOOP);
            walkRightAnimation.setPlayMode(Animation.PlayMode.LOOP);
            walkUpAnimation.setPlayMode(Animation.PlayMode.LOOP);
        }
    }

    /**
     * Renders the enemy.
     *
     * @param batch The {@code SpriteBatch} used for rendering.
     */
    public void render(SpriteBatch batch) {
        if (super.active && currentFrame != null) { // Add null check for safety, although it shouldn't be null now
            batch.draw(currentFrame, x * 32, y * 32, 32, 32);
        }
    }


    /**
     * Disposes of enemy textures.
     */

}
