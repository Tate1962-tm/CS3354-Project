package chess.model;

import java.io.Serializable;

/**
 * Represents a single move made during the game.
 * Stores all information needed to display history and support undo.
 */
public class Move implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Piece piece;
    private final int fromRow;
    private final int fromCol;
    private final int toRow;
    private final int toCol;
    private final Piece capturedPiece; // null if no capture

    /**
     * Constructs a Move record.
     * @param piece         the piece that moved
     * @param fromRow       starting row (0-7)
     * @param fromCol       starting column (0-7)
     * @param toRow         destination row (0-7)
     * @param toCol         destination column (0-7)
     * @param capturedPiece the piece captured, or null
     */
    public Move(Piece piece, int fromRow, int fromCol, int toRow, int toCol, Piece capturedPiece) {
        this.piece = piece;
        this.fromRow = fromRow;
        this.fromCol = fromCol;
        this.toRow = toRow;
        this.toCol = toCol;
        this.capturedPiece = capturedPiece;
    }

    /** @return the piece that moved */
    public Piece getPiece() { return piece; }

    /** @return starting row */
    public int getFromRow() { return fromRow; }

    /** @return starting column */
    public int getFromCol() { return fromCol; }

    /** @return destination row */
    public int getToRow() { return toRow; }

    /** @return destination column */
    public int getToCol() { return toCol; }

    /** @return the captured piece, or null if none */
    public Piece getCapturedPiece() { return capturedPiece; }

    /**
     * Returns a human-readable description of this move.
     * Uses standard chess column labels (a-h) and row labels (1-8).
     * @return move description string
     */
    @Override
    public String toString() {
        String from = "" + (char)('a' + fromCol) + (8 - fromRow);
        String to   = "" + (char)('a' + toCol)   + (8 - toRow);
        String desc = piece.getLabel() + ": " + from + " → " + to;
        if (capturedPiece != null) {
            desc += " ✕" + capturedPiece.getLabel();
        }
        return desc;
    }
}
