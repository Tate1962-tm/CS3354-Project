import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * SettingsDialog.java  (Phase 3)
 *
 * Modal dialog that lets the player customise the board appearance:
 * colour theme, square size, and coordinate visibility.
 * Changes are applied immediately to the live board on "Apply".
 */
public class SettingsDialog extends JDialog {

    // =========================================================================
    // Preset themes
    // =========================================================================

    public enum Theme {
        CLASSIC("Classic Wood",
                new Color(240, 217, 181), new Color(181, 136, 99)),
        TOURNAMENT("Tournament Green",
                   new Color(238, 238, 210), new Color(118, 150, 86)),
        MIDNIGHT("Midnight Blue",
                 new Color(222, 227, 230), new Color(58, 93, 130)),
        GRAPHITE("Graphite",
                 new Color(200, 200, 200), new Color(90, 90, 90));

        public final String label;
        public final Color  light;
        public final Color  dark;

        Theme(String label, Color light, Color dark) {
            this.label = label;
            this.light = light;
            this.dark  = dark;
        }

        @Override public String toString() { return label; }
    }

    // =========================================================================
    // Fields
    // =========================================================================

    private final ChessBoardPanel boardPanel;

    private JComboBox<Theme>   themeCombo;
    private JComboBox<Integer> sizeCombo;

    // =========================================================================
    // Construction
    // =========================================================================

    public SettingsDialog(JFrame parent, ChessBoardPanel boardPanel) {
        super(parent, "Board Settings", true);
        this.boardPanel = boardPanel;
        buildUI();
        pack();
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    // =========================================================================
    // UI
    // =========================================================================

    private void buildUI() {
        JPanel main = new JPanel(new GridBagLayout());
        main.setBackground(new Color(45, 45, 45));
        main.setBorder(new EmptyBorder(18, 22, 18, 22));
        main.setPreferredSize(new Dimension(320, 200));

        GridBagConstraints lc = new GridBagConstraints();
        lc.anchor = GridBagConstraints.WEST;
        lc.insets = new Insets(8, 0, 8, 14);
        lc.gridx  = 0;

        GridBagConstraints fc = new GridBagConstraints();
        fc.fill    = GridBagConstraints.HORIZONTAL;
        fc.insets  = new Insets(8, 0, 8, 0);
        fc.gridx   = 1;
        fc.weightx = 1.0;

        Color labelColor = new Color(200, 180, 130);
        Font  labelFont  = new Font("Monospaced", Font.BOLD, 12);

        // Theme
        lc.gridy = 0; fc.gridy = 0;
        main.add(label("Board Theme:", labelColor, labelFont), lc);
        themeCombo = new JComboBox<>(Theme.values());
        styleCombo(themeCombo);
        main.add(themeCombo, fc);

        // Square size
        lc.gridy = 1; fc.gridy = 1;
        main.add(label("Square Size:", labelColor, labelFont), lc);
        sizeCombo = new JComboBox<>(new Integer[]{64, 72, 80, 88, 96});
        sizeCombo.setSelectedItem(boardPanel.getSquareSize());
        styleCombo(sizeCombo);
        main.add(sizeCombo, fc);

        // Buttons
        lc.gridy = 2; fc.gridy = 2;
        lc.gridwidth = 2;
        lc.fill = GridBagConstraints.HORIZONTAL;

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btns.setBackground(new Color(45, 45, 45));

        JButton cancel = button("Cancel", new Color(80, 80, 80));
        JButton apply  = button("Apply",  new Color(70, 110, 50));
        cancel.addActionListener(e -> dispose());
        apply.addActionListener(e  -> applySettings());

        btns.add(cancel);
        btns.add(apply);
        main.add(btns, lc);

        setContentPane(main);
    }

    private void applySettings() {
        Theme   theme = (Theme)   themeCombo.getSelectedItem();
        Integer size  = (Integer) sizeCombo.getSelectedItem();

        if (theme != null) {
            boardPanel.setLightSquare(theme.light);
            boardPanel.setDarkSquare(theme.dark);
        }
        if (size != null) {
            boardPanel.setSquareSize(size);
        }
        dispose();
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private JLabel label(String text, Color fg, Font font) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(fg);
        lbl.setFont(font);
        return lbl;
    }

    private <T> void styleCombo(JComboBox<T> combo) {
        combo.setBackground(new Color(60, 60, 60));
        combo.setForeground(new Color(220, 220, 220));
        combo.setFont(new Font("Monospaced", Font.PLAIN, 12));
    }

    private JButton button(String text, Color bg) {
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
