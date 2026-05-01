/**
 * PlayerColor.java
 * Enum representing the two players in chess.
 */
public enum PlayerColor {
    WHITE, BLACK;

    /** Returns the opponent's color. */
    public PlayerColor opposite() {
        return this == WHITE ? BLACK : WHITE;
    }
}
