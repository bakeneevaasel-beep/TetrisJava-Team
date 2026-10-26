import java.awt.Color;

/**
 * GameBoard represents the Tetris playing field.
 * Responsible for:
 * - 20x10 grid of cells
 * - Tracking occupied cells and their colors
 * - Detecting and clearing completed horizontal lines
 * - Managing player score and lines cleared
 *
 * Developer: Нурик (branch: feature/game-board)
 */
public class GameBoard {
    public static final int ROWS = 20;
    public static final int COLS = 10;

    // Line clearing scoring table: 0, 1 line, 2 lines, 3 lines, 4 lines (Tetris)
    private static final int[] LINE_SCORES = {0, 100, 300, 500, 800};

    private final Color[][] grid;
    private int score;
    private int linesClearedTotal;

    public GameBoard() {
        this.grid = new Color[ROWS][COLS];
        this.score = 0;
        this.linesClearedTotal = 0;
    }

    /**
     * Checks if given coordinates are within grid bounds.
     */
    public boolean isWithinBounds(int row, int col) {
        return row >= 0 && row < ROWS && col >= 0 && col < COLS;
    }

    /**
     * Checks if a cell is occupied by a locked block.
     * Cells above the board (row < 0) are considered empty.
     * Cells outside the left, right, and bottom boundaries are considered blocked/occupied.
     */
    public boolean isCellOccupied(int row, int col) {
        if (row < 0 && col >= 0 && col < COLS) {
            return false;
        }
        if (!isWithinBounds(row, col)) {
            return true;
        }
        return grid[row][col] != null;
    }

    /**
     * Sets the color of a specific cell on the board.
     */
    public void setCell(int row, int col, Color color) {
        if (isWithinBounds(row, col)) {
            grid[row][col] = color;
        }
    }

    /**
     * Gets the color of a cell, or null if empty or out of bounds.
     */
    public Color getCell(int row, int col) {
        if (isWithinBounds(row, col)) {
            return grid[row][col];
        }
        return null;
    }

    /**
     * Clears full horizontal lines, shifts rows down, and updates score.
     * @return number of cleared lines in this operation
     */
    public int clearFullLines() {
        int clearedCount = 0;

        for (int r = ROWS - 1; r >= 0; r--) {
            if (isRowFull(r)) {
                clearedCount++;
                shiftRowsDown(r);
                r++; // Recheck the same row index because rows shifted down
            }
        }

        if (clearedCount > 0) {
            linesClearedTotal += clearedCount;
            int points = clearedCount <= 4 ? LINE_SCORES[clearedCount] : LINE_SCORES[4] * clearedCount / 4;
            score += points;
        }

        return clearedCount;
    }

    private boolean isRowFull(int row) {
        for (int c = 0; c < COLS; c++) {
            if (grid[row][c] == null) {
                return false;
            }
        }
        return true;
    }

    private void shiftRowsDown(int clearedRow) {
        for (int r = clearedRow; r > 0; r--) {
            for (int c = 0; c < COLS; c++) {
                grid[r][c] = grid[r - 1][c];
            }
        }
        // Top row becomes empty
        for (int c = 0; c < COLS; c++) {
            grid[0][c] = null;
        }
    }

    /**
     * Adds bonus points directly to the score (e.g. for soft/hard drop).
     */
    public void addScore(int points) {
        if (points > 0) {
            this.score += points;
        }
    }

    /**
     * Resets the board, score, and cleared line count.
     */
    public void reset() {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                grid[r][c] = null;
            }
        }
        this.score = 0;
        this.linesClearedTotal = 0;
    }

    public int getScore() {
        return score;
    }

    public int getLinesClearedTotal() {
        return linesClearedTotal;
    }

    public int getRows() {
        return ROWS;
    }

    public int getCols() {
        return COLS;
    }
}
