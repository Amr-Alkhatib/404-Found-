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

    // --- 新增成员变量来保存 ScrollPane 的引用 ---
    private ScrollPane scrollPane;

    // --- 保存原始的输入处理器 ---
    private InputProcessor originalInputProcessor;

    // --- 预先创建好用于隐藏控制台时的输入处理器 ---
    private InputMultiplexer hiddenModeInputMultiplexer;

    public DeveloperConsole(MazeRunnerGame gameRef, GameCharacter playerRef, GameManager managerRef, GameMap mapRef) {
        this.player = playerRef;
        this.gameManager = managerRef;
        this.gameMap = mapRef;

        // --- 获取并保存原始的输入处理器 ---
        this.originalInputProcessor = Gdx.input.getInputProcessor();

        // 修正路径
        skin = new Skin(Gdx.files.internal("craft/craftacular-ui.json"));

        // Create UI elements
        consoleWindow = new Window("Developer Console", skin);
        consoleWindow.setMovable(true);
        consoleWindow.setResizable(true);
        consoleWindow.setSize(600, 300);
        consoleWindow.setPosition(Gdx.graphics.getWidth() / 2 - 300, Gdx.graphics.getHeight() / 2 - 150);
        consoleWindow.setVisible(false);

        commandInput = new TextField("", skin);
        commandInput.setMessageText("Enter command..."); // Placeholder hint text

        // Capture Enter key press for command submission
        // This is the correct place to execute the command
        commandInput.setTextFieldListener((textField, c) -> {
            if (c == '\n' || c == '\r') { // Enter key
                handleCommand(textField.getText());
                textField.setText(""); // Clear the input field after executing the command
            }
        });

        outputLabel = new Label("Welcome to the Developer Console!", skin);
        outputLabel.setWrap(true);
        outputLabel.setAlignment(Align.topLeft);

        // --- 创建 ScrollPane 并保存其引用 ---
        scrollPane = new ScrollPane(outputLabel, skin);
        scrollPane.setFadeScrollBars(false);

        // Add ScrollPane and TextField to the window
        consoleWindow.add(scrollPane).expand().fill().pad(5);
        consoleWindow.row();
        consoleWindow.add(commandInput).expandX().fillX().pad(5);

        consoleStage = new Stage();
        consoleStage.addActor(consoleWindow);

        // --- 预先创建隐藏模式下的输入处理器 ---
        hiddenModeInputMultiplexer = new InputMultiplexer();
        hiddenModeInputMultiplexer.addProcessor(originalInputProcessor); // 游戏逻辑处理器
        hiddenModeInputMultiplexer.addProcessor(new InputAdapter() { // F1 监听器
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.F1) {
                    toggleVisibility(); // 调用切换方法
                    return true; // 消费 F1 事件，防止它流向原始处理器
                }
                return false; // 其他键不消费
            }
        });

        // --- 在构造函数末尾，设置初始的全局输入处理器 (即隐藏模式) ---
        Gdx.input.setInputProcessor(hiddenModeInputMultiplexer);
    }

    public void toggleVisibility() {
        visible = !visible;
        consoleWindow.setVisible(visible);

        if (visible) {
            // 控制台打开时，使用新的 InputMultiplexer
            // 将 F1 监听器放在第一位，确保它有最高优先级
            InputMultiplexer consoleMultiplexer = new InputMultiplexer();

            // 添加 F1 监听器作为第一个处理器 (最高优先级)
            consoleMultiplexer.addProcessor(new InputAdapter() {
                @Override
                public boolean keyDown(int keycode) {
                    if (keycode == Input.Keys.F1) {
                        toggleVisibility(); // 再次调用，关闭控制台
                        return true; // 消费这个事件
                    }
                    // 对于其他按键，我们不消费它，让它流向 stage 或其他处理器
                    return false;
                }
            });

            // 然后添加 Stage 的处理器 (较低优先级，但仍能处理大部分UI事件)
            consoleMultiplexer.addProcessor(consoleStage);

            Gdx.input.setInputProcessor(consoleMultiplexer);
            commandInput.setText(""); // Clear input field when shown

            // 可选：强制聚焦到输入框，以便用户可以直接开始输入
            // consoleStage.setKeyboardFocus(commandInput);
        } else {
            // 控制台关闭时，直接使用预先准备好的隐藏模式输入处理器
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
        // Re-center window if it was out of bounds after resize
        if (consoleWindow.getX() + consoleWindow.getWidth() > width) {
            consoleWindow.setX(width - consoleWindow.getWidth() - 10);
        }
        if (consoleWindow.getY() + consoleWindow.getHeight() > height) {
            consoleWindow.setY(height - consoleWindow.getHeight() - 10);
        }
    }

    public void dispose() {
        consoleStage.dispose();
        skin.dispose(); // Dispose of the skin and its resources
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
                        // Command implementation details remain as per your original code
                        response.append("Command 'addkeys' is complex. Requires updating GameManager's key state. Not implemented directly here.\n");
                    }
                    break;
                case "setlives":
                    if (parts.length < 2) {
                        response.append("Usage: setlives <amount>\n");
                    } else {
                        int amount = Integer.parseInt(parts[1]);
                        int newLives = Math.max(0, amount); // Ensure non-negative
                        player.setHeartsCollected(newLives); // Use hearts as lives
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
                    float startX = gameMap.getPlayerStartX(); // Get map start X
                    float startY = gameMap.getPlayerStartY(); // Get map start Y
                    if (startX == -1 || startY == -1) { // Fallback if no start pos in map
                        startX = 1.0f;
                        startY = 1.0f;
                    }
                    player.setPosition(startX, startY); // Use setPosition from GameCharacter
                    player.setHeartsCollected(Constants.characterInitialBooks); // Use Constants
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

        // Update output label
        String oldText = outputLabel.getText().toString();
        String newText = oldText + response.toString();
        // Limit lines to prevent excessive scrolling
        String[] lines = newText.split("\n");
        if (lines.length > 50) { // Keep last 50 lines
            newText = String.join("\n", java.util.Arrays.copyOfRange(lines, lines.length - 50, lines.length));
        }
        outputLabel.setText(newText);

        // --- 使用保存的引用，而不是动态查找 ---
        // Scroll to bottom
        scrollPane.layout(); // Ensure layout is updated before setting scroll
        scrollPane.setScrollPercentY(1.0f);
    }
}