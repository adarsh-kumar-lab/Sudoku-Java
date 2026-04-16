
package sudokuvalidator;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Main game panel — handles:
 *   - 4 difficulty levels (Easy / Medium / Hard / Expert)
 *   - Live countdown timer
 *   - Mistake counter (max 5)
 *   - Hint system (3 per game)
 *   - Notes mode toggle
 *   - Auto-solve button
 *   - Number pad (1-9)
 *   - Win detection + leaderboard save
 *   - New game / restart
 *
 * Group 1 | B.Tech CSE Sec-E | Centurion University
 */
public class GamePanel extends JPanel {

    // ── Difficulty colours ──────────────────────────────────
    private static final Color[] DIFF_COLORS = {
        new Color(74,  222, 128), // Easy   - green
        new Color(250, 204, 21),  // Medium - yellow
        new Color(251, 146, 60),  // Hard   - orange
        new Color(248, 113, 113), // Expert - red
    };
    private static final String[] DIFF_NAMES = {"EASY","MEDIUM","HARD","EXPERT"};
    private static final SudokuEngine.Difficulty[] DIFFS = {
        SudokuEngine.Difficulty.EASY,
        SudokuEngine.Difficulty.MEDIUM,
        SudokuEngine.Difficulty.HARD,
        SudokuEngine.Difficulty.EXPERT,
    };

    // ── State ────────────────────────────────────────────────
    private SudokuEngine.Difficulty currentDiff = SudokuEngine.Difficulty.EASY;
    private int diffIndex = 0;
    private int[][] solution;
    private boolean gameActive  = false;
    private boolean gameSolved  = false;
    private int  mistakes       = 0;
    private int  hintsLeft      = 3;
    private int  hintsUsed      = 0;
    private int  timerSeconds   = 0;
    private int  sessionId      = -1;
    private String playerName   = "Player";

    // ── Swing components ────────────────────────────────────
    private SudokuGrid     grid;
    private JLabel         lblTimer, lblMistakes, lblLevel, lblStatus;
    private JButton[]      numButtons  = new JButton[9];
    private JButton        btnNotes, btnHint, btnSolve, btnNew, btnErase;
    private JToggleButton[]diffButtons = new JToggleButton[4];
    private Timer          swingTimer;
    private MainWindow     parent;

    // ────────────────────────────────────────────────────────
    public GamePanel(MainWindow parent, String playerName) {
        this.parent     = parent;
        this.playerName = playerName;
        setBackground(SudokuGrid.COL_BG);
        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(16, 16, 16, 16));

        add(buildTopBar(),     BorderLayout.NORTH);
        add(buildCenter(),     BorderLayout.CENTER);
        add(buildControls(),   BorderLayout.EAST);
        add(buildBottomBar(),  BorderLayout.SOUTH);

        swingTimer = new Timer(1000, e -> {
            timerSeconds++;
            lblTimer.setText(SudokuEngine.formatTime(timerSeconds));
        });

        startNewGame(diffIndex);
    }

    // ── Top bar: level selector + timer ──────────────────────
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout(10, 0));
        bar.setBackground(SudokuGrid.COL_BG);

        // Difficulty toggle buttons
        JPanel diffPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        diffPanel.setBackground(SudokuGrid.COL_BG);
        ButtonGroup bg = new ButtonGroup();
        for (int i = 0; i < 4; i++) {
            final int idx = i;
            JToggleButton btn = new JToggleButton(DIFF_NAMES[i]);
            btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
            btn.setFocusPainted(false);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            styleToggleBtn(btn, DIFF_COLORS[i], i == 0);
            btn.addActionListener(e -> startNewGame(idx));
            diffButtons[i] = btn;
            bg.add(btn);
            diffPanel.add(btn);
        }
        diffButtons[0].setSelected(true);
        bar.add(diffPanel, BorderLayout.WEST);

        // Timer + mistake counter
        JPanel rightInfo = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        rightInfo.setBackground(SudokuGrid.COL_BG);

        lblMistakes = makeInfoLabel("MISTAKES: 0/5", new Color(248,113,113));
        lblTimer    = makeInfoLabel("00:00", Color.WHITE);

        rightInfo.add(lblMistakes);
        rightInfo.add(lblTimer);
        bar.add(rightInfo, BorderLayout.EAST);

        return bar;
    }

    // ── Center: grid ─────────────────────────────────────────
    private JPanel buildCenter() {
        grid = new SudokuGrid();
        grid.setInputListener((r, c, v) -> onCellChanged(r, c, v));
        JPanel wrap = new JPanel(new GridBagLayout());
        wrap.setBackground(SudokuGrid.COL_BG);
        wrap.add(grid);
        return wrap;
    }

    // ── Right-side controls: numpad + action buttons ──────────
    private JPanel buildControls() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(SudokuGrid.COL_BG);
        panel.setBorder(new EmptyBorder(0, 12, 0, 0));

        // Number pad 1-9
        JPanel numPad = new JPanel(new GridLayout(3, 3, 6, 6));
        numPad.setBackground(SudokuGrid.COL_BG);
        numPad.setAlignmentX(Component.CENTER_ALIGNMENT);
        for (int i = 0; i < 9; i++) {
            final int num = i + 1;
            JButton btn = new JButton(String.valueOf(num));
            btn.setFont(new Font("Segoe UI", Font.BOLD, 18));
            btn.setBackground(new Color(28, 28, 48));
            btn.setForeground(Color.WHITE);
            btn.setBorder(BorderFactory.createLineBorder(new Color(50,50,80), 1));
            btn.setFocusPainted(false);
            btn.setPreferredSize(new Dimension(50, 50));
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.addMouseListener(hoverEffect(btn, new Color(50,90,180), new Color(28,28,48)));
            btn.addActionListener(e -> onNumberInput(num));
            numButtons[i] = btn;
            numPad.add(btn);
        }

        JLabel numLabel = new JLabel("NUMBER PAD");
        numLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        numLabel.setForeground(new Color(100,100,140));
        numLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        numLabel.setBorder(new EmptyBorder(0,0,6,0));

        panel.add(numLabel);
        panel.add(numPad);
        panel.add(Box.createVerticalStrut(16));

        // Action buttons
        btnErase = makeActionBtn("⌫  ERASE",  new Color(100,120,180));
        btnNotes = makeActionBtn("✎  NOTES: OFF", new Color(130,90,200));
        btnHint  = makeActionBtn("💡 HINT (3)",  new Color(240,180,0));
        btnSolve = makeActionBtn("⚡ AUTO-SOLVE", new Color(60,160,240));
        btnNew   = makeActionBtn("↺  NEW GAME",  new Color(60,180,100));

        btnErase.addActionListener(e -> { grid.erase(); updateNumCounts(); });
        btnNotes.addActionListener(e -> toggleNotes());
        btnHint.addActionListener(e  -> useHint());
        btnSolve.addActionListener(e -> doAutoSolve());
        btnNew.addActionListener(e   -> startNewGame(diffIndex));

        for (JButton b : new JButton[]{btnErase,btnNotes,btnHint,btnSolve,btnNew}) {
            b.setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(b);
            panel.add(Box.createVerticalStrut(6));
        }

        return panel;
    }

    // ── Bottom status bar ─────────────────────────────────────
    private JPanel buildBottomBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(14, 14, 22));
        bar.setBorder(new EmptyBorder(8, 12, 8, 12));

        lblLevel  = new JLabel("EASY  ·  46 clues given", SwingConstants.LEFT);
        lblStatus = new JLabel("Select a cell and enter a number", SwingConstants.RIGHT);

        for (JLabel l : new JLabel[]{lblLevel, lblStatus}) {
            l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            l.setForeground(new Color(100, 100, 140));
            bar.add(l, l == lblLevel ? BorderLayout.WEST : BorderLayout.EAST);
        }
        return bar;
    }

    // ── Start a new game ─────────────────────────────────────
    public void startNewGame(int dIdx) {
        diffIndex    = dIdx;
        currentDiff  = DIFFS[dIdx];
        Color accent = DIFF_COLORS[dIdx];

        gameSolved   = false;
        gameActive   = true;
        mistakes     = 0;
        hintsLeft    = 3;
        hintsUsed    = 0;
        timerSeconds = 0;

        swingTimer.stop();
        lblTimer.setText("00:00");
        lblMistakes.setText("MISTAKES: 0/5");

        // Style diff buttons
        for (int i = 0; i < 4; i++) styleToggleBtn(diffButtons[i], DIFF_COLORS[i], i == dIdx);
        diffButtons[dIdx].setSelected(true);

        // Generate puzzle
        lblStatus.setText("Generating " + currentDiff.label() + " puzzle…");
        SwingWorker<int[][][], Void> worker = new SwingWorker<>() {
            @Override protected int[][][] doInBackground() {
                return SudokuEngine.generatePuzzle(currentDiff);
            }
            @Override protected void done() {
                try {
                    int[][][] data = get();
                    solution = data[1];
                    grid.loadPuzzle(data[0], data[1]);
                    grid.setAccentColor(accent);
                    updateNumCounts();
                    swingTimer.start();
                    lblLevel.setText(DIFF_NAMES[dIdx] + "  ·  "
                        + currentDiff.clues + " clues given");
                    lblStatus.setText("Puzzle ready — Good luck!");
                    btnHint.setText("💡 HINT (" + hintsLeft + ")");
                    // Save session to DB
                    sessionId = DatabaseConnection.saveGameSession(
                        playerName, currentDiff.name().toLowerCase(), "game");
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        };
        worker.execute();
    }

    // ── Called when a cell is changed ────────────────────────
    private void onCellChanged(int r, int c, int v) {
        if (!gameActive || gameSolved) return;
        if (solution != null && v != 0 && v != solution[r][c]) {
            mistakes++;
            lblMistakes.setText("MISTAKES: " + mistakes + "/5");
            lblMistakes.setForeground(mistakes >= 3
                ? new Color(248,113,113) : new Color(250,204,21));
            if (mistakes >= 5) {
                gameActive = false;
                swingTimer.stop();
                lblStatus.setText("Too many mistakes! Starting new game…");
                JOptionPane.showMessageDialog(this,
                    "5 mistakes reached!\nStarting a new game.",
                    "Game Over", JOptionPane.WARNING_MESSAGE);
                startNewGame(diffIndex);
                return;
            }
        }
        // Check win
        SudokuEngine.ValidationResult vr = SudokuEngine.validate(grid.getBoard());
        if (SudokuEngine.isComplete(grid.getBoard()) && vr.valid) {
            onGameWon();
        } else {
            grid.setInvalidCells(vr.invalidCells);
        }
        updateNumCounts();
        lblStatus.setText(vr.valid ? "Looking good!" : vr.message.split("\n")[0]);
    }

    private void onNumberInput(int num) {
        if (!gameActive || gameSolved) return;
        boolean complete = grid.inputNumber(num);
        int[][] b = grid.getBoard();
        SudokuEngine.ValidationResult vr = SudokuEngine.validate(b);
        if (SudokuEngine.isComplete(b) && vr.valid) { onGameWon(); return; }
        grid.setInvalidCells(vr.invalidCells);
        updateNumCounts();
        // Check mistake
        int r = grid.getSelRow(), c = grid.getSelCol();
        if (r >= 0 && c >= 0 && solution != null
                && b[r][c] != 0 && b[r][c] != solution[r][c]) {
            mistakes++;
            lblMistakes.setText("MISTAKES: " + mistakes + "/5");
        }
    }

    // ── Game won! ─────────────────────────────────────────────
    private void onGameWon() {
        if (gameSolved) return;
        gameSolved = true;
        gameActive = false;
        swingTimer.stop();

        // Save to DB
        DatabaseConnection.finishGameSession(sessionId, true,
            timerSeconds, mistakes, hintsUsed);
        DatabaseConnection.saveToLeaderboard(playerName,
            currentDiff.name().toLowerCase(),
            timerSeconds, mistakes, hintsUsed);

        lblStatus.setText("🎉 SOLVED!  Time: " + SudokuEngine.formatTime(timerSeconds)
            + "  Mistakes: " + mistakes);

        String msg = String.format(
            "🎉 Congratulations, %s!\n\n"
            + "Difficulty : %s\n"
            + "Time       : %s\n"
            + "Mistakes   : %d\n"
            + "Hints used : %d\n\n"
            + "Score saved to leaderboard!",
            playerName, currentDiff.label(),
            SudokuEngine.formatTime(timerSeconds),
            mistakes, hintsUsed);

        JOptionPane.showMessageDialog(this, msg, "🏆 Puzzle Solved!",
            JOptionPane.INFORMATION_MESSAGE);
    }

    // ── Hint ──────────────────────────────────────────────────
    private void useHint() {
        if (!gameActive || hintsLeft <= 0) {
            lblStatus.setText("No hints remaining!");
            return;
        }
        int[] cell = grid.revealHint();
        if (cell != null) {
            hintsLeft--;
            hintsUsed++;
            btnHint.setText("💡 HINT (" + hintsLeft + ")");
            btnHint.setEnabled(hintsLeft > 0);
            updateNumCounts();
            lblStatus.setText("Hint revealed at Row " + (cell[0]+1) + ", Col " + (cell[1]+1));
        }
    }

    // ── Notes toggle ──────────────────────────────────────────
    private void toggleNotes() {
        boolean on = !grid.isNotesMode();
        grid.setNotesMode(on);
        btnNotes.setText("✎  NOTES: " + (on ? "ON" : "OFF"));
        btnNotes.setForeground(on ? new Color(200, 150, 255) : Color.WHITE);
        lblStatus.setText(on ? "Notes mode ON — enter pencil marks" : "Notes mode OFF");
    }

    // ── Auto-solve ─────────────────────────────────────────────
    private void doAutoSolve() {
        int opt = JOptionPane.showConfirmDialog(this,
            "Auto-solve will reveal the full solution.\nAre you sure?",
            "Auto Solve", JOptionPane.YES_NO_OPTION);
        if (opt != JOptionPane.YES_OPTION) return;
        gameActive = false;
        gameSolved = true;
        swingTimer.stop();
        grid.autoSolve();
        updateNumCounts();
        lblStatus.setText("Board solved automatically.");
        DatabaseConnection.finishGameSession(sessionId, false,
            timerSeconds, mistakes, hintsUsed);
    }

    // ── Update number button counts ───────────────────────────
    private void updateNumCounts() {
        int[][] b = grid.getBoard();
        int[] counts = new int[10];
        for (int[] row : b) for (int v : row) if (v > 0) counts[v]++;
        for (int i = 0; i < 9; i++) {
            int n = i + 1;
            if (counts[n] >= 9) {
                numButtons[i].setEnabled(false);
                numButtons[i].setForeground(new Color(50, 50, 70));
            } else {
                numButtons[i].setEnabled(true);
                numButtons[i].setForeground(Color.WHITE);
            }
        }
    }

    // ── UI Helpers ────────────────────────────────────────────
    private JLabel makeInfoLabel(String text, Color fg) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 13));
        l.setForeground(fg);
        return l;
    }

    private JButton makeActionBtn(String text, Color fg) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setBackground(new Color(22, 22, 38));
        b.setForeground(fg);
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(50,50,80), 1),
            new EmptyBorder(7, 14, 7, 14)));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setMaximumSize(new Dimension(200, 38));
        b.setPreferredSize(new Dimension(170, 38));
        b.addMouseListener(hoverEffect(b,
            new Color(fg.getRed()/3, fg.getGreen()/3, fg.getBlue()/3, 60),
            new Color(22,22,38)));
        return b;
    }

    private void styleToggleBtn(JToggleButton btn, Color accent, boolean selected) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setBackground(selected ? new Color(accent.getRed()/4,
            accent.getGreen()/4, accent.getBlue()/4) : new Color(22, 22, 38));
        btn.setForeground(selected ? accent : new Color(100, 100, 140));
        btn.setBorder(BorderFactory.createLineBorder(
            selected ? accent : new Color(40, 40, 60), 1));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private MouseAdapter hoverEffect(AbstractButton btn, Color hover, Color normal) {
        return new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(hover); }
            public void mouseExited(MouseEvent e)  { btn.setBackground(normal); }
        };
    }
}
