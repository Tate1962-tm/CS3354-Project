package chess.position;

/**
 * Represents a position on the chessboard using row and column indices.
 * Rows are 0-7 (corresponding to ranks 1-8) and columns are 0-7 (corresponding to files A-H).
 */
public class Position {

    /** The row index (0 = rank 1, 7 = rank 8). */
    private int row;

    /** The column index (0 = file A, 7 = file H). */
    private int col;

    /**
     * Constructs a Position with the given row and column.
     *
     * @param row the row index (0–7)
     * @param col the column index (0–7)
     */
    public Position(int row, int col) {
        this.row = row;
        this.col = col;
    }

    /**
     * Returns the row index.
     *
     * @return the row index (0–7)
     */
    public int getRow() {
        return row;
    }

    /**
     * Returns the column index.
     *
     * @return the column index (0–7)
     */
    public int getCol() {
        return col;
    }

    /**
     * Parses a chess notation string (e.g., "E2") into a Position.
     * Column letters A–H map to indices 0–7; row numbers 1–8 map to indices 0–7.
     *
     * @param notation the algebraic notation string (e.g., "E2")
     * @return the corresponding Position, or null if the notation is invalid
     */
    public static Position fromNotation(String notation) {
        if (notation == null || notation.length() != 2) return null;
        char file = Character.toUpperCase(notation.charAt(0));
        char rank = notation.charAt(1);
        if (file < 'A' || file > 'H') return null;
        if (rank < '1' || rank > '8') return null;
        int col = file - 'A';
        int row = rank - '1';
        return new Position(row, col);
    }

    /**
     * Converts this Position to standard chess notation (e.g., "E2").
     *
     * @return the algebraic notation string
     */
    public String toNotation() {
        char file = (char) ('A' + col);
        char rank = (char) ('1' + row);
        return "" + file + rank;
    }

    /**
     * Checks whether this position is within the valid board bounds (0–7 for both row and col).
     *
     * @return true if the position is on the board
     */
    public boolean isValid() {
        return row >= 0 && row <= 7 && col >= 0 && col <= 7;
    }

    /**
     * Checks equality based on row and column values.
     *
     * @param obj the object to compare
     * @return true if both row and column match
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Position)) return false;
        Position other = (Position) obj;
        return this.row == other.row && this.col == other.col;
    }

    /**
     * Returns a hash code based on row and column.
     *
     * @return hash code
     */
    @Override
    public int hashCode() {
        return 31 * row + col;
    }

    /**
     * Returns the notation string representation of this position.
     *
     * @return algebraic notation (e.g., "E2")
     */
    @Override
    public String toString() {
        return toNotation();
    }
}
