import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;

public class StartPanel extends BasePanel {
    private Image scaledBackground;

    public StartPanel() {
        super();

        setLayout(null);

        // Background
        ImageIcon backgroundImg = new ImageIcon("background.png");
        Image img = backgroundImg.getImage();
        scaledBackground = img.getScaledInstance(600, 600, Image.SCALE_SMOOTH);

        // Custom round start button
        RoundButton startBtn = new RoundButton("Start");
        startBtn.setBounds(200, 200, 200, 60);
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
        RoundButton settingBtn= new RoundButton("Settings");
        settingBtn.setBounds(200,285,200,60);
        add(settingBtn);
        settingBtn.addActionListener(e -> {
            JDialog dialog = new JDialog(
                    (JFrame) SwingUtilities.getWindowAncestor(this),
                    "",
                    true
            );

            dialog.setSize(350, 350);
            dialog.setUndecorated(true);
            dialog.setLocationRelativeTo(null);

            dialog.setContentPane(new SettingPanel());
            dialog.setVisible(true);
        });
        // Custom round exit button
        RoundButton exitBtn = new RoundButton("Exit");
        exitBtn.setBounds(200, 370, 200, 60);
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
class RoundButton extends JButton {

    public RoundButton(String text) {
        super(text);

        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setFont(new Font("Comic Sans MS", Font.BOLD, 26));
        setForeground(Color.BLACK);  // text color
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();

        // Smooth edges
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        // Semi-transparent yellow circle (alpha = 150)
        g2.setColor(new Color(255, 230, 0, 150));
        g2.fillOval(0, 0, getWidth(), getHeight());

        // Outline (almost opaque)
        g2.setColor(new Color(180, 140, 0, 220));
        g2.setStroke(new BasicStroke(4));
        g2.drawOval(0, 0, getWidth(), getHeight());

        // Draw text centered
        super.paintComponent(g);

        g2.dispose();
    }

    @Override
    public boolean contains(int x, int y) {
        Ellipse2D circle = new Ellipse2D.Float(0, 0, getWidth(), getHeight());
        return circle.contains(x, y);
    }
}
