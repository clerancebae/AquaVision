import java.awt.*;

public class FishColorConfig {

    // Player color (can be changed from UI / menu)
    private static Color playerColor = new Color(255, 140, 0);

    // Enemy color (can scale later per mission)
    private static Color enemyColor = new Color(100, 180, 255);

    public static Color getPlayerColor() {
        return playerColor;
    }

    public static void setPlayerColor(Color color) {
        playerColor = color;
    }

    public static Color getEnemyColor() {
        return enemyColor;
    }

    public static void setEnemyColor(Color color) {
        enemyColor = color;
    }
}
