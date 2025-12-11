import javax.swing.*;
import java.awt.*;

public class SettingPanel extends JPanel {

    public SettingPanel() {
        setBackground(new Color(189, 237, 255));
        setLayout(null);

        // ---- Close Icon 30x30 ----
        ImageIcon icon = new ImageIcon("close.png");
        Image scaled = icon.getImage().getScaledInstance(90, 60, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaled);

        JButton closeBtn = new JButton(scaledIcon);
        closeBtn.setBounds(290, 10, 60, 60);
        closeBtn.setBorderPainted(false);
        closeBtn.setContentAreaFilled(false);
        closeBtn.setFocusPainted(false);
        closeBtn.setOpaque(false);

        closeBtn.addActionListener(e -> {
            Window w = SwingUtilities.getWindowAncestor(this);
            if (w != null) w.dispose();
        });

        add(closeBtn);
    }
}