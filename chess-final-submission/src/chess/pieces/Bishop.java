package chess.pieces;

import chess.position.Position;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Bishop chess piece.
 * Bishops move any number of squares diagonally.
 */
public class Bishop extends Piece {

    /**
     * Constructs a Bishop with the given color and position.
     *
     * @param color    the color of the bishop
     * @param position the starting position of the bishop
     */
    public Bishop(Color color, Position position) {
        super(color, position);
    }

    /**
     * Computes all valid diagonal moves for this bishop.
     * Stops at the first piece encountered; can capture an opponent's piece.
     *
     * @param board the current 8x8 board state
     * @return list of valid destination positions
     */
    @Override
    public List<Position> possibleMoves(Piece[][] board) {
        List<Position> moves = new ArrayList<>();
        int[][] directions = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};

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
     * Returns the display symbol for this bishop.
     *
     * @return "wB" for white, "bB" for black
     */
    @Override
    public String getSymbol() {
        return (color == Color.WHITE) ? "wB" : "bB";
    }
}
