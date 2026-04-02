package chess;

import javax.swing.SwingUtilities;

/**
 * Entry point for the Chess Game application.
 * Launches the GUI on the Swing Event Dispatch Thread.
 */
public class Main {
    /**
     * Main method — starts the chess application.
     *
     * @param args command-line arguments (unused)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ChessFrame frame = new ChessFrame();
            frame.setVisible(true);
        });
    }
}
