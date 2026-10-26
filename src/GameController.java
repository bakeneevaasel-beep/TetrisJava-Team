/**
 * GameController coordinates game rules and mechanics.
 * Responsible for:
 * - Moving and rotating active pieces
 * - Checking collisions with borders and locked blocks
 * - Locking pieces onto the GameBoard
 * - Handling game state (play, pause, game over, restart)
 *
 * Developer: Актан (branch: feature/game-controller)
 */
public class GameController {
    private final GameBoard board;
    private Tetromino currentPiece;
    private Tetromino nextPiece;
    private int currentX;
    private int currentY;

    private boolean gameOver;
    private boolean paused;

    public GameController(GameBoard board) {
        this.board = board;
        resetGame();
    }

    /**
     * Resets the game to initial playing state.
     */
    public void resetGame() {
        board.reset();
        gameOver = false;
        paused = false;
        nextPiece = Tetromino.getRandomTetromino();
        spawnPiece();
    }

    /**
     * Spawns the next piece at the top center of the board.
     * Sets game over if the spawned piece immediately collides.
     */
    public void spawnPiece() {
        currentPiece = (nextPiece != null) ? nextPiece : Tetromino.getRandomTetromino();
        nextPiece = Tetromino.getRandomTetromino();

        // Center piece horizontally on 10-column board
        currentX = 3;
        // Since row 0 in 4x4 matrix is blank, currentY = -1 places the first block at board row 0
        currentY = -1;

        if (checkCollision(currentPiece, currentX, currentY)) {
            gameOver = true;
        }
    }

    /**
     * Checks if a piece collides with board boundaries or locked blocks.
     */
    public boolean checkCollision(Tetromino piece, int targetX, int targetY) {
        if (piece == null) {
            return false;
        }

        int[][] shape = piece.getShape();
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                if (shape[r][c] != 0) {
                    int boardX = targetX + c;
                    int boardY = targetY + r;

                    // Left, right, bottom boundary checks
                    if (boardX < 0 || boardX >= GameBoard.COLS || boardY >= GameBoard.ROWS) {
                        return true;
                    }

                    // Collision with locked blocks (ignore space above top edge)
                    if (boardY >= 0 && board.isCellOccupied(boardY, boardX)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Moves active piece left by 1 column if unobstructed.
     */
    public boolean moveLeft() {
        if (paused || gameOver) {
            return false;
        }
        if (!checkCollision(currentPiece, currentX - 1, currentY)) {
            currentX--;
            return true;
        }
        return false;
    }

    /**
     * Moves active piece right by 1 column if unobstructed.
     */
    public boolean moveRight() {
        if (paused || gameOver) {
            return false;
        }
        if (!checkCollision(currentPiece, currentX + 1, currentY)) {
            currentX++;
            return true;
        }
        return false;
    }

    /**
     * Moves active piece down by 1 row.
     * If obstructed, locks the piece and clears lines.
     * @return true if moved down, false if locked
     */
    public boolean moveDown() {
        if (paused || gameOver) {
            return false;
        }

        if (!checkCollision(currentPiece, currentX, currentY + 1)) {
            currentY++;
            return true;
        } else {
            lockCurrentPiece();
            return false;
        }
    }

    /**
     * Soft drop: moves down 1 step and awards 1 bonus point.
     */
    public boolean softDrop() {
        if (paused || gameOver) {
            return false;
        }
        boolean moved = moveDown();
        if (moved) {
            board.addScore(1);
        }
        return moved;
    }

    /**
     * Hard drop: instantaneously drops piece to lowest valid position and locks it.
     */
    public void hardDrop() {
        if (paused || gameOver) {
            return;
        }

        int dropDistance = 0;
        while (!checkCollision(currentPiece, currentX, currentY + 1)) {
            currentY++;
            dropDistance++;
        }
        board.addScore(dropDistance * 2);
        lockCurrentPiece();
    }

    /**
     * Rotates active piece clockwise with basic wall-kick support.
     */
    public boolean rotate() {
        if (paused || gameOver || currentPiece == null) {
            return false;
        }

        Tetromino rotated = currentPiece.rotateClockwise();

        // 1. Try rotation at current position
        if (!checkCollision(rotated, currentX, currentY)) {
            currentPiece = rotated;
            return true;
        }

        // 2. Wall kick attempts: 1 block left, 1 block right, 2 blocks left, 2 blocks right
        int[] kickOffsets = {-1, 1, -2, 2};
        for (int offset : kickOffsets) {
            if (!checkCollision(rotated, currentX + offset, currentY)) {
                currentPiece = rotated;
                currentX += offset;
                return true;
            }
        }

        return false;
    }

    /**
     * Transfers blocks of the current piece onto the board and clears lines.
     */
    public void lockCurrentPiece() {
        if (currentPiece == null) {
            return;
        }

        int[][] shape = currentPiece.getShape();
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                if (shape[r][c] != 0) {
                    int boardX = currentX + c;
                    int boardY = currentY + r;

                    if (boardY < 0) {
                        // Locked above board -> game over
                        gameOver = true;
                    } else {
                        board.setCell(boardY, boardX, currentPiece.getColor());
                    }
                }
            }
        }

        board.clearFullLines();

        if (!gameOver) {
            spawnPiece();
        }
    }

    /**
     * Calculates the Y coordinate where the current piece would land if dropped.
     */
    public int getGhostY() {
        if (currentPiece == null) {
            return currentY;
        }
        int ghostY = currentY;
        while (!checkCollision(currentPiece, currentX, ghostY + 1)) {
            ghostY++;
        }
        return ghostY;
    }

    public void togglePause() {
        if (!gameOver) {
            paused = !paused;
        }
    }

    public void setPaused(boolean paused) {
        if (!gameOver) {
            this.paused = paused;
        }
    }

    public boolean isPaused() {
        return paused;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public GameBoard getBoard() {
        return board;
    }

    public Tetromino getCurrentPiece() {
        return currentPiece;
    }

    public Tetromino getNextPiece() {
        return nextPiece;
    }

    public int getCurrentX() {
        return currentX;
    }

    public int getCurrentY() {
        return currentY;
    }
}
