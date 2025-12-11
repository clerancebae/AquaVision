import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.awt.Taskbar; // <--- 1. Add this import

void main() {
    JFrame frame = new JFrame();
    frame.setSize(600, 600);
    URL url = getClass().getResource("/appLogo.png");

    if (url == null) {
        System.out.println("Error: Image not found!");
    } else {
        System.out.println("Success: Found image at " + url);
        Image image = Toolkit.getDefaultToolkit().getImage(url);

        // 2. This sets the icon for the Window (and Cmd+Tab switcher)
        frame.setIconImage(image);

        // 3. THIS is the specific code to change the Mac Dock Icon
        try {
            // Check if the system supports changing the Taskbar/Dock icon
            if (Taskbar.isTaskbarSupported()) {
                Taskbar.getTaskbar().setIconImage(image);
            }
        } catch (UnsupportedOperationException e) {
            System.out.println("The os does not support: 'taskbar.setIconImage'");
        } catch (SecurityException e) {
            System.out.println("There was a security exception for: 'taskbar.setIconImage'");
        }
    }

    StartPanel panel = new StartPanel();
    frame.add(panel);
    frame.setUndecorated(true);
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
}