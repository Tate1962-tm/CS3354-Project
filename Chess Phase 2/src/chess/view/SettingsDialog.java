package chess.view;

import chess.controller.GameController;
import chess.model.BoardSettings;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Modal dialog that allows the player to customize board appearance.
 * Implements Feature 2: board theme, size, and coordinate toggle.
 */
public class SettingsDialog extends JDialog {

    private final GameController controller;
    private final BoardPanel boardPanel;

    private JComboBox<BoardSettings.Theme>     themeCombo;
    private JComboBox<BoardSettings.BoardSize> sizeCombo;
    private JCheckBox coordsCheckBox;

    /**
     * Constructs the settings dialog.
     * @param parent     the parent frame
     * @param controller the game controller
     * @param boardPanel the board panel to refresh on apply
     */
    public SettingsDialog(JFrame parent, GameController controller, BoardPanel boardPanel) {
        super(parent, "Board Settings", true);
        this.controller = controller;
        this.boardPanel = boardPanel;
        buildUI();
        pack();
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    /**
     * Builds the settings form UI.
     */
    private void buildUI() {
        BoardSettings settings = controller.getSettings();

        JPanel main = new JPanel(new GridBagLayout());
        main.setBackground(new Color(45, 45, 45));
        main.setBorder(new EmptyBorder(18, 22, 18, 22));
        main.setPreferredSize(new Dimension(320, 230));

        GridBagConstraints lc = new GridBagConstraints();
        lc.anchor = GridBagConstraints.WEST;
        lc.insets = new Insets(8, 0, 8, 14);
        lc.gridx = 0;

        GridBagConstraints fc = new GridBagConstraints();
        fc.fill   = GridBagConstraints.HORIZONTAL;
        fc.insets = new Insets(8, 0, 8, 0);
        fc.gridx  = 1;
        fc.weightx = 1.0;

        Color labelColor = new Color(200, 180, 130);
        Font  labelFont  = new Font("Monospaced", Font.BOLD, 12);

        // Theme selector
        lc.gridy = 0; fc.gridy = 0;
        JLabel themeLabel = new JLabel("Board Theme:");
        themeLabel.setForeground(labelColor);
        themeLabel.setFont(labelFont);
        main.add(themeLabel, lc);

        themeCombo = new JComboBox<>(BoardSettings.Theme.values());
        themeCombo.setSelectedItem(settings.getTheme());
        styleCombo(themeCombo);
        main.add(themeCombo, fc);

        // Board size selector
        lc.gridy = 1; fc.gridy = 1;
        JLabel sizeLabel = new JLabel("Board Size:");
        sizeLabel.setForeground(labelColor);
        sizeLabel.setFont(labelFont);
        main.add(sizeLabel, lc);

        sizeCombo = new JComboBox<>(BoardSettings.BoardSize.values());
        sizeCombo.setSelectedItem(settings.getBoardSize());
        styleCombo(sizeCombo);
        main.add(sizeCombo, fc);

        // Coordinates toggle
        lc.gridy = 2; fc.gridy = 2;
        JLabel coordLabel = new JLabel("Show Coordinates:");
        coordLabel.setForeground(labelColor);
        coordLabel.setFont(labelFont);
        main.add(coordLabel, lc);

        coordsCheckBox = new JCheckBox();
        coordsCheckBox.setSelected(settings.isShowCoordinates());
        coordsCheckBox.setBackground(new Color(45, 45, 45));
        main.add(coordsCheckBox, fc);

        // Buttons row
        lc.gridy = 3; fc.gridy = 3;
        lc.gridwidth = 2;
        lc.fill = GridBagConstraints.HORIZONTAL;

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonRow.setBackground(new Color(45, 45, 45));

        JButton cancelBtn = makeButton("Cancel", new Color(80, 80, 80));
        JButton applyBtn  = makeButton("Apply",  new Color(70, 110, 50));

        cancelBtn.addActionListener(e -> dispose());
        applyBtn.addActionListener(e -> applySettings());

        buttonRow.add(cancelBtn);
        buttonRow.add(applyBtn);
        main.add(buttonRow, lc);

        setContentPane(main);
        getContentPane().setBackground(new Color(45, 45, 45));
    }

    /**
     * Applies selected settings to the game and refreshes the board.
     */
    private void applySettings() {
        BoardSettings settings = controller.getSettings();
        settings.setTheme((BoardSettings.Theme) themeCombo.getSelectedItem());
        settings.setBoardSize((BoardSettings.BoardSize) sizeCombo.getSelectedItem());
        settings.setShowCoordinates(coordsCheckBox.isSelected());
        boardPanel.refresh();
        dispose();
    }

    /**
     * Styles a JComboBox to match the dark theme.
     * @param combo the combo box to style
     */
    private <T> void styleCombo(JComboBox<T> combo) {
        combo.setBackground(new Color(60, 60, 60));
        combo.setForeground(new Color(220, 220, 220));
        combo.setFont(new Font("Monospaced", Font.PLAIN, 12));
    }

    /**
     * Creates a styled button with a given label and background color.
     * @param text  button label
     * @param bg    background color
     * @return configured JButton
     */
    private JButton makeButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Monospaced", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(6, 14, 6, 14));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
