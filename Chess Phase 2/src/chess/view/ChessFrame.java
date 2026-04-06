package chess.view;

import chess.controller.GameController;

import javax.swing.*;
import java.awt.*;

/**
 * The main application window for the Chess Game.
 * Contains the menu bar (Feature 1), the board panel, and the history panel (Feature 3).
 */
public class ChessFrame extends JFrame {

    private final GameController controller;
    private BoardPanel   boardPanel;
    private HistoryPanel historyPanel;

    /**
     * Constructs and displays the chess game window.
     * @param controller the game controller managing all logic
     */
    public ChessFrame(GameController controller) {
        this.controller = controller;
        controller.setFrame(this);

        setTitle("Chess Game — Phase 2");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBackground(new Color(30, 30, 30));

        buildUI();
        buildMenuBar();

        pack();
        setMinimumSize(getSize());
        setLocationRelativeTo(null);
        setVisible(true);
    }

    /**
     * Builds the main content area with board and history panel side by side.
     */
    private void buildUI() {
        JPanel contentPane = new JPanel(new BorderLayout(0, 0));
        contentPane.setBackground(new Color(30, 30, 30));

        boardPanel   = new BoardPanel(controller);
        historyPanel = new HistoryPanel(controller);

        controller.setBoardPanel(boardPanel);
        controller.setHistoryPanel(historyPanel);

        contentPane.add(boardPanel,   BorderLayout.CENTER);
        contentPane.add(historyPanel, BorderLayout.EAST);

        setContentPane(contentPane);
    }

    /**
     * Builds the menu bar with File and View menus.
     * Implements Feature 1: New Game, Save Game, Load Game, Settings.
     */
    private void buildMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(new Color(40, 40, 40));
        menuBar.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));

        // ---- Game menu ----
        JMenu gameMenu = makeMenu("Game");

        JMenuItem newItem  = makeMenuItem("New Game",  "Ctrl+N");
        JMenuItem saveItem = makeMenuItem("Save Game", "Ctrl+S");
        JMenuItem loadItem = makeMenuItem("Load Game", "Ctrl+L");
        JMenuItem quitItem = makeMenuItem("Quit",      "Ctrl+Q");

        newItem.addActionListener(e  -> controller.newGame());
        saveItem.addActionListener(e -> controller.saveGame(this));
        loadItem.addActionListener(e -> controller.loadGame(this));
        quitItem.addActionListener(e -> System.exit(0));

        gameMenu.add(newItem);
        gameMenu.addSeparator();
        gameMenu.add(saveItem);
        gameMenu.add(loadItem);
        gameMenu.addSeparator();
        gameMenu.add(quitItem);

        // ---- View menu ----
        JMenu viewMenu = makeMenu("View");

        JMenuItem settingsItem = makeMenuItem("Board Settings…", null);
        settingsItem.addActionListener(e -> {
            SettingsDialog dialog = new SettingsDialog(this, controller, boardPanel);
            dialog.setVisible(true);
        });
        viewMenu.add(settingsItem);

        menuBar.add(gameMenu);
        menuBar.add(viewMenu);
        setJMenuBar(menuBar);
    }

    /**
     * Creates a styled menu.
     * @param text the menu label
     * @return configured JMenu
     */
    private JMenu makeMenu(String text) {
        JMenu menu = new JMenu(text);
        menu.setForeground(new Color(210, 190, 150));
        menu.setFont(new Font("Monospaced", Font.BOLD, 13));
        menu.getPopupMenu().setBackground(new Color(45, 45, 45));
        return menu;
    }

    /**
     * Creates a styled menu item with an optional accelerator key.
     * @param text        the item label
     * @param accelerator keyboard shortcut text (e.g., "Ctrl+N"), or null
     * @return configured JMenuItem
     */
    private JMenuItem makeMenuItem(String text, String accelerator) {
        JMenuItem item = new JMenuItem(text);
        item.setForeground(new Color(220, 210, 190));
        item.setBackground(new Color(45, 45, 45));
        item.setFont(new Font("Monospaced", Font.PLAIN, 12));

        if (accelerator != null) {
            try {
                item.setAccelerator(KeyStroke.getKeyStroke(accelerator.replace("Ctrl+", "control ")));
            } catch (Exception ignored) { }
        }
        return item;
    }

    /**
     * Returns the board panel (used by controller for repaint calls).
     * @return the BoardPanel instance
     */
    public BoardPanel getBoardPanel()     { return boardPanel; }

    /**
     * Returns the history panel.
     * @return the HistoryPanel instance
     */
    public HistoryPanel getHistoryPanel() { return historyPanel; }
}
