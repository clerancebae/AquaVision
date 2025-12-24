
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Game extends BasePanel {
    private int levelNumber;
    private Player player;
    private Timer gameTimer;
    private boolean isGameOver = false;
    private boolean isPaused = false;
    private JDialog pauseDialog;

    // Phase System
    private static final int TOTAL_PHASES = 15;
    private int currentPhase = 0;
    private long phaseStartTime;
    private List<PhaseData> phaseRecords = new ArrayList<>();

    // Fish spawning system (pattern-based)
    private final List<EnemyFish> enemyFishes = new ArrayList<>();
    private PatternManager patternManager;
    private Timer patternTimer;

    // UI Elements
    private JProgressBar progressBar;
    private JLabel phaseTimerLabel;
    private JLabel phaseLabel;

    // Mission completion callback
    private MissionCompletionListener completionListener;

    public Game(int levelNumber) {
        super();
        this.levelNumber = levelNumber;

        setLayout(null);
        setFocusable(true);
        requestFocusInWindow();

        // Initialize pattern manager
        patternManager = new PatternManager(levelNumber);

        // Initialize player
        player = new Player(300, 300);

        // Add a title label
        JLabel titleLabel = new JLabel("Mission " + levelNumber);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 36));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBounds(200, 20, 200, 50);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(titleLabel);

        // Phase progress bar (visual dots)
        progressBar = new JProgressBar(0, TOTAL_PHASES);
        progressBar.setValue(0);
        progressBar.setStringPainted(false);
        progressBar.setBounds(200, 80, 200, 20);
        progressBar.setForeground(new Color(0, 255, 100));
        progressBar.setBackground(new Color(50, 50, 50));
        add(progressBar);

        // Phase label (small, for reference)
        phaseLabel = new JLabel("Phase 0/" + TOTAL_PHASES);
        phaseLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        phaseLabel.setForeground(Color.LIGHT_GRAY);
        phaseLabel.setBounds(520, 30, 100, 20);
        add(phaseLabel);

//        // Phase timer (big, prominent)
//        phaseTimerLabel = new JLabel("0.0s");
//        phaseTimerLabel.setFont(new Font("Arial", Font.BOLD, 48));
//        phaseTimerLabel.setForeground(Color.CYAN);
//        phaseTimerLabel.setBounds(250, 120, 150, 60);
//        add(phaseTimerLabel);

        // Add a back button
        JButton backButton = new JButton("Back");
        backButton.setBounds(20, 20, 100, 40);
        backButton.setFont(new Font("Arial", Font.BOLD, 16));
        backButton.setFocusPainted(false);
        backButton.addActionListener(e -> returnToMissionPanel());
        add(backButton);

        // Add keyboard controls
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    showPauseMenu();
                    return;
                }
                player.keyPressed(e);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                player.keyReleased(e);
            }
        });

        // Start game loop
        gameTimer = new Timer(16, e -> {
            updateGame();
            repaint();
        });
        gameTimer.start();

        // Start first phase
        startPhase(0);
    }

    // Mission completion listener interface
    public interface MissionCompletionListener {
        void onMissionCompleted(int levelNumber, List<PhaseData> phaseData);
        void onMissionFailed(int levelNumber, List<PhaseData> phaseData);
    }

    public void setCompletionListener(MissionCompletionListener listener) {
        this.completionListener = listener;
    }

    private void startPhase(int phase) {
        currentPhase = phase;
        phaseStartTime = System.currentTimeMillis();

        // Create phase data record
        PhaseData data = new PhaseData(levelNumber, currentPhase + 1);
        phaseRecords.add(data);

        // Clear existing enemies
        enemyFishes.clear();

        // Update UI
        progressBar.setValue(currentPhase);
        phaseLabel.setText("Phase " + (currentPhase + 1) + "/" + TOTAL_PHASES);

        // Start pattern spawning
        spawnPattern(phase);

        System.out.println("Phase " + (currentPhase + 1) + " started!");
    }

    private void spawnPattern(int phase) {
        FishPattern pattern = patternManager.getPattern(phase);

        for (SpawnInstruction instruction : pattern.spawns) {
            Timer spawnTimer = new Timer((int)instruction.delay, e -> {
                enemyFishes.add(new EnemyFish(
                        instruction.x,
                        instruction.y,
                        instruction.vx,
                        instruction.vy
                ));
                ((Timer)e.getSource()).stop();
            });
            spawnTimer.setRepeats(false);
            spawnTimer.start();
        }
    }

    private void advancePhase() {
        // Complete current phase
        PhaseData currentData = phaseRecords.get(phaseRecords.size() - 1);
        currentData.complete(true);

        System.out.println("Phase " + (currentPhase + 1) + " completed in " +
                (currentData.survivedDuration / 1000.0) + "s");

        if (currentPhase + 1 >= TOTAL_PHASES) {
            // Mission completed!
            completeMission();
        } else {
            // Start next phase
            startPhase(currentPhase + 1);
        }
    }

    private void completeMission() {
        gameTimer.stop();
        isGameOver = true;

        // Show completion dialog
        JDialog completionDialog = new JDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this),
                "Mission Complete!",
                true
        );

        JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));
        panel.setBackground(new Color(189, 237, 255));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));

        JLabel title = new JLabel("🎉 Mission " + levelNumber + " Complete! 🎉", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(Color.BLACK);

        // Calculate total time
        long totalTime = 0;
        for (PhaseData data : phaseRecords) {
            totalTime += data.survivedDuration;
        }

        JLabel timeLabel = new JLabel("Total Time: " + (totalTime / 1000.0) + "s", SwingConstants.CENTER);
        timeLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        timeLabel.setForeground(Color.BLACK);

        JButton continueButton = new JButton("Continue");
        continueButton.setFont(new Font("Arial", Font.BOLD, 16));
        continueButton.addActionListener(e -> {
            completionDialog.dispose();
            if (completionListener != null) {
                completionListener.onMissionCompleted(levelNumber, phaseRecords);
            }
            returnToMissionPanel();
        });

        panel.add(title);
        panel.add(timeLabel);
        panel.add(new JLabel()); // Spacer
        panel.add(continueButton);

        completionDialog.setUndecorated(true);
        completionDialog.setContentPane(panel);
        completionDialog.pack();
        completionDialog.setLocationRelativeTo(this);
        completionDialog.setVisible(true);
    }

    private void failMission() {
        gameTimer.stop();
        isGameOver = true;

        // Complete current phase as failed
        if (!phaseRecords.isEmpty()) {
            PhaseData currentData = phaseRecords.get(phaseRecords.size() - 1);
            currentData.complete(false);
        }

        // Show failure dialog
        JDialog failDialog = new JDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this),
                "Mission Failed",
                true
        );

        JPanel panel = new JPanel(new GridLayout(5, 1, 10, 10));
        panel.setBackground(new Color(189, 237, 255));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));

        JLabel title = new JLabel("Collision! Try Again ", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(Color.black);

        JLabel phaseInfo = new JLabel("Failed at Phase " + (currentPhase + 1) + "/" + TOTAL_PHASES, SwingConstants.CENTER);
        phaseInfo.setFont(new Font("Arial", Font.PLAIN, 16));
        phaseInfo.setForeground(Color.BLACK);

        JButton retryButton = new JButton("Retry Mission");
        JButton exitButton = new JButton("Exit to Missions");

        retryButton.addActionListener(e -> {
            failDialog.dispose();
            restartMission();
        });

        exitButton.addActionListener(e -> {
            failDialog.dispose();
            if (completionListener != null) {
                completionListener.onMissionFailed(levelNumber, phaseRecords);
            }
            returnToMissionPanel();
        });

        panel.add(title);
        panel.add(phaseInfo);
        panel.add(new JLabel()); // Spacer
        panel.add(retryButton);
        panel.add(exitButton);

        failDialog.setUndecorated(true);
        failDialog.setContentPane(panel);
        failDialog.pack();
        failDialog.setLocationRelativeTo(this);
        failDialog.setVisible(true);
    }

    private void restartMission() {
        enemyFishes.clear();
        player = new Player(300, 300);
        isPaused = false;
        isGameOver = false;
        currentPhase = 0;
        phaseRecords.clear();
        gameTimer.start();
        startPhase(0);
        requestFocusInWindow();
    }

    private void returnToMissionPanel() {
        if (gameTimer != null) {
            gameTimer.stop();
        }
        JFrame gameFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        if (gameFrame != null) {
            gameFrame.dispose();
        }
    }

    private void showPauseMenu() {
        if (isGameOver || isPaused) return;

        isPaused = true;
        gameTimer.stop();

        pauseDialog = new JDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this),
                "Paused",
                true
        );

        JPanel panel = new JPanel(new GridLayout(5, 1, 10, 10));
        panel.setBackground(new Color(189, 237, 255));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));

        JLabel title = new JLabel("PAUSED", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(Color.BLACK);


        JButton resume = new JButton("Resume");
        JButton restart = new JButton("Restart");
        JButton returnToLobby = new JButton("Return to Missions");
        JButton exit = new JButton("Exit");

        panel.add(title);
        panel.add(resume);
        panel.add(restart);
        panel.add(returnToLobby);
        panel.add(exit);

        resume.addActionListener(e -> resumeGame());
        restart.addActionListener(e -> {
            pauseDialog.dispose();
            restartMission();
        });
        returnToLobby.addActionListener(e -> {
            pauseDialog.dispose();
            returnToMissionPanel();
        });
        exit.addActionListener(e -> System.exit(0));

        pauseDialog.setUndecorated(true);
        pauseDialog.setContentPane(panel);
        pauseDialog.pack();
        pauseDialog.setLocationRelativeTo(this);
        pauseDialog.setVisible(true);
    }

    private void resumeGame() {
        pauseDialog.dispose();
        isPaused = false;
        gameTimer.start();
        requestFocusInWindow();
    }

    private void updateGame() {
        if (isGameOver || isPaused) return;

        // Update phase timer
        long elapsed = System.currentTimeMillis() - phaseStartTime;
//        phaseTimerLabel.setText(String.format("%.1fs", elapsed / 1000.0));

        // Update player
        player.update();

        // Check collision with enemy fish
        int paddingX = 22;
        int paddingY = 6;

        Rectangle playerBounds = new Rectangle(
                player.getX() + paddingX,
                player.getY() + paddingY,
                player.getWidth() - (paddingX * 2),
                player.getHeight() - (paddingY * 2)
        );

        for (EnemyFish fish : enemyFishes) {
            Rectangle fishBounds = new Rectangle(
                    (int)fish.x + paddingX,
                    (int)fish.y + paddingY,
                    fish.getWidth() - (paddingX * 2),
                    fish.getHeight() - (paddingY * 2)
            );

            if (playerBounds.intersects(fishBounds)) {
                // Collision detected - mission fails
                failMission();
                return;
            }
        }

        // Update enemy fish
        Iterator<EnemyFish> it = enemyFishes.iterator();
        boolean allFishGone = true;
        while (it.hasNext()) {
            EnemyFish fish = it.next();
            fish.update();

            // Remove fish that went off-screen
            if (fish.x < -100 || fish.x > 700 || fish.y < -100 || fish.y > 700) {
                it.remove();
            } else {
                allFishGone = false;
            }
        }

        // If all pattern fish are gone, advance to next phase
        if (allFishGone && elapsed > 1000) { // Wait at least 1 second
            advancePhase();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Black background (dichoptic requirement)
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(new Color(0,0,0));
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // Draw enemy fish
        for (EnemyFish fish : enemyFishes) {
            fish.draw(g);
        }

        // Draw player (on top)
        player.draw(g);
    }
}

// Phase Data Storage Class
class PhaseData {
    int missionNumber;
    int phaseNumber;
    long startTime;
    long endTime;
    long survivedDuration;
    boolean completed;

    public PhaseData(int mission, int phase) {
        this.missionNumber = mission;
        this.phaseNumber = phase;
        this.startTime = System.currentTimeMillis();
    }

    public void complete(boolean success) {
        this.endTime = System.currentTimeMillis();
        this.survivedDuration = endTime - startTime;
        this.completed = success;
    }

    @Override
    public String toString() {
        return String.format("Mission %d, Phase %d: %.2fs (%s)",
                missionNumber, phaseNumber, survivedDuration / 1000.0,
                completed ? "Success" : "Failed");
    }
}

// Pattern Manager
class PatternManager {
    private int missionLevel;

    public PatternManager(int level) {
        this.missionLevel = level;
    }

    public FishPattern getPattern(int phase) {
        // Difficulty scales with mission level
        return createPattern(missionLevel, phase);
    }

    private FishPattern createPattern(int mission, int phase) {
        FishPattern pattern = new FishPattern();

        // Mission-based difficulty scaling
        // Mission 1: baseSpeed = 1.5 (easiest)
        // Mission 2: baseSpeed = 1.8
        // Mission 3: baseSpeed = 2.1, etc.
        double baseSpeed = 1.2 + (mission * 0.3);

        // Additional complexity multiplier for higher missions
        double complexityFactor = 1.0 + (mission - 1) * 0.15;
        int extraFish = Math.max(0, (mission - 1)); // More fish in higher missions

        switch (phase) {
            case 0: // Warm-up: Single horizontal tracking
                pattern.addSpawn(0, -50, 300, baseSpeed * 0.9, 0, 30);
                // Higher missions: Add challenge fish
                if (mission >= 2) {
                    pattern.addSpawn(800, 650, 200, -baseSpeed * 0.9, 0, 30);
                }
                break;

            case 1: // Warm-up: Opposite direction tracking
                pattern.addSpawn(0, 650, 300, -baseSpeed * 0.9, 0, 30);
                if (mission >= 2) {
                    pattern.addSpawn(800, -50, 400, baseSpeed * 0.9, 0, 30);
                }
                break;

            case 2: // Warm-up: Vertical tracking
                pattern.addSpawn(0, 300, -50, 0, baseSpeed * 0.9, 30);
                if (mission >= 2) {
                    pattern.addSpawn(700, 300, 650, 0, -baseSpeed * 0.9, 30);
                }
                break;

            case 3: // Diagonal pursuit
                pattern.addSpawn(0, -50, -50, baseSpeed, baseSpeed * 0.8, 30);
                pattern.addSpawn(600, 650, 650, -baseSpeed, -baseSpeed * 0.8, 30);
                // Mission 2+: Add crossing diagonal
                if (mission >= 2) {
                    pattern.addSpawn(300, 650, -50, -baseSpeed, baseSpeed * 0.8, 30);
                }
                if (mission >= 3) {
                    pattern.addSpawn(300, -50, 650, baseSpeed, -baseSpeed * 0.8, 30);
                }
                break;

            case 4: // Smooth pursuit
                pattern.addSpawn(0, -50, 150, baseSpeed * 1.1, 0, 30);
                pattern.addSpawn(500, -50, 450, baseSpeed * 1.1, 0, 30);
                pattern.addSpawn(1000, 650, 300, -baseSpeed * 1.1, 0, 30);
                // Higher missions: More waves
                if (mission >= 3) {
                    pattern.addSpawn(1500, -50, 300, baseSpeed * 1.1, 0, 30);
                }
                break;

            case 5: // Saccadic training
                pattern.addSpawn(0, -50, 100, baseSpeed * 1.2, 0, 30);
                pattern.addSpawn(600, 650, 500, -baseSpeed * 1.2, 0, 30);
                pattern.addSpawn(1200, 300, -50, 0, baseSpeed * 1.2, 30);
                // Mission 2+: Faster transitions
                if (mission >= 2) {
                    pattern.addSpawn(1600, 300, 650, 0, -baseSpeed * 1.2, 30);
                }
                if (mission >= 4) {
                    pattern.addSpawn(2000, -50, 300, baseSpeed * 1.2, 0, 30);
                }
                break;

            case 6: // Convergence
                pattern.addSpawn(0, -50, 200, baseSpeed * complexityFactor, baseSpeed * 0.5, 30);
                pattern.addSpawn(0, -50, 400, baseSpeed * complexityFactor, -baseSpeed * 0.5, 30);
                pattern.addSpawn(700, 650, 200, -baseSpeed * complexityFactor, baseSpeed * 0.5, 30);
                pattern.addSpawn(700, 650, 400, -baseSpeed * complexityFactor, -baseSpeed * 0.5, 30);
                // Mission 3+: Add vertical convergence
                if (mission >= 3) {
                    pattern.addSpawn(1400, 300, -50, 0, baseSpeed * complexityFactor, 30);
                    pattern.addSpawn(1400, 300, 650, 0, -baseSpeed * complexityFactor, 30);
                }
                break;

            case 7: // Divergence
                pattern.addSpawn(0, 300, 200, 0, -baseSpeed * 0.6, 30);
                pattern.addSpawn(0, 300, 400, 0, baseSpeed * 0.6, 30);
                pattern.addSpawn(700, 300, 300, baseSpeed * 0.8 * complexityFactor, 0, 30);
                pattern.addSpawn(700, 300, 300, -baseSpeed * 0.8 * complexityFactor, 0, 30);
                if (mission >= 3) {
                    pattern.addSpawn(1400, 150, 300, baseSpeed * 0.7, baseSpeed * 0.5, 30);
                    pattern.addSpawn(1400, 450, 300, baseSpeed * 0.7, -baseSpeed * 0.5, 30);
                }
                break;

            case 8: // Figure-8 simulation
                pattern.addSpawn(0, -50, 150, baseSpeed * 1.1 * complexityFactor, baseSpeed * 0.4, 30);
                pattern.addSpawn(400, 650, 150, -baseSpeed * 1.1 * complexityFactor, baseSpeed * 0.4, 30);
                pattern.addSpawn(800, 650, 450, -baseSpeed * 1.1 * complexityFactor, -baseSpeed * 0.4, 30);
                pattern.addSpawn(1200, -50, 450, baseSpeed * 1.1 * complexityFactor, -baseSpeed * 0.4, 30);
                // Mission 2+: Double figure-8
                if (mission >= 2) {
                    pattern.addSpawn(1600, -50, 300, baseSpeed * 1.1 * complexityFactor, 0, 30);
                    pattern.addSpawn(2000, 650, 300, -baseSpeed * 1.1 * complexityFactor, 0, 30);
                }
                break;

            case 9: // Peripheral awareness
                pattern.addSpawn(0, -50, 50, baseSpeed * complexityFactor, 0, 30);
                pattern.addSpawn(0, -50, 550, baseSpeed * complexityFactor, 0, 30);
                pattern.addSpawn(500, 650, 150, -baseSpeed * complexityFactor, 0, 30);
                pattern.addSpawn(500, 650, 450, -baseSpeed * complexityFactor, 0, 30);
                pattern.addSpawn(1000, 100, -50, 0, baseSpeed * complexityFactor, 30);
                pattern.addSpawn(1000, 500, -50, 0, baseSpeed * complexityFactor, 30);
                // Mission 3+: Add diagonal peripheral
                if (mission >= 3) {
                    pattern.addSpawn(1500, -50, -50, baseSpeed * complexityFactor, baseSpeed * complexityFactor, 30);
                    pattern.addSpawn(1500, 650, 650, -baseSpeed * complexityFactor, -baseSpeed * complexityFactor, 30);
                }
                break;

            case 10: // Vergence training
                pattern.addSpawn(0, -50, 150, baseSpeed * 1.2 * complexityFactor, baseSpeed * 0.3, 30);
                pattern.addSpawn(0, -50, 450, baseSpeed * 1.2 * complexityFactor, -baseSpeed * 0.3, 30);
                pattern.addSpawn(500, 650, 450, -baseSpeed * 1.2 * complexityFactor, baseSpeed * 0.3, 30);
                pattern.addSpawn(500, 650, 150, -baseSpeed * 1.2 * complexityFactor, -baseSpeed * 0.3, 30);
                pattern.addSpawn(1000, 300, -50, 0, baseSpeed * 1.2 * complexityFactor, 30);
                // Mission 4+: Add more crossing patterns
                if (mission >= 4) {
                    pattern.addSpawn(1500, 300, 650, 0, -baseSpeed * 1.2 * complexityFactor, 30);
                    pattern.addSpawn(2000, -50, 300, baseSpeed * 1.2 * complexityFactor, 0, 30);
                }
                break;

            case 11: // Rapid tracking
                int fishCount = 6 + extraFish;
                for (int i = 0; i < fishCount; i++) {
                    int delay = i * 300;
                    if (i % 2 == 0) {
                        pattern.addSpawn(delay, -50, 200 + (i * 50), baseSpeed * 1.3 * complexityFactor, 0, 30);
                    } else {
                        pattern.addSpawn(delay, 650, 200 + (i * 50), -baseSpeed * 1.3 * complexityFactor, 0, 30);
                    }
                }
                break;

            case 12: // Circular pursuit
                pattern.addSpawn(0, -50, 300, baseSpeed * 1.1 * complexityFactor, baseSpeed * 0.5, 30);
                pattern.addSpawn(400, 300, -50, baseSpeed * 0.5 * complexityFactor, baseSpeed * 1.1, 30);
                pattern.addSpawn(800, 650, 300, -baseSpeed * 1.1 * complexityFactor, -baseSpeed * 0.5, 30);
                pattern.addSpawn(1200, 300, 650, -baseSpeed * 0.5 * complexityFactor, -baseSpeed * 1.1, 30);
                pattern.addSpawn(1600, -50, 150, baseSpeed * 1.1 * complexityFactor, baseSpeed * 0.8, 30);
                pattern.addSpawn(1600, 650, 450, -baseSpeed * 1.1 * complexityFactor, -baseSpeed * 0.8, 30);
                // Mission 3+: Counter-rotating circles
                if (mission >= 3) {
                    pattern.addSpawn(2000, 650, 150, -baseSpeed * 1.1 * complexityFactor, baseSpeed * 0.8, 30);
                    pattern.addSpawn(2000, -50, 450, baseSpeed * 1.1 * complexityFactor, -baseSpeed * 0.8, 30);
                }
                break;

            case 13: // Advanced vergence
                pattern.addSpawn(0, -50, 100, baseSpeed * 1.2 * complexityFactor, baseSpeed * 0.6, 30);
                pattern.addSpawn(0, -50, 500, baseSpeed * 1.2 * complexityFactor, -baseSpeed * 0.6, 30);
                pattern.addSpawn(500, 650, 100, -baseSpeed * 1.2 * complexityFactor, baseSpeed * 0.6, 30);
                pattern.addSpawn(500, 650, 500, -baseSpeed * 1.2 * complexityFactor, -baseSpeed * 0.6, 30);
                pattern.addSpawn(1000, 150, -50, baseSpeed * 0.4 * complexityFactor, baseSpeed * 1.2, 30);
                pattern.addSpawn(1000, 450, -50, -baseSpeed * 0.4 * complexityFactor, baseSpeed * 1.2, 30);
                pattern.addSpawn(1500, 300, 650, 0, -baseSpeed * 1.3 * complexityFactor, 30);
                // Mission 5: Add ultimate challenge
                if (mission >= 5) {
                    pattern.addSpawn(2000, -50, -50, baseSpeed * 1.3 * complexityFactor, baseSpeed * 1.3, 30);
                    pattern.addSpawn(2000, 650, 650, -baseSpeed * 1.3 * complexityFactor, -baseSpeed * 1.3, 30);
                }
                break;

            case 14: // FINAL BOSS
                double finalSpeed = baseSpeed * 1.4 * complexityFactor;

                // Fast horizontal pursuit
                pattern.addSpawn(0, -50, 200, finalSpeed, 0, 30);
                pattern.addSpawn(0, 650, 400, -finalSpeed, 0, 30);

                // Diagonal crossing
                pattern.addSpawn(500, -50, -50, finalSpeed * 0.9, finalSpeed * 0.9, 30);
                pattern.addSpawn(500, 650, 650, -finalSpeed * 0.9, -finalSpeed * 0.9, 30);

                // Converging paths
                pattern.addSpawn(1000, -50, 300, finalSpeed * 0.95, finalSpeed * 0.3, 30);
                pattern.addSpawn(1000, 650, 300, -finalSpeed * 0.95, finalSpeed * 0.3, 30);

                // Vertical chase
                pattern.addSpawn(1500, 300, -50, 0, finalSpeed, 30);
                pattern.addSpawn(1800, 300, 650, 0, -finalSpeed, 30);

                // Final diagonal sweep
                pattern.addSpawn(2100, -50, 450, finalSpeed * 0.95, -finalSpeed * 0.5, 30);
                pattern.addSpawn(2100, 650, 150, -finalSpeed * 0.95, finalSpeed * 0.5, 30);

                // Mission 2+: Add chaos waves
                if (mission >= 2) {
                    pattern.addSpawn(2500, 300, -50, 0, finalSpeed, 30);
                    pattern.addSpawn(2800, -50, 300, finalSpeed, 0, 30);
                }

                // Mission 3+: Add pincer finale
                if (mission >= 3) {
                    pattern.addSpawn(3100, -50, 100, finalSpeed, finalSpeed * 0.4, 30);
                    pattern.addSpawn(3100, -50, 500, finalSpeed, -finalSpeed * 0.4, 30);
                    pattern.addSpawn(3100, 650, 100, -finalSpeed, finalSpeed * 0.4, 30);
                    pattern.addSpawn(3100, 650, 500, -finalSpeed, -finalSpeed * 0.4, 30);
                }
                break;

            default:
                pattern.addSpawn(0, -50, 300, baseSpeed, 0, 30);
        }

        return pattern;
    }
}

// Fish Pattern Class
class FishPattern {
    List<SpawnInstruction> spawns = new ArrayList<>();

    public void addSpawn(long delay, double x, double y, double vx, double vy, int size) {
        spawns.add(new SpawnInstruction(delay, x, y, vx, vy, size));
    }
}

// Spawn Instruction
class SpawnInstruction {
    long delay;
    double x, y;
    double vx, vy;
    int size;

    public SpawnInstruction(long delay, double x, double y, double vx, double vy, int size) {
        this.delay = delay;
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.size = size;
    }
}

// Enemy Fish class
class EnemyFish {
    double x, y;
    double vx, vy;
    double phase;

    private int width = 100;
    private int height = 40;

    private Image fishImage;
    private boolean facingRight;

    EnemyFish(double x, double y, double vx, double vy) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.phase = Math.random() * Math.PI * 2;
        this.facingRight = vx < 0;

        loadFishImage();
    }

    private void loadFishImage() {
        try {
            String imagePath = LazyEyeConfig.getEnemyImagePath();
            ImageIcon icon = new ImageIcon(imagePath);
            if (icon.getImage() != null && icon.getIconWidth() > 0) {
                fishImage = icon.getImage()
                        .getScaledInstance(width, height, Image.SCALE_SMOOTH);
            } else {
                fishImage = null;
            }
        } catch (Exception e) {
            fishImage = null;
        }
    }

    void update() {
        x += vx;
        y += vy;

        // Daha yumuşak dalga hareketi
        y += Math.sin(phase) * 0.3;
        phase += 0.1;
    }

    void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        if (fishImage != null) {
            if (facingRight) {
                g2d.drawImage(fishImage, (int) x, (int) y, width, height, null);
            } else {
                g2d.drawImage(fishImage, (int) x + width, (int) y, -width, height, null);
            }
        } else {
            drawSimpleFish(g2d);
        }
    }

    private void drawSimpleFish(Graphics2D g2d) {
        g2d.setColor(new Color(100, 180, 255));

        if (facingRight) {
            g2d.fillOval((int) x, (int) y, width - 10, height);
            int[] tailX = {(int) x, (int) x - 15, (int) x};
            int[] tailY = {(int) y + 5, (int) y + height / 2, (int) y + height - 5};
            g2d.fillPolygon(tailX, tailY, 3);
        } else {
            g2d.fillOval((int) x + 10, (int) y, width - 10, height);
            int[] tailX = {(int) x + width, (int) x + width + 15, (int) x + width};
            int[] tailY = {(int) y + 5, (int) y + height / 2, (int) y + height - 5};
            g2d.fillPolygon(tailX, tailY, 3);
        }
    }

    // 🔍 Collision / ölçüm için
    public int getX() { return (int) x; }
    public int getY() { return (int) y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    // Add this inside the EnemyFish class
    public void reloadSkin() {
        loadFishImage(); // Reruns the logic to check LazyEyeConfig again
    }
}


// Player class
class Player {
    private double x, y;
    private double velocityX = 0;
    private double velocityY = 0;
    private double speed = 5.0;
    private double acceleration = 0.5;
    private double friction = 0.92;
    private int width = 100;
    private int height = 40;
    private boolean facingRight = true;

    private boolean upPressed = false;
    private boolean downPressed = false;
    private boolean leftPressed = false;
    private boolean rightPressed = false;

    private Image fishImage;

    public Player(int startX, int startY) {
        this.x = startX;
        this.y = startY;
        loadFishImage();
    }

    private void loadFishImage() {
        try {
            String imagePath = LazyEyeConfig.getPlayerImagePath();
            ImageIcon icon = new ImageIcon(imagePath);
            if (icon.getImage() != null && icon.getIconWidth() > 0) {
                fishImage = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
            } else {
                fishImage = null;
            }
        } catch (Exception e) {
            fishImage = null;
        }
    }

    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_UP || key == KeyEvent.VK_W) upPressed = true;
        if (key == KeyEvent.VK_DOWN || key == KeyEvent.VK_S) downPressed = true;
        if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) leftPressed = true;
        if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) rightPressed = true;
    }

    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_UP || key == KeyEvent.VK_W) upPressed = false;
        if (key == KeyEvent.VK_DOWN || key == KeyEvent.VK_S) downPressed = false;
        if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) leftPressed = false;
        if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) rightPressed = false;
    }

    public void update() {
        if (upPressed) velocityY -= acceleration;
        if (downPressed) velocityY += acceleration;
        if (leftPressed) {
            velocityX -= acceleration;
            facingRight = false;
        }
        if (rightPressed) {
            velocityX += acceleration;
            facingRight = true;
        }

        double currentSpeed = Math.sqrt(velocityX * velocityX + velocityY * velocityY);
        if (currentSpeed > speed) {
            velocityX = (velocityX / currentSpeed) * speed;
            velocityY = (velocityY / currentSpeed) * speed;
        }

        velocityX *= friction;
        velocityY *= friction;

        x += velocityX;
        y += velocityY;

        if (x < 0) {
            x = 0;
            velocityX = 0;
        }
        if (x > 600 - width) {
            x = 600 - width;
            velocityX = 0;
        }
        if (y < 0) {
            y = 0;
            velocityY = 0;
        }
        if (y > 600 - height) {
            y = 600 - height;
            velocityY = 0;
        }
    }

    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (fishImage != null) {
            if (!facingRight) {
                g2d.drawImage(fishImage, (int)x, (int)y, width, height, null);
            } else {
                g2d.drawImage(fishImage, (int)x + width, (int)y, -width, height, null);
            }
        } else {
            drawSimpleFish(g2d);
        }
    }

    private void drawSimpleFish(Graphics2D g2d) {
        // Body (oval)
        g2d.setColor(new Color(255, 140, 0)); // Orange
        if (facingRight) {
            g2d.fillOval((int) x, (int) y, width - 10, height);
            // Tail
            int[] tailX = {(int) x, (int) x - 15, (int) x};
            int[] tailY = {(int) y + 10, (int) y + height / 2, (int) y + height - 10};
            g2d.fillPolygon(tailX, tailY, 3);
            // Eye
            g2d.setColor(Color.WHITE);
            g2d.fillOval((int) x + width - 25, (int) y + 10, 8, 8);
            g2d.setColor(Color.BLACK);
            g2d.fillOval((int) x + width - 23, (int) y + 12, 4, 4);
        } else {
            g2d.fillOval((int) x + 10, (int) y, width - 10, height);
            // Tail
            int[] tailX = {(int) x + width, (int) x + width + 15, (int) x + width};
            int[] tailY = {(int) y + 10, (int) y + height / 2, (int) y + height - 10};
            g2d.fillPolygon(tailX, tailY, 3);
            // Eye
            g2d.setColor(Color.WHITE);
            g2d.fillOval((int) x + 15, (int) y + 10, 8, 8);
            g2d.setColor(Color.BLACK);
            g2d.fillOval((int) x + 17, (int) y + 12, 4, 4);
        }
    }

    public int getX() {
        return (int) x;
    }

    public int getY() {
        return (int) y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
    // Add this inside the Player class
    public void reloadSkin() {
        loadFishImage(); // Reruns the logic to check LazyEyeConfig again
    }
}

/*
import java.io.FileWriter;
import java.io.IOException;

public class ProgressLogger {

    private static final String FILE_NAME = "progress.txt";

    public static void save(
            int mission,
            int phase,
            int density,
            double speed,
            long time,
            boolean success
    ) {
        try (FileWriter writer = new FileWriter(FILE_NAME, true)) {

            String line =
                    "mission=" + mission + ";" +
                    "phase=" + phase + ";" +
                    "density=" + density + ";" +
                    "speed=" + speed + ";" +
                    "time=" + time + ";" +
                    "success=" + success;

            writer.write(line + "\n");

        } catch (IOException e) {
            System.out.println("Progress save error!");
        }
    }
}

 */