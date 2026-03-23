package chess.pieces;

import chess.position.Position;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Knight chess piece.
 * Knights move in an L-shape: two squares in one direction and one square perpendicular.
 * Knights can jump over other pieces.
 */
public class Knight extends Piece {

    /**
     * Constructs a Knight with the given color and position.
     *
     * @param color    the color of the knight
     * @param position the starting position of the knight
     */
    public Knight(Color color, Position position) {
        super(color, position);
    }

    /**
     * Computes all valid L-shaped moves for this knight.
     * A knight can land on any square reachable by an L-move that is either empty
     * or occupied by an opponent's piece.
     *
     * @param board the current 8x8 board state
     * @return list of valid destination positions
     */
    @Override
    public List<Position> possibleMoves(Piece[][] board) {
        List<Position> moves = new ArrayList<>();
        int[][] jumps = {
            {2, 1}, {2, -1}, {-2, 1}, {-2, -1},
            {1, 2}, {1, -2}, {-1, 2}, {-1, -2}
        };

        for (int[] jump : jumps) {
            int r = position.getRow() + jump[0];
            int c = position.getCol() + jump[1];
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
     * Returns the display symbol for this knight.
     *
     * @return "wN" for white, "bN" for black
     */
    @Override
    public String getSymbol() {
        return (color == Color.WHITE) ? "wN" : "bN";
    }
}
