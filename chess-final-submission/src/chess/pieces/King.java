package chess.pieces;

import chess.position.Position;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a King chess piece.
 * The King moves exactly one square in any direction.
 * Tracks movement state for castling eligibility.
 */
public class King extends Piece {

    /** Tracks whether this king has moved, used for castling eligibility. */
    private boolean hasMoved;

    /**
     * Constructs a King with the given color and position.
     *
     * @param color    the color of the king
     * @param position the starting position of the king
     */
    public King(Color color, Position position) {
        super(color, position);
        this.hasMoved = false;
    }

    /**
     * Returns whether this king has moved from its starting square.
     *
     * @return true if the king has moved
     */
    public boolean hasMoved() {
        return hasMoved;
    }

    /**
     * Marks this king as having moved.
     */
    public void setHasMoved() {
        this.hasMoved = true;
    }

    /**
     * Computes all one-square moves in any direction for this king.
     * Only includes squares that are empty or occupied by an opponent's piece.
     * Does NOT filter out moves that would put the king in check
     * (that filtering is done by the Board class).
     *
     * @param board the current 8x8 board state
     * @return list of valid destination positions
     */
    @Override
    public List<Position> possibleMoves(Piece[][] board) {
        List<Position> moves = new ArrayList<>();
        int[][] directions = {
            {1, 0}, {-1, 0}, {0, 1}, {0, -1},
            {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
        };

        for (int[] dir : directions) {
            int r = position.getRow() + dir[0];
            int c = position.getCol() + dir[1];
            Position dest = new Position(r, c);
            if (dest.isValid()) {
                Piece target = board[r][c];
                if (target == null || target.getColor() != this.color) {
                    moves.add(dest);
                }
            }
        }
        return moves;
    }

    /**
     * Returns the display symbol for this king.
     *
     * @return "wK" for white, "bK" for black
     */
    @Override
    public String getSymbol() {
        return (color == Color.WHITE) ? "wK" : "bK";
    }
}
