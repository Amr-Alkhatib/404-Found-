// File: core/src/de/tum/cit/fop/maze/world/GameCharacter.java
package de.tum.cit.fop.maze.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

import java.util.List;

/**
 * A class representing the player's character with a certain position on the game map.
 */
public final class GameCharacter extends MapElement {

    // === 控制键 ===
    private int upKey;
    private int downKey;
    private int leftKey;
    private int rightKey;

    // --- 修改构造函数：接受明确的初始坐标 ---
    public GameCharacter(float initialX, float initialY, int upKey, int downKey, int leftKey, int rightKey) {
        super(initialX, initialY); // 设置初始坐标
        this.upKey = upKey;
        this.downKey = downKey;
        this.leftKey = leftKey;
        this.rightKey = rightKey;
        loadTexturesAndAnimations();
        super.currentMovementDirection = Direction.RIGHT;
        defaultFrame = idleRight;
    }

    // --- 保留旧构造函数（可能需要调整调用方）或创建一个使用默认出生点的构造函数 ---
    // Option 1: 创建一个使用默认出生点 (1, 1) 的构造函数
    public GameCharacter(int upKey, int downKey, int leftKey, int rightKey) {
        this(1.0f, 1.0f, upKey, downKey, leftKey, rightKey); // 默认出生点 (1, 1)
    }


    // === 状态字段 ===
    private boolean sprinting = false;
    private boolean boosted = false;
    private float painTime = Constants.characterPainGainTolerance;
    private float gainTime = Constants.characterPainGainTolerance;
    private float boostedTime = Constants.characterBoostLast;
    private int heartsCollected = Constants.characterInitialBooks;  // 改为 hearts

    // === 新增：速度乘数（用于 MorphTrap 减速）===
    private float speedMultiplier = 1.0f;

    // === 动画字段 ===
    private Animation<TextureRegion> walkUpAnimation;
    private Animation<TextureRegion> walkDownAnimation;
    private Animation<TextureRegion> walkLeftAnimation;
    private Animation<TextureRegion> walkRightAnimation;
    private Animation<TextureRegion> damageUpAnimation;
    private Animation<TextureRegion> damageDownAnimation;
    private Animation<TextureRegion> damageLeftAnimation;
    private Animation<TextureRegion> damageRightAnimation;
    private TextureRegion layInPain;
    private TextureRegion idleUp;
    private TextureRegion idleDown;
    private TextureRegion idleLeft;
    private TextureRegion idleRight;
    private Animation<TextureRegion> currentAnimation;
    private TextureRegion defaultFrame;
    private boolean isAnimating = false;
    private float animationTime = 0f;

    // === 核心：计算移动速度（支持 boost + sprint + multiplier）===
    @Override
    protected float calculateSpeed(float delta) {
        float baseSpeed = Constants.characterBaseWalkingSpeed;

        if (boosted) {
            baseSpeed = Constants.characterBoostedWalking;
        }

        if (sprinting) {
            baseSpeed *= Constants.characterRunningBoost;
        }

        // 应用外部乘数（如 MorphTrap 的 0.5）
        baseSpeed *= speedMultiplier;

        return baseSpeed * delta;
    }

    // === 公共接口 ===
    public void setSprinting(boolean sprinting) {
        this.sprinting = sprinting;
    }

    public void setSpeedMultiplier(float multiplier) {
        this.speedMultiplier = multiplier;
    }

    public void boostWalking() {
        boosted = true;
        boostedTime = 0f;
    }

    public void loseHearts(int amount) {  // 方法名改为 loseHearts
        heartsCollected -= amount;
        painTime = 0.0f;
        if (heartsCollected < 0) heartsCollected = 0;
    }

    public void collectHeart() {  // 方法名改为 collectHeart
        gainTime = 0.0f;
        if (heartsCollected < Constants.characterMaxBooks) {  // 注意这里的常量可能也需要调整
            heartsCollected++;
        }
    }

    public boolean hasHearts() {  // 方法名改为 hasHearts
        return heartsCollected > 0;
    }

    public int getHeartsCollected() {  // 方法名改为 getHeartsCollected
        return heartsCollected;
    }

    public boolean isBoosted() {
        return boosted;
    }

    // --- 新增：用于加载存档时设置位置 ---
    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    // === 更新逻辑 ===
    public void update(float delta, List<Wall> walls) {
        animationTime += delta;
        painTime += delta;
        gainTime += delta;
        boostedTime += delta;

        if (boosted && boostedTime >= Constants.characterBoostLast) {
            boosted = false;
        }

        boolean moved = false;

        if (Gdx.input.isKeyPressed(upKey) || Gdx.input.isKeyPressed(Input.Keys.UP)) {
            moveUp(delta, walls);
            if (painTime < Constants.characterPainGainTolerance)
                startAnimation(damageUpAnimation);
            else
                startAnimation(walkUpAnimation);
            moved = true;
        }
        if (Gdx.input.isKeyPressed(downKey) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            moveDown(delta, walls);
            if (painTime < Constants.characterPainGainTolerance)
                startAnimation(damageDownAnimation);
            else
                startAnimation(walkDownAnimation);
            moved = true;
        }
        if (Gdx.input.isKeyPressed(leftKey) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            moveLeft(delta, walls);
            if (painTime < Constants.characterPainGainTolerance)
                startAnimation(damageLeftAnimation);
            else
                startAnimation(walkLeftAnimation);
            moved = true;
        }
        if (Gdx.input.isKeyPressed(rightKey) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            moveRight(delta, walls);
            if (painTime < Constants.characterPainGainTolerance)
                startAnimation(damageRightAnimation);
            else
                startAnimation(walkRightAnimation);
            moved = true;
        }

        if (!moved) {
            stopAnimation();
            if (painTime < Constants.characterPainGainTolerance) {
                defaultFrame = layInPain;
            }
        }
    }

    // === 渲染与动画 ===
    private void loadTexturesAndAnimations() {
        Texture characterSheet = new Texture(Gdx.files.internal("assets/character.png"));

        walkDownAnimation = createAnimation(characterSheet, 0, 0);
        walkRightAnimation = createAnimation(characterSheet, 0, 32);
        walkUpAnimation = createAnimation(characterSheet, 0, 64);
        walkLeftAnimation = createAnimation(characterSheet, 0, 96);

        damageDownAnimation = createAnimation(characterSheet, 144, 0);
        damageRightAnimation = createAnimation(characterSheet, 144, 32);
        damageUpAnimation = createAnimation(characterSheet, 144, 64);
        damageLeftAnimation = createAnimation(characterSheet, 144, 96);

        layInPain = new TextureRegion(characterSheet, 216, 0, 16, 32);

        idleDown = new TextureRegion(characterSheet, 0, 0, 16, 32);
        idleRight = new TextureRegion(characterSheet, 0, 32, 16, 32);
        idleUp = new TextureRegion(characterSheet, 0, 64, 16, 32);
        idleLeft = new TextureRegion(characterSheet, 0, 96, 16, 32);
    }

    private Animation<TextureRegion> createAnimation(Texture texture, int startX, int startY) {
        int frameWidth = 16;
        int frameHeight = 32;
        int frameCount = 4;
        float frameDuration = 0.1f;

        Array<TextureRegion> frames = new Array<>();
        for (int i = 0; i < frameCount; i++) {
            frames.add(new TextureRegion(texture, startX + (i * frameWidth), startY, frameWidth, frameHeight));
        }
        return new Animation<>(frameDuration, frames);
    }

    public void startAnimation(Animation<TextureRegion> animation) {
        if (currentAnimation != animation) {
            currentAnimation = animation;
            animationTime = 0f;
        }
        isAnimating = true;
    }

    public void stopAnimation() {
        isAnimating = false;
        switch (currentMovementDirection) {
            case UP -> defaultFrame = idleUp;
            case DOWN -> defaultFrame = idleDown;
            case LEFT -> defaultFrame = idleLeft;
            case RIGHT -> defaultFrame = idleRight;
        }
    }

    public void setHeartsCollected(int count) {
        this.heartsCollected = count;
        if (this.heartsCollected < 0) this.heartsCollected = 0; // 确保不小于0
        if (this.heartsCollected > Constants.characterMaxBooks) this.heartsCollected = Constants.characterMaxBooks; // 确保不超过最大值
    }

    @Override
    public void render(SpriteBatch batch) {
        TextureRegion frame = isAnimating ? currentAnimation.getKeyFrame(animationTime, true) : defaultFrame;

        if (painTime < Constants.characterPainGainTolerance) {
            batch.setColor(1f, 0.3f, 0.3f, 1);
        }
        if (gainTime < Constants.characterPainGainTolerance) {
            batch.setColor(0.6f, 1f, 0.8f, 1);
        }
        batch.draw(frame, x * 32, y * 32, 32, 32);
        batch.setColor(1, 1, 1, 1);
    }
}