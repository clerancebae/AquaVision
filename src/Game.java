import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class Game extends BasePanel {
    private int levelNumber;
    private Image backgroundImage;
    private Player player;
    private Timer gameTimer;

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
                player.keyPressed(e);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                player.keyReleased(e);
            }
        });

        // Start game loop
        gameTimer = new Timer(16, e -> {
            player.update();
            repaint();
        });
        gameTimer.start();
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

        // Draw player
        player.draw(g);
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

    private void drawSimpleFish(Graphics2D g2d) {
        // Body (oval)
        g2d.setColor(new Color(255, 140, 0)); // Orange
        if (facingRight) {
            g2d.fillOval((int)x, (int)y, width - 10, height);
            // Tail
            int[] tailX = {(int)x, (int)x - 15, (int)x};
            int[] tailY = {(int)y + 30, (int)y + height/2, (int)y + height - 10};
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
}