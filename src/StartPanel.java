import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

public class StartPanel extends BasePanel {
    private final Image scaledBackground;

    public StartPanel() {
        super();

        setLayout(null);

        // Background
        ImageIcon backgroundImg = new ImageIcon("background2.png");
        Image img = backgroundImg.getImage();
        scaledBackground = img.getScaledInstance(600, 600, Image.SCALE_SMOOTH);

        // --- CHANGED TO ROUNDEDBUTTON ---
        RoundedButton startBtn = new RoundedButton("Start");
        startBtn.setBounds(230, 230, 160, 60);
        add(startBtn);

        startBtn.addActionListener(e -> {
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
            MissionPanel missionPanel = new MissionPanel(this);
            frame.getContentPane().removeAll();
            frame.getContentPane().add(missionPanel);
            frame.revalidate();
            frame.repaint();
            missionPanel.requestFocusInWindow();
        });

        // --- CHANGED TO ROUNDEDBUTTON ---
        RoundedButton settingBtn = new RoundedButton("Settings");
        settingBtn.setBounds(230, 315, 160, 60);
        add(settingBtn);

        settingBtn.addActionListener(e -> {
            JDialog dialog = new JDialog(
                    (JFrame) SwingUtilities.getWindowAncestor(this),
                    "",
                    true
            );
            dialog.setSize(400, 550);
            dialog.setUndecorated(true);
            dialog.setLocationRelativeTo(null);
            dialog.setContentPane(new SettingPanel());
            dialog.setVisible(true);
        });

        // --- CHANGED TO ROUNDEDBUTTON ---
        RoundedButton exitBtn = new RoundedButton("Exit");
        exitBtn.setBounds(230, 400, 160, 60);
        add(exitBtn);

        exitBtn.addActionListener(e -> System.exit(0));

        setPreferredSize(new Dimension(600, 600));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (scaledBackground != null) {
            g.drawImage(scaledBackground, 0, 0, this);
        }
    }
}


// -------------------------------
// ROUND  BUTTON CLASS
// -------------------------------

class RoundedButton extends JButton {

    // Colors
    private Color colorTop = new Color(245, 235, 100);    // Light Yellow
    private Color colorBottom = new Color(210, 180, 20);  // Darker Gold
    private Color borderColor = new Color(100, 80, 0);    // Dark Olive/Brown Outline

    private boolean isHovered = false;
    private boolean isPressed = false;

    // ARC SIZE: Controls how round the corners are.
    // Higher number = rounder. 30 is a good medium curve.
    private int arcWidth = 30;
    private int arcHeight = 30;

    public RoundedButton(String text) {
        super(text);

        // Basic setup
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);

        // Font styling
        setFont(new Font("Comic Sans MS", Font.BOLD, 28));
        setForeground(Color.BLACK);

        // Mouse listeners
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                isPressed = false;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                isPressed = true;
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isPressed = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();

        // 1. Enable Antialiasing
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // 2. Determine Colors
        Color top = colorTop;
        Color bottom = colorBottom;

        if (isPressed) {
            top = colorTop.darker();
            bottom = colorBottom.darker();
        } else if (isHovered) {
            top = colorTop.brighter();
            bottom = colorBottom.brighter();
        }

        // 3. Create Gradient Paint
        GradientPaint gp = new GradientPaint(0, 0, top, 0, height, bottom);
        g2.setPaint(gp);

        // 4. Fill Round Rectangle (CHANGED FROM OVAL)
        g2.fillRoundRect(0, 0, width, height, arcWidth, arcHeight);

        // 5. Draw Border
        g2.setColor(borderColor);
        g2.setStroke(new BasicStroke(3f));
        // Draw slightly inside to prevent clipping
        g2.drawRoundRect(1, 1, width - 3, height - 3, arcWidth, arcHeight);

        g2.dispose();

        // 6. Draw Text
        super.paintComponent(g);
    }

    // Ensure clicks only register inside the rounded shape
    @Override
    public boolean contains(int x, int y) {
        if (shape == null || !shape.getBounds().equals(getBounds())) {
            shape = new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), arcWidth, arcHeight);
        }
        return shape.contains(x, y);
    }

    private Shape shape;
}