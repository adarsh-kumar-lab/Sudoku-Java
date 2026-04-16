
package sudokuvalidator;

import java.sql.*;
import javax.swing.JOptionPane;

/**
 * Singleton JDBC connection manager for sudoku_db.
 * Handles all database operations:
 *   - Table creation (auto-setup on first run)
 *   - Saving game sessions
 *   - Saving validation results
 *   - Fetching leaderboard
 *   - Fetching validation history
 *
 * Group 1 | B.Tech CSE Sec-E | Centurion University
 */
public class DatabaseConnection {

    // ── Change PASSWORD to your MySQL root password ──────────
    private static final String URL      = "jdbc:mysql://localhost:3306/sudoku_db"
                                         + "?useSSL=false&serverTimezone=UTC"
                                         + "&allowPublicKeyRetrieval=true";
    private static final String USER     = "root";
    private static final String PASSWORD = "x@x*9F8P,8&q7EW"; // <-- CHANGE THIS

    private static Connection connection = null;

    // ── Singleton connection ─────────────────────────────────
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            } catch (ClassNotFoundException e) {
                throw new SQLException(
                    "MySQL JDBC Driver not found!\n"
                    + "Add mysql-connector-j-8.0.33.jar to your classpath.", e);
            }
        }
        return connection;
    }

    // ── Auto-create all tables on first run ──────────────────
    public static boolean setupDatabase() {
        try {
            Connection conn = getConnection();
            Statement stmt  = conn.createStatement();

            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS game_sessions ("
                + "id           INT AUTO_INCREMENT PRIMARY KEY,"
                + "player_name  VARCHAR(100) DEFAULT 'Player',"
                + "difficulty   VARCHAR(10)  NOT NULL,"
                + "mode         VARCHAR(20)  NOT NULL,"
                + "is_solved    TINYINT(1)   DEFAULT 0,"
                + "time_taken   INT          DEFAULT 0,"
                + "mistakes     INT          DEFAULT 0,"
                + "hints_used   INT          DEFAULT 0,"
                + "started_at   DATETIME     DEFAULT NOW(),"
                + "finished_at  DATETIME     NULL)"
            );

            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS validation_history ("
                + "id             INT AUTO_INCREMENT PRIMARY KEY,"
                + "board_snapshot TEXT         NOT NULL,"
                + "is_valid       TINYINT(1)   NOT NULL,"
                + "result_message VARCHAR(500) NOT NULL,"
                + "validated_at   DATETIME     DEFAULT NOW())"
            );

            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS leaderboard ("
                + "id           INT AUTO_INCREMENT PRIMARY KEY,"
                + "player_name  VARCHAR(100) NOT NULL,"
                + "difficulty   VARCHAR(10)  NOT NULL,"
                + "time_taken   INT          NOT NULL,"
                + "mistakes     INT          DEFAULT 0,"
                + "hints_used   INT          DEFAULT 0,"
                + "played_at    DATETIME     DEFAULT NOW())"
            );

            stmt.close();
            System.out.println("[DB] Setup complete — all tables ready.");
            return true;

        } catch (SQLException e) {
            System.err.println("[DB] Setup failed: " + e.getMessage());
            JOptionPane.showMessageDialog(null,
                "Database connection failed!\n\n"
                + "Make sure:\n"
                + "1. MySQL is running\n"
                + "2. Database 'sudoku_db' exists\n"
                + "3. Password is correct in DatabaseConnection.java\n"
                + "4. mysql-connector-j-8.0.33.jar is in classpath\n\n"
                + "Error: " + e.getMessage(),
                "DB Connection Error",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    // ── Save a new game session, returns generated ID ────────
    public static int saveGameSession(String playerName, String difficulty,
                                      String mode) {
        String sql = "INSERT INTO game_sessions "
                   + "(player_name, difficulty, mode) VALUES (?, ?, ?)";
        try (PreparedStatement ps = getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, playerName);
            ps.setString(2, difficulty);
            ps.setString(3, mode);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("[DB] saveGameSession failed: " + e.getMessage());
        }
        return -1;
    }

    // ── Update session when game is finished ─────────────────
    public static void finishGameSession(int sessionId, boolean solved,
                                         int timeSec, int mistakes,
                                         int hintsUsed) {
        String sql = "UPDATE game_sessions SET is_solved=?, time_taken=?,"
                   + " mistakes=?, hints_used=?, finished_at=NOW() WHERE id=?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, solved ? 1 : 0);
            ps.setInt(2, timeSec);
            ps.setInt(3, mistakes);
            ps.setInt(4, hintsUsed);
            ps.setInt(5, sessionId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DB] finishGameSession failed: " + e.getMessage());
        }
    }

    // ── Add to leaderboard ───────────────────────────────────
    public static void saveToLeaderboard(String playerName, String difficulty,
                                         int timeSec, int mistakes,
                                         int hintsUsed) {
        String sql = "INSERT INTO leaderboard "
                   + "(player_name, difficulty, time_taken, mistakes, hints_used)"
                   + " VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, playerName);
            ps.setString(2, difficulty);
            ps.setInt(3, timeSec);
            ps.setInt(4, mistakes);
            ps.setInt(5, hintsUsed);
            ps.executeUpdate();
            System.out.println("[DB] Leaderboard entry saved.");
        } catch (SQLException e) {
            System.err.println("[DB] saveToLeaderboard failed: " + e.getMessage());
        }
    }

    // ── Save a validation attempt ────────────────────────────
    public static void saveValidation(String snapshot, boolean isValid,
                                      String message) {
        String sql = "INSERT INTO validation_history "
                   + "(board_snapshot, is_valid, result_message) VALUES (?,?,?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, snapshot);
            ps.setInt(2, isValid ? 1 : 0);
            ps.setString(3, message);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DB] saveValidation failed: " + e.getMessage());
        }
    }

    // ── Fetch leaderboard (top 20 by time, per difficulty) ───
    public static ResultSet getLeaderboard(String difficulty) {
        try {
            String sql = "SELECT player_name, difficulty, time_taken, "
                       + "mistakes, hints_used, played_at "
                       + "FROM leaderboard "
                       + (difficulty.equals("ALL") ? ""
                          : "WHERE difficulty = '" + difficulty + "' ")
                       + "ORDER BY time_taken ASC LIMIT 20";
            return getConnection().createStatement().executeQuery(sql);
        } catch (SQLException e) {
            System.err.println("[DB] getLeaderboard failed: " + e.getMessage());
            return null;
        }
    }

    // ── Fetch validation history ─────────────────────────────
    public static ResultSet getValidationHistory() {
        try {
            String sql = "SELECT id, validated_at, "
                       + "CASE WHEN is_valid=1 THEN 'Valid' ELSE 'Invalid' END AS result,"
                       + " result_message FROM validation_history "
                       + "ORDER BY id DESC LIMIT 50";
            return getConnection().createStatement().executeQuery(sql);
        } catch (SQLException e) {
            System.err.println("[DB] getValidationHistory failed: " + e.getMessage());
            return null;
        }
    }

    // ── Close connection (call on app exit) ──────────────────
    public static void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("[DB] Connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("[DB] Close failed: " + e.getMessage());
        }
    }
}
