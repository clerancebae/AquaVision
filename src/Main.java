import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.awt.Taskbar;

public class Main {

    public static void main(String[] args) {

        JFrame frame = new JFrame();
        frame.setSize(600, 600);

        URL url = Main.class.getResource("/appLogo.png");

        if (url == null) {
            System.out.println("Error: Image not found! Check path and Resources Root.");
        } else {
            System.out.println("Success: Found image at " + url);
            Image image = Toolkit.getDefaultToolkit().getImage(url);

            frame.setIconImage(image);

            // macOS Dock icon
            try {
                if (Taskbar.isTaskbarSupported()) {
                    Taskbar.getTaskbar().setIconImage(image);
                }
            } catch (Exception e) {
                System.out.println("Could not set dock/taskbar icon: " + e.getMessage());
            }
        }

        SoundManager.init();
        DatabaseManager.initialize();
        DatabaseManager.loadUserSettings();

        StartPanel panel = new StartPanel();
        frame.add(panel);
        frame.setUndecorated(true);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
