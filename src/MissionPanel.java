import javax.swing.*;
import java.awt.*;
import java.awt.image.ImageFilter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MissionPanel extends BasePanel {
    private JPanel strtPanel;

    private static final int BUTTON_SIZE = 60;
    private static final int FONT_SIZE = 20;

    private final int[][] levelPositions = {
            {170, 520}, // Level 1
            {370, 340}, // Level 2
            {170, 220}, // Level 3
            {350, 100}, // Level 4
            {260, 20},  // Level 5
    };

    private List<JButton> levelButtons = new ArrayList<>();
    private Image backgroundImage;
    private ImageIcon shellIcon;
    private ImageIcon lockedIcon;

    // Track unlocked levels (can be loaded from save file later)
    private int maxUnlockedLevel = 1;

    public MissionPanel(JPanel strtPanel) {
        this.setPreferredSize(new Dimension(600, 600));
        this.setLayout(null);
        this.strtPanel = strtPanel;

        loadImages();
        initButtons();
    }

    private void loadImages() {
        System.out.println("--- Loading Images ---");

        // Background
        backgroundImage = safeLoadImage("MissionBackground.png");

        // Active icon
        Image imgActive = safeLoadImage("level_active.png");
        if (imgActive != null) {
            Image scaled = imgActive.getScaledInstance(BUTTON_SIZE, BUTTON_SIZE, Image.SCALE_SMOOTH);
            shellIcon = new ImageIcon(scaled);
            System.out.println("SUCCESS: level_active.png loaded.");
        } else {
            System.err.println("ERROR: level_active.png not found!");
            shellIcon = null;
        }

        // Locked icon
        Image imgLocked = safeLoadImage("level_locked.png");
        if (imgLocked != null) {
            Image scaled = imgLocked.getScaledInstance(BUTTON_SIZE, BUTTON_SIZE, Image.SCALE_SMOOTH);
            lockedIcon = new ImageIcon(scaled);
            System.out.println("SUCCESS: level_locked.png loaded.");
        } else {
            if (shellIcon != null) {
                System.out.println("INFO: level_locked.png not found, using grayscale.");
                lockedIcon = createGrayIcon(shellIcon);
            } else {
                lockedIcon = null;
            }
        }
    }

    private Image safeLoadImage(String fileName) {
        try {
            URL url = getClass().getResource("/" + fileName);
            if (url != null) {
                return new ImageIcon(url).getImage();
            }
            return new ImageIcon(fileName).getImage();
        } catch (Exception e) {
            return null;
        }
    }

    private ImageIcon createGrayIcon(ImageIcon icon) {
        Image img = icon.getImage();
        ImageFilter filter = new GrayFilter(true, 50);
        java.awt.image.ImageProducer producer = new java.awt.image.FilteredImageSource(img.getSource(), filter);
        Image grayImg = Toolkit.getDefaultToolkit().createImage(producer);
        return new ImageIcon(grayImg);
    }

    private void initButtons() {
        // Return button
        JButton returnButton = new JButton();
        returnButton.setBounds(10, 10, 50, 50);

        Image img = safeLoadImage("return.png");
        if (img != null) {
            Image scaled = img.getScaledInstance(100, 50, Image.SCALE_SMOOTH);
            returnButton.setIcon(new ImageIcon(scaled));
        }

        returnButton.setContentAreaFilled(false);
        returnButton.setBorderPainted(false);
        returnButton.setFocusPainted(false);

        returnButton.addActionListener(e -> {
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
            frame.getContentPane().removeAll();
            frame.getContentPane().add(strtPanel);
            frame.revalidate();
            frame.repaint();
            strtPanel.requestFocusInWindow();
        });

        this.add(returnButton);

        // Level buttons
        for (int i = 0; i < levelPositions.length; i++) {
            JButton btn = new JButton();
            final int levelNum = i + 1;
            btn.setBounds(levelPositions[i][0], levelPositions[i][1], BUTTON_SIZE, BUTTON_SIZE);

            btn.setHorizontalTextPosition(JButton.CENTER);
            btn.setVerticalTextPosition(JButton.CENTER);
            btn.setFont(new Font("Arial", Font.BOLD, FONT_SIZE));
            btn.setForeground(Color.WHITE);
            btn.setOpaque(false);
            btn.setContentAreaFilled(false);
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);

            if (levelNum <= maxUnlockedLevel) {
                // Unlocked level
                if (shellIcon != null) {
                    btn.setIcon(shellIcon);
                } else {
                    btn.setBackground(Color.GREEN);
                    btn.setOpaque(true);
                }

                btn.setText(String.valueOf(levelNum));
                btn.setEnabled(true);

                btn.addActionListener(e -> {
                    System.out.println("Mission " + levelNum + " clicked!");
                    openGameFrame(levelNum);
                });

            } else {
                // Locked level
                if (lockedIcon != null) {
                    btn.setIcon(lockedIcon);
                    btn.setDisabledIcon(lockedIcon);
                } else {
                    btn.setBackground(Color.GRAY);
                    btn.setOpaque(true);
                }

                btn.setText("");
                btn.setEnabled(false);
            }

            this.add(btn);
            levelButtons.add(btn);
        }
    }

    private void openGameFrame(int levelNum) {
        System.out.println("Opening game frame for Mission " + levelNum);

        JFrame gameFrame = new JFrame("Mission " + levelNum);
        gameFrame.setSize(600, 600);
        gameFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        gameFrame.setLocationRelativeTo(null);
        gameFrame.setUndecorated(true);

        Game gamePanel = new Game(levelNum);

        // Set completion listener to receive mission results
        gamePanel.setCompletionListener(new Game.MissionCompletionListener() {
            @Override
            public void onMissionCompleted(int level, List<PhaseData> phaseData) {
                System.out.println("✅ Mission " + level + " COMPLETED!");
                System.out.println("=== Phase Performance Data ===");

                long totalTime = 0;
                for (PhaseData data : phaseData) {
                    System.out.println(data.toString());
                    totalTime += data.survivedDuration;
                }

                System.out.println("Total Mission Time: " + (totalTime / 1000.0) + "s");
                System.out.println("==============================");

                // Unlock next level
                if (level < 5) {
                    unlockLevel(level + 1);
                }

                gameFrame.dispose();
            }

            @Override
            public void onMissionFailed(int level, List<PhaseData> phaseData) {
                System.out.println("❌ Mission " + level + " FAILED");
                System.out.println("=== Phase Performance Data (Partial) ===");

                for (PhaseData data : phaseData) {
                    System.out.println(data.toString());
                }

                System.out.println("=======================================");
                gameFrame.dispose();
            }
        });

        gameFrame.add(gamePanel);
        gameFrame.setVisible(true);
        gamePanel.requestFocusInWindow();

        System.out.println("Game frame created and shown!");
    }

    private void unlockLevel(int level) {
        if (level > maxUnlockedLevel && level <= 5) {
            maxUnlockedLevel = level;
            System.out.println("🔓 Mission " + level + " UNLOCKED!");

            // Update button visuals
            JButton btn = levelButtons.get(level - 1);

            if (shellIcon != null) {
                btn.setIcon(shellIcon);
            } else {
                btn.setBackground(Color.GREEN);
                btn.setOpaque(true);
            }

            btn.setText(String.valueOf(level));
            btn.setEnabled(true);

            final int levelNum = level;
            btn.addActionListener(e -> openGameFrame(levelNum));

            repaint();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, this.getWidth(), this.getHeight(), this);
        } else {
            g.setColor(new Color(100, 200, 255));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}