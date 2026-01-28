# DancingLineMaze

## Overview

DancingLineMaze is a 2D top-down maze game developed in Java using the LibGDX framework.

The game takes place in an ancient, enchanted library filled with restless spirits and dangerous obstacles.
The player is trapped inside the maze and must find a way out before losing all collected knowledge.

To escape, the player must locate the **Key of Knowledge**, unlock the exit, and reach it successfully.


## Authors

- Huaijing Hou
- Amr Alkhatib
- Samuel Tobias Deißinger
- Chengcheng Deng


## Project Structure

- A UML class diagram for this project is included in the project folder (`UML_Diagram.png`).
- The source code is organized into logical packages following object-oriented design principles.


## Application

### Screens

The game consists of the following main screens:

- **MenuScreen** – Main menu of the game
- **GameScreen** – Core gameplay screen
- **PauseMenuScreen** – Pause menu during gameplay
- **SelectMapScreen** – Level selection screen
- **AcknowledgementScreen** – Credits and acknowledgements

The application starts in the **MenuScreen**, where the player can:

- start the game from the first level using **"Enter Library"**;
- select a specific level via **"Select Section"**;
- view credits in the **Acknowledgements** screen;
- quit the game using **"Quit"**.

During gameplay, pressing **Esc** pauses the game and opens the pause menu, where the player can resume, select another level, or return to the main menu.


## Gameplay

DancingLineMaze takes place in a maze consisting of traversable paths surrounded by walls.
Multiple maps are available, each offering different layouts and increasing difficulty.


## Game Mechanics

- Maze-based navigation with walls and paths
- Player movement with optional sprinting
- Enemies that patrol and chase the player when nearby
- Static and dynamic traps that damage the player
- Key-based exit unlocking system
- Heads-Up Display (HUD) showing:
  - collected books (lives)
  - key status
  - elapsed play time
  - exit direction indicator


## Controls

- Movement: **Arrow keys** or **W / A / S / D**
- Sprint: **Shift**
- Pause: **Esc**


## Obstacles and Items

### Obstacles
- **Fires of Knowledge** – Static traps that cause the player to lose a book
- **Library Hallway Spirits** – Enemies that chase the player when nearby
- **Library Toll Spirits** – Enemies guarding exits

The player starts with three **Books** and can carry up to five.
Losing all books results in game over.

### Items
- **Key of Knowledge** – Required to unlock the exit
- **Speed Manuals** – Temporary speed boost items


## Getting Started

### Dependencies

- Java JDK 11 or newer
- Gradle


### Running the Game

1. Clone the repository
2. Open the project in IntelliJ IDEA
3. Run the desktop launcher configuration


### Notes

- If the game does not start, ensure the correct desktop module is selected.
- On macOS, the VM option `-XstartOnFirstThread` may be required.


## Acknowledgements

- Graphics from **OpenGameArt.org**
- Music from **Pixabay** and **Free Music Archive**
- Built with **LibGDX**


## Enjoy

We hope you enjoy playing **DancingLineMaze**!