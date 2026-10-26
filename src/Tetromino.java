import java.awt.Color;
import java.util.Random;

/**
 * Tetromino represents one of the 7 classic Tetris pieces.
 * Responsible for:
 * - 7 figure types (I, O, T, S, Z, J, L)
 * - 4x4 matrix representation
 * - Figure rotation (90 degrees clockwise)
 * - Color and type mapping
 *
 * Developer: Бека (branch: feature/tetromino)
 */
public class Tetromino {
    public enum ShapeType {
        I, O, T, S, Z, J, L
    }

    private static final Random RANDOM = new Random();

    private final ShapeType type;
    private final Color color;
    private final int[][] shape; // 4x4 matrix

    public Tetromino(ShapeType type, Color color, int[][] shape) {
        this.type = type;
        this.color = color;
        this.shape = deepCopyMatrix(shape);
    }

    /**
     * Creates a new Tetromino with default shape and color for the specified type.
     */
    public static Tetromino create(ShapeType type) {
        switch (type) {
            case I:
                return new Tetromino(type, new Color(0, 220, 220), new int[][]{
                    {0, 0, 0, 0},
                    {1, 1, 1, 1},
                    {0, 0, 0, 0},
                    {0, 0, 0, 0}
                });
            case O:
                return new Tetromino(type, new Color(245, 215, 20), new int[][]{
                    {0, 0, 0, 0},
                    {0, 1, 1, 0},
                    {0, 1, 1, 0},
                    {0, 0, 0, 0}
                });
            case T:
                return new Tetromino(type, new Color(170, 50, 230), new int[][]{
                    {0, 0, 0, 0},
                    {0, 1, 0, 0},
                    {1, 1, 1, 0},
                    {0, 0, 0, 0}
                });
            case S:
                return new Tetromino(type, new Color(60, 215, 60), new int[][]{
                    {0, 0, 0, 0},
                    {0, 1, 1, 0},
                    {1, 1, 0, 0},
                    {0, 0, 0, 0}
                });
            case Z:
                return new Tetromino(type, new Color(235, 55, 55), new int[][]{
                    {0, 0, 0, 0},
                    {1, 1, 0, 0},
                    {0, 1, 1, 0},
                    {0, 0, 0, 0}
                });
            case J:
                return new Tetromino(type, new Color(55, 95, 235), new int[][]{
                    {0, 0, 0, 0},
                    {1, 0, 0, 0},
                    {1, 1, 1, 0},
                    {0, 0, 0, 0}
                });
            case L:
                return new Tetromino(type, new Color(245, 145, 25), new int[][]{
                    {0, 0, 0, 0},
                    {0, 0, 1, 0},
                    {1, 1, 1, 0},
                    {0, 0, 0, 0}
                });
            default:
                throw new IllegalArgumentException("Unknown shape type: " + type);
        }
    }

    /**
     * Returns a random Tetromino among the 7 standard shapes.
     */
    public static Tetromino getRandomTetromino() {
        ShapeType[] types = ShapeType.values();
        ShapeType randomType = types[RANDOM.nextInt(types.length)];
        return create(randomType);
    }

    /**
     * Rotates the 4x4 matrix 90 degrees clockwise and returns a new Tetromino.
     */
    public Tetromino rotateClockwise() {
        if (this.type == ShapeType.O) {
            // O piece does not need rotation
            return copy();
        }

        int[][] rotated = new int[4][4];
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                rotated[c][3 - r] = this.shape[r][c];
            }
        }
        return new Tetromino(this.type, this.color, rotated);
    }

    /**
     * Creates a deep copy of this Tetromino.
     */
    public Tetromino copy() {
        return new Tetromino(this.type, this.color, this.shape);
    }

    public ShapeType getType() {
        return type;
    }

    public Color getColor() {
        return color;
    }

    public int[][] getShape() {
        return deepCopyMatrix(shape);
    }

    private static int[][] deepCopyMatrix(int[][] source) {
        int[][] target = new int[4][4];
        for (int r = 0; r < 4; r++) {
            System.arraycopy(source[r], 0, target[r], 0, 4);
        }
        return target;
    }
}
