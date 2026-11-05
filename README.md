# 404 Found: Maze Runner

A 2D top-down maze game in Java and libGDX. It was our team project for **Fundamentals of Programming (FOP)** at TUM, winter semester 2025/26.

Find the key, avoid enemies and traps, and reach the exit, across five hand-built levels and an endless mode with procedurally generated mazes.

<!-- Add a screenshot or GIF: put it at docs/gameplay.png and remove these comment markers.
![Gameplay](docs/gameplay.png)
-->

## Features

- **Five levels** loaded from `.properties` map files, plus an **endless mode** with procedurally generated maps.
- **Procedural generation with a connectivity check.** Random walls are placed, a breadth-first search checks that the exit can be reached from the start, and if it can't, a path is carved through and the check runs again.
- **Enemies with simple AI.** Enemies chase the player once within sight range and idle otherwise. Ghost enemies guard a fixed point.
- **Traps and morph traps, hearts, speed boosts and keys,** with a HUD and an arrow that points to the exit.
- **Progression.** Score, a skill tree where score buys upgrades (speed, extra hearts…), achievements and a leaderboard.
- **Save and load,** settings, a pause menu and a developer console (`godmode`, `addkeys`, `setlives`, `killall`…) for testing.

## My part

<!-- Fill this in before publishing, e.g.:
I built the procedural endless mode and its connectivity check, the skill tree and the save system.
-->

## Run it

Requires JDK 17.

```bash
./gradlew desktop:run        # Windows: gradlew.bat desktop:run
```

## Tech

Java 17 · libGDX · Gradle

## Team

Built by a team of students for TUM FOP 2025/26. The exercise template was provided by the course.
