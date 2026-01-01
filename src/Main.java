import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class Main {

    public static void main(String[] args) {

        // Safe on all platforms (ignored on Windows)
        System.setProperty("apple.awt.application.name", "AquaVision");

        SwingUtilities.invokeLater(() -> {

            JFrame frame = new JFrame("AquaVision");
            frame.setSize(600, 600);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            URL url = Main.class.getResource("/appLogo.png");
            if (url != null) {
                Image image = Toolkit.getDefaultToolkit().getImage(url);
                frame.setIconImage(image);

                // Works on Windows + macOS
                try {
                    if (Taskbar.isTaskbarSupported()) {
                        Taskbar.getTaskbar().setIconImage(image);
                    }
                } catch (Exception ignored) {}
            }

            SoundManager.init();
            DatabaseManager.initialize();
            DatabaseManager.loadUserSettings();

            StartPanel panel = new StartPanel();
            frame.add(panel);

            frame.setUndecorated(true);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
