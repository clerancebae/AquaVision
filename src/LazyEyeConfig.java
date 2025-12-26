public class LazyEyeConfig {
    // Default to false (Left Eye/Normal)
    private static boolean isRightEyeSelected = false;

    public static void setRightEye(boolean selected) {
        isRightEyeSelected = selected;
    }

    public static boolean isRightEyeSelected() {
        return isRightEyeSelected;
    }

    public static String getPlayerImagePath() {
        if (isRightEyeSelected) {
            // Right Eye Rule: Player becomes Red Happy
            return "dz_left_blue_fish.png";
        }
        // Left Eye Rule (Default): Player is Blue Happy
        return "dzleft_red_fish.png";
    }

    public static String getEnemyImagePath() {
        if (isRightEyeSelected) {
            // Right Eye Rule: Enemy becomes Blue Horrible
            return "dzleft_red_fish.png";
        }
        // Left Eye Rule (Default): Enemy is Red Horrible
        return "dz_left_blue_fish.png";
    }
}