import java.awt.*;
import java.sql.*;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:progress.db";

    public static void initialize() {
        String sql = """
            CREATE TABLE IF NOT EXISTS mission_progress (
                mission INTEGER PRIMARY KEY,
                total_attempts INTEGER DEFAULT 0,
                successful_completions INTEGER DEFAULT 0,
                highest_phase_reached INTEGER DEFAULT 0,
                last_updated TEXT DEFAULT CURRENT_TIMESTAMP
            );
            """;

        String settingsSql = """
            CREATE TABLE IF NOT EXISTS user_settings (
                id INTEGER PRIMARY KEY CHECK (id = 1),
                lazy_eye_right INTEGER DEFAULT 0,
                player_color INTEGER NOT NULL,
                enemy_color INTEGER NOT NULL,
                last_updated TEXT DEFAULT CURRENT_TIMESTAMP
            );
            """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            stmt.execute(sql);
            stmt.execute(settingsSql);
            System.out.println("DB ready!");

        } catch (SQLException e) {
            System.err.println("DB Start Error: " + e.getMessage());
        }
    }

    // ================= MISSION PROGRESS =================

    public static void incrementAttempt(int mission) {
        String sql = """
            INSERT INTO mission_progress (mission, total_attempts, highest_phase_reached)
            VALUES (?, 1, 0)
            ON CONFLICT(mission) DO UPDATE SET
                total_attempts = total_attempts + 1,
                last_updated = CURRENT_TIMESTAMP
            """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, mission);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Attempt Increment Error: " + e.getMessage());
        }
    }

    public static void updateHighestPhase(int mission, int phase) {
        String sql = """
            INSERT INTO mission_progress (mission, highest_phase_reached)
            VALUES (?, ?)
            ON CONFLICT(mission) DO UPDATE SET
                highest_phase_reached = MAX(highest_phase_reached, excluded.highest_phase_reached),
                last_updated = CURRENT_TIMESTAMP
            """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, mission);
            pstmt.setInt(2, phase + 1);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Phase Update Error: " + e.getMessage());
        }
    }

    public static void incrementCompletion(int mission) {
        String sql = """
            UPDATE mission_progress
            SET successful_completions = successful_completions + 1,
                last_updated = CURRENT_TIMESTAMP
            WHERE mission = ?
            """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, mission);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Incremention Error: " + e.getMessage());
        }
    }

    public static String getProgressReport(int mission) {
        String sql = "SELECT * FROM mission_progress WHERE mission = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, mission);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int attempts = rs.getInt("total_attempts");
                int completions = rs.getInt("successful_completions");
                int highest = rs.getInt("highest_phase_reached");

                double successRate = attempts > 0
                        ? (completions * 100.0 / attempts)
                        : 0;

                return String.format(
                        "Attempts: %d | Succesful: %d | Success Rate: %.1f%% | Highest Phase: %d/15",
                        attempts, completions, successRate, highest
                );
            }
        } catch (SQLException e) {
            System.err.println("Document error: " + e.getMessage());
        }
        return "Not played yet";
    }

    // ================= USER SETTINGS =================

    public static void saveUserSettings(
            boolean isRightEye,
            Color playerColor,
            Color enemyColor
    ) {
        String sql = """
            INSERT INTO user_settings (id, lazy_eye_right, player_color, enemy_color)
            VALUES (1, ?, ?, ?)
            ON CONFLICT(id) DO UPDATE SET
                lazy_eye_right = excluded.lazy_eye_right,
                player_color = excluded.player_color,
                enemy_color = excluded.enemy_color,
                last_updated = CURRENT_TIMESTAMP
            """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, isRightEye ? 1 : 0);
            pstmt.setInt(2, playerColor.getRGB());
            pstmt.setInt(3, enemyColor.getRGB());
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Settings save error: " + e.getMessage());
        }
    }

    public static void loadUserSettings() {
        String sql = "SELECT * FROM user_settings WHERE id = 1";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                boolean isRightEye = rs.getInt("lazy_eye_right") == 1;
                Color playerColor = new Color(rs.getInt("player_color"), true);
                Color enemyColor  = new Color(rs.getInt("enemy_color"), true);

                LazyEyeConfig.setRightEye(isRightEye);
                LazyEyeConfig.setPlayerColor(playerColor);
                LazyEyeConfig.setEnemyColor(enemyColor);
            }

        } catch (SQLException e) {
            System.err.println("Settings yükleme hatası: " + e.getMessage());
        }
    }
}
