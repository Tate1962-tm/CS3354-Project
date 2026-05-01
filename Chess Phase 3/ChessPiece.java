/**
 * ChessPiece.java
 *
 * Represents a single chess piece with a type, color, and board position.
 * Provides Unicode symbols, algebraic short names, and a deep-copy method
 * for use in MoveValidator simulations.
 */
public class ChessPiece {

    private PieceType   type;
    private PlayerColor color;
    private int         row;
    private int         col;

    /**
     * Constructs a chess piece.
     *
     * @param type  piece type (PAWN, ROOK, …)
     * @param color WHITE or BLACK
     * @param row   current row (0 = black back rank)
     * @param col   current column (0 = a-file)
     */
    public ChessPiece(PieceType type, PlayerColor color, int row, int col) {
        this.type  = type;
        this.color = color;
        this.row   = row;
        this.col   = col;
    }

    // -------------------------------------------------------------------------
    // Getters / setters
    // -------------------------------------------------------------------------

    public PieceType   getType()  { return type; }
    public PlayerColor getColor() { return color; }
    public int         getRow()   { return row; }
    public int         getCol()   { return col; }

    public void setRow(int row) { this.row = row; }
    public void setCol(int col) { this.col = col; }

    /** Promotes this piece (changes its type in-place). */
    public void promote(PieceType newType) { this.type = newType; }

    // -------------------------------------------------------------------------
    // Display helpers
    // -------------------------------------------------------------------------

    /**
     * Returns the Unicode chess symbol for this piece.
     * White pieces use hollow glyphs; black pieces use filled glyphs.
     */
    public String getSymbol() {
        if (color == PlayerColor.WHITE) {
            switch (type) {
                case KING:   return "\u2654";
                case QUEEN:  return "\u2655";
                case ROOK:   return "\u2656";
                case BISHOP: return "\u2657";
                case KNIGHT: return "\u2658";
                case PAWN:   return "\u2659";
            }
        } else {
            switch (type) {
                case KING:   return "\u265A";
                case QUEEN:  return "\u265B";
                case ROOK:   return "\u265C";
                case BISHOP: return "\u265D";
                case KNIGHT: return "\u265E";
                case PAWN:   return "\u265F";
            }
        }
        return "?";
    }

    /**
     * Returns a two-character algebraic abbreviation used in move history,
     * e.g. "WK" for White King, "BN" for Black Knight.
     */
    public String getShortName() {
        String prefix = (color == PlayerColor.WHITE) ? "W" : "B";
        switch (type) {
            case KING:   return prefix + "K";
            case QUEEN:  return prefix + "Q";
            case ROOK:   return prefix + "R";
            case BISHOP: return prefix + "B";
            case KNIGHT: return prefix + "N";
            case PAWN:   return prefix + "P";
            default:     return "??";
        }
    }

    /**
     * Creates a deep copy of this piece (same type, color, and position).
     * Used by MoveValidator when simulating moves on a cloned board.
     */
    public ChessPiece copy() {
        return new ChessPiece(type, color, row, col);
    }

    @Override
    public String toString() {
        return color + " " + type + " @(" + row + "," + col + ")";
    }
}
