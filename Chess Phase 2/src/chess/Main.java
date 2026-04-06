package chess;

import chess.controller.GameController;
import chess.view.ChessFrame;

import javax.swing.*;

/**
 * Entry point for the Chess Game Phase 2 application.
 * Launches the GUI on the Swing Event Dispatch Thread.
 */
public class Main {

    /**
     * Application main method.
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Apply system look-and-feel for native file dialogs
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) { }

            GameController controller = new GameController();
            new ChessFrame(controller);
        });
    }
}
