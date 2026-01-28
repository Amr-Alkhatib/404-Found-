package de.tum.cit.fop.maze.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

public class MorphTrap extends Obstacle {

    private static Texture texture;
    private static Animation<TextureRegion> morphAnimation;
    private float stateTime;
    private boolean affectingPlayer = false;

    // 👇 自己管理 active 状态
    private boolean active = true;

    public MorphTrap(float x, float y) {
        super(x, y);
        // 不再调用 setActive(true)，因为父类没有
        loadTexturesAndAnimations();
        this.stateTime = 0f;
    }

    // 👇 提供自己的 isActive()
    public boolean isActive() {
        return active;
    }

    // 如果需要，也可以提供 setActive
    public void setActive(boolean active) {
        this.active = active;
    }

    private void loadTexturesAndAnimations() {
        if (texture == null) {
            texture = new Texture(Gdx.files.internal("objects/morph_trap.png"));

            Array<TextureRegion> frames = new Array<>();
            final int frameWidth = 32;
            final int frameHeight = 32;
            final int cols = 3;
            final int rows = 2;

            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < cols; col++) {
                    int u = col * frameWidth;
                    int v = row * frameHeight;
                    frames.add(new TextureRegion(texture, u, v, frameWidth, frameHeight));
                }
            }

            morphAnimation = new Animation<>(0.15f, frames, Animation.PlayMode.LOOP);
        }
    }
    public void activate() {
        setActive(true);
    }
    @Override
    public void update(float delta) {
        if (!isActive()) return;
        stateTime += delta;
    }

    @Override
    public void render(SpriteBatch batch) {
        if (!isActive()) return;
        if (morphAnimation == null) return;
        TextureRegion currentFrame = morphAnimation.getKeyFrame(stateTime, true);
        batch.draw(currentFrame, getX() * 32, getY() * 32, 32, 32);
    }

    public static void disposeResources() {
        if (texture != null) {
            texture.dispose();
            texture = null;
            morphAnimation = null;
        }
    }

    @Override
    public void dispose() {}

    public boolean isAffectingPlayer() {
        return affectingPlayer;
    }

    public void setAffectingPlayer(boolean affectingPlayer) {
        this.affectingPlayer = affectingPlayer;
    }

    public void trigger() {
        if (!isActive()) return;
        setAffectingPlayer(true);
    }
}