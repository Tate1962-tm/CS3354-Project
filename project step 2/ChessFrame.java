package chess;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;

/**
 * The main application window for the Chess Game.
 *
 * <p>Contains:
 * <ul>
 *   <li>Menu Bar with New Game / Save / Load (Extra Feature 1)</li>
 *   <li>Settings entry point (Extra Feature 2)</li>
 *   <li>History panel with Undo (Extra Feature 3)</li>
 *   <li>BoardPanel for piece rendering and interaction</li>
 * </ul>
 * </p>
 */
public class ChessFrame extends JFrame {

    private GameState  state;
    private BoardTheme theme;
    private BoardPanel boardPanel;
    private HistoryPanel historyPanel;

    /** Keeps a reference to the settings dialog so it is not duplicated. */
    private SettingsDialog settingsDialog;

    /**
     * Constructs and lays out the main chess window.
     */
    public ChessFrame() {
        super("Chess Game");
        state = new GameState();
        theme = new BoardTheme();
        buildUI();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
    }

    // -------------------------------------------------------------------------
    // UI construction
    // -------------------------------------------------------------------------

    private void buildUI() {
        setJMenuBar(buildMenuBar());

        boardPanel = new BoardPanel(state, theme);
        boardPanel.setMoveListener(this::onMove);

        historyPanel = new HistoryPanel();
        historyPanel.setUndoAction(this::onUndo);
        historyPanel.refresh(state);

        JPanel main = new JPanel(new BorderLayout());
        main.add(boardPanel,   BorderLayout.CENTER);
        main.add(historyPanel, BorderLayout.EAST);
        setContentPane(main);
    }

    // -------------------------------------------------------------------------
    // Menu Bar (Extra Feature 1)
    // -------------------------------------------------------------------------

    private JMenuBar buildMenuBar() {
        JMenuBar bar = new JMenuBar();

        // ---- Game menu ----
        JMenu gameMenu = new JMenu("Game");
        gameMenu.setMnemonic('G');

        JMenuItem newItem  = new JMenuItem("New Game");
        JMenuItem saveItem = new JMenuItem("Save Game");
        JMenuItem loadItem = new JMenuItem("Load Game");
        JMenuItem exitItem = new JMenuItem("Exit");

        newItem .setAccelerator(KeyStroke.getKeyStroke("ctrl N"));
        saveItem.setAccelerator(KeyStroke.getKeyStroke("ctrl S"));
        loadItem.setAccelerator(KeyStroke.getKeyStroke("ctrl O"));

        newItem .addActionListener(e -> onNewGame());
        saveItem.addActionListener(e -> onSaveGame());
        loadItem.addActionListener(e -> onLoadGame());
        exitItem.addActionListener(e -> System.exit(0));

        gameMenu.add(newItem);
        gameMenu.addSeparator();
        gameMenu.add(saveItem);
        gameMenu.add(loadItem);
        gameMenu.addSeparator();
        gameMenu.add(exitItem);

        // ---- Settings menu (Extra Feature 2) ----
        JMenu settingsMenu = new JMenu("Settings");
        settingsMenu.setMnemonic('S');
        JMenuItem customizeItem = new JMenuItem("Customize Board…");
        customizeItem.addActionListener(e -> onOpenSettings());
        settingsMenu.add(customizeItem);

        // ---- Help menu ----
        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Chess Game — Phase 2\nDeveloped with Java Swing\n\nClick a piece to select it,\nthen click a destination to move.\nOr drag and drop pieces.",
                "About", JOptionPane.INFORMATION_MESSAGE));
        helpMenu.add(aboutItem);

        bar.add(gameMenu);
        bar.add(settingsMenu);
        bar.add(helpMenu);
        return bar;
    }

    // -------------------------------------------------------------------------
    // Event handlers
    // -------------------------------------------------------------------------

    /**
     * Called after every move with the captured piece (null if empty square).
     *
     * @param captured the piece that was captured, or null
     */
    private void onMove(Piece captured) {
        historyPanel.refresh(state);

        if (captured != null && captured.getType() == Piece.Type.KING) {
            // Determine winner: the player who just moved (opposite of current turn)
            Piece.Color winner = state.getCurrentTurn() == Piece.Color.WHITE
                    ? Piece.Color.BLACK : Piece.Color.WHITE;
            String winnerName = winner == Piece.Color.WHITE ? "White" : "Black";

            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this,
                        "🎉 " + winnerName + " wins by capturing the King!",
                        "Game Over", JOptionPane.INFORMATION_MESSAGE);
                System.exit(0);
            });
        }
    }

    /** Resets the game after asking for confirmation. */
    private void onNewGame() {
        int choice = JOptionPane.showConfirmDialog(this,
                "Start a new game? Current progress will be lost.",
                "New Game", JOptionPane.YES_NO_OPTION);
        if (choice != JOptionPane.YES_OPTION) return;

        state.reset();
        boardPanel.repaint();
        historyPanel.refresh(state);
    }

    /** Saves the current game state to a user-chosen file. */
    private void onSaveGame() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Save Game");
        fc.setFileFilter(new FileNameExtensionFilter("Chess Save (*.chess)", "chess"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File file = fc.getSelectedFile();
        if (!file.getName().endsWith(".chess")) file = new File(file.getPath() + ".chess");
        try {
            state.saveToFile(file);
            JOptionPane.showMessageDialog(this, "Game saved to:\n" + file.getAbsolutePath(),
                    "Saved", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Save failed: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Loads a previously saved game from a user-chosen file. */
    private void onLoadGame() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Load Game");
        fc.setFileFilter(new FileNameExtensionFilter("Chess Save (*.chess)", "chess"));
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File file = fc.getSelectedFile();
        try {
            GameState loaded = GameState.loadFromFile(file);
            // Swap state in-place and rewire the board panel
            state = loaded;
            boardPanel.setMoveListener(null); // detach old reference
            JPanel content = (JPanel) getContentPane();
            content.remove(boardPanel);
            boardPanel = new BoardPanel(state, theme);
            boardPanel.setMoveListener(this::onMove);
            content.add(boardPanel, BorderLayout.CENTER);
            historyPanel.refresh(state);
            revalidate();
            repaint();
        } catch (IOException | ClassNotFoundException ex) {
            JOptionPane.showMessageDialog(this, "Load failed: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Opens (or raises) the settings dialog. */
    private void onOpenSettings() {
        if (settingsDialog == null || !settingsDialog.isDisplayable()) {
            settingsDialog = new SettingsDialog(this, theme, this::onThemeChanged);
        }
        settingsDialog.setVisible(true);
        settingsDialog.toFront();
    }

    /**
     * Applies a new theme from the settings dialog.
     *
     * @param newTheme the theme chosen by the user
     */
    private void onThemeChanged(BoardTheme newTheme) {
        theme = newTheme;
        boardPanel.setTheme(newTheme);
        pack(); // resize window to new board size
    }

    /** Undoes the last move. */
    private void onUndo() {
        boolean ok = state.undo();
        if (!ok) {
            JOptionPane.showMessageDialog(this, "Nothing to undo.", "Undo",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        boardPanel.repaint();
        historyPanel.refresh(state);
    }
}
