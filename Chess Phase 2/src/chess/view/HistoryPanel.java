package chess.view;

import chess.controller.GameController;
import chess.model.Move;
import chess.model.Piece;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * Side panel that displays move history, captured pieces, and an Undo button.
 * Implements Feature 3 of the project requirements.
 */
public class HistoryPanel extends JPanel {

    private final GameController controller;

    private JList<String>  moveList;
    private DefaultListModel<String> moveListModel;
    private JLabel  capturedByWhiteLabel;
    private JLabel  capturedByBlackLabel;
    private JButton undoButton;

    /** Dark panel background color */
    private static final Color PANEL_BG  = new Color(30, 30, 30);
    /** Header text color */
    private static final Color HEADER_FG = new Color(180, 150, 100);
    /** Body text color */
    private static final Color TEXT_FG   = new Color(210, 210, 210);

    /**
     * Constructs the history panel.
     * @param controller the game controller
     */
    public HistoryPanel(GameController controller) {
        this.controller = controller;
        setBackground(PANEL_BG);
        setBorder(new EmptyBorder(12, 10, 12, 10));
        setLayout(new BorderLayout(0, 10));
        setPreferredSize(new Dimension(200, 0));
        buildUI();
    }

    /**
     * Assembles all sub-components of the panel.
     */
    private void buildUI() {
        // Title
        JLabel title = new JLabel("GAME HISTORY");
        title.setForeground(HEADER_FG);
        title.setFont(new Font("Monospaced", Font.BOLD, 13));
        title.setBorder(new EmptyBorder(0, 0, 6, 0));
        add(title, BorderLayout.NORTH);

        // Center: move list + captured pieces
        JPanel center = new JPanel();
        center.setBackground(PANEL_BG);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        // Move list
        JLabel movesTitle = makeHeader("Moves");
        center.add(movesTitle);
        center.add(Box.createVerticalStrut(4));

        moveListModel = new DefaultListModel<>();
        moveList = new JList<>(moveListModel);
        moveList.setBackground(new Color(22, 22, 22));
        moveList.setForeground(TEXT_FG);
        moveList.setFont(new Font("Monospaced", Font.PLAIN, 11));
        moveList.setSelectionBackground(new Color(60, 60, 80));
        moveList.setBorder(new EmptyBorder(4, 6, 4, 6));

        JScrollPane scrollPane = new JScrollPane(moveList);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60)));
        scrollPane.setPreferredSize(new Dimension(180, 240));
        scrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 240));
        scrollPane.getViewport().setBackground(new Color(22, 22, 22));
        center.add(scrollPane);

        center.add(Box.createVerticalStrut(14));

        // Captured pieces
        center.add(makeHeader("Captured by White"));
        center.add(Box.createVerticalStrut(4));
        capturedByWhiteLabel = makeCapturedLabel();
        center.add(capturedByWhiteLabel);

        center.add(Box.createVerticalStrut(10));

        center.add(makeHeader("Captured by Black"));
        center.add(Box.createVerticalStrut(4));
        capturedByBlackLabel = makeCapturedLabel();
        center.add(capturedByBlackLabel);

        add(center, BorderLayout.CENTER);

        // Undo button at bottom
        undoButton = new JButton("⟵  UNDO MOVE");
        undoButton.setBackground(new Color(70, 50, 30));
        undoButton.setForeground(new Color(240, 200, 130));
        undoButton.setFont(new Font("Monospaced", Font.BOLD, 12));
        undoButton.setFocusPainted(false);
        undoButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(120, 90, 50), 1),
            new EmptyBorder(8, 10, 8, 10)
        ));
        undoButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        undoButton.addActionListener(e -> controller.undoMove());
        add(undoButton, BorderLayout.SOUTH);
    }

    /**
     * Creates a styled section header label.
     * @param text header text
     * @return configured JLabel
     */
    private JLabel makeHeader(String text) {
        JLabel lbl = new JLabel(text.toUpperCase());
        lbl.setForeground(HEADER_FG);
        lbl.setFont(new Font("Monospaced", Font.BOLD, 10));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    /**
     * Creates a styled label for displaying captured pieces.
     * @return configured JLabel
     */
    private JLabel makeCapturedLabel() {
        JLabel lbl = new JLabel(" ");
        lbl.setForeground(TEXT_FG);
        lbl.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 16));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setBorder(new EmptyBorder(2, 4, 2, 4));
        return lbl;
    }

    /**
     * Rebuilds the move list and captured pieces display from current game state.
     * Should be called after every move or undo.
     */
    public void refresh() {
        List<Move> history = controller.getBoard().getMoveHistory();

        moveListModel.clear();
        for (int i = 0; i < history.size(); i++) {
            String prefix = ((i % 2 == 0) ? "W " : "B ") + (i / 2 + 1) + ". ";
            moveListModel.addElement(prefix + history.get(i).toString());
        }
        // Scroll to latest move
        if (!moveListModel.isEmpty()) {
            moveList.ensureIndexIsVisible(moveListModel.size() - 1);
        }

        // Captured pieces as symbols
        capturedByWhiteLabel.setText(buildCapturedString(controller.getBoard().getCapturedByWhite()));
        capturedByBlackLabel.setText(buildCapturedString(controller.getBoard().getCapturedByBlack()));

        // Disable undo when no moves remain
        undoButton.setEnabled(!history.isEmpty());
    }

    /**
     * Builds a string of Unicode piece symbols from a list of captured pieces.
     * @param pieces list of captured Piece objects
     * @return string of symbols, or dash if empty
     */
    private String buildCapturedString(List<Piece> pieces) {
        if (pieces.isEmpty()) return "–";
        StringBuilder sb = new StringBuilder();
        for (Piece p : pieces) {
            sb.append(p.getSymbol()).append(" ");
        }
        return sb.toString().trim();
    }
}
