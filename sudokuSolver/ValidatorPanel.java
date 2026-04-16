
package sudokuvalidator;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Standalone Validator Panel — user enters any Sudoku board manually
 * and validates it. Results saved to MySQL validation_history table.
 *
 * Group 1 | B.Tech CSE Sec-E | Centurion University
 */
public class ValidatorPanel extends JPanel {

    private SudokuGrid grid;
    private JLabel     lblResult;
    private JButton    btnValidate, btnClear, btnSolve, btnHistory;

    public ValidatorPanel() {
        setBackground(SudokuGrid.COL_BG);
        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(16, 16, 16, 16));

        add(buildTitle(),   BorderLayout.NORTH);
        add(buildCenter(),  BorderLayout.CENTER);
        add(buildButtons(), BorderLayout.SOUTH);
    }

    private JPanel buildTitle() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER));
        p.setBackground(SudokuGrid.COL_BG);
        JLabel l = new JLabel("SUDOKU VALIDATOR  —  Enter any board and validate");
        l.setFont(new Font("Segoe UI", Font.BOLD, 15));
        l.setForeground(new Color(150, 180, 255));
        p.add(l);
        return p;
    }

    private JPanel buildCenter() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(SudokuGrid.COL_BG);
        grid = new SudokuGrid(); // no solution set — pure input mode
        p.add(grid);
        return p;
    }

    private JPanel buildButtons() {
        JPanel outer = new JPanel(new BorderLayout(0, 10));
        outer.setBackground(SudokuGrid.COL_BG);

        // Result label
        lblResult = new JLabel("Enter a Sudoku board above, then click Validate.",
                                SwingConstants.CENTER);
        lblResult.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblResult.setForeground(new Color(150, 180, 255));
        lblResult.setOpaque(true);
        lblResult.setBackground(new Color(20, 20, 35));
        lblResult.setBorder(new EmptyBorder(10, 16, 10, 16));
        outer.add(lblResult, BorderLayout.NORTH);

        // Buttons
        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        row.setBackground(SudokuGrid.COL_BG);

        btnValidate = makeBtn("✔  VALIDATE",   new Color(60, 200, 100));
        btnSolve    = makeBtn("⚡ SOLVE IT",    new Color(100, 180, 255));
        btnClear    = makeBtn("✖  CLEAR",       new Color(200, 80,  80));
        btnHistory  = makeBtn("📋 HISTORY",     new Color(180, 140, 255));

        btnValidate.addActionListener(e -> doValidate());
        btnSolve.addActionListener(e    -> doSolve());
        btnClear.addActionListener(e    -> doClear());
        btnHistory.addActionListener(e  -> showHistory());

        row.add(btnValidate);
        row.add(btnSolve);
        row.add(btnClear);
        row.add(btnHistory);
        outer.add(row, BorderLayout.SOUTH);

        // Number pad strip at bottom
        JPanel numPad = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 6));
        numPad.setBackground(SudokuGrid.COL_BG);
        JLabel hint = new JLabel("Click cells then use keys 1–9  or  ");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        hint.setForeground(new Color(80, 80, 110));
        numPad.add(hint);
        for (int n = 1; n <= 9; n++) {
            final int num = n;
            JButton b = new JButton(String.valueOf(n));
            b.setFont(new Font("Segoe UI", Font.BOLD, 13));
            b.setBackground(new Color(28, 28, 48));
            b.setForeground(Color.WHITE);
            b.setBorder(BorderFactory.createLineBorder(new Color(50,50,80)));
            b.setFocusPainted(false);
            b.setPreferredSize(new Dimension(36, 32));
            b.addActionListener(e -> grid.inputNumber(num));
            numPad.add(b);
        }
        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(SudokuGrid.COL_BG);
        center.add(outer, BorderLayout.NORTH);
        center.add(numPad, BorderLayout.SOUTH);
        return center;
    }

    // ── Validate ─────────────────────────────────────────────
    private void doValidate() {
        int[][] board = grid.getBoard();
        SudokuEngine.ValidationResult vr = SudokuEngine.validate(board);
        grid.setInvalidCells(vr.invalidCells);

        if (vr.valid) {
            lblResult.setText("✔  VALID SUDOKU  —  All constraints satisfied!");
            lblResult.setBackground(new Color(20, 60, 30));
            lblResult.setForeground(new Color(100, 230, 120));
        } else {
            String first = vr.message.split("\n")[0];
            lblResult.setText("✘  INVALID  —  " + first);
            lblResult.setBackground(new Color(60, 20, 20));
            lblResult.setForeground(new Color(255, 100, 100));
        }

        // Save to MySQL
        String snap = SudokuEngine.boardToSnapshot(board);
        DatabaseConnection.saveValidation(snap, vr.valid, vr.message);
    }

    // ── Solve ─────────────────────────────────────────────────
    private void doSolve() {
        int[][] board = SudokuEngine.copyBoard(grid.getBoard());
        boolean ok = SudokuEngine.solve(board);
        if (ok) {
            grid.loadPuzzle(board, board);
            grid.clearInvalid();
            lblResult.setText("⚡ Board solved using backtracking algorithm!");
            lblResult.setBackground(new Color(20, 40, 70));
            lblResult.setForeground(new Color(100, 180, 255));
        } else {
            lblResult.setText("✘  This board has no solution.");
            lblResult.setBackground(new Color(60, 20, 20));
            lblResult.setForeground(new Color(255, 100, 100));
        }
    }

    // ── Clear ─────────────────────────────────────────────────
    private void doClear() {
        grid.loadPuzzle(SudokuEngine.emptyBoard(), null);
        lblResult.setText("Board cleared. Enter a new Sudoku board.");
        lblResult.setBackground(new Color(20, 20, 35));
        lblResult.setForeground(new Color(150, 180, 255));
    }

    // ── History ───────────────────────────────────────────────
    private void showHistory() {
        HistoryPanel hp = new HistoryPanel();
        hp.setVisible(true);
    }

    // ── Helper ───────────────────────────────────────────────
    private JButton makeBtn(String text, Color fg) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setBackground(new Color(22, 22, 38));
        b.setForeground(fg);
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(50,50,80), 1),
            new EmptyBorder(8, 18, 8, 18)));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                b.setBackground(new Color(fg.getRed()/4, fg.getGreen()/4, fg.getBlue()/4, 80));
            }
            public void mouseExited(MouseEvent e) { b.setBackground(new Color(22,22,38)); }
        });
        return b;
    }
}
