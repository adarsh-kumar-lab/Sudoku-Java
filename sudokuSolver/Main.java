
package sudokuvalidator;

import javax.swing.*;
import java.awt.*;

/**
 * Entry point for the Sudoku Application.
 *
 * Startup sequence:
 *   1. Ask player name
 *   2. Setup MySQL database (create tables if not exist)
 *   3. Launch MainWindow
 *
 * Project : Sudoku Game — Puzzle Generator + Solver + Validator
 * Team    : Group 1, B.Tech CSE Sec-E, Centurion University
 * Members : Jagadananda Panda (283), Jiban Pradip Sahu (282),
 *           Biswajit Gantayat (264), Adarsh Tiwari (292)
 * Guide   : Prof. Patra Srimanta Kumar
 */
public class Main {

    public static void main(String[] args) {

        // Use system look-and-feel for native file dialogs etc.
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {

            // ── Step 1: Ask player name ──────────────────────
            String name = JOptionPane.showInputDialog(
                null,
                "Welcome to Sudoku!\n\nEnter your name to track scores:",
                "Sudoku — Group 1 | Centurion University",
                JOptionPane.QUESTION_MESSAGE);

            if (name == null) System.exit(0); // user cancelled
            name = name.trim().isEmpty() ? "Player" : name.trim();

            // ── Step 2: Connect to DB ────────────────────────
            boolean dbOk = DatabaseConnection.setupDatabase();
            if (!dbOk) {
                int opt = JOptionPane.showConfirmDialog(
                    null,
                    "Could not connect to MySQL database.\n"
                    + "The game will run WITHOUT database features.\n\n"
                    + "Continue anyway?",
                    "DB Unavailable", JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
                if (opt != JOptionPane.YES_OPTION) System.exit(0);
            }

            // ── Step 3: Launch main window ───────────────────
            MainWindow window = new MainWindow(name);
            window.setVisible(true);
        });
    }
}
