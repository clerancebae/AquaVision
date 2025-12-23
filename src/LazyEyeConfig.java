public class LazyEyeConfig {
    private static boolean isRightEyeSelected = false;

    public static void setRightEye(boolean selected) {
        isRightEyeSelected = selected;
    }

    public static boolean isRightEyeSelected() {
        return isRightEyeSelected;
    }

    public static String getPlayerImagePath() {
        if (isRightEyeSelected) return "enemy_fish.png";
        return "player_fish.png";
    }

    public static String getEnemyImagePath() {
        if (isRightEyeSelected) return "player_fish.png";
        return "enemy_fish.png";
    }
}