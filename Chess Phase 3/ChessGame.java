import javax.swing.*;

/**
 * ChessGame.java  (Phase 3 — entry point)
 *
 * Bootstraps the application on the Swing Event Dispatch Thread.
 *
 * <h3>How to run</h3>
 * <pre>
 *   javac *.java
 *   java ChessGame
 * </pre>
 *
 * Requires Java 11 or later.
 */
public class ChessGame {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Apply the system look-and-feel for native file chooser dialogs
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) { }

            GameState gameState = new GameState();
            new ChessFrame(gameState);
        });
    }
}
