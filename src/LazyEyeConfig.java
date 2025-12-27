import java.awt.Color;
public class LazyEyeConfig {

    private static boolean isRightEyeSelected = false;

    // Kullanıcının seçtiği GERÇEK renkler
    private static Color basePlayerColor = new Color(0, 100, 255);
    private static Color baseEnemyColor  = new Color(200, 0, 0);

    public static void setRightEye(boolean selected) {
        isRightEyeSelected = selected;
    }

    // UI renk seçtiğinde burası çağrılır
    public static void setPlayerColor(Color c) {
        basePlayerColor = c;
    }

    public static void setEnemyColor(Color c) {
        baseEnemyColor = c;
    }

    // OYUNDA KULLANILAN renkler
    public static Color getPlayerColor() {
        return isRightEyeSelected ? baseEnemyColor : basePlayerColor;
    }

    public static Color getEnemyColor() {
        return isRightEyeSelected ? basePlayerColor : baseEnemyColor;
    }

    public static boolean isRightEyeSelected() {
        return isRightEyeSelected;
    }
}
