package chess.model;

/**
 * Enum representing the two players in chess.
 */
public enum PlayerColor {
    WHITE, BLACK;

    /**
     * Returns the opposite color.
     * @return the opponent's color
     */
    public PlayerColor opposite() {
        return this == WHITE ? BLACK : WHITE;
    }
}
