import javax.swing.*;
import java.awt.*;
import java.io.*;

/**
 * ChessFrame.java  (Phase 3)
 *
 * The main application window.  Wires together:
 * <ul>
 *   <li>{@link ChessBoardPanel} — interactive board rendering</li>
 *   <li>{@link HistoryPanel}    — move history and captured pieces</li>
 *   <li>Menu bar               — New Game, Save, Load, Undo, Settings, Quit</li>
 * </ul>
 *
 * Implements {@link ChessBoardPanel.GameEventListener} to receive move
 * notifications and game-ending events from the board panel.
 */
public class ChessFrame extends JFrame implements ChessBoardPanel.GameEventListener {

    private final GameState      gameState;
    private final ChessBoardPanel boardPanel;
    private final HistoryPanel    historyPanel;

    /**
     * Constructs and displays the chess application window.
     *
     * @param gameState the shared game model
     */
    public ChessFrame(GameState gameState) {
        super("Chess Game \u2014 Phase 3");
        this.gameState = gameState;

        boardPanel   = new ChessBoardPanel(gameState, this);
        historyPanel = new HistoryPanel(gameState, this::undoMove);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        buildUI();
        buildMenuBar();

        pack();
        setMinimumSize(getSize());
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // =========================================================================
    // Layout
    // =========================================================================

    private void buildUI() {
        JPanel content = new JPanel(new BorderLayout(0, 0));
        content.setBackground(new Color(30, 30, 30));
        content.add(boardPanel,   BorderLayout.CENTER);
        content.add(historyPanel, BorderLayout.EAST);
        setContentPane(content);
    }

    // =========================================================================
    // Menu bar
    // =========================================================================

    private void buildMenuBar() {
        JMenuBar bar = new JMenuBar();
        bar.setBackground(new Color(40, 40, 40));
        bar.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));

        // ---- Game menu ----
        JMenu gameMenu = menu("Game");

        JMenuItem newItem    = menuItem("New Game",  "Ctrl+N");
        JMenuItem undoItem   = menuItem("Undo Move", "Ctrl+Z");
        JMenuItem saveItem   = menuItem("Save Game", "Ctrl+S");
        JMenuItem loadItem   = menuItem("Load Game", "Ctrl+L");
        JMenuItem quitItem   = menuItem("Quit",      "Ctrl+Q");

        newItem.addActionListener(e  -> newGame());
        undoItem.addActionListener(e -> undoMove());
        saveItem.addActionListener(e -> saveGame());
        loadItem.addActionListener(e -> loadGame());
        quitItem.addActionListener(e -> System.exit(0));

        gameMenu.add(newItem);
        gameMenu.addSeparator();
        gameMenu.add(undoItem);
        gameMenu.addSeparator();
        gameMenu.add(saveItem);
        gameMenu.add(loadItem);
        gameMenu.addSeparator();
        gameMenu.add(quitItem);

        // ---- View menu ----
        JMenu viewMenu = menu("View");

        JMenuItem settingsItem = menuItem("Board Settings\u2026", null);
        settingsItem.addActionListener(e ->
            new SettingsDialog(this, boardPanel).setVisible(true));
        viewMenu.add(settingsItem);

        bar.add(gameMenu);
        bar.add(viewMenu);
        setJMenuBar(bar);
    }

    // =========================================================================
    // Game actions
    // =========================================================================

    private void newGame() {
        int choice = JOptionPane.showConfirmDialog(
            this,
            "Start a new game? The current game will be lost.",
            "New Game", JOptionPane.YES_NO_OPTION
        );
        if (choice != JOptionPane.YES_OPTION) return;
        gameState.newGame();
        refreshViews();
    }

    private void undoMove() {
        GameState.MoveRecord undone = gameState.undoLastMove();
        if (undone == null) {
            JOptionPane.showMessageDialog(this,
                "No moves to undo.", "Undo",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        refreshViews();
    }

    /**
     * Saves the current board position to a plain-text file chosen by the user.
     * Uses {@link GameState#serialize()} — no Java serialization / binary format.
     */
    private void saveGame() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Game");
        chooser.setSelectedFile(new File("chess_save.txt"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        try (PrintWriter pw = new PrintWriter(
                new FileWriter(chooser.getSelectedFile()))) {
            pw.print(gameState.serialize());
            JOptionPane.showMessageDialog(this,
                "Game saved to:\n" + chooser.getSelectedFile().getAbsolutePath(),
                "Save Successful", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                "Failed to save: " + ex.getMessage(),
                "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Loads a saved game from a plain-text file chosen by the user.
     * Uses {@link GameState#deserialize(String)}.
     */
    private void loadGame() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Load Game");
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        try {
            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new FileReader(chooser.getSelectedFile()))) {
                String line;
                while ((line = br.readLine()) != null) sb.append(line).append("\n");
            }
            gameState.deserialize(sb.toString());
            refreshViews();
            JOptionPane.showMessageDialog(this,
                "Game loaded from:\n" + chooser.getSelectedFile().getAbsolutePath(),
                "Load Successful", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                "Failed to load: " + ex.getMessage(),
                "Load Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // =========================================================================
    // GameEventListener callbacks
    // =========================================================================

    /**
     * Called by {@link ChessBoardPanel} after every successful move.
     * Refreshes the history panel so the new move appears immediately.
     */
    @Override
    public void onMoveMade() {
        historyPanel.refresh();
    }

    /**
     * Called by {@link ChessBoardPanel} when checkmate is detected.
     * Shows a game-over dialog and asks whether to start a new game.
     */
    @Override
    public void onCheckmate(PlayerColor winner) {
        int choice = JOptionPane.showConfirmDialog(
            this,
            winner.name() + " wins by checkmate!\n\nPlay again?",
            "Checkmate \u2014 " + winner.name() + " Wins!",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.INFORMATION_MESSAGE
        );
        if (choice == JOptionPane.YES_OPTION) {
            gameState.newGame();
            refreshViews();
        }
    }

    /**
     * Called by {@link ChessBoardPanel} when stalemate is detected.
     * Shows a draw dialog and asks whether to start a new game.
     */
    @Override
    public void onStalemate() {
        int choice = JOptionPane.showConfirmDialog(
            this,
            "Stalemate \u2014 the game is a draw!\n\nPlay again?",
            "Stalemate",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.INFORMATION_MESSAGE
        );
        if (choice == JOptionPane.YES_OPTION) {
            gameState.newGame();
            refreshViews();
        }
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    /** Repaints both the board and the history panel. */
    private void refreshViews() {
        boardPanel.refresh();
        historyPanel.refresh();
    }

    private JMenu menu(String text) {
        JMenu m = new JMenu(text);
        m.setForeground(new Color(210, 190, 150));
        m.setFont(new Font("Monospaced", Font.BOLD, 13));
        m.getPopupMenu().setBackground(new Color(45, 45, 45));
        return m;
    }

    private JMenuItem menuItem(String text, String accel) {
        JMenuItem item = new JMenuItem(text);
        item.setForeground(new Color(220, 210, 190));
        item.setBackground(new Color(45, 45, 45));
        item.setFont(new Font("Monospaced", Font.PLAIN, 12));
        if (accel != null) {
            try {
                item.setAccelerator(
                    KeyStroke.getKeyStroke(accel.replace("Ctrl+", "control ")));
            } catch (Exception ignored) { }
        }
        return item;
    }
}
