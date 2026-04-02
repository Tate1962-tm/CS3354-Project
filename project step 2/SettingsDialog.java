package chess;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

/**
 * Settings dialog that allows players to customize the chessboard
 * and piece appearance (Extra Feature 2).
 *
 * <p>Changes are applied in real-time when the "Apply" button is clicked.</p>
 */
public class SettingsDialog extends JDialog {

    private final JComboBox<BoardTheme.BoardStyle> boardStyleCombo;
    private final JComboBox<BoardTheme.BoardSize>  boardSizeCombo;
    private final JComboBox<BoardTheme.PieceStyle> pieceStyleCombo;

    /** Called with the new theme when the user clicks Apply or OK. */
    private final Consumer<BoardTheme> applyCallback;

    private BoardTheme currentTheme;

    /**
     * Constructs the settings dialog.
     *
     * @param owner         the parent frame
     * @param theme         the current theme (will be cloned for editing)
     * @param applyCallback invoked whenever the user applies changes
     */
    public SettingsDialog(Frame owner, BoardTheme theme, Consumer<BoardTheme> applyCallback) {
        super(owner, "Settings", false); // non-modal so user can see live preview
        this.currentTheme = theme;
        this.applyCallback = applyCallback;

        boardStyleCombo = new JComboBox<>(BoardTheme.BoardStyle.values());
        boardStyleCombo.setSelectedItem(theme.getBoardStyle());

        boardSizeCombo = new JComboBox<>(BoardTheme.BoardSize.values());
        boardSizeCombo.setSelectedItem(theme.getBoardSize());

        pieceStyleCombo = new JComboBox<>(BoardTheme.PieceStyle.values());
        pieceStyleCombo.setSelectedItem(theme.getPieceStyle());

        buildUI();
        pack();
        setLocationRelativeTo(owner);
    }

    private void buildUI() {
        JPanel content = new JPanel(new GridBagLayout());
        content.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;

        // Title
        JLabel title = new JLabel("Board & Piece Settings");
        title.setFont(new Font("SansSerif", Font.BOLD, 15));
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
        content.add(title, c);
        c.gridwidth = 1;

        // Board style
        c.gridx = 0; c.gridy = 1; c.weightx = 0;
        content.add(new JLabel("Board Style:"), c);
        c.gridx = 1; c.weightx = 1;
        content.add(boardStyleCombo, c);

        // Board size
        c.gridx = 0; c.gridy = 2; c.weightx = 0;
        content.add(new JLabel("Board Size:"), c);
        c.gridx = 1; c.weightx = 1;
        content.add(boardSizeCombo, c);

        // Piece style
        c.gridx = 0; c.gridy = 3; c.weightx = 0;
        content.add(new JLabel("Piece Colors:"), c);
        c.gridx = 1; c.weightx = 1;
        content.add(pieceStyleCombo, c);

        // Buttons
        JButton applyBtn = new JButton("Apply");
        JButton closeBtn = new JButton("Close");

        applyBtn.addActionListener(e -> applySettings());
        closeBtn.addActionListener(e -> dispose());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.add(applyBtn);
        buttons.add(closeBtn);

        c.gridx = 0; c.gridy = 4; c.gridwidth = 2; c.insets = new Insets(14, 6, 0, 6);
        content.add(buttons, c);

        setContentPane(content);
    }

    private void applySettings() {
        BoardTheme newTheme = new BoardTheme();
        newTheme.setBoardStyle((BoardTheme.BoardStyle) boardStyleCombo.getSelectedItem());
        newTheme.setBoardSize ((BoardTheme.BoardSize)  boardSizeCombo.getSelectedItem());
        newTheme.setPieceStyle((BoardTheme.PieceStyle) pieceStyleCombo.getSelectedItem());
        currentTheme = newTheme;
        applyCallback.accept(newTheme);
    }
}
