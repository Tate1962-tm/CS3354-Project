package chess.pieces;

import chess.position.Position;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Rook chess piece.
 * Rooks move any number of squares horizontally or vertically.
 */
public class Rook extends Piece {

    /** Tracks whether this rook has moved, used for castling eligibility. */
    private boolean hasMoved;

    /**
     * Constructs a Rook with the given color and position.
     *
     * @param color    the color of the rook
     * @param position the starting position of the rook
     */
    public Rook(Color color, Position position) {
        super(color, position);
        this.hasMoved = false;
    }

    /**
     * Returns whether this rook has moved from its starting square.
     *
     * @return true if the rook has moved
     */
    public boolean hasMoved() {
        return hasMoved;
    }

    /**
     * Marks this rook as having moved.
     */
    public void setHasMoved() {
        this.hasMoved = true;
    }

    /**
     * Computes all valid moves for the rook along ranks and files.
     * Stops at the first piece encountered; can capture an opponent's piece.
     *
     * @param board the current 8x8 board state
     * @return list of valid destination positions
     */
    @Override
    public List<Position> possibleMoves(Piece[][] board) {
        List<Position> moves = new ArrayList<>();
        int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

        for (int[] dir : directions) {
            int r = position.getRow() + dir[0];
            int c = position.getCol() + dir[1];
            while (r >= 0 && r <= 7 && c >= 0 && c <= 7) {
                Piece target = board[r][c];
                if (target == null) {
                    moves.add(new Position(r, c));
                } else {
                    if (target.getColor() != this.color) {
                        moves.add(new Position(r, c));
                    }
                    break;
                }
                r += dir[0];
                c += dir[1];
            }
        }
        return moves;
    }

    /**
     * Returns the display symbol for this rook.
     *
     * @return "wR" for white, "bR" for black
     */
    @Override
    public String getSymbol() {
        return (color == Color.WHITE) ? "wR" : "bR";
    }
}
