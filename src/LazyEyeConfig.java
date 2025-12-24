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
            return "red_happy_fish.png";
        }
        // Left Eye Rule (Default): Player is Blue Happy
        return "blue_happy_fish.png";
    }

    public static String getEnemyImagePath() {
        if (isRightEyeSelected) {
            // Right Eye Rule: Enemy becomes Blue Horrible
            return "blue_horror_fish.png";
        }
        // Left Eye Rule (Default): Enemy is Red Horrible
        return "red_horror_fish.png";
    }
}