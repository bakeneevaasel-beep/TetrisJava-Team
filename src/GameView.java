import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

/**
 * GameView renders the Tetris interface using Swing.
 * Responsible for:
 * - Rendering game grid, locked blocks, and falling piece
 * - Rendering ghost piece projection
 * - Sidebar with score, cleared lines, and next piece preview
 * - Pause and Game Over screens
 * - Keyboard input handling via InputMap and ActionMap
 *
 * Developer: Айпери (branch: feature/game-view)
 */
public class GameView extends JPanel {
    public static final int CELL_SIZE = 30;
    public static final int BOARD_WIDTH = GameBoard.COLS * CELL_SIZE; // 300px
    public static final int BOARD_HEIGHT = GameBoard.ROWS * CELL_SIZE; // 600px
    public static final int SIDEBAR_WIDTH = 200; // 200px
    public static final int TOTAL_WIDTH = BOARD_WIDTH + SIDEBAR_WIDTH; // 500px

    // Palette
    private static final Color BG_BOARD = new Color(24, 25, 32);
    private static final Color BG_SIDEBAR = new Color(32, 34, 44);
    private static final Color GRID_LINE = new Color(42, 45, 58);
    private static final Color TEXT_PRIMARY = new Color(240, 240, 245);
    private static final Color TEXT_MUTED = new Color(160, 165, 185);
    private static final Color ACCENT_COLOR = new Color(74, 144, 226);
    private static final Color PREVIEW_BG = new Color(20, 21, 28);

    private final GameController controller;
    private final Runnable restartHandler;

    public GameView(GameController controller, Runnable restartHandler) {
        this.controller = controller;
        this.restartHandler = restartHandler;

        setPreferredSize(new Dimension(TOTAL_WIDTH, BOARD_HEIGHT));
        setBackground(BG_BOARD);
        setFocusable(true);

        setupKeyBindings();
    }

    /**
     * Configures keyboard controls using InputMap and ActionMap.
     */
    private void setupKeyBindings() {
        InputMap im = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getActionMap();

        // Left
        bindKey(im, am, "LEFT", KeyEvent.VK_LEFT, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (controller.moveLeft()) {
                    repaint();
                }
            }
        });
        bindKey(im, am, "A", KeyEvent.VK_A, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (controller.moveLeft()) {
                    repaint();
                }
            }
        });

        // Right
        bindKey(im, am, "RIGHT", KeyEvent.VK_RIGHT, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (controller.moveRight()) {
                    repaint();
                }
            }
        });
        bindKey(im, am, "D", KeyEvent.VK_D, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (controller.moveRight()) {
                    repaint();
                }
            }
        });

        // Soft drop
        bindKey(im, am, "DOWN", KeyEvent.VK_DOWN, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.softDrop();
                repaint();
            }
        });
        bindKey(im, am, "S", KeyEvent.VK_S, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.softDrop();
                repaint();
            }
        });

        // Rotate
        bindKey(im, am, "UP", KeyEvent.VK_UP, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (controller.rotate()) {
                    repaint();
                }
            }
        });
        bindKey(im, am, "W", KeyEvent.VK_W, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (controller.rotate()) {
                    repaint();
                }
            }
        });

        // Hard drop
        bindKey(im, am, "SPACE", KeyEvent.VK_SPACE, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.hardDrop();
                repaint();
            }
        });

        // Pause
        bindKey(im, am, "P", KeyEvent.VK_P, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.togglePause();
                repaint();
            }
        });

        // Restart
        bindKey(im, am, "R", KeyEvent.VK_R, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (restartHandler != null) {
                    restartHandler.run();
                } else {
                    controller.resetGame();
                }
                repaint();
            }
        });
    }

    private void bindKey(InputMap im, ActionMap am, String name, int keyCode, AbstractAction action) {
        im.put(KeyStroke.getKeyStroke(keyCode, 0), name);
        am.put(name, action);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // 1. Draw playing board grid & locked blocks
        drawGameBoard(g2);

        // 2. Draw ghost piece projection
        drawGhostPiece(g2);

        // 3. Draw active falling piece
        drawActivePiece(g2);

        // 4. Draw sidebar (stats, preview, instructions)
        drawSidebar(g2);

        // 5. Draw overlay if paused or game over
        if (controller.isGameOver()) {
            drawGameOverOverlay(g2);
        } else if (controller.isPaused()) {
            drawPausedOverlay(g2);
        }
    }

    private void drawGameBoard(Graphics2D g) {
        GameBoard board = controller.getBoard();

        // Fill board background
        g.setColor(BG_BOARD);
        g.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);

        // Grid lines
        g.setColor(GRID_LINE);
        for (int r = 0; r <= GameBoard.ROWS; r++) {
            g.drawLine(0, r * CELL_SIZE, BOARD_WIDTH, r * CELL_SIZE);
        }
        for (int c = 0; c <= GameBoard.COLS; c++) {
            g.drawLine(c * CELL_SIZE, 0, c * CELL_SIZE, BOARD_HEIGHT);
        }

        // Locked cells
        for (int r = 0; r < GameBoard.ROWS; r++) {
            for (int c = 0; c < GameBoard.COLS; c++) {
                Color color = board.getCell(r, c);
                if (color != null) {
                    drawBlock(g, c * CELL_SIZE, r * CELL_SIZE, color, false);
                }
            }
        }
    }

    private void drawActivePiece(Graphics2D g) {
        Tetromino piece = controller.getCurrentPiece();
        if (piece == null || controller.isGameOver()) {
            return;
        }

        int[][] shape = piece.getShape();
        int originX = controller.getCurrentX();
        int originY = controller.getCurrentY();
        Color color = piece.getColor();

        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                if (shape[r][c] != 0) {
                    int boardX = (originX + c) * CELL_SIZE;
                    int boardY = (originY + r) * CELL_SIZE;
                    if (originY + r >= 0) {
                        drawBlock(g, boardX, boardY, color, false);
                    }
                }
            }
        }
    }

    private void drawGhostPiece(Graphics2D g) {
        Tetromino piece = controller.getCurrentPiece();
        if (piece == null || controller.isGameOver() || controller.isPaused()) {
            return;
        }

        int[][] shape = piece.getShape();
        int originX = controller.getCurrentX();
        int ghostY = controller.getGhostY();
        Color color = piece.getColor();

        // Do not draw ghost if it overlaps active piece
        if (ghostY == controller.getCurrentY()) {
            return;
        }

        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                if (shape[r][c] != 0) {
                    int boardX = (originX + c) * CELL_SIZE;
                    int boardY = (ghostY + r) * CELL_SIZE;
                    if (ghostY + r >= 0) {
                        drawBlock(g, boardX, boardY, color, true);
                    }
                }
            }
        }
    }

    /**
     * Draws an individual 3D-styled block or a translucent ghost block.
     */
    private void drawBlock(Graphics2D g, int x, int y, Color baseColor, boolean isGhost) {
        if (isGhost) {
            Color ghostFill = new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 40);
            Color ghostBorder = new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 160);
            g.setColor(ghostFill);
            g.fillRoundRect(x + 2, y + 2, CELL_SIZE - 4, CELL_SIZE - 4, 6, 6);
            g.setColor(ghostBorder);
            g.drawRoundRect(x + 2, y + 2, CELL_SIZE - 4, CELL_SIZE - 4, 6, 6);
            return;
        }

        // Fill body
        g.setColor(baseColor);
        g.fillRoundRect(x + 1, y + 1, CELL_SIZE - 2, CELL_SIZE - 2, 6, 6);

        // Top-left highlight
        Color highlight = new Color(
            Math.min(255, baseColor.getRed() + 50),
            Math.min(255, baseColor.getGreen() + 50),
            Math.min(255, baseColor.getBlue() + 50)
        );
        g.setColor(highlight);
        g.drawLine(x + 2, y + 2, x + CELL_SIZE - 3, y + 2);
        g.drawLine(x + 2, y + 2, x + 2, y + CELL_SIZE - 3);

        // Bottom-right shadow
        Color shadow = new Color(
            Math.max(0, baseColor.getRed() - 60),
            Math.max(0, baseColor.getGreen() - 60),
            Math.max(0, baseColor.getBlue() - 60)
        );
        g.setColor(shadow);
        g.drawLine(x + CELL_SIZE - 2, y + 2, x + CELL_SIZE - 2, y + CELL_SIZE - 2);
        g.drawLine(x + 2, y + CELL_SIZE - 2, x + CELL_SIZE - 2, y + CELL_SIZE - 2);
    }

    private void drawSidebar(Graphics2D g) {
        int x = BOARD_WIDTH;
        int y = 0;

        // Background
        g.setColor(BG_SIDEBAR);
        g.fillRect(x, y, SIDEBAR_WIDTH, BOARD_HEIGHT);

        // Separator line
        g.setColor(GRID_LINE);
        g.drawLine(x, 0, x, BOARD_HEIGHT);

        int pad = 20;
        int curY = 30;

        // Title
        g.setColor(ACCENT_COLOR);
        g.setFont(new Font("Segoe UI", Font.BOLD, 22));
        g.drawString("TETRIS", x + pad, curY);

        curY += 35;
        // Score section
        g.setColor(TEXT_MUTED);
        g.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        g.drawString("СЧЁТ", x + pad, curY);

        curY += 25;
        g.setColor(TEXT_PRIMARY);
        g.setFont(new Font("Segoe UI", Font.BOLD, 24));
        g.drawString(String.valueOf(controller.getBoard().getScore()), x + pad, curY);

        curY += 35;
        // Lines section
        g.setColor(TEXT_MUTED);
        g.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        g.drawString("ЛИНИИ", x + pad, curY);

        curY += 25;
        g.setColor(TEXT_PRIMARY);
        g.setFont(new Font("Segoe UI", Font.BOLD, 20));
        g.drawString(String.valueOf(controller.getBoard().getLinesClearedTotal()), x + pad, curY);

        curY += 35;
        // Next piece label
        g.setColor(TEXT_MUTED);
        g.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        g.drawString("СЛЕДУЮЩАЯ:", x + pad, curY);

        curY += 15;
        // Next piece preview box
        int previewSize = 120;
        int previewX = x + pad;
        int previewY = curY;
        g.setColor(PREVIEW_BG);
        g.fillRoundRect(previewX, previewY, previewSize, previewSize, 8, 8);
        g.setColor(GRID_LINE);
        g.drawRoundRect(previewX, previewY, previewSize, previewSize, 8, 8);

        drawNextPiecePreview(g, previewX, previewY, previewSize);

        curY += previewSize + 30;

        // Controls help box
        g.setColor(TEXT_MUTED);
        g.setFont(new Font("Segoe UI", Font.BOLD, 12));
        g.drawString("УПРАВЛЕНИЕ:", x + pad, curY);

        curY += 20;
        g.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        g.setColor(TEXT_PRIMARY);
        String[] controls = {
            "← / → , A / D : Движение",
            "↑ / W : Поворот",
            "↓ / S : Мягкий сброс",
            "Пробел : Быстрый сброс",
            "P : Пауза",
            "R : Перезапуск"
        };
        for (String line : controls) {
            g.drawString(line, x + pad, curY);
            curY += 18;
        }

        // Author note
        curY += 15;
        g.setColor(TEXT_MUTED);
        g.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        g.drawString("Team: 5 Students Edition", x + pad, curY);
    }

    private void drawNextPiecePreview(Graphics2D g, int px, int py, int boxSize) {
        Tetromino next = controller.getNextPiece();
        if (next == null) {
            return;
        }

        int[][] shape = next.getShape();
        Color color = next.getColor();
        int miniCell = 22;

        // Calculate bounding box of the piece to center it
        int minR = 4, maxR = -1, minC = 4, maxC = -1;
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                if (shape[r][c] != 0) {
                    if (r < minR) minR = r;
                    if (r > maxR) maxR = r;
                    if (c < minC) minC = c;
                    if (c > maxC) maxC = c;
                }
            }
        }

        int pieceW = (maxC - minC + 1) * miniCell;
        int pieceH = (maxR - minR + 1) * miniCell;
        int startX = px + (boxSize - pieceW) / 2 - minC * miniCell;
        int startY = py + (boxSize - pieceH) / 2 - minR * miniCell;

        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                if (shape[r][c] != 0) {
                    int bx = startX + c * miniCell;
                    int by = startY + r * miniCell;

                    g.setColor(color);
                    g.fillRoundRect(bx + 1, by + 1, miniCell - 2, miniCell - 2, 4, 4);
                    g.setColor(new Color(255, 255, 255, 70));
                    g.drawLine(bx + 2, by + 2, bx + miniCell - 3, by + 2);
                }
            }
        }
    }

    private void drawPausedOverlay(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);

        g.setColor(TEXT_PRIMARY);
        g.setFont(new Font("Segoe UI", Font.BOLD, 28));
        drawCenteredString(g, "ПАУЗА", 0, BOARD_WIDTH, BOARD_HEIGHT / 2 - 20);

        g.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        g.setColor(TEXT_MUTED);
        drawCenteredString(g, "Нажмите P для продолжения", 0, BOARD_WIDTH, BOARD_HEIGHT / 2 + 20);
    }

    private void drawGameOverOverlay(Graphics2D g) {
        g.setColor(new Color(15, 15, 20, 220));
        g.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);

        g.setColor(new Color(245, 70, 70));
        g.setFont(new Font("Segoe UI", Font.BOLD, 30));
        drawCenteredString(g, "ИГРА ОКОНЧЕНА", 0, BOARD_WIDTH, BOARD_HEIGHT / 2 - 50);

        g.setColor(TEXT_PRIMARY);
        g.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        drawCenteredString(g, "Итоговый счёт: " + controller.getBoard().getScore(), 0, BOARD_WIDTH, BOARD_HEIGHT / 2);

        g.setColor(TEXT_MUTED);
        g.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        drawCenteredString(g, "Нажмите R для новой игры", 0, BOARD_WIDTH, BOARD_HEIGHT / 2 + 40);
    }

    private void drawCenteredString(Graphics2D g, String text, int minX, int maxX, int y) {
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int x = minX + (maxX - minX - textWidth) / 2;
        g.drawString(text, x, y);
    }
}
