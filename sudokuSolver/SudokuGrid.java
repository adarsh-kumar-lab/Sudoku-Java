package sudokuvalidator;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class SudokuGrid extends JPanel {

    public static final Color COL_BG           = new Color(18,  18,  28);
    public static final Color COL_GIVEN_BG     = new Color(28,  28,  48);
    public static final Color COL_SELECTED     = new Color(70,  130, 220, 120);
    public static final Color COL_HIGHLIGHT    = new Color(40,  60,  100, 80);
    public static final Color COL_SAME_NUM     = new Color(90,  160, 255, 90);
    public static final Color COL_INVALID      = new Color(200, 40,  40,  180);
    public static final Color COL_CORRECT      = new Color(30,  160, 80,  120);
    public static final Color COL_GIVEN_TEXT   = new Color(240, 240, 255);
    public static final Color COL_PLAYER_TEXT  = new Color(100, 180, 255);
    public static final Color COL_INVALID_TEXT = new Color(255, 80,  80);
    public static final Color COL_NOTE_TEXT    = new Color(130, 180, 255);
    public static final Color COL_BOX_BORDER   = new Color(80,  100, 180);
    public static final Color COL_CELL_BORDER  = new Color(35,  35,  60);

    private final JPanel[][]     cellPanels  = new JPanel[9][9];
    private final JLabel[][]     cellLabels  = new JLabel[9][9];
    private final JLabel[][][]   noteLabels  = new JLabel[9][9][9];

    private int[][] board      = SudokuEngine.emptyBoard();
    private int[][] solution   = null;
    private boolean[][] given  = new boolean[9][9];
    private boolean[][] invalid= new boolean[9][9];
    // ===== MISSING METHODS FIX =====

public int[][] getBoard() {
    return board;
}

public int getSelRow() {
    return selRow;
}

public int getSelCol() {
    return selCol;
}

public void setNotesMode(boolean on) {
    this.notesMode = on;
}

public boolean isNotesMode() {
    return notesMode;
}

public void clearInvalid() {
    this.invalid = new boolean[9][9];
    refreshAll();
}

public void setInputListener(CellInputListener l) {
    this.inputListener = l;
}

public void setAccentColor(Color c) {
    this.accentColor = c;
    setBorder(BorderFactory.createLineBorder(c, 3));
    refreshAll();
}

public void autoSolve() {
    if (solution != null) {
        board = SudokuEngine.copyBoard(solution);
    } else {
        SudokuEngine.solve(board);
    }
    invalid = new boolean[9][9];
    selRow = -1;
    selCol = -1;
    refreshAll();
}

public int[] revealHint() {
    if (solution == null) return null;

    java.util.List<int[]> empties = new java.util.ArrayList<>();

    for (int r = 0; r < 9; r++)
        for (int c = 0; c < 9; c++)
            if (!given[r][c] && board[r][c] == 0)
                empties.add(new int[]{r, c});

    if (empties.isEmpty()) return null;

    java.util.Collections.shuffle(empties);
    int[] cell = empties.get(0);

    board[cell[0]][cell[1]] = solution[cell[0]][cell[1]];
    given[cell[0]][cell[1]] = true;

    selectCell(cell[0], cell[1]);
    refreshAll();

    return cell;
}

// ===== INTERFACE (IMPORTANT) =====
public interface CellInputListener {
    void onCellChanged(int row, int col, int value);
}

    private int selRow = -1, selCol = -1;
    private boolean notesMode  = false;

    // ✅ FIX HERE (3D array)
    private boolean[][][] notes  = new boolean[9][9][10];

    private CellInputListener inputListener = null;
    private Color accentColor = COL_BOX_BORDER;

    public SudokuGrid() {
        setLayout(new GridLayout(3, 3, 3, 3));
        setBackground(COL_BOX_BORDER);
        setBorder(BorderFactory.createLineBorder(COL_BOX_BORDER, 3));
        buildGrid();
        setFocusable(true);
        addKeyListener(new GridKeyListener());
    }

    private void buildGrid() {
        for (int br = 0; br < 3; br++) {
            for (int bc = 0; bc < 3; bc++) {
                JPanel box = new JPanel(new GridLayout(3, 3, 1, 1));
                box.setBackground(COL_CELL_BORDER);
                for (int r = 0; r < 3; r++) {
                    for (int c = 0; c < 3; c++) {
                        int row = br*3+r, col = bc*3+c;
                        cellPanels[row][col] = createCellPanel(row, col);
                        box.add(cellPanels[row][col]);
                    }
                }
                add(box);
            }
        }
    }

    private JPanel createCellPanel(int row, int col) {
        JPanel cell = new JPanel(null);
        cell.setBackground(COL_BG);
        cell.setPreferredSize(new Dimension(56, 56));
        cell.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel lbl = new JLabel("", SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lbl.setForeground(COL_GIVEN_TEXT);
        lbl.setBounds(0, 0, 56, 56);
        cell.add(lbl);
        cellLabels[row][col] = lbl;

        for (int n = 0; n < 9; n++) {
            JLabel note = new JLabel(String.valueOf(n+1), SwingConstants.CENTER);
            note.setFont(new Font("Segoe UI", Font.PLAIN, 8));
            note.setForeground(COL_NOTE_TEXT);
            note.setVisible(false);
            int nr = n / 3, nc = n % 3;
            note.setBounds(nc*18 + 2, nr*18 + 2, 16, 16);
            cell.add(note);
            noteLabels[row][col][n] = note;
        }

        cell.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                selectCell(row, col);
                requestFocusInWindow();
            }
        });

        return cell;
    }

    public void loadPuzzle(int[][] puzzle, int[][] sol) {
        this.board    = SudokuEngine.copyBoard(puzzle);
        this.solution = sol != null ? SudokuEngine.copyBoard(sol) : null;
        this.given    = new boolean[9][9];
        this.invalid  = new boolean[9][9];

        // ✅ FIX HERE
        this.notes    = new boolean[9][9][10];

        this.selRow   = -1; this.selCol = -1;

        for (int r = 0; r < 9; r++)
            for (int c = 0; c < 9; c++)
                if (puzzle[r][c] != 0) given[r][c] = true;

        refreshAll();
    }

    public boolean inputNumber(int num) {
        if (selRow < 0 || given[selRow][selCol]) return false;

        if (notesMode) {
            notes[selRow][selCol][num] = !notes[selRow][selCol][num];
            board[selRow][selCol] = 0;
            refreshCell(selRow, selCol);
            return false;
        }

        board[selRow][selCol] = num;

        // ✅ FIX HERE
        notes[selRow][selCol] = new boolean[10];

        clearRelatedNotes(selRow, selCol, num);
        refreshAll();
        return checkComplete();
    }

    public void erase() {
        if (selRow < 0 || given[selRow][selCol]) return;
        board[selRow][selCol] = 0;

        // ✅ FIX HERE
        notes[selRow][selCol] = new boolean[10];

        refreshAll();
    }

    private void clearRelatedNotes(int row, int col, int num) {
        for (int i = 0; i < 9; i++) {
            notes[row][i][num] = false;
            notes[i][col][num] = false;
        }

        int br = (row/3)*3, bc = (col/3)*3;
        for (int r = br; r < br+3; r++)
            for (int c = bc; c < bc+3; c++)
                notes[r][c][num] = false;
    }

    private void refreshAll() {
        for (int r = 0; r < 9; r++)
            for (int c = 0; c < 9; c++)
                refreshCell(r, c);
    }

    private void refreshCell(int r, int c) {
        JPanel panel = cellPanels[r][c];
        JLabel label = cellLabels[r][c];
        int v = board[r][c];

        boolean hasNotes = false;
        for (int n = 1; n <= 9; n++)
            if (notes[r][c][n]) { hasNotes = true; break; }

        if (v != 0 && !hasNotes) {
            label.setText(String.valueOf(v));
            label.setVisible(true);
        } else {
            label.setText("");
            label.setVisible(false);

            for (int n = 0; n < 9; n++) {
                noteLabels[r][c][n].setVisible(notes[r][c][n+1]);
            }
        }

        panel.repaint();
    }

    private void selectCell(int r, int c) {
        selRow = r;
        selCol = c;
        refreshAll();
    }

    private boolean checkComplete() {
        if (!SudokuEngine.isComplete(board)) return false;
        SudokuEngine.ValidationResult vr = SudokuEngine.validate(board);
        if (vr.valid) return true;
        setInvalidCells(vr.invalidCells);
        return false;
    }

    public void setInvalidCells(boolean[][] inv) {
        this.invalid = inv;
        refreshAll();
    }

    private class GridKeyListener extends KeyAdapter {
        @Override
        public void keyTyped(KeyEvent e) {
            char ch = e.getKeyChar();
            if (ch >= '1' && ch <= '9') {
                int num = ch - '0';
                inputNumber(num);
            }
        }
    }
}