package chess.model;

import java.awt.Color;
import java.io.Serializable;

/**
 * Holds visual settings for the chessboard such as colors and square size.
 * Implements Serializable so settings can be saved with the game.
 */
public class BoardSettings implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Available board color themes */
    public enum Theme {
        CLASSIC("Classic Wood", new Color(240, 217, 181), new Color(181, 136, 99)),
        TOURNAMENT("Tournament Green", new Color(238, 238, 210), new Color(118, 150, 86)),
        MIDNIGHT("Midnight Blue", new Color(222, 227, 230), new Color(58, 93, 130)),
        GRAPHITE("Graphite", new Color(200, 200, 200), new Color(90, 90, 90));

        private final String label;
        private final Color lightColor;
        private final Color darkColor;

        Theme(String label, Color light, Color dark) {
            this.label = label;
            this.lightColor = light;
            this.darkColor = dark;
        }

        public String getLabel()     { return label; }
        public Color getLightColor() { return lightColor; }
        public Color getDarkColor()  { return darkColor; }

        @Override public String toString() { return label; }
    }

    /** Available square size options */
    public enum BoardSize {
        SMALL(64), MEDIUM(80), LARGE(96);

        private final int squareSize;
        BoardSize(int size) { this.squareSize = size; }
        public int getSquareSize() { return squareSize; }
        @Override public String toString() { return name().charAt(0) + name().substring(1).toLowerCase(); }
    }

    private Theme theme;
    private BoardSize boardSize;
    private boolean showCoordinates;

    /**
     * Constructs default board settings.
     */
    public BoardSettings() {
        theme = Theme.CLASSIC;
        boardSize = BoardSize.MEDIUM;
        showCoordinates = true;
    }

    public Theme getTheme()                      { return theme; }
    public void setTheme(Theme theme)            { this.theme = theme; }

    public BoardSize getBoardSize()              { return boardSize; }
    public void setBoardSize(BoardSize size)     { this.boardSize = size; }

    public boolean isShowCoordinates()           { return showCoordinates; }
    public void setShowCoordinates(boolean show) { this.showCoordinates = show; }

    public Color getLightColor() { return theme.getLightColor(); }
    public Color getDarkColor()  { return theme.getDarkColor(); }
    public int getSquareSize()   { return boardSize.getSquareSize(); }
}
