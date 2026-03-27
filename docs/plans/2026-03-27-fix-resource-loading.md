# Fix Resource Loading for jpackage Builds

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Fix all resource loading so the app works correctly when packaged with jpackage as a native installer.

**Architecture:** Replace all file-system relative paths with classpath-based resource loading (`getClass().getResource()`). Include resources in JAR at build time. Handle SQLite DB path using app-relative directory resolution.

**Tech Stack:** Java 21, Swing, SQLite (sqlite-jdbc), GitHub Actions, jpackage

---

## Problem Summary

All resources (images, audio, DB) are loaded via relative file paths like `new ImageIcon("resources/background.png")`. This only works when CWD = project root (IDE). After jpackage, CWD is unpredictable, so all resources fail to load silently, causing white screens.

Additionally, `safeLoadImage()` in MissionPanel has a double-prefix bug, sqlite-jdbc is not included in the JAR, and the workflow only builds for Windows.

## Resource Loading Points to Fix

| File | Line | Current (broken) | Target (fixed) |
|------|------|-------------------|-----------------|
| StartPanel.java | 16 | `new ImageIcon("resources/background.png")` | `getClass().getResource("/background.png")` |
| MissionPanel.java | 47,50,61,101 | `safeLoadImage("resources/X.png")` | `safeLoadImage("/X.png")` |
| MissionPanel.java | 78 | `getResource("resources/" + fileName)` | `getResource(fileName)` — remove double prefix |
| MissionPanel.java | 82 | `new ImageIcon(fileName)` fallback | Remove file-system fallback |
| MissionPanel.java | 269 | `"jdbc:sqlite:progress.db"` | Use app-dir-relative path |
| SettingPanel.java | 21 | `new ImageIcon("resources/close.png")` | `getClass().getResource("/close.png")` |
| SettingPanel.java | 47 | `new ImageIcon("resources/level_active.png")` | `getClass().getResource("/level_active.png")` |
| SoundManager.java | 16 | `new File("resources/Child_Game_Bg_Music.wav")` | `getResource("/Child_Game_Bg_Music.wav")` |
| DatabaseManager.java | 7 | `"jdbc:sqlite:progress.db"` | Use app-dir-relative path |

---

### Task 1: Fix MissionPanel resource loading

**Files:**
- Modify: `src/MissionPanel.java`

**Step 1: Fix `safeLoadImage` — remove double prefix, remove file-system fallback, add null check**

```java
private Image safeLoadImage(String resourcePath) {
    try {
        URL url = getClass().getResource(resourcePath);
        if (url != null) {
            return new ImageIcon(url).getImage();
        }
        System.err.println("Resource not found: " + resourcePath);
        return null;
    } catch (Exception e) {
        System.err.println("Error loading resource: " + resourcePath + " - " + e.getMessage());
        return null;
    }
}
```

**Step 2: Fix all callers — change `"resources/X.png"` to `"/X.png"`**

```java
backgroundImage = safeLoadImage("/MissionBackground.png");
Image imgActive = safeLoadImage("/level_active.png");
Image imgLocked = safeLoadImage("/level_locked.png");
Image img = safeLoadImage("/return.png");
```

**Step 3: Commit**

```bash
git add src/MissionPanel.java
git commit -m "fix: MissionPanel resource loading — use classpath instead of file paths"
```

---

### Task 2: Fix StartPanel resource loading

**Files:**
- Modify: `src/StartPanel.java`

**Step 1: Replace file-system ImageIcon with classpath loading**

Change line 16-18 from:
```java
ImageIcon backgroundImg = new ImageIcon("resources/background.png");
```
To:
```java
URL bgUrl = getClass().getResource("/background.png");
ImageIcon backgroundImg = (bgUrl != null) ? new ImageIcon(bgUrl) : new ImageIcon();
```

Add import `java.net.URL` if missing.

**Step 2: Commit**

```bash
git add src/StartPanel.java
git commit -m "fix: StartPanel background — use classpath resource loading"
```

---

### Task 3: Fix SettingPanel resource loading

**Files:**
- Modify: `src/SettingPanel.java`

**Step 1: Fix close button icon (line 21)**

```java
URL closeUrl = getClass().getResource("/close.png");
ImageIcon icon = (closeUrl != null) ? new ImageIcon(closeUrl) : new ImageIcon();
```

**Step 2: Fix slider icon (line 47)**

```java
URL sliderUrl = getClass().getResource("/level_active.png");
ImageIcon sliderIcon = (sliderUrl != null) ? new ImageIcon(sliderUrl) : new ImageIcon();
```

Add import `java.net.URL`.

**Step 3: Commit**

```bash
git add src/SettingPanel.java
git commit -m "fix: SettingPanel icons — use classpath resource loading"
```

---

### Task 4: Fix SoundManager resource loading

**Files:**
- Modify: `src/SoundManager.java`

**Step 1: Change from File to classpath URL**

```java
public static void init() {
    try {
        URL soundUrl = SoundManager.class.getResource("/Child_Game_Bg_Music.wav");
        if (soundUrl == null) {
            System.err.println("Sound file not found in classpath");
            return;
        }
        AudioInputStream audio = AudioSystem.getAudioInputStream(soundUrl);
        music = AudioSystem.getClip();
        music.open(audio);
        volume = (FloatControl) music.getControl(FloatControl.Type.MASTER_GAIN);
        music.loop(Clip.LOOP_CONTINUOUSLY);
    } catch (Exception e) {
        e.printStackTrace();
    }
}
```

Add import `java.net.URL`, remove import `java.io.File`.

**Step 2: Commit**

```bash
git add src/SoundManager.java
git commit -m "fix: SoundManager — load audio from classpath instead of file system"
```

---

### Task 5: Fix DatabaseManager path

**Files:**
- Modify: `src/DatabaseManager.java`
- Modify: `src/MissionPanel.java` (the inline DB connection at line 269)

**Step 1: Resolve DB path relative to JAR location**

In DatabaseManager, replace the static DB_URL with a resolved path:

```java
private static final String DB_URL;

static {
    String dbPath;
    try {
        // Resolve path relative to where the JAR/app is located
        String jarDir = System.getProperty("user.dir");
        dbPath = new java.io.File(jarDir, "progress.db").getAbsolutePath();
    } catch (Exception e) {
        dbPath = "progress.db";
    }
    DB_URL = "jdbc:sqlite:" + dbPath;
}
```

Note: `user.dir` is the working directory. For jpackage apps on Windows this may not be ideal, but sqlite-jdbc will create the file if it doesn't exist. A better approach for production would be `user.home`, but to keep behavior consistent with current (DB alongside app), we use `user.dir` for now.

Actually, a more robust approach: use app data directory.

```java
private static final String DB_URL;

static {
    String userHome = System.getProperty("user.home");
    java.io.File appDir = new java.io.File(userHome, ".aquavision");
    appDir.mkdirs();
    String dbPath = new java.io.File(appDir, "progress.db").getAbsolutePath();
    DB_URL = "jdbc:sqlite:" + dbPath;
}
```

**Step 2: Fix MissionPanel inline DB connection (line 269)**

Replace the hardcoded connection string with `DatabaseManager.getConnection()` or use the same path. Simplest: add a public method to DatabaseManager.

Add to DatabaseManager:
```java
public static Connection getConnection() throws SQLException {
    return DriverManager.getConnection(DB_URL);
}
```

Then in MissionPanel line 269:
```java
try (Connection conn = DatabaseManager.getConnection();
```

**Step 3: Commit**

```bash
git add src/DatabaseManager.java src/MissionPanel.java
git commit -m "fix: DatabaseManager — use stable app data directory for SQLite DB"
```

---

### Task 6: Fix GitHub Actions workflow — include resources in JAR and add sqlite-jdbc

**Files:**
- Modify: `.github/workflows/windows-build.yml`

**Step 1: Download sqlite-jdbc, include resources in JAR, fix compilation classpath**

```yaml
name: Build Installers
on:
  push:
    branches: [ main ]
    tags:
      - 'v*'

jobs:
  build:
    strategy:
      matrix:
        include:
          - os: windows-latest
            type: exe
            artifact: AquaVision-Windows
          - os: macos-latest
            type: dmg
            artifact: AquaVision-macOS
          - os: ubuntu-latest
            type: deb
            artifact: AquaVision-Linux
    runs-on: ${{ matrix.os }}

    steps:
      - name: Checkout repository
        uses: actions/checkout@v4

      - name: Set up Java 21
        uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '21'

      - name: Download sqlite-jdbc
        run: |
          curl -L -o sqlite-jdbc.jar https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.45.1.0/sqlite-jdbc-3.45.1.0.jar

      - name: Compile Java sources
        shell: bash
        run: |
          mkdir -p build
          javac -encoding UTF-8 -cp sqlite-jdbc.jar -d build src/*.java

      - name: Create JAR with resources
        shell: bash
        run: |
          cp -r resources/* build/
          jar cfe AquaVision.jar Main -C build .

      - name: Build native installer
        shell: bash
        run: |
          mkdir -p output
          jpackage \
            --name AquaVision \
            --input . \
            --main-jar AquaVision.jar \
            --main-class Main \
            --type ${{ matrix.type }} \
            --dest output \
            --java-options "-Dsun.java2d.opengl=true" \
            --app-version "1.0.0"

      - name: Upload artifact
        uses: actions/upload-artifact@v4
        with:
          name: ${{ matrix.artifact }}
          path: output/*
```

Note: The `--input .` flag includes the `sqlite-jdbc.jar` in the app directory, and jpackage auto-adds JARs from the input to the classpath. The resources are inside `AquaVision.jar` so they are accessible via `getClass().getResource()`.

**Step 2: Commit**

```bash
git add .github/workflows/windows-build.yml
git commit -m "feat: multi-platform build workflow with resources in JAR and sqlite-jdbc"
```

---

### Task 7: Verification — local build test

**Step 1: Compile and build JAR locally**

```bash
mkdir -p build
javac -encoding UTF-8 -d build src/*.java
cp -r resources/* build/
jar cfe AquaVision.jar Main -C build .
```

(Note: compilation will warn about missing sqlite-jdbc at compile time only if needed — the code uses java.sql.* which is in the JDK. sqlite-jdbc is only needed at runtime.)

**Step 2: Test JAR runs**

```bash
java -jar AquaVision.jar
```

Verify: app launches, Start menu shows background, clicking Start shows MissionPanel with background and level buttons visible.

**Step 3: Commit all changes if not already committed**
