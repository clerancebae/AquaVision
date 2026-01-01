
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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

    // Fish spawning system
    private final List<EnemyFish> enemyFishes = new ArrayList<>();
    private PatternManager patternManager;

    // UI Elements
    private JProgressBar progressBar;
    private JLabel phaseLabel;

    private MissionCompletionListener completionListener;

    private long lastHitTime = 0;
    private static final long INVINCIBILITY_MS = 500;


    public Game(int levelNumber) {
        super();
        this.levelNumber = levelNumber;

        DatabaseManager.incrementAttempt(levelNumber);

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


        // Add a back button
        JButton backButton = new JButton("Back");
        backButton.setBounds(20, 20, 100, 40);
        backButton.setFont(new Font("Arial", Font.BOLD, 16));
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

        PhaseData data = new PhaseData(levelNumber, currentPhase + 1);
        phaseRecords.add(data);

        enemyFishes.clear();

        progressBar.setValue(currentPhase);
        phaseLabel.setText("Phase " + (currentPhase + 1) + "/" + TOTAL_PHASES);

        spawnPattern(phase);
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
        PhaseData currentData = phaseRecords.get(phaseRecords.size() - 1);
        currentData.complete(true);

        DatabaseManager.updateHighestPhase(levelNumber, currentPhase);

        if (currentPhase + 1 >= TOTAL_PHASES) {
            completeMission();
        } else {
            startPhase(currentPhase + 1);
        }
    }
    private void completeMission() {
        gameTimer.stop();
        isGameOver = true;

        DatabaseManager.incrementCompletion(levelNumber);
        DatabaseManager.updateHighestPhase(levelNumber, TOTAL_PHASES - 1);

        String progressReport = DatabaseManager.getProgressReport(levelNumber);

        long totalTime = 0;
        for (PhaseData data : phaseRecords) totalTime += data.survivedDuration;
        double totalSeconds = totalTime / 1000.0;
        DatabaseManager.logAttempt(levelNumber, 15, true, totalTime / 1000.0);

        JDialog dialog = new JDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this),
                "Mission Complete!",
                true
        );

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(189, 237, 255));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 30, 15, 30));

        JLabel title = new JLabel("🎉 CONGRATS! 🎉");
        title.setFont(new Font("Arial", Font.BOLD, 26));
        title.setForeground(new Color(0, 100, 200));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel missionLabel = new JLabel("Mission " + levelNumber + " Completed!");
        missionLabel.setFont(new Font("Arial", Font.BOLD, 18));
        missionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel timeLabel = new JLabel("Total Time: " + String.format("%.1f", totalSeconds) + " seconds");
        timeLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        timeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel statsLabel = new JLabel(progressReport.replace("\n", " "));
        statsLabel.setFont(new Font("Arial", Font.PLAIN, 15));
        statsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // === YENİ: GELİŞME GRAFİĞİ BUTONU ===
        JButton graphButton = new JButton("View Success Rate Graph");
        graphButton.setFont(new Font("Arial", Font.BOLD, 15));
        graphButton.setFocusPainted(false);
        graphButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        graphButton.setBackground(new Color(0, 150, 0));
        graphButton.setForeground(Color.WHITE);
        graphButton.setOpaque(true);
        graphButton.setBorderPainted(false);
        graphButton.addActionListener(e -> {
            String graph = DatabaseManager.generateAsciiSuccessRateGraph(levelNumber);
            JTextArea graphArea = new JTextArea(graph);
            graphArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
            graphArea.setEditable(false);
            graphArea.setBackground(new Color(240, 240, 240));
            graphArea.setForeground(Color.BLACK);

            JScrollPane scrollPane = new JScrollPane(graphArea);
            scrollPane.setPreferredSize(new Dimension(560, 420));

            JOptionPane.showMessageDialog(
                    dialog,
                    scrollPane,
                    "Mission " + levelNumber + " - Improvement Graph",
                    JOptionPane.INFORMATION_MESSAGE
            );
        });

        panel.add(title);
        panel.add(Box.createVerticalStrut(8));
        panel.add(missionLabel);
        panel.add(Box.createVerticalStrut(6));
        panel.add(timeLabel);
        panel.add(Box.createVerticalStrut(6));
        panel.add(statsLabel);
        panel.add(Box.createVerticalStrut(12));
        panel.add(graphButton);                    // <-- Grafik butonu eklendi
        panel.add(Box.createVerticalStrut(12));

        JButton continueBtn = new JButton("Continue");
        continueBtn.setFont(new Font("Arial", Font.BOLD, 16));
        continueBtn.setFocusPainted(false);
        continueBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        continueBtn.addActionListener(e -> {
            dialog.dispose();
            if (completionListener != null)
                completionListener.onMissionCompleted(levelNumber, phaseRecords);
            returnToMissionPanel();
        });

        panel.add(continueBtn);

        dialog.setUndecorated(true);
        dialog.setContentPane(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void failMission() {
        gameTimer.stop();
        isGameOver = true;

        DatabaseManager.updateHighestPhase(levelNumber, currentPhase);

        if (!phaseRecords.isEmpty())
            phaseRecords.get(phaseRecords.size() - 1).complete(false);

        int currentReached = currentPhase + 1;
        long totalTime = 0;
        for (PhaseData data : phaseRecords) totalTime += data.survivedDuration;
        DatabaseManager.logAttempt(levelNumber, currentReached, false, totalTime / 1000.0);

        int previousRecord = 0;

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:progress.db");
             PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT highest_phase_reached FROM mission_progress WHERE mission = ?")) {

            pstmt.setInt(1, levelNumber);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) previousRecord = rs.getInt("highest_phase_reached");
        } catch (Exception ignored) {}

        int improvement = currentReached - previousRecord;
        String progressMsg = improvement > 0
                ? "NEW RECORD! +" + improvement + " phase improvement!"
                : "You got stuck at phase " + currentReached;

        JDialog dialog = new JDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this),
                "Collision!",
                true
        );

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(189, 237, 255));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 30, 15, 30));

        JLabel title = new JLabel("COLLISION!");
        title.setFont(new Font("Arial", Font.BOLD, 26));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel phaseLabel = new JLabel("Phase " + currentReached + " / " + TOTAL_PHASES);
        phaseLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        phaseLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel progressLabel = new JLabel(progressMsg);
        progressLabel.setFont(new Font("Arial", Font.BOLD, 16));
        progressLabel.setForeground(improvement > 0 ? new Color(0, 120, 0) : Color.BLACK);
        progressLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel motivation1 = new JLabel("Every attempt strengthens your eyes.");
        JLabel motivation2 = new JLabel("Be patient, you'll soon see the difference.");
        motivation1.setFont(new Font("Arial", Font.PLAIN, 14));
        motivation2.setFont(new Font("Arial", Font.PLAIN, 14));
        motivation1.setAlignmentX(Component.CENTER_ALIGNMENT);
        motivation2.setAlignmentX(Component.CENTER_ALIGNMENT);

        // === YENİ: GELİŞME GRAFİĞİ BUTONU ===
        JButton graphButton = new JButton("View Success Rate Graph");
        graphButton.setFont(new Font("Arial", Font.BOLD, 15));
        graphButton.setFocusPainted(false);
        graphButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        graphButton.setBackground(new Color(200, 0, 0)); // Kırmızımsı, dikkat çeksin
        graphButton.setForeground(Color.WHITE);
        graphButton.setOpaque(true);
        graphButton.setBorderPainted(false);
        graphButton.addActionListener(e -> {
            String graph = DatabaseManager.generateAsciiSuccessRateGraph(levelNumber);
            JTextArea graphArea = new JTextArea(graph);
            graphArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
            graphArea.setEditable(false);
            graphArea.setBackground(new Color(240, 240, 240));
            graphArea.setForeground(Color.BLACK);

            JScrollPane scrollPane = new JScrollPane(graphArea);
            scrollPane.setPreferredSize(new Dimension(560, 420));

            JOptionPane.showMessageDialog(
                    dialog,
                    scrollPane,
                    "Mission " + levelNumber + " - Improvement Graph",
                    JOptionPane.INFORMATION_MESSAGE
            );
        });

        panel.add(title);
        panel.add(Box.createVerticalStrut(6));
        panel.add(phaseLabel);
        panel.add(Box.createVerticalStrut(4));
        panel.add(progressLabel);
        panel.add(Box.createVerticalStrut(6));
        panel.add(motivation1);
        panel.add(motivation2);
        panel.add(Box.createVerticalStrut(12));
        panel.add(graphButton);                    // <-- Grafik butonu eklendi
        panel.add(Box.createVerticalStrut(12));

        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);

        JButton retryBtn = new JButton("Try Again");
        JButton exitBtn = new JButton("Exit");

        retryBtn.setFont(new Font("Arial", Font.BOLD, 15));
        exitBtn.setFont(new Font("Arial", Font.BOLD, 15));

        retryBtn.addActionListener(e -> {
            dialog.dispose();
            restartMission();
        });

        exitBtn.addActionListener(e -> {
            dialog.dispose();
            if (completionListener != null)
                completionListener.onMissionFailed(levelNumber, phaseRecords);
            returnToMissionPanel();
        });

        buttonPanel.add(retryBtn);
        buttonPanel.add(exitBtn);

        panel.add(buttonPanel);

        dialog.setUndecorated(true);
        dialog.setContentPane(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void restartMission() {
        enemyFishes.clear();
        player = new Player(300, 300);
        isPaused = false;
        isGameOver = false;
        currentPhase = 0;
        phaseRecords.clear();
        DatabaseManager.incrementAttempt(levelNumber);

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

        long elapsed = System.currentTimeMillis() - phaseStartTime;

        // Update player
        player.update();

        // Update enemy fish
        Iterator<EnemyFish> it = enemyFishes.iterator();
        boolean allFishGone = true;

        while (it.hasNext()) {
            EnemyFish fish = it.next();
            fish.update();

            if (fish.x < -100 || fish.x > 700 || fish.y < -100 || fish.y > 700) {
                it.remove();
            } else {
                allFishGone = false;
            }
        }

        // Multi-component collision detection
        java.util.List<Rectangle> playerParts = player.getBodyParts();

        for (EnemyFish fish : enemyFishes) {
            java.util.List<Rectangle> fishParts = fish.getBodyParts();

            // Check each player part against each fish part
            for (Rectangle playerPart : playerParts) {
                for (Rectangle fishPart : fishParts) {
                    if (playerPart.intersects(fishPart)) {
                        failMission();
                        return;
                    }
                }
            }
        }

        if (allFishGone && elapsed > 1000) {
            advancePhase();
        }
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Black background (dichoptic requirement)
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(new Color(8, 8, 8));
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
    double x, y, vx, vy, phase;
    private int width = 100;
    private int height = 40;
    private boolean facingRight;


    EnemyFish(double x, double y, double vx, double vy) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.phase = Math.random() * Math.PI * 2;
        this.facingRight = vx > 0;
    }


    void update() {
        x += vx;
        y += vy;

        y += Math.sin(phase) * 0.3;
        phase += 0.1;
    }

    void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        Color enemyColor = LazyEyeConfig.getEnemyColor();


        FishRenderer.drawFish(g2d, (int)x, (int)y, width, height, enemyColor, facingRight);
    }

    public java.util.List<Rectangle> getBodyParts() {
        java.util.List<Rectangle> parts = new java.util.ArrayList<>();

        // Account for the 0.85 scale in FishRenderer
        double scale = 0.85;
        int scaledW = (int)(width * scale);
        int scaledH = (int)(height * scale);
        int offsetX = (width - scaledW) / 2;
        int offsetY = (height - scaledH) / 2;

        // Shrink hitboxes by 15% for more forgiving collision
        double shrink = 0.85;

        if (facingRight) {
            // Head (front right)
            int headX = (int) (x + offsetX + scaledW * 0.62);     // biraz daha sağa (öne doğru)
            int headY = (int) (y + offsetY + scaledH * 0.20);     // daha merkezli, yukarı-aşağı geniş
            int headW = (int) (scaledW * 0.32);                  // daha geniş kafa (shrink kaldirdim ki erken çarpsın)
            int headH = (int) (scaledH * 0.60);                  // daha uzun kafa
            parts.add(new Rectangle(headX, headY, headW, headH));

            // Body middle
            int bodyX = (int) (x + offsetX + scaledW * 0.35);
            int bodyY = (int) (y + offsetY + scaledH * 0.25);
            int bodyW = (int) (scaledW * 0.28 * shrink);
            int bodyH = (int) (scaledH * 0.50 * shrink);
            parts.add(new Rectangle(bodyX, bodyY, bodyW, bodyH));

            // Tail base (back left)
            int tailX = (int) (x + offsetX + scaledW * 0.16);
            int tailY = (int) (y + offsetY + scaledH * 0.38);
            int tailW = (int) (scaledW * 0.22 * shrink);
            int tailH = (int) (scaledH * 0.24 * shrink);
            parts.add(new Rectangle(tailX, tailY, tailW, tailH));
        } else {
            // Head (front left)
            int headX = (int) (x + offsetX + scaledW * 0.00);     // tam sola dayalı, daha öne!
            int headY = (int) (y + offsetY + scaledH * 0.20);     // dikeyde daha merkezli
            int headW = (int) (scaledW * 0.32);                  // daha geniş kafa
            int headH = (int) (scaledH * 0.60);                  // daha uzun kafa
            parts.add(new Rectangle(headX, headY, headW, headH));

            // Body middle
            int bodyX = (int) (x + offsetX + scaledW * 0.26);
            int bodyY = (int) (y + offsetY + scaledH * 0.25);
            int bodyW = (int) (scaledW * 0.28 * shrink);
            int bodyH = (int) (scaledH * 0.50 * shrink);
            parts.add(new Rectangle(bodyX, bodyY, bodyW, bodyH));

            // Tail base (back right)
            int tailX = (int) (x + offsetX + scaledW * 0.52);
            int tailY = (int) (y + offsetY + scaledH * 0.38);
            int tailW = (int) (scaledW * 0.22 * shrink);
            int tailH = (int) (scaledH * 0.24 * shrink);
            parts.add(new Rectangle(tailX, tailY, tailW, tailH));
        }

        return parts;
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


    public Player(int startX, int startY) {
        this.x = startX;
        this.y = startY;
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

        if (x < 0) { x = 0; velocityX = 0; }
        if (x > 600 - width) { x = 600 - width; velocityX = 0; }
        if (y < 0) { y = 0; velocityY = 0; }
        if (y > 600 - height) { y = 600 - height; velocityY = 0; }
    }

    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        Color playerColor = LazyEyeConfig.getPlayerColor();


        FishRenderer.drawFish(g2d, (int)x, (int)y, width, height, playerColor, facingRight);
    }
    public java.util.List<Rectangle> getBodyParts() {
        java.util.List<Rectangle> parts = new java.util.ArrayList<>();

        // Account for the 0.85 scale in FishRenderer
        double scale = 0.85;
        int scaledW = (int)(width * scale);
        int scaledH = (int)(height * scale);
        int offsetX = (width - scaledW) / 2;
        int offsetY = (height - scaledH) / 2;

        // Shrink hitboxes by 15% for more forgiving collision
        double shrink = 0.85;

        if (facingRight) {
            // Head (front right)
            int headX = (int) (x + offsetX + scaledW * 0.62);     // biraz daha sağa (öne doğru)
            int headY = (int) (y + offsetY + scaledH * 0.20);     // daha merkezli, yukarı-aşağı geniş
            int headW = (int) (scaledW * 0.32);                  // daha geniş kafa (shrink kaldirdim ki erken çarpsın)
            int headH = (int) (scaledH * 0.60);                  // daha uzun kafa
            parts.add(new Rectangle(headX, headY, headW, headH));

            // Body middle
            int bodyX = (int) (x + offsetX + scaledW * 0.35);
            int bodyY = (int) (y + offsetY + scaledH * 0.25);
            int bodyW = (int) (scaledW * 0.28 * shrink);
            int bodyH = (int) (scaledH * 0.50 * shrink);
            parts.add(new Rectangle(bodyX, bodyY, bodyW, bodyH));

            // Tail base (back left)
            int tailX = (int) (x + offsetX + scaledW * 0.16);
            int tailY = (int) (y + offsetY + scaledH * 0.38);
            int tailW = (int) (scaledW * 0.22 * shrink);
            int tailH = (int) (scaledH * 0.24 * shrink);
            parts.add(new Rectangle(tailX, tailY, tailW, tailH));
        } else {
            // Head (front left)
            int headX = (int) (x + offsetX + scaledW * 0.00);     // tam sola dayalı, daha öne!
            int headY = (int) (y + offsetY + scaledH * 0.20);     // dikeyde daha merkezli
            int headW = (int) (scaledW * 0.32);                  // daha geniş kafa
            int headH = (int) (scaledH * 0.60);                  // daha uzun kafa
            parts.add(new Rectangle(headX, headY, headW, headH));

            // Body middle
            int bodyX = (int) (x + offsetX + scaledW * 0.26);
            int bodyY = (int) (y + offsetY + scaledH * 0.25);
            int bodyW = (int) (scaledW * 0.28 * shrink);
            int bodyH = (int) (scaledH * 0.50 * shrink);
            parts.add(new Rectangle(bodyX, bodyY, bodyW, bodyH));

            // Tail base (back right)
            int tailX = (int) (x + offsetX + scaledW * 0.52);
            int tailY = (int) (y + offsetY + scaledH * 0.38);
            int tailW = (int) (scaledW * 0.22 * shrink);
            int tailH = (int) (scaledH * 0.24 * shrink);
            parts.add(new Rectangle(tailX, tailY, tailW, tailH));
        }

        return parts;
    }
}