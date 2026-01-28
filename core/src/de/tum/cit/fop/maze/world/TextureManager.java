package de.tum.cit.fop.maze.world;

import com.badlogic.gdx.graphics.Texture;

/**
 * A class to help manage texture sources.
 */

public class TextureManager {

    /**
     * Basic tiles source.
     */
    public static final Texture basictilesTexture = new Texture("assets/basictiles.png");

    /**
     * Character textures source.
     */
    public static final Texture characterTexture = new Texture("assets/character.png");

    /**
     * Mobs source.
     */
    public static final Texture mobsTexture = new Texture("assets/mobs.png");

    /**
     * Objects source.
     */
    public static final Texture objectsTexture = new Texture("assets/objects.png");

    /**
     * Things source.
     */
    public static final Texture thingsTexture = new Texture("assets/things.png");


    /**
     * Disposes of the sources.
     */
    public static void dispose() {
        basictilesTexture.dispose();
        characterTexture.dispose();
        mobsTexture.dispose();
        objectsTexture.dispose();
        thingsTexture.dispose();
    }
}
