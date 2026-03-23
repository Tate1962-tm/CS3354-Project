import chess.game.Game;

/**
 * Entry point for the Java Chess application.
 * Creates a new game instance and starts it.
 */
public class Main {

    /**
     * Main method that launches the chess game.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        Game game = new Game();
        game.start();
    }
}
