package chess.model;

import java.io.Serializable;

/**
 * Represents a single chess piece with a type and color.
 * Implements Serializable to support save/load functionality.
 */
public class Piece implements Serializable {
    private static final long serialVersionUID = 1L;

    private final PieceType type;
    private final PlayerColor color;

    /**
     * Constructs a chess piece.
     * @param type  the type of piece (e.g., PAWN, KING)
     * @param color the color of the piece (WHITE or BLACK)
     */
    public Piece(PieceType type, PlayerColor color) {
        this.type = type;
        this.color = color;
    }

    /** @return the type of this piece */
    public PieceType getType() { return type; }

    /** @return the color of this piece */
    public PlayerColor getColor() { return color; }

    /**
     * Returns a Unicode symbol representing this piece.
     * @return Unicode chess symbol string
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
     * Returns a short algebraic notation label for the piece.
     * @return short string label, e.g. "WK" for White King
     */
    public String getLabel() {
        String c = (color == PlayerColor.WHITE) ? "W" : "B";
        switch (type) {
            case KING:   return c + "K";
            case QUEEN:  return c + "Q";
            case ROOK:   return c + "R";
            case BISHOP: return c + "B";
            case KNIGHT: return c + "N";
            case PAWN:   return c + "P";
            default:     return "??";
        }
    }

    @Override
    public String toString() {
        return color + " " + type;
    }
}
