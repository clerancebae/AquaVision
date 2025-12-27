import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicSliderUI;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;

public class SettingPanel extends BasePanel {

    private JPanel playerColorBox;
    private JPanel enemyColorBox;
    private JPanel mainPreviewPanel;

    public SettingPanel() {
        setBackground(new Color(189, 237, 255));
        setLayout(null);
        setSize(400, 550);

        // --- Kapatma Butonu ---
        ImageIcon icon = new ImageIcon("close.png");
        Image scaled = (icon.getImage() != null) ? icon.getImage().getScaledInstance(60, 40, Image.SCALE_SMOOTH) : null;
        JButton closeBtn = new JButton((scaled != null) ? new ImageIcon(scaled) : null);
        if (scaled == null) closeBtn.setText("X");
        closeBtn.setBounds(320, 10, 60, 40);
        closeBtn.setBorderPainted(false); closeBtn.setContentAreaFilled(false); closeBtn.setFocusPainted(false); closeBtn.setOpaque(false);
        closeBtn.addActionListener(e -> { Window w = SwingUtilities.getWindowAncestor(this); if (w != null) w.dispose(); });
        add(closeBtn);

        // --- Başlık ---
        JLabel title = new JLabel("Game Settings");
        title.setBounds(30, 10, 200, 40);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        title.setForeground(new Color(0, 60, 120));
        add(title);

        // --- Ses Ayarı ---
        JLabel musicLabel = new JLabel("Music Volume");
        musicLabel.setBounds(30, 60, 150, 20);
        musicLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        musicLabel.setForeground(new Color(0, 60, 120));
        add(musicLabel);

        JSlider slider = new JSlider(0, 100, 50);
        slider.setBounds(30, 85, 300, 40);
        slider.setPaintTicks(false); slider.setPaintLabels(false);
        ImageIcon sliderIcon = new ImageIcon("level_active.png");
        slider.setUI(new SeaSliderUI(slider, (sliderIcon.getImage() != null) ? sliderIcon.getImage() : null));
        slider.setOpaque(false); slider.setBackground(new Color(189, 237, 255));
        slider.addChangeListener(e -> {
            float dB = -70f + 76f * (float) Math.pow(slider.getValue() / 100f, 0.42);
            SoundManager.setVolume(dB);
        });
        add(slider);

        // --- Göz Seçimi ---
        JLabel eyeTitle = new JLabel("Target Eye (Lazy Eye)");
        eyeTitle.setBounds(30, 130, 200, 20);
        eyeTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        eyeTitle.setForeground(new Color(0, 60, 120));
        add(eyeTitle);

        JRadioButton rbLeft = new JRadioButton("Left Eye");
        JRadioButton rbRight = new JRadioButton("Right Eye");
        rbLeft.setBounds(30, 155, 100, 30); rbRight.setBounds(140, 155, 100, 30);
        rbLeft.setOpaque(false); rbRight.setOpaque(false);
        rbLeft.setForeground(new Color(0, 60, 120)); rbRight.setForeground(new Color(0, 60, 120));
        ButtonGroup group = new ButtonGroup(); group.add(rbLeft); group.add(rbRight);

        if (LazyEyeConfig.isRightEyeSelected()) rbRight.setSelected(true); else rbLeft.setSelected(true);
        ActionListener eyeListener = e -> {
            LazyEyeConfig.setRightEye(rbRight.isSelected());

            DatabaseManager.saveUserSettings(
                    LazyEyeConfig.isRightEyeSelected(),
                    LazyEyeConfig.getPlayerColor(),
                    LazyEyeConfig.getEnemyColor()
            );

            updateColorBoxes();
            mainPreviewPanel.repaint();
        };
        rbLeft.addActionListener(eyeListener); rbRight.addActionListener(eyeListener);
        add(rbLeft); add(rbRight);

        // --- RENK AYARLAMA ---
        JLabel lblCalib = new JLabel("Color Calibration (Click box for Live Preview):");
        lblCalib.setBounds(30, 200, 350, 20);
        lblCalib.setFont(new Font("Arial", Font.BOLD, 14));
        lblCalib.setForeground(new Color(0, 60, 120));
        add(lblCalib);

        // Player Box
        JLabel lblPlayer = new JLabel("Player:");
        lblPlayer.setBounds(30, 230, 60, 30); add(lblPlayer);
        playerColorBox = new JPanel();
        playerColorBox.setBounds(80, 230, 60, 30);
        playerColorBox.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        playerColorBox.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        playerColorBox.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                openLiveColorDialog("Choose Player Color", true);
            }
        });
        add(playerColorBox);

        // Others Box
        JLabel lblEnemy = new JLabel("Others:");
        lblEnemy.setBounds(180, 230, 60, 30); add(lblEnemy);
        enemyColorBox = new JPanel();
        enemyColorBox.setBounds(230, 230, 60, 30);
        enemyColorBox.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        enemyColorBox.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        enemyColorBox.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                openLiveColorDialog("Choose Others Color", false);
            }
        });
        add(enemyColorBox);

        updateColorBoxes();

        JLabel lblPreview = new JLabel("Current Settings Preview:");
        lblPreview.setBounds(30, 280, 300, 20);
        lblPreview.setFont(new Font("Arial", Font.BOLD, 14));
        lblPreview.setForeground(new Color(0, 60, 120));
        add(lblPreview);

        mainPreviewPanel = new CreatePreviewPanel(80, 40);
        mainPreviewPanel.setBounds(30, 305, 320, 170);
        mainPreviewPanel.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        add(mainPreviewPanel);
    }

    private void updateColorBoxes() {
        playerColorBox.setBackground(LazyEyeConfig.getPlayerColor());
        enemyColorBox.setBackground(LazyEyeConfig.getEnemyColor());
    }

    private void openLiveColorDialog(String title, boolean isPlayer) {
        JDialog liveDialog = new JDialog((Dialog) SwingUtilities.getWindowAncestor(this), title, true);
        liveDialog.setLayout(new BorderLayout());
        liveDialog.setSize(850, 480);

        JColorChooser colorChooser = new JColorChooser(isPlayer ? LazyEyeConfig.getPlayerColor() : LazyEyeConfig.getEnemyColor());

        JPanel bigLivePreview = new CreatePreviewPanel(140, 70);
        bigLivePreview.setPreferredSize(new Dimension(300, 400));
        bigLivePreview.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.WHITE),
                "LIVE PREVIEW",
                2, 2, new Font("Arial",Font.BOLD,14), Color.WHITE));

        colorChooser.getSelectionModel().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                Color newColor = colorChooser.getColor();
                if (isPlayer) LazyEyeConfig.setPlayerColor(newColor);
                else LazyEyeConfig.setEnemyColor(newColor);
                bigLivePreview.repaint();
            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.DARK_GRAY);
        JButton okButton = new JButton("OK - Save Color");
        okButton.setFont(new Font("Arial", Font.BOLD, 14));
        okButton.addActionListener(e -> {
            updateColorBoxes();
            mainPreviewPanel.repaint();

            DatabaseManager.saveUserSettings(
                    LazyEyeConfig.isRightEyeSelected(),
                    LazyEyeConfig.getPlayerColor(),
                    LazyEyeConfig.getEnemyColor()
            );

            liveDialog.dispose();
        });

        buttonPanel.add(okButton);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, colorChooser, bigLivePreview);
        splitPane.setDividerLocation(550);

        liveDialog.add(splitPane, BorderLayout.CENTER);
        liveDialog.add(buttonPanel, BorderLayout.SOUTH);
        liveDialog.setLocationRelativeTo(this);
        liveDialog.setVisible(true);
    }

    private class CreatePreviewPanel extends JPanel {
        int fishW, fishH;
        public CreatePreviewPanel(int w, int h) { this.fishW = w; this.fishH = h; }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;

            int panelW = getWidth();
            int panelH = getHeight();

            g2d.setColor(new Color(8, 8, 8)); // Siyah Arka Plan
            g2d.fillRect(0, 0, panelW, panelH);

            int centerX = (panelW - fishW) / 2;

            // Player
            int playerY = (panelH / 4) - (fishH / 2);
            FishRenderer.drawFish(g2d, centerX, playerY, fishW, fishH, LazyEyeConfig.getPlayerColor(), true);

            g2d.setColor(Color.GRAY);
            g2d.setFont(new Font("Arial", Font.PLAIN, 12));
            FontMetrics fm = g2d.getFontMetrics();
            int labelX = centerX + (fishW - fm.stringWidth("PLAYER")) / 2;
            g2d.drawString("PLAYER", labelX, playerY + fishH + 20);

            // Çizgi
            g2d.setColor(Color.DARK_GRAY);
            g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0));
            g2d.drawLine(20, panelH / 2, panelW - 20, panelH / 2);

            // Enemy
            int enemyY = (panelH * 3 / 4) - (fishH / 2);
            FishRenderer.drawFish(g2d, centerX, enemyY, fishW, fishH, LazyEyeConfig.getEnemyColor(), false);

            g2d.setColor(Color.GRAY);
            labelX = centerX + (fishW - fm.stringWidth("OTHERS")) / 2;
            g2d.drawString("OTHERS", labelX, enemyY + fishH + 20);
        }
    }
}

class SeaSliderUI extends BasicSliderUI {
    private Image thumbImage; private static final int TRACK_HEIGHT = 24; private static final int TRACK_ARC = 24;
    public SeaSliderUI(JSlider b, Image image) { super(b); this.thumbImage = image; }
    @Override public void paintFocus(Graphics g) { }
    @Override public void paintTrack(Graphics g) {
        Graphics2D g2d = (Graphics2D) g; g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int trackY = trackRect.y + (trackRect.height - TRACK_HEIGHT) / 2; int w = trackRect.width; int h = TRACK_HEIGHT;
        g2d.setPaint(new GradientPaint(0, trackY, new Color(255, 235, 120), 0, trackY + h, new Color(210, 140, 30)));
        g2d.fillRoundRect(trackRect.x, trackY - 2, w, h + 4, TRACK_ARC + 4, TRACK_ARC + 4);
        g2d.setColor(new Color(0, 60, 120)); g2d.fillRoundRect(trackRect.x + 4, trackY + 2, w - 8, h - 4, TRACK_ARC, TRACK_ARC);
        int fillWidth = (int)((w - 8) * ((float)(slider.getValue() - slider.getMinimum()) / (slider.getMaximum() - slider.getMinimum())));
        if (fillWidth > w) fillWidth = w;
        g2d.setPaint(new GradientPaint(0, trackY, new Color(100, 240, 255), 0, trackY + h, new Color(0, 150, 220)));
        Shape oldClip = g2d.getClip(); g2d.setClip(new RoundRectangle2D.Float(trackRect.x + 4, trackY + 2, w - 8, h - 4, TRACK_ARC, TRACK_ARC));
        g2d.fillRect(trackRect.x + 4, trackY, fillWidth, h); g2d.setClip(oldClip);
    }
    @Override public void paintThumb(Graphics g) {
        Graphics2D g2d = (Graphics2D) g; g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (thumbImage != null) g2d.drawImage(thumbImage, thumbRect.x, thumbRect.y - 8, 45, 40, null);
        else { g2d.setColor(new Color(255, 200, 50)); g2d.fillOval(thumbRect.x, thumbRect.y, thumbRect.width, thumbRect.height); }
    }
    @Override protected Dimension getThumbSize() { return new Dimension(45, 40); }
}