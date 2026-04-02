package chess;

/**
 * Represents a chess piece with a type and color.
 * Immutable value object used on the board.
 */
public class Piece {

    /** The two player colors. */
    public enum Color { WHITE, BLACK }

    /** The six piece types. */
    public enum Type { KING, QUEEN, ROOK, BISHOP, KNIGHT, PAWN }

    private final Color color;
    private final Type type;

    /**
     * Constructs a Piece with the given color and type.
     *
     * @param color the piece's color
     * @param type  the piece's type
     */
    public Piece(Color color, Type type) {
        this.color = color;
        this.type = type;
    }

    /** @return the color of this piece */
    public Color getColor() { return color; }

    /** @return the type of this piece */
    public Type getType() { return type; }

    /**
     * Returns the Unicode chess symbol for this piece.
     *
     * @return a single Unicode character string
     */
    public String getSymbol() {
        return switch (color) {
            case WHITE -> switch (type) {
                case KING   -> "\u2654";
                case QUEEN  -> "\u2655";
                case ROOK   -> "\u2656";
                case BISHOP -> "\u2657";
                case KNIGHT -> "\u2658";
                case PAWN   -> "\u2659";
            };
            case BLACK -> switch (type) {
                case KING   -> "\u265A";
                case QUEEN  -> "\u265B";
                case ROOK   -> "\u265C";
                case BISHOP -> "\u265D";
                case KNIGHT -> "\u265E";
                case PAWN   -> "\u265F";
            };
        };
    }

    @Override
    public String toString() {
        return color + "_" + type;
    }
}
