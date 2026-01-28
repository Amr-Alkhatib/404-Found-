package de.tum.cit.fop.maze.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

/**
 * A class representing a trap with an always animated state.
 */
public class Trap extends Obstacle {

    /**
     * Constructor for class {@code Trap}.
     *
     * @param x The x position of the trap on the map.
     * @param y The y position of the trap on the map.
     */
    public Trap(float x, float y) {
        super(x, y);
        loadTexturesAndAnimations();
        stateTime = 0f;
    }
    public void activate() {
        setActive(true); // 使用已有的 setActive 方法来激活陷阱
    }

    /**
     * The texture used for rendering.
     */
    private static Texture texture;

    /**
     * The animation of the trap.
     */
    private static Animation<TextureRegion> trapAnimation;

    /**
     * A property signifying the trap's animation state.
     */
    private float stateTime;


    /**
     * Updates the trap's animation state.
     *
     * @param delta The time elapsed since the last frame.
     */
    public void update(float delta) {
        if (!isActive()) return;
        stateTime += delta;
    }


    /**
     * Loads the texture and animation frames for the trap.
     */
    private void loadTexturesAndAnimations() {
        if (texture == null) {
            texture = new Texture(Gdx.files.internal("assets/objects.png"));

            Array<TextureRegion> frames = new Array<>();
            for (int i = 4; i < 10; i++) {
                frames.add(new TextureRegion(texture, 16 * i, 16 * 3, 16, 16));
            }

            trapAnimation = new Animation<>(0.2f, frames, Animation.PlayMode.LOOP);
        }
    }

    /**
     * Renders the trap using the animation.
     *
     * @param batch The {@code SpriteBatch} used for rendering.
     */
    public void render(SpriteBatch batch) {
        if (!isActive()) return;
        TextureRegion currentFrame = trapAnimation.getKeyFrame(stateTime, true);
        batch.draw(currentFrame, x * 32, y * 32, 32, 32);
    }



    /**
     * Disposes of the texture to free resources.
     */
    @Override
    public void dispose() {
        if (texture != null) {
            texture.dispose();
            texture = null;
            trapAnimation = null;
        }
    }
}
