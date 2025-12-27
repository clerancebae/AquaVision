# Palik — A Small Java Swing Fish Mission Game

A lightweight Java Swing game that presents short "missions" with phased enemy patterns. Palik includes a simple local progress database, sound effects, and several graphics assets. It is intended as a small desktop game or learning project for Java GUI and game logic.

> Note: This README was created from a code scan. The repository contains additional source and asset files — if anything here doesn't match what you see locally, inspect the `src/` folder and the project root on GitHub.

---

## Table of contents
- [What the project does](#what-the-project-does)
- [Why it’s useful](#why-its-useful)
- [Project structure](#project-structure)
- [Requirements](#requirements)
- [Get started (quick start)](#get-started-quick-start)
  - [Run in an IDE (recommended)](#run-in-an-ide-recommended)
  - [Command line (javac/java)](#command-line-javacjava)
- [Configuration and data](#configuration-and-data)
- [Controls & gameplay notes](#controls--gameplay-notes)
- [Where to get help](#where-to-get-help)
- [Maintainers & contributing](#maintainers--contributing)
- [Acknowledgements](#acknowledgements)

## What the project does
Palik is a desktop game written using Java Swing. Players navigate a fish (player) through phased enemy patterns and missions. The project implements:
- Mission/phase system and a game loop
- Local SQLite-based progress storage (`progress.db`)
- Sound via javax.sound APIs and included WAV asset(s)
- Graphics assets (PNG/JPEG) stored in the repository

## Why it’s useful
- Small, self-contained example of a Java Swing game and game loop
- Demonstrates simple local persistence using SQLite via JDBC
- Useful for learning GUI programming, event handling, simple game patterns, and resource handling in Java
- Easy to extend (add levels, patterns, or a build system)

## Project structure (high level)
- `src/` — Java source files (UI panels, Game logic, DatabaseManager, SoundManager, etc.)
- Root image/audio assets (PNG, WAV) used by the game
- `progress.db` — SQLite DB file used for storing mission progress (committed for example/seed data)
- `.gitignore`, project metadata files

Key source files to review:
- `src/Main.java` — application entry / window setup
- `src/Game.java` — main game loop and phase system
- `src/StartPanel.java`, `src/MissionPanel.java`, `src/SettingPanel.java` — UI panels
- `src/DatabaseManager.java` — SQLite / JDBC helpers
- `src/SoundManager.java` — audio initialization and playback

(You can open `src/` to see all classes and implementation details.)

## Requirements
- Java Development Kit (JDK) — 11+ recommended
- SQLite JDBC driver (e.g., org.xerial:sqlite-jdbc) on the classpath when running the compiled app
  - The project uses `jdbc:sqlite:progress.db` in `DatabaseManager.java`, so a JDBC driver is required at runtime.

No build files (Maven/Gradle) are included in the repository root, so the instructions below use either an IDE or the plain `javac` tool.

## Get started (quick start)

### Run in an IDE (recommended)
1. Open your IDE (IntelliJ IDEA, Eclipse).
2. Import the repository or open the project folder.
3. Ensure the project SDK is set to JDK 11+.
4. Add the SQLite JDBC jar to the project's classpath (download and add as a library).
   - Example jar: `sqlite-jdbc-<version>.jar` from Maven Central.
5. Mark `src/` as a sources root (if needed) so resources are available on the classpath.
6. Run `src/Main.java` from the IDE run configuration.
   - Note: `src/Main.java` contains the application startup code — if your IDE cannot detect an entrypoint (method signature), check `src/Main.java` and run using an appropriate entry method or edit to add a standard `public static void main(String[] args)` wrapper if necessary.

### Command line (javac/java)
1. Install JDK 11+.
2. Download the SQLite JDBC jar and put it in a `lib/` directory.
   - Example: `lib/sqlite-jdbc.jar`
3. Compile all sources:
   - Unix/macOS:
     - mkdir out
     - javac -d out -cp "lib/*" src/*.java
   - Windows (PowerShell/CMD):
     - mkdir out
     - javac -d out -cp "lib/*" src\*.java
4. Run the app:
   - Unix/macOS:
     - java -cp "out:lib/*" Main
   - Windows:
     - java -cp "out;lib/*" Main
Notes:
- Ensure your working directory contains `progress.db` (the repo includes a `progress.db` sample). The app expects `jdbc:sqlite:progress.db`.
- If resources (images/sounds) are not found at runtime, verify `src/` is on the classpath or adjust resource loading (IDE usually handles this).

## Configuration and data
- Database: `progress.db` stores mission progress and user settings (`user_settings` and `mission_progress` tables). `DatabaseManager.initialize()` creates the required tables if missing.
- User settings: `user_settings` table stores lazy-eye and color settings.
- Assets: images and audio are included at repository root and `src/appLogo.png`. Confirm these are available on the runtime classpath.

## Controls & gameplay notes
- Press ESC to open the pause menu (implemented in `Game.java`).
- The UI contains on-screen buttons (e.g., Back).
- Keyboard controls are handled via the Player class — inspect `src/Player.java` (or the player implementation) for specific keys and behaviors.

## Where to get help
- Open an issue: https://github.com/clerancebae/palik/issues
- Browse source in `src/` for implementation details and comments.
- For JDBC/SQLite questions, refer to:
  - SQLite JDBC: https://github.com/xerial/sqlite-jdbc
  - Java Sound (javax.sound.sampled) docs: https://docs.oracle.com/javase/8/docs/technotes/guides/sound/

## Maintainers & contributing
- Maintainer: repository owner `clerancebae`
- Contributing: We follow standard GitHub contribution flow. If you want to contribute:
  1. Fork the repository.
  2. Create a branch for your feature/fix: `git checkout -b feat/my-feature`
  3. Open a pull request with a clear description of changes.
  4. Report bugs via Issues and reference them in PRs.

Please see CONTRIBUTING guidelines (create `CONTRIBUTING.md` at the repo root if you want project-specific rules) and the LICENSE file for licensing terms.

## Acknowledgements
- Uses Java Swing and Java Sound APIs.
- SQLite (via JDBC) is used for local persistence.
- Asset files included in the repository were provided by the project author.

---

If you want, I can:
- Add a minimal `pom.xml` or `build.gradle` so the project is buildable with Maven/Gradle.
- Create a small CONTRIBUTING.md template and a basic LICENSE file (choose a license) to make contribution and distribution clearer.
