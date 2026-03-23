package chess.pieces;

import chess.position.Position;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Pawn chess piece.
 * Pawns move forward one square, or two squares from their starting rank.
 * They capture diagonally forward. Supports en passant tracking via hasMoved flag.
 */
public class Pawn extends Piece {

    /** Tracks whether this pawn has already moved (used to restrict two-square advance). */
    private boolean hasMoved;

    /**
     * Constructs a Pawn with the given color and position.
     *
     * @param color    the color of the pawn
     * @param position the starting position of the pawn
     */
    public Pawn(Color color, Position position) {
        super(color, position);
        this.hasMoved = false;
    }

    /**
     * Returns whether this pawn has moved from its starting square.
     *
     * @return true if the pawn has moved
     */
    public boolean hasMoved() {
        return hasMoved;
    }

    /**
     * Marks this pawn as having moved.
     */
    public void setHasMoved() {
        this.hasMoved = true;
    }

    /**
     * Computes the list of possible moves for this pawn.
     * White pawns move upward (increasing row); black pawns move downward (decreasing row).
     * Includes one-square advance, two-square advance from start, and diagonal captures.
     *
     * @param board the current 8x8 board state
     * @return list of valid destination positions
     */
    @Override
    public List<Position> possibleMoves(Piece[][] board) {
        List<Position> moves = new ArrayList<>();
        int row = position.getRow();
        int col = position.getCol();
        int direction = (color == Color.WHITE) ? 1 : -1;

        // One square forward
        Position oneForward = new Position(row + direction, col);
        if (oneForward.isValid() && board[oneForward.getRow()][oneForward.getCol()] == null) {
            moves.add(oneForward);

            // Two squares forward from starting rank
            if (!hasMoved) {
                Position twoForward = new Position(row + 2 * direction, col);
                if (twoForward.isValid() && board[twoForward.getRow()][twoForward.getCol()] == null) {
                    moves.add(twoForward);
                }
            }
        }

        // Diagonal captures
        int[] captureCols = {col - 1, col + 1};
        for (int captureCol : captureCols) {
            Position capturePos = new Position(row + direction, captureCol);
            if (capturePos.isValid()) {
                Piece target = board[capturePos.getRow()][capturePos.getCol()];
                if (target != null && target.getColor() != this.color) {
                    moves.add(capturePos);
                }
            }
        }

        return moves;
    }

    /**
     * Returns the display symbol for this pawn.
     *
     * @return "wp" for white, "bp" for black
     */
    @Override
    public String getSymbol() {
        return (color == Color.WHITE) ? "wp" : "bp";
    }
}
