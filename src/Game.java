import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class Game extends BasePanel {
    private int levelNumber;
    private Image backgroundImage;
    private Player player;
    private Timer gameTimer;
    private boolean isGameOver = false;
    private boolean isPaused = false;
    private JDialog pauseDialog;

    // Fish spawning system
    private final List<EnemyFish> enemyFishes = new ArrayList<>();
    private final Random rand = new Random();

    public Game(int levelNumber) {
        super();
        this.levelNumber = levelNumber;

        setLayout(null);
        setFocusable(true);
        requestFocusInWindow();

        // Load static game background
        loadBackground();

        // Initialize player
        player = new Player(300, 300);

        // Add a title label
        JLabel titleLabel = new JLabel("Level " + levelNumber);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 36));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBounds(200, 20, 200, 50);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(titleLabel);


        // Add a back/return button
        JButton backButton = new JButton("Back");
        backButton.setBounds(20, 20, 100, 40);
        backButton.setFont(new Font("Arial", Font.BOLD, 16));
        backButton.setFocusPainted(false);

        backButton.addActionListener(e -> {
            if (gameTimer != null) {
                gameTimer.stop();
            }
            JFrame gameFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            if (gameFrame != null) {
                gameFrame.dispose();
            }
        });
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

        // Start fish spawning with Gaussian timing
        scheduleNextSpawn();
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
        title.setForeground(Color.WHITE);

        JButton resume = new JButton("Resume");
        JButton restart = new JButton("Restart");
        JButton returnTolobby =  new JButton("Return to Lobby");
        JButton exit = new JButton("Exit");


        panel.add(title);
        panel.add(resume);
        panel.add(restart);
        panel.add(returnTolobby);
        panel.add(exit);

        resume.addActionListener(e -> resumeGame());
        restart.addActionListener(e -> restartGame());
        returnTolobby.addActionListener(e -> returnTolobby());
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

    private void restartGame() {
        pauseDialog.dispose();
        enemyFishes.clear();
        player = new Player(300, 300);
        isPaused = false;
        isGameOver = false;
        gameTimer.start();
        requestFocusInWindow();
    }
    private void returnTolobby(){
        pauseDialog.dispose();
        if (gameTimer != null) {
            gameTimer.stop();
        }
        JFrame gameFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        if (gameFrame != null) {
            gameFrame.dispose();
        }
        requestFocusInWindow();
    }

    private void scheduleNextSpawn() {
        // Gaussian distribution for spawn timing (avg 800ms, variation ±300ms)
        int delay = (int) clamp(
                rand.nextGaussian() * 300 + 800,
                300,
                2000
        );

        Timer spawnTimer = new Timer(delay, e -> {
            spawnEnemyFish();
            ((Timer) e.getSource()).stop();
            scheduleNextSpawn(); // Schedule next spawn
        });
        spawnTimer.start();
    }

    private void spawnEnemyFish() {
        boolean fromLeft = rand.nextBoolean();

        double y;
        if (rand.nextDouble() < 0.7) {
            // 70%: Center-weighted spawn (Gaussian distribution)
            y = rand.nextGaussian() * 80 + 300; // Center around middle (300)
        } else {
            // 30%: Top/bottom threat (Uniform distribution)
            y = rand.nextDouble() * 600;
        }

        // Keep fish within reasonable bounds
        y = clamp(y, 40, 560);

        // Random speed
        double speed = 1.5 + rand.nextDouble() * 2.5;

        // Starting position and direction
        double x = fromLeft ? -50 : 650;
        double vx = fromLeft ? speed : -speed;

        enemyFishes.add(new EnemyFish(x, y, vx));
    }

    private void updateGame() {
        player.update();

        // Update enemy fish
        Iterator<EnemyFish> it = enemyFishes.iterator();
        while (it.hasNext()) {
            EnemyFish fish = it.next();
            fish.update();

            // Remove fish that went off-screen
            if (fish.x < -100 || fish.x > 700) {
                it.remove();
            }
        }
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(value, max));
    }

    private void loadBackground() {
        try {
            ImageIcon bgIcon = new ImageIcon("gameBackground.jpeg");
            if (bgIcon.getImage() != null && bgIcon.getIconWidth() > 0) {
                backgroundImage = bgIcon.getImage();
                System.out.println("Game background loaded successfully!");
            } else {
                System.out.println("Game background not found, using gradient.");
                backgroundImage = null;
            }
        } catch (Exception e) {
            System.out.println("Error loading game background: " + e.getMessage());
            backgroundImage = null;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw background image if available
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            // Draw a gradient background as fallback
            Graphics2D g2d = (Graphics2D) g;
            GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(135, 206, 250),
                    0, getHeight(), new Color(70, 130, 180)
            );
            g2d.setPaint(gradient);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }

        // Draw enemy fish
        for (EnemyFish fish : enemyFishes) {
            fish.draw(g);
        }

        // Draw player (on top)
        player.draw(g);
    }
}

// Enemy Fish class
class EnemyFish {
    double x, y;
    double vx;
    double phase;
    private int size = 30;
    private Image fishImage;
    private boolean facingRight;

    EnemyFish(double x, double y, double vx) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.phase = Math.random() * Math.PI * 2;
        this.facingRight = vx > 0;
        loadFishImage();
    }

    private void loadFishImage() {
        try {
            ImageIcon icon = new ImageIcon("enemy_fish.png");
            if (icon.getImage() != null && icon.getIconWidth() > 0) {
                fishImage = icon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
            } else {
                fishImage = null;
            }
        } catch (Exception e) {
            fishImage = null;
        }
    }

    void update() {
        x += vx;

        // Slight sine wave movement for natural swimming
        y += Math.sin(phase) * 0.5;
        phase += 0.1;
    }

    void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (fishImage != null) {
            // Draw fish image with proper direction
            if (facingRight) {
                g2d.drawImage(fishImage, (int)x, (int)y, size, size, null);
            } else {
                // Flip horizontally for left-facing fish
                g2d.drawImage(fishImage, (int)x + size, (int)y, -size, size, null);
            }
        } else {
            // Fallback: simple fish shape
            drawSimpleFish(g2d);
        }
    }

    private void drawSimpleFish(Graphics2D g2d) {
        // Choose color based on size (smaller = lighter)
        g2d.setColor(new Color(100, 180, 255));

        if (facingRight) {
            g2d.fillOval((int)x, (int)y, size - 5, size - 10);
            // Tail
            int[] tailX = {(int)x, (int)x - 8, (int)x};
            int[] tailY = {(int)y + 5, (int)y + (size-10)/2, (int)y + size - 15};
            g2d.fillPolygon(tailX, tailY, 3);
            // Eye
            g2d.setColor(Color.WHITE);
            g2d.fillOval((int)x + size - 15, (int)y + 5, 4, 4);
            g2d.setColor(Color.BLACK);
            g2d.fillOval((int)x + size - 14, (int)y + 6, 2, 2);
        } else {
            g2d.fillOval((int)x + 5, (int)y, size - 5, size - 10);
            // Tail
            int[] tailX = {(int)x + size, (int)x + size + 8, (int)x + size};
            int[] tailY = {(int)y + 5, (int)y + (size-10)/2, (int)y + size - 15};
            g2d.fillPolygon(tailX, tailY, 3);
            // Eye
            g2d.setColor(Color.WHITE);
            g2d.fillOval((int)x + 10, (int)y + 5, 4, 4);
            g2d.setColor(Color.BLACK);
            g2d.fillOval((int)x + 11, (int)y + 6, 2, 2);
        }
    }
}

// Player class - represents the fish
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
            ImageIcon icon = new ImageIcon("player_fish.png");
            if (icon.getImage() != null && icon.getIconWidth() > 0) {
                fishImage = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                System.out.println("Player fish image loaded!");
            } else {
                fishImage = null;
                System.out.println("Player fish image not found, using default shape.");
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
        // Apply acceleration based on key presses
        if (upPressed) velocityY -= acceleration;
        if (downPressed) velocityY += acceleration;
        if (leftPressed) {
            velocityX -= acceleration;
            facingRight = true;
        }
        if (rightPressed) {
            velocityX += acceleration;
            facingRight = false;
        }

        // Limit speed
        double currentSpeed = Math.sqrt(velocityX * velocityX + velocityY * velocityY);
        if (currentSpeed > speed) {
            velocityX = (velocityX / currentSpeed) * speed;
            velocityY = (velocityY / currentSpeed) * speed;
        }

        // Apply friction
        velocityX *= friction;
        velocityY *= friction;

        // Update position
        x += velocityX;
        y += velocityY;

        // Keep player within bounds
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

    private void drawSimpleFish(Graphics2D g2d) {
        // Body (oval)
        g2d.setColor(new Color(255, 140, 0)); // Orange
        if (facingRight) {
            g2d.fillOval((int)x, (int)y, width - 10, height);
            // Tail
            int[] tailX = {(int)x, (int)x - 15, (int)x};
            int[] tailY = {(int)y + 10, (int)y + height/2, (int)y + height - 10};
            g2d.fillPolygon(tailX, tailY, 3);
            // Eye
            g2d.setColor(Color.WHITE);
            g2d.fillOval((int)x + width - 25, (int)y + 10, 8, 8);
            g2d.setColor(Color.BLACK);
            g2d.fillOval((int)x + width - 23, (int)y + 12, 4, 4);
        } else {
            g2d.fillOval((int)x + 10, (int)y, width - 10, height);
            // Tail
            int[] tailX = {(int)x + width, (int)x + width + 15, (int)x + width};
            int[] tailY = {(int)y + 10, (int)y + height/2, (int)y + height - 10};
            g2d.fillPolygon(tailX, tailY, 3);
            // Eye
            g2d.setColor(Color.WHITE);
            g2d.fillOval((int)x + 15, (int)y + 10, 8, 8);
            g2d.setColor(Color.BLACK);
            g2d.fillOval((int)x + 17, (int)y + 12, 4, 4);
        }
    }

    public int getX() { return (int)x; }
    public int getY() { return (int)y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (fishImage != null) {
            // Draw fish image (flip horizontally if facing left)
            if (facingRight) {
                g2d.drawImage(fishImage, (int)x, (int)y, width, height, null);
            } else {
                // Flip the image horizontally
                g2d.drawImage(fishImage, (int)x + width, (int)y, -width, height, null);
            }
        } else {
            // Draw a simple fish shape as fallback
            drawSimpleFish(g2d);
        }
    }
}


