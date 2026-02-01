package de.tum.cit.fop.maze;

import com.badlogic.gdx.*;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import de.tum.cit.fop.maze.world.Constants;
import de.tum.cit.fop.maze.world.GameCharacter;
import de.tum.cit.fop.maze.world.GameMap;

public class DeveloperConsole {
    private Stage consoleStage;
    private Window consoleWindow;
    private TextField commandInput;
    private Label outputLabel;
    private Skin skin;
    private boolean visible = false; // 初始为 false，控制台启动时隐藏

    private GameCharacter player;
    private GameManager gameManager;
    private GameMap gameMap;

    private ScrollPane scrollPane;

    private InputProcessor originalInputProcessor;

    private InputMultiplexer hiddenModeInputMultiplexer;

    public DeveloperConsole(MazeRunnerGame gameRef, GameCharacter playerRef, GameManager managerRef, GameMap mapRef) {
        this.player = playerRef;
        this.gameManager = managerRef;
        this.gameMap = mapRef;

        this.originalInputProcessor = Gdx.input.getInputProcessor();

        skin = new Skin(Gdx.files.internal("craft/craftacular-ui.json"));

        consoleWindow = new Window("Developer Console", skin);
        consoleWindow.setMovable(true);
        consoleWindow.setResizable(true);
        consoleWindow.setSize(600, 300);
        consoleWindow.setPosition(Gdx.graphics.getWidth() / 2 - 300, Gdx.graphics.getHeight() / 2 - 150);
        consoleWindow.setVisible(false);

        commandInput = new TextField("", skin);
        commandInput.setMessageText("Enter command...");

        commandInput.setTextFieldListener((textField, c) -> {
            if (c == '\n' || c == '\r') {
                handleCommand(textField.getText());
                textField.setText("");
            }
        });

        outputLabel = new Label("Welcome to the Developer Console!", skin);
        outputLabel.setWrap(true);
        outputLabel.setAlignment(Align.topLeft);

        scrollPane = new ScrollPane(outputLabel, skin);
        scrollPane.setFadeScrollBars(false);

        consoleWindow.add(scrollPane).expand().fill().pad(5);
        consoleWindow.row();
        consoleWindow.add(commandInput).expandX().fillX().pad(5);

        consoleStage = new Stage();
        consoleStage.addActor(consoleWindow);

        hiddenModeInputMultiplexer = new InputMultiplexer();
        hiddenModeInputMultiplexer.addProcessor(originalInputProcessor);
        hiddenModeInputMultiplexer.addProcessor(new InputAdapter() { // F1 监听器
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.F1) {
                    toggleVisibility();
                    return true;
                }
                return false;
            }
        });

        Gdx.input.setInputProcessor(hiddenModeInputMultiplexer);
    }

    public void toggleVisibility() {
        visible = !visible;
        consoleWindow.setVisible(visible);

        if (visible) {
            InputMultiplexer consoleMultiplexer = new InputMultiplexer();


            consoleMultiplexer.addProcessor(new InputAdapter() {
                @Override
                public boolean keyDown(int keycode) {
                    if (keycode == Input.Keys.F1) {
                        toggleVisibility();
                        return true;
                    }
                    return false;
                }
            });

            consoleMultiplexer.addProcessor(consoleStage);

            Gdx.input.setInputProcessor(consoleMultiplexer);
            commandInput.setText("");

        } else {

            Gdx.input.setInputProcessor(hiddenModeInputMultiplexer);
        }
    }

    public boolean isVisible() {
        return visible;
    }

    public void act(float delta) {
        if (visible) {
            consoleStage.act(delta);
        }
    }

    public void draw() {
        if (visible) {
            consoleStage.draw();
        }
    }

    public void resize(int width, int height) {
        consoleStage.getViewport().update(width, height, true);
        if (consoleWindow.getX() + consoleWindow.getWidth() > width) {
            consoleWindow.setX(width - consoleWindow.getWidth() - 10);
        }
        if (consoleWindow.getY() + consoleWindow.getHeight() > height) {
            consoleWindow.setY(height - consoleWindow.getHeight() - 10);
        }
    }

    public void dispose() {
        consoleStage.dispose();
        skin.dispose();
    }

    private void handleCommand(String command) {
        if (command == null || command.trim().isEmpty()) return;

        String[] parts = command.trim().toLowerCase().split("\\s+");
        String cmd = parts[0];

        StringBuilder response = new StringBuilder("> ").append(command).append("\n");

        try {
            switch (cmd) {
                case "help":
                    response.append("Available commands: help, addhearts <amount>, addkeys <amount>, setlives <amount>, godmode, killall, win, reset, quit\n");
                    break;
                case "addhearts":
                    if (parts.length < 2) {
                        response.append("Usage: addhearts <amount>\n");
                    } else {
                        int amount = Integer.parseInt(parts[1]);
                        int currentHearts = player.getHeartsCollected(); // Use actual method
                        int newHearts = Math.min(currentHearts + amount, Constants.characterMaxBooks); // Use imported Constants
                        player.setHeartsCollected(newHearts); // Use actual method
                        response.append("Added ").append(amount).append(" hearts. Total: ").append(newHearts).append("\n");
                    }
                    break;
                case "addkeys":
                    if (parts.length < 2) {
                        response.append("Usage: addkeys <amount>\n");
                    } else {
                        int amount = Integer.parseInt(parts[1]);
                        response.append("Command 'addkeys' is complex. Requires updating GameManager's key state. Not implemented directly here.\n");
                    }
                    break;
                case "setlives":
                    if (parts.length < 2) {
                        response.append("Usage: setlives <amount>\n");
                    } else {
                        int amount = Integer.parseInt(parts[1]);
                        int newLives = Math.max(0, amount);
                        player.setHeartsCollected(newLives);
                        response.append("Set lives (hearts) to ").append(newLives).append(".\n");
                    }
                    break;
                case "godmode":
                    response.append("Command 'godmode' requires modifying GameManager logic (e.g., making player immune). Not implemented directly here.\n");
                    break;
                case "killall":
                    int killedCount = 0;
                    for (var enemy : gameManager.getEnemies()) {
                        if (enemy.isActive()) {
                            enemy.deactivate();
                            killedCount++;
                        }
                    }
                    response.append("Deactivated ").append(killedCount).append(" active enemies.\n");
                    break;
                case "win":
                    response.append("Unlocked all exits. Move player to an exit to win.\n");
                    break;
                case "reset":
                    float startX = gameMap.getPlayerStartX();
                    float startY = gameMap.getPlayerStartY();
                    if (startX == -1 || startY == -1) {
                        startX = 1.0f;
                        startY = 1.0f;
                    }
                    player.setPosition(startX, startY);
                    player.setHeartsCollected(Constants.characterInitialBooks);
                    response.append("Reset player position to start and hearts to initial value.\n");
                    break;
                case "quit":
                    Gdx.app.exit();
                    break;
                default:
                    response.append("Unknown command: ").append(cmd).append("\n");
            }
        } catch (NumberFormatException e) {
            response.append("Error: Invalid number format for argument.\n");
        } catch (Exception e) {
            response.append("Error executing command: ").append(e.getMessage()).append("\n");
        }

        String oldText = outputLabel.getText().toString();
        String newText = oldText + response.toString();

        String[] lines = newText.split("\n");
        if (lines.length > 50) {
            newText = String.join("\n", java.util.Arrays.copyOfRange(lines, lines.length - 50, lines.length));
        }
        outputLabel.setText(newText);

        scrollPane.layout();
        scrollPane.setScrollPercentY(1.0f);
    }
}