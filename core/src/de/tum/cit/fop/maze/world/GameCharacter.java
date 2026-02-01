package de.tum.cit.fop.maze.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import java.util.List;

/** * A class representing the player's character with a certain position on the game map. */
public final class GameCharacter extends MapElement {

    private int upKey;
    private int downKey;
    private int leftKey;
    private int rightKey;

    private int maxHearts = Constants.characterMaxBooks;

    private int heartsCollected = Constants.characterInitialBooks;

    public GameCharacter(float initialX, float initialY, int upKey, int downKey, int leftKey, int rightKey) {
        super(initialX, initialY);
        this.upKey = upKey;
        this.downKey = downKey;
        this.leftKey = leftKey;
        this.rightKey = rightKey;


        loadTexturesAndAnimations();
        super.currentMovementDirection = Direction.RIGHT;
        defaultFrame = idleRight;
    }


    public GameCharacter(int upKey, int downKey, int leftKey, int rightKey) {
        this(1.0f, 1.0f, upKey, downKey, leftKey, rightKey);
    }

    private boolean sprinting = false;
    private boolean boosted = false;
    private float painTime = Constants.characterPainGainTolerance;
    private float gainTime = Constants.characterPainGainTolerance;
    private float boostedTime = Constants.characterBoostLast;
    private float speedMultiplier = 1.0f;
    private float skillMultiplier = 1.0f;
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


    protected float calculateSpeed(float delta) {
        float baseSpeed = Constants.characterBaseWalkingSpeed;
        if (boosted) {
            baseSpeed = Constants.characterBoostedWalking;
        }
        if (sprinting) {
            baseSpeed *= Constants.characterRunningBoost;
        }

        baseSpeed *= speedMultiplier;
        baseSpeed *= skillMultiplier;

        return baseSpeed * delta;
    }

    public void setSprinting(boolean sprinting) {
        this.sprinting = sprinting;
    }

    public void setSpeedMultiplier(float multiplier) {
        this.speedMultiplier = multiplier;
    }

    public void applySpeedBoost() {
        this.skillMultiplier = 1.2f;
    }

    public void boostWalking() {
        boosted = true;
        boostedTime = 0f;
    }

    public void loseHearts(int amount) {
        heartsCollected -= amount;
        painTime = 0.0f;
        if (heartsCollected < 0) heartsCollected = 0;
    }

    public void collectHeart() {
        gainTime = 0.0f;
        if (heartsCollected < Constants.characterMaxBooks) {
            heartsCollected++;
        }
    }

    public boolean hasHearts() {
        return heartsCollected > 0;
    }

    public int getHeartsCollected() {
        return heartsCollected;
    }

    // ✅ 修复：添加 getMaxHearts 方法
    public int getMaxHearts() {
        return maxHearts;
    }

    public boolean isBoosted() {
        return boosted;
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

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
        if (this.heartsCollected < 0)
            this.heartsCollected = 0;
        if (this.heartsCollected > Constants.characterMaxBooks)
            this.heartsCollected = Constants.characterMaxBooks;
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
        float scale = 1.5f;

        float drawWidth = 16 * scale;
        float drawHeight = 32 * scale;

        float drawX = (x * 32) + (32 - drawWidth) / 2;

        float yOffset = -2f;
        float drawY = (y * 32) + yOffset;

        batch.draw(frame, drawX, drawY, drawWidth, drawHeight);
        batch.setColor(1, 1, 1, 1);
    }

}