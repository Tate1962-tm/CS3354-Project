package chess.controller;

import chess.model.*;
import chess.view.BoardPanel;
import chess.view.ChessFrame;
import chess.view.HistoryPanel;

import javax.swing.*;
import java.io.*;

/**
 * Central controller that manages game flow, connects the Board model
 * to the view panels, handles save/load, new game, and undo actions.
 */
public class GameController {

    private Board         board;
    private BoardSettings settings;

    // View references (set after construction to avoid circular dependency)
    private ChessFrame   frame;
    private BoardPanel   boardPanel;
    private HistoryPanel historyPanel;

    /**
     * Constructs a new GameController with a fresh board and default settings.
     */
    public GameController() {
        board    = new Board();
        settings = new BoardSettings();
    }

    // -------------------------------------------------------------------------
    // View wiring
    // -------------------------------------------------------------------------

    /** @param frame the main application window */
    public void setFrame(ChessFrame frame)         { this.frame = frame; }

    /** @param panel the board rendering panel */
    public void setBoardPanel(BoardPanel panel)    { this.boardPanel = panel; }

    /** @param panel the history side panel */
    public void setHistoryPanel(HistoryPanel panel){ this.historyPanel = panel; }

    // -------------------------------------------------------------------------
    // Model accessors
    // -------------------------------------------------------------------------

    /** @return the current Board model */
    public Board getBoard() { return board; }

    /** @return the current BoardSettings */
    public BoardSettings getSettings() { return settings; }

    // -------------------------------------------------------------------------
    // Game actions
    // -------------------------------------------------------------------------

    /**
     * Attempts to move a piece from one square to another.
     * Validates that the source square belongs to the current player,
     * then executes the move, checks for King capture, and refreshes views.
     *
     * @param fromRow source row
     * @param fromCol source column
     * @param toRow   destination row
     * @param toCol   destination column
     */
    public void attemptMove(int fromRow, int fromCol, int toRow, int toCol) {
        Piece piece = board.getPiece(fromRow, fromCol);
        if (piece == null) return;
        if (piece.getColor() != board.getCurrentTurn()) return;

        // Prevent moving to a square occupied by own piece
        Piece target = board.getPiece(toRow, toCol);
        if (target != null && target.getColor() == piece.getColor()) return;

        Piece captured = board.movePiece(fromRow, fromCol, toRow, toCol);

        refreshViews();

        // Check for King capture (endgame)
        if (captured != null && captured.getType() == PieceType.KING) {
            PlayerColor winner = piece.getColor();
            SwingUtilities.invokeLater(() -> {
                String msg = winner.name() + " wins by capturing the King!";
                JOptionPane.showMessageDialog(
                    frame, msg, "Game Over – " + winner.name() + " Wins!",
                    JOptionPane.INFORMATION_MESSAGE
                );
                System.exit(0);
            });
        }
    }

    /**
     * Undoes the most recent move.
     * If no moves exist, shows a dialog informing the user.
     */
    public void undoMove() {
        Move undone = board.undoLastMove();
        if (undone == null) {
            JOptionPane.showMessageDialog(frame, "No moves to undo.", "Undo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        refreshViews();
    }

    /**
     * Starts a new game by resetting the board to its initial state.
     * Prompts the user for confirmation first.
     */
    public void newGame() {
        int choice = JOptionPane.showConfirmDialog(
            frame,
            "Start a new game? The current game will be lost.",
            "New Game",
            JOptionPane.YES_NO_OPTION
        );
        if (choice != JOptionPane.YES_OPTION) return;

        board.reset();
        refreshViews();
    }

    /**
     * Saves the current game state to a file chosen by the user.
     * Uses Java object serialization. Saves both board and settings.
     *
     * @param parent the parent frame for the file chooser dialog
     */
    public void saveGame(java.awt.Component parent) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Game");
        chooser.setSelectedFile(new File("chess_save.dat"));
        int result = chooser.showSaveDialog(parent);
        if (result != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(board);
            oos.writeObject(settings);
            JOptionPane.showMessageDialog(parent, "Game saved to:\n" + file.getAbsolutePath(),
                "Save Successful", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(parent, "Failed to save: " + ex.getMessage(),
                "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Loads a saved game from a file chosen by the user.
     * Restores both board state and settings.
     *
     * @param parent the parent frame for the file chooser dialog
     */
    public void loadGame(java.awt.Component parent) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Load Game");
        int result = chooser.showOpenDialog(parent);
        if (result != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            board    = (Board) ois.readObject();
            settings = (BoardSettings) ois.readObject();
            refreshViews();
            JOptionPane.showMessageDialog(parent, "Game loaded from:\n" + file.getAbsolutePath(),
                "Load Successful", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException | ClassNotFoundException ex) {
            JOptionPane.showMessageDialog(parent, "Failed to load: " + ex.getMessage(),
                "Load Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // -------------------------------------------------------------------------
    // View refresh
    // -------------------------------------------------------------------------

    /**
     * Refreshes all view panels to reflect the current game state.
     * Safe to call from any thread (dispatches to EDT if needed).
     */
    public void refreshViews() {
        Runnable r = () -> {
            if (boardPanel   != null) boardPanel.refresh();
            if (historyPanel != null) historyPanel.refresh();
        };
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            SwingUtilities.invokeLater(r);
        }
    }
}
