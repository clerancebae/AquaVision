// DatabaseManager.java
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

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Veritabanı hazır!");
        } catch (SQLException e) {
            System.err.println("DB başlatma hatası: " + e.getMessage());
        }
    }

    // Yeni deneme başladığında
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
            System.err.println("Attempt artırma hatası: " + e.getMessage());
        }
    }

    // En yüksek phase güncelle
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
            pstmt.setInt(2, phase + 1); // phase 0'dan başlıyor
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Phase güncelleme hatası: " + e.getMessage());
        }
    }

    // Mission tamamlandığında
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
            System.err.println("Tamamlanma artırma hatası: " + e.getMessage());
        }
    }

    // İlerleme raporu (mission seçim ekranında kullanabilirsin)
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

                double successRate = attempts > 0 ? (completions * 100.0 / attempts) : 0;

                return String.format(
                        "Deneme: %d | Başarılı: %d | Başarı Oranı: %.1f%% | En Yüksek Phase: %d/15",
                        attempts, completions, successRate, highest
                );
            }
        } catch (SQLException e) {
            System.err.println("Rapor hatası: " + e.getMessage());
        }
        return "Henüz oynanmadı";
    }
}