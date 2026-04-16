
package sudokuvalidator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * Core Sudoku engine — handles:
 *   1. Puzzle Generation (4 difficulty levels)
 *   2. Puzzle Solving   (backtracking algorithm)
 *   3. Board Validation (row + column + 3x3 box)
 *   4. Utility methods  (snapshot, copy, complete-check)
 *
 * No GUI code. No database code. Pure logic.
 *
 * Group 1 | B.Tech CSE Sec-E | Centurion University
 */
public class SudokuEngine {

    // ── Difficulty: number of GIVEN clues ────────────────────
    public enum Difficulty {
        EASY(46), MEDIUM(36), HARD(28), EXPERT(22);

        public final int clues;
        Difficulty(int clues) { this.clues = clues; }

        public String label() { return name().charAt(0)
            + name().substring(1).toLowerCase(); }
    }

    // ── Validation Result ────────────────────────────────────
    public static class ValidationResult {
        public final boolean     valid;
        public final String      message;
        public final boolean[][] invalidCells;   // [row][col] = true if bad cell

        public ValidationResult(boolean valid, String message,
                                 boolean[][] invalidCells) {
            this.valid        = valid;
            this.message      = message;
            this.invalidCells = invalidCells;
        }
    }

    // ── Generate a new puzzle + its solution ─────────────────
    /**
     * Returns int[2][9][9]:
     *   result[0] = puzzle  (0 = empty cell)
     *   result[1] = solution (fully filled)
     */
    public static int[][][] generatePuzzle(Difficulty diff) {
        int[][] solution = generateFullBoard();
        int[][] puzzle   = copyBoard(solution);

        int toRemove = 81 - diff.clues;

        // Shuffle positions for random removal
        List<Integer> positions = new ArrayList<>();
        for (int i = 0; i < 81; i++) positions.add(i);
        Collections.shuffle(positions);

        int removed = 0;
        for (int pos : positions) {
            if (removed >= toRemove) break;
            int r = pos / 9, c = pos % 9;
            int backup = puzzle[r][c];
            puzzle[r][c] = 0;

            // Verify puzzle still has unique solution
            int[][] test = copyBoard(puzzle);
            if (countSolutions(test, 0) == 1) {
                removed++;
            } else {
                puzzle[r][c] = backup; // restore if multiple solutions
            }
        }

        return new int[][][] { puzzle, solution };
    }

    // ── Backtracking solver ──────────────────────────────────
    /**
     * Solves board in-place using backtracking.
     * @return true if solved, false if unsolvable
     */
    public static boolean solve(int[][] board) {
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                if (board[r][c] == 0) {
                    for (int num = 1; num <= 9; num++) {
                        if (isPlacementValid(board, r, c, num)) {
                            board[r][c] = num;
                            if (solve(board)) return true;
                            board[r][c] = 0;
                        }
                    }
                    return false; // no valid number found
                }
            }
        }
        return true; // all cells filled
    }

    // ── Validate a board ─────────────────────────────────────
    /**
     * Validates board against all 3 Sudoku constraints.
     * Empty cells (0) are skipped.
     */
    public static ValidationResult validate(int[][] board) {
        boolean[][] invalid = new boolean[9][9];
        boolean     isValid = true;
        StringBuilder msg   = new StringBuilder();

        // 1. Check rows
        for (int r = 0; r < 9; r++) {
            HashSet<Integer> seen = new HashSet<>();
            for (int c = 0; c < 9; c++) {
                int v = board[r][c];
                if (v == 0) continue;
                if (v < 1 || v > 9) {
                    invalid[r][c] = true; isValid = false;
                    msg.append("Invalid number at Row ").append(r+1)
                       .append(", Col ").append(c+1).append("\n");
                    continue;
                }
                if (!seen.add(v)) {
                    invalid[r][c] = true; isValid = false;
                    msg.append("Duplicate '").append(v)
                       .append("' in Row ").append(r+1).append("\n");
                }
            }
        }

        // 2. Check columns
        for (int c = 0; c < 9; c++) {
            HashSet<Integer> seen = new HashSet<>();
            for (int r = 0; r < 9; r++) {
                int v = board[r][c];
                if (v == 0) continue;
                if (!seen.add(v)) {
                    invalid[r][c] = true; isValid = false;
                    msg.append("Duplicate '").append(v)
                       .append("' in Column ").append(c+1).append("\n");
                }
            }
        }

        // 3. Check 3x3 boxes
        for (int br = 0; br < 9; br += 3) {
            for (int bc = 0; bc < 9; bc += 3) {
                HashSet<Integer> seen = new HashSet<>();
                for (int r = br; r < br+3; r++) {
                    for (int c = bc; c < bc+3; c++) {
                        int v = board[r][c];
                        if (v == 0) continue;
                        if (!seen.add(v)) {
                            invalid[r][c] = true; isValid = false;
                            msg.append("Duplicate '").append(v)
                               .append("' in Box (").append(br/3+1)
                               .append(",").append(bc/3+1).append(")\n");
                        }
                    }
                }
            }
        }

        String result = isValid
            ? "Valid Sudoku! All constraints satisfied."
            : msg.toString().trim();
        return new ValidationResult(isValid, result, invalid);
    }

    // ── Check if placing num at (r,c) is valid ───────────────
    public static boolean isPlacementValid(int[][] board, int r, int c, int num) {
        // Row check
        for (int j = 0; j < 9; j++)
            if (board[r][j] == num) return false;
        // Column check
        for (int i = 0; i < 9; i++)
            if (board[i][c] == num) return false;
        // Box check
        int br = (r / 3) * 3, bc = (c / 3) * 3;
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                if (board[br+i][bc+j] == num) return false;
        return true;
    }

    // ── Check if board is fully filled ───────────────────────
    public static boolean isComplete(int[][] board) {
        for (int[] row : board)
            for (int v : row)
                if (v == 0) return false;
        return true;
    }

    // ── Board → 81-char string for DB storage ────────────────
    public static String boardToSnapshot(int[][] board) {
        StringBuilder sb = new StringBuilder(81);
        for (int r = 0; r < 9; r++)
            for (int c = 0; c < 9; c++)
                sb.append(board[r][c]);
        return sb.toString();
    }

    // ── Deep copy a board ────────────────────────────────────
    public static int[][] copyBoard(int[][] src) {
        int[][] copy = new int[9][9];
        for (int i = 0; i < 9; i++)
            copy[i] = src[i].clone();
        return copy;
    }

    // ── Empty 9x9 board ──────────────────────────────────────
    public static int[][] emptyBoard() {
        return new int[9][9];
    }

    // ── Format seconds → "MM:SS" ─────────────────────────────
    public static String formatTime(int seconds) {
        return String.format("%02d:%02d", seconds / 60, seconds % 60);
    }

    // ── PRIVATE HELPERS ──────────────────────────────────────

    /** Generates a completely filled valid Sudoku board randomly. */
    private static int[][] generateFullBoard() {
        int[][] board = new int[9][9];
        fillBoard(board, 0);
        return board;
    }

    /** Recursive random board filler. */
    private static boolean fillBoard(int[][] board, int pos) {
        if (pos == 81) return true;
        int r = pos / 9, c = pos % 9;
        List<Integer> nums = new ArrayList<>();
        for (int i = 1; i <= 9; i++) nums.add(i);
        Collections.shuffle(nums);
        for (int n : nums) {
            if (isPlacementValid(board, r, c, n)) {
                board[r][c] = n;
                if (fillBoard(board, pos + 1)) return true;
                board[r][c] = 0;
            }
        }
        return false;
    }

    /**
     * Counts the number of solutions (stops at 2 for efficiency).
     * Used to ensure generated puzzles have a unique solution.
     */
    private static int countSolutions(int[][] board, int count) {
        if (count > 1) return count; // early exit
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                if (board[r][c] == 0) {
                    for (int n = 1; n <= 9; n++) {
                        if (isPlacementValid(board, r, c, n)) {
                            board[r][c] = n;
                            count = countSolutions(board, count);
                            board[r][c] = 0;
                        }
                    }
                    return count;
                }
            }
        }
        return count + 1; // found a complete solution
    }
}
