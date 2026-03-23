package chess.pieces;

import chess.position.Position;
import java.util.List;

/**
 * Abstract base class representing a chess piece.
 * All specific piece types (Pawn, Rook, Knight, Bishop, Queen, King) extend this class.
 */
public abstract class Piece {

    /**
     * Enum representing the color of a chess piece.
     */
    public enum Color {
        /** White player's piece. */
        WHITE,
        /** Black player's piece. */
        BLACK
    }

    /** The color of this piece (WHITE or BLACK). */
    protected Color color;

    /** The current position of this piece on the board. */
    protected Position position;

    /**
     * Constructs a Piece with the given color and starting position.
     *
     * @param color    the color of the piece
     * @param position the initial position of the piece
     */
    public Piece(Color color, Position position) {
        this.color = color;
        this.position = position;
    }

    /**
     * Returns the color of this piece.
     *
     * @return the piece's color
     */
    public Color getColor() {
        return color;
    }

    /**
     * Returns the current position of this piece.
     *
     * @return the piece's position
     */
    public Position getPosition() {
        return position;
    }

    /**
     * Sets the position of this piece.
     *
     * @param position the new position
     */
    public void setPosition(Position position) {
        this.position = position;
    }

    /**
     * Returns a list of all squares this piece can potentially move to,
     * given the current board state. Does not account for check legality.
     *
     * @param board the 8x8 board array of pieces (null = empty square)
     * @return list of valid destination positions
     */
    public abstract List<Position> possibleMoves(Piece[][] board);

    /**
     * Returns the two-character text symbol used to display this piece on the board.
     * White pieces start with 'w' and black pieces start with 'b'.
     *
     * @return the display symbol (e.g., "wp", "bR")
     */
    public abstract String getSymbol();

    /**
     * Returns a string representation of the piece (its symbol).
     *
     * @return the symbol string
     */
    @Override
    public String toString() {
        return getSymbol();
    }
}
