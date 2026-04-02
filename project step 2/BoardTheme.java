package chess;

import java.awt.Color;
import java.io.Serializable;

/**
 * Encapsulates visual settings for the chessboard and pieces.
 * Used by the Settings Window (Feature 2) to customize appearance.
 */
public class BoardTheme implements Serializable {

    /** Available board color presets. */
    public enum BoardStyle {
        CLASSIC("Classic Wood", new Color(0xF0D9B5), new Color(0xB58863)),
        MODERN("Modern Gray",   new Color(0xEEEED2), new Color(0x769656)),
        OCEAN("Ocean Blue",     new Color(0xD6E4F0), new Color(0x2471A3)),
        MIDNIGHT("Midnight",    new Color(0x2C3E50), new Color(0x1A252F));

        private final String label;
        private final Color light;
        private final Color dark;

        BoardStyle(String label, Color light, Color dark) {
            this.label = label; this.light = light; this.dark = dark;
        }
        public String getLabel()  { return label; }
        public Color  getLightSquare() { return light; }
        public Color  getDarkSquare()  { return dark; }
        @Override public String toString() { return label; }
    }

    /** Available piece font-size presets (board size). */
    public enum BoardSize {
        SMALL(56), MEDIUM(72), LARGE(90);
        private final int squarePx;
        BoardSize(int px) { this.squarePx = px; }
        public int getSquarePx() { return squarePx; }
        @Override public String toString() {
            return name().charAt(0) + name().substring(1).toLowerCase();
        }
    }

    /** Available piece color styles. */
    public enum PieceStyle {
        CLASSIC("Classic",  new Color(0xFFFACD), new Color(0x1C1C1C)),
        PASTEL ("Pastel",   new Color(0xFFB3BA), new Color(0xBAE1FF)),
        BOLD   ("Bold",     new Color(0xFFFFFF), new Color(0xFF4500));

        private final String label;
        private final Color whiteColor;
        private final Color blackColor;

        PieceStyle(String label, Color w, Color b) {
            this.label = label; this.whiteColor = w; this.blackColor = b;
        }
        public String getLabel() { return label; }
        public Color getWhiteColor() { return whiteColor; }
        public Color getBlackColor() { return blackColor; }
        @Override public String toString() { return label; }
    }

    private BoardStyle boardStyle;
    private BoardSize  boardSize;
    private PieceStyle pieceStyle;

    /** Creates a default (Classic, Medium, Classic) theme. */
    public BoardTheme() {
        boardStyle = BoardStyle.CLASSIC;
        boardSize  = BoardSize.MEDIUM;
        pieceStyle = PieceStyle.CLASSIC;
    }

    public BoardStyle getBoardStyle() { return boardStyle; }
    public void setBoardStyle(BoardStyle s) { this.boardStyle = s; }

    public BoardSize getBoardSize() { return boardSize; }
    public void setBoardSize(BoardSize s) { this.boardSize = s; }

    public PieceStyle getPieceStyle() { return pieceStyle; }
    public void setPieceStyle(PieceStyle s) { this.pieceStyle = s; }
}
