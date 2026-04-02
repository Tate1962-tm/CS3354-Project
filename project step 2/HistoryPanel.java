package chess;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Side panel showing move history, captured pieces, and an Undo button.
 * Implements Extra Feature 3 (Game History Panel with Undo Button).
 */
public class HistoryPanel extends JPanel {

    private final DefaultListModel<String> historyModel = new DefaultListModel<>();
    private final JList<String> historyList = new JList<>(historyModel);

    private final JLabel capturedByWhiteLabel = new JLabel();
    private final JLabel capturedByBlackLabel = new JLabel();
    private final JLabel turnLabel             = new JLabel();

    private final JButton undoButton;

    /** Callback invoked when the Undo button is pressed. */
    private Runnable undoAction;

    /**
     * Constructs the history/sidebar panel.
     */
    public HistoryPanel() {
        setLayout(new BorderLayout(6, 6));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        setPreferredSize(new Dimension(200, 0));
        setBackground(new Color(0x2E2E2E));

        // --- Turn indicator ---
        turnLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        turnLabel.setForeground(Color.WHITE);
        turnLabel.setHorizontalAlignment(SwingConstants.CENTER);
        turnLabel.setBorder(BorderFactory.createEmptyBorder(4, 0, 8, 0));

        // --- Captured pieces ---
        JPanel capturedPanel = new JPanel(new GridLayout(4, 1, 2, 2));
        capturedPanel.setBackground(new Color(0x2E2E2E));
        capturedPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY), "Captured Pieces",
                0, 0, new Font("SansSerif", Font.BOLD, 11), Color.LIGHT_GRAY));

        JLabel wLabel = makeLabel("White captured:");
        capturedByWhiteLabel.setFont(new Font("Serif", Font.PLAIN, 14));
        capturedByWhiteLabel.setForeground(Color.WHITE);

        JLabel bLabel = makeLabel("Black captured:");
        capturedByBlackLabel.setFont(new Font("Serif", Font.PLAIN, 14));
        capturedByBlackLabel.setForeground(Color.WHITE);

        capturedPanel.add(wLabel);
        capturedPanel.add(capturedByWhiteLabel);
        capturedPanel.add(bLabel);
        capturedPanel.add(capturedByBlackLabel);

        // --- Move history list ---
        historyList.setBackground(new Color(0x1C1C1C));
        historyList.setForeground(Color.GREEN);
        historyList.setFont(new Font("Monospaced", Font.PLAIN, 12));
        historyList.setSelectionBackground(new Color(0x3A3A3A));
        JScrollPane scroll = new JScrollPane(historyList);
        scroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY), "Move History",
                0, 0, new Font("SansSerif", Font.BOLD, 11), Color.LIGHT_GRAY));

        // --- Undo button ---
        undoButton = new JButton("⬅ Undo");
        undoButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        undoButton.setBackground(new Color(0x8B0000));
        undoButton.setForeground(Color.WHITE);
        undoButton.setFocusPainted(false);
        undoButton.setBorderPainted(false);
        undoButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        undoButton.addActionListener(e -> { if (undoAction != null) undoAction.run(); });

        // --- Layout ---
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(new Color(0x2E2E2E));
        top.add(turnLabel, BorderLayout.NORTH);
        top.add(capturedPanel, BorderLayout.CENTER);

        add(top,        BorderLayout.NORTH);
        add(scroll,     BorderLayout.CENTER);
        add(undoButton, BorderLayout.SOUTH);
    }

    // -------------------------------------------------------------------------
    // Public update API
    // -------------------------------------------------------------------------

    /**
     * Refreshes the panel to reflect the current game state.
     *
     * @param state the current game state
     */
    public void refresh(GameState state) {
        // Turn label
        String turn = state.getCurrentTurn() == Piece.Color.WHITE ? "⬜ White's Turn" : "⬛ Black's Turn";
        turnLabel.setText(turn);

        // Captured pieces
        capturedByWhiteLabel.setText(buildSymbols(state.getCapturedByWhite()));
        capturedByBlackLabel.setText(buildSymbols(state.getCapturedByBlack()));

        // Move history
        historyModel.clear();
        List<String> history = state.getMoveHistory();
        for (int i = 0; i < history.size(); i++) {
            historyModel.addElement((i + 1) + ". " + history.get(i));
        }
        // Scroll to bottom
        if (!historyModel.isEmpty()) {
            historyList.ensureIndexIsVisible(historyModel.size() - 1);
        }

        // Undo button enabled only when there are moves to undo
        undoButton.setEnabled(state.canUndo());
    }

    /**
     * Registers the action to perform when Undo is clicked.
     *
     * @param action the undo callback
     */
    public void setUndoAction(Runnable action) {
        this.undoAction = action;
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private String buildSymbols(List<Piece> pieces) {
        StringBuilder sb = new StringBuilder();
        for (Piece p : pieces) sb.append(p.getSymbol()).append(" ");
        return sb.toString().trim().isEmpty() ? "—" : sb.toString().trim();
    }

    private JLabel makeLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lbl.setForeground(Color.LIGHT_GRAY);
        return lbl;
    }
}
