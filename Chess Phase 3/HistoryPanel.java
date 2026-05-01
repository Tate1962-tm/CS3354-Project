import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * HistoryPanel.java  (Phase 3)
 *
 * Side panel displayed to the right of the board showing:
 * <ul>
 *   <li>Scrollable move history in algebraic notation</li>
 *   <li>Captured pieces for each side (as Unicode symbols)</li>
 *   <li>Current game status (CHECK / CHECKMATE / STALEMATE / active turn)</li>
 *   <li>Undo Move button</li>
 * </ul>
 */
public class HistoryPanel extends JPanel {

    private static final Color PANEL_BG  = new Color(30, 30, 30);
    private static final Color HEADER_FG = new Color(180, 150, 100);
    private static final Color TEXT_FG   = new Color(210, 210, 210);

    private final GameState gameState;
    private final Runnable  onUndo;

    private DefaultListModel<String> moveListModel;
    private JList<String>            moveList;
    private JLabel capturedByWhiteLabel;
    private JLabel capturedByBlackLabel;
    private JLabel statusLabel;
    private JButton undoButton;

    /**
     * @param gameState the game model (read-only from this panel)
     * @param onUndo    callback invoked when the Undo button is clicked
     */
    public HistoryPanel(GameState gameState, Runnable onUndo) {
        this.gameState = gameState;
        this.onUndo    = onUndo;

        setBackground(PANEL_BG);
        setBorder(new EmptyBorder(12, 10, 12, 10));
        setLayout(new BorderLayout(0, 10));
        setPreferredSize(new Dimension(210, 0));

        buildUI();
    }

    // =========================================================================
    // UI construction
    // =========================================================================

    private void buildUI() {
        // Title
        JLabel title = styledLabel("GAME HISTORY", HEADER_FG,
                                   new Font("Monospaced", Font.BOLD, 13));
        title.setBorder(new EmptyBorder(0, 0, 6, 0));
        add(title, BorderLayout.NORTH);

        // Centre: move list + captured pieces
        JPanel centre = new JPanel();
        centre.setBackground(PANEL_BG);
        centre.setLayout(new BoxLayout(centre, BoxLayout.Y_AXIS));

        // -- Status bar (shows CHECK, CHECKMATE, STALEMATE, or whose turn) --
        statusLabel = styledLabel("WHITE'S TURN", new Color(120, 200, 120),
                                  new Font("Monospaced", Font.BOLD, 11));
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        statusLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 60, 60)),
            new EmptyBorder(4, 6, 4, 6)
        ));
        statusLabel.setOpaque(true);
        statusLabel.setBackground(new Color(40, 40, 40));
        centre.add(statusLabel);
        centre.add(Box.createVerticalStrut(10));

        // -- Move list --
        centre.add(sectionHeader("Moves"));
        centre.add(Box.createVerticalStrut(4));

        moveListModel = new DefaultListModel<>();
        moveList = new JList<>(moveListModel);
        moveList.setBackground(new Color(22, 22, 22));
        moveList.setForeground(TEXT_FG);
        moveList.setFont(new Font("Monospaced", Font.PLAIN, 11));
        moveList.setSelectionBackground(new Color(60, 60, 80));
        moveList.setBorder(new EmptyBorder(4, 6, 4, 6));

        JScrollPane scroll = new JScrollPane(moveList);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60)));
        scroll.setPreferredSize(new Dimension(190, 200));
        scroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        scroll.getViewport().setBackground(new Color(22, 22, 22));
        centre.add(scroll);
        centre.add(Box.createVerticalStrut(14));

        // -- Captured pieces --
        centre.add(sectionHeader("Captured by White"));
        centre.add(Box.createVerticalStrut(4));
        capturedByWhiteLabel = capturedLabel();
        centre.add(capturedByWhiteLabel);
        centre.add(Box.createVerticalStrut(10));

        centre.add(sectionHeader("Captured by Black"));
        centre.add(Box.createVerticalStrut(4));
        capturedByBlackLabel = capturedLabel();
        centre.add(capturedByBlackLabel);

        add(centre, BorderLayout.CENTER);

        // -- Undo button at the bottom --
        undoButton = new JButton("\u27F5  UNDO MOVE");
        undoButton.setBackground(new Color(70, 50, 30));
        undoButton.setForeground(new Color(240, 200, 130));
        undoButton.setFont(new Font("Monospaced", Font.BOLD, 12));
        undoButton.setFocusPainted(false);
        undoButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(120, 90, 50), 1),
            new EmptyBorder(8, 10, 8, 10)
        ));
        undoButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        undoButton.addActionListener(e -> onUndo.run());
        add(undoButton, BorderLayout.SOUTH);
    }

    // =========================================================================
    // Refresh
    // =========================================================================

    /**
     * Rebuilds all dynamic content from the current {@link GameState}.
     * Should be called after every move or undo.
     */
    public void refresh() {
        List<GameState.MoveRecord> history = gameState.getMoveHistory();

        // Rebuild move list
        moveListModel.clear();
        for (int i = 0; i < history.size(); i++) {
            String prefix = ((i % 2 == 0) ? "W " : "B ") + (i / 2 + 1) + ". ";
            moveListModel.addElement(prefix + history.get(i).describe());
        }
        if (!moveListModel.isEmpty()) {
            moveList.ensureIndexIsVisible(moveListModel.size() - 1);
        }

        // Rebuild captured-piece strings from move history
        StringBuilder capturedByWhite = new StringBuilder();
        StringBuilder capturedByBlack = new StringBuilder();
        for (GameState.MoveRecord rec : history) {
            if (rec.captured != null) {
                // The mover captured the piece
                if (rec.piece.getColor() == PlayerColor.WHITE) {
                    capturedByWhite.append(rec.captured.getSymbol()).append(" ");
                } else {
                    capturedByBlack.append(rec.captured.getSymbol()).append(" ");
                }
            }
        }
        capturedByWhiteLabel.setText(
            capturedByWhite.length() > 0 ? capturedByWhite.toString().trim() : "\u2013");
        capturedByBlackLabel.setText(
            capturedByBlack.length() > 0 ? capturedByBlack.toString().trim() : "\u2013");

        // Status bar
        updateStatus();

        // Enable undo only when there is history
        undoButton.setEnabled(!history.isEmpty());
    }

    /**
     * Updates the status label to reflect check / checkmate / stalemate / active turn.
     */
    private void updateStatus() {
        String text;
        Color  fg;
        Color  bg;

        if (gameState.isCheckmate()) {
            PlayerColor winner = gameState.getCurrentTurn().opposite();
            text = winner.name() + " WINS! CHECKMATE";
            fg   = new Color(255, 220, 80);
            bg   = new Color(140, 30, 30);
        } else if (gameState.isStalemate()) {
            text = "STALEMATE — DRAW";
            fg   = new Color(200, 200, 200);
            bg   = new Color(60, 60, 90);
        } else if (gameState.isCurrentPlayerInCheck()) {
            text = gameState.getCurrentTurn().name() + " IS IN CHECK!";
            fg   = new Color(255, 100, 80);
            bg   = new Color(80, 20, 20);
        } else {
            text = gameState.getCurrentTurn().name() + "'S TURN";
            fg   = new Color(120, 200, 120);
            bg   = new Color(40, 40, 40);
        }

        statusLabel.setText(text);
        statusLabel.setForeground(fg);
        statusLabel.setBackground(bg);
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private JLabel styledLabel(String text, Color fg, Font font) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(fg);
        lbl.setFont(font);
        return lbl;
    }

    private JLabel sectionHeader(String text) {
        JLabel lbl = styledLabel(text.toUpperCase(), HEADER_FG,
                                 new Font("Monospaced", Font.BOLD, 10));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JLabel capturedLabel() {
        JLabel lbl = new JLabel("\u2013");
        lbl.setForeground(TEXT_FG);
        lbl.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 16));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setBorder(new EmptyBorder(2, 4, 2, 4));
        return lbl;
    }
}
