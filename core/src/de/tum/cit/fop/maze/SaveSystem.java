package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;

import java.io.*;

/**
 * Handles saving and loading of the game state using Java Serialization.
 */
public class SaveSystem {

    private static final String SAVE_FILE_PATH = "game_save.dat";

    /**
     * Saves the provided GameState object to a file.
     *
     * @param gameState The current state of the game to be saved.
     */
    public static void saveGame(GameState gameState) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SAVE_FILE_PATH))) {
            oos.writeObject(gameState);
            System.out.println("Game saved successfully to " + SAVE_FILE_PATH);
        } catch (IOException e) {
            System.err.println("Error saving game: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public static void saveTotalScore(int score) {
        Gdx.app.getPreferences("MazeRunnerPrefs").putInteger("total_score", score).flush();
    }
    public static int loadTotalScore() {
        return Gdx.app.getPreferences("MazeRunnerPrefs").getInteger("total_score", 0);
    }
    /**
     * Loads a GameState object from the save file.
     *
     * @return The loaded GameState object, or null if loading fails or no save exists.
     */
    public static GameState loadGame() {
        File saveFile = new File(SAVE_FILE_PATH);
        if (!saveFile.exists()) {
            System.out.println("No save file found at " + SAVE_FILE_PATH + ".");
            return null;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(SAVE_FILE_PATH))) {
            GameState loadedState = (GameState) ois.readObject(); // Deserialize the object
            System.out.println("Game loaded successfully from " + SAVE_FILE_PATH);
            return loadedState;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading game: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Deletes the saved game file, effectively clearing the saved state.
     */
    public static void clearSave() {
        File saveFile = new File(SAVE_FILE_PATH);
        if (saveFile.exists()) {
            boolean deleted = saveFile.delete();
            if (deleted) {
                System.out.println("Save file '" + SAVE_FILE_PATH + "' deleted successfully.");
            } else {
                System.err.println("Failed to delete save file '" + SAVE_FILE_PATH + "'.");
            }
        } else {
            System.out.println("No save file to delete at '" + SAVE_FILE_PATH + "'.");
        }
    }
}