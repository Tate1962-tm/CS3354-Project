package chess.pieces;

import chess.position.Position;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Queen chess piece.
 * The Queen combines the movement of both the Rook and the Bishop,
 * moving any number of squares horizontally, vertically, or diagonally.
 */
public class Queen extends Piece {

    /**
     * Constructs a Queen with the given color and position.
     *
     * @param color    the color of the queen
     * @param position the starting position of the queen
     */
    public Queen(Color color, Position position) {
        super(color, position);
    }

    /**
     * Computes all valid moves for this queen.
     * Combines horizontal, vertical, and diagonal directions.
     * Stops at the first piece encountered; can capture an opponent's piece.
     *
     * @param board the current 8x8 board state
     * @return list of valid destination positions
     */
    @Override
    public List<Position> possibleMoves(Piece[][] board) {
        List<Position> moves = new ArrayList<>();
        // All 8 directions: rook + bishop combined
        int[][] directions = {
            {1, 0}, {-1, 0}, {0, 1}, {0, -1},
            {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
        };

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
     * Returns the display symbol for this queen.
     *
     * @return "wQ" for white, "bQ" for black
     */
    @Override
    public String getSymbol() {
        return (color == Color.WHITE) ? "wQ" : "bQ";
    }
}
