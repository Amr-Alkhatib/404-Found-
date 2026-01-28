package de.tum.cit.fop.maze.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Wall extends MapElement {

    private static Texture texture;
    private static TextureRegion wallRegion;

    public Wall(float x, float y) {
        super(x, y);
        super.traversable = false;
        loadTextures();
    }

    private void loadTextures() {
        if (texture == null) {
            // Hier den neuen Namen deiner Datei eintragen!
            texture = new Texture(Gdx.files.internal("assets/wall.png"));

            // Wir nehmen einfach das ganze Bild als Textur
            wallRegion = new TextureRegion(texture);
        }
    }

    public void render(SpriteBatch batch, GameMap maze) {
        batch.draw(wallRegion, x * 32, y * 32, 32, 32);
    }


    @Override
    public void render(SpriteBatch batch) {
        batch.draw(wallRegion, x * 32, y * 32, 32, 32);
    }

    @Override
    public void dispose() {
        if (texture != null) {
            texture.dispose();
            texture = null;
        }
    }
}