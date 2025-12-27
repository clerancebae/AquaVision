import java.awt.Color;

public class LazyEyeConfig {

    private static boolean isRightEyeSelected = false;

    // Varsayılan Renkler (Saf RGB)
    private static Color playerColor = new Color(0, 100, 255); // Mavi tonu
    private static Color enemyColor = new Color(200, 0, 0);    // Kırmızı tonu

    public static void setRightEye(boolean selected) {
        isRightEyeSelected = selected;

        if (selected) {
            // Sağ göz tembel
            playerColor = new Color(200, 0, 0);
            enemyColor = new Color(0, 100, 255);
        } else {
            // Sol göz tembel
            playerColor = new Color(0, 100, 255);
            enemyColor = new Color(200, 0, 0);
        }
    }

    public static void setPlayerColor(Color c) {
        playerColor = c;
    }

    public static void setEnemyColor(Color c) {
        enemyColor = c;
    }

    public static Color getPlayerColor() {
        return playerColor;
    }

    public static Color getEnemyColor() {
        return enemyColor;
    }

    public static boolean isRightEyeSelected() {
        return isRightEyeSelected;
    }
}