package chess.view;

import chess.model.Piece;
import chess.model.PlayerColor;

import java.awt.*;

/**
 * Utility class responsible for rendering chess pieces onto board squares.
 * Uses large Unicode chess symbols with a drop shadow for clarity.
 */
public class PieceRenderer {

    /**
     * Draws a chess piece centered within a square.
     *
     * @param g          the Graphics context
     * @param piece      the Piece to draw
     * @param x          the x pixel coordinate of the square's top-left corner
     * @param y          the y pixel coordinate of the square's top-left corner
     * @param squareSize the width/height of the square in pixels
     */
    public static void drawPiece(Graphics g, Piece piece, int x, int y, int squareSize) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        String symbol = piece.getSymbol();
        int fontSize  = (int)(squareSize * 0.72);
        Font font     = new Font("Segoe UI Symbol", Font.PLAIN, fontSize);

        // Fallback fonts for systems without Segoe UI Symbol
        if (!fontCanDisplay(font, symbol)) {
            font = new Font("Apple Color Emoji", Font.PLAIN, fontSize);
        }
        if (!fontCanDisplay(font, symbol)) {
            font = new Font("Noto Chess", Font.PLAIN, fontSize);
        }

        g2.setFont(font);
        FontMetrics fm = g2.getFontMetrics();
        int textWidth  = fm.stringWidth(symbol);
        int textHeight = fm.getAscent();
        int drawX = x + (squareSize - textWidth)  / 2;
        int drawY = y + (squareSize + textHeight)  / 2 - fm.getDescent();

        // Draw shadow
        g2.setColor(new Color(0, 0, 0, 60));
        g2.drawString(symbol, drawX + 2, drawY + 2);

        // Draw piece — white pieces get a dark outline for contrast
        if (piece.getColor() == PlayerColor.WHITE) {
            g2.setColor(new Color(30, 30, 30));
            // Thin outline by drawing at offsets
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if (dx != 0 || dy != 0) {
                        g2.drawString(symbol, drawX + dx, drawY + dy);
                    }
                }
            }
            g2.setColor(Color.WHITE);
        } else {
            g2.setColor(new Color(20, 20, 20));
        }
        g2.drawString(symbol, drawX, drawY);
    }

    /**
     * Checks whether a given font can render the first character of a string.
     * @param font   the Font to check
     * @param symbol the symbol string
     * @return true if the font can display it
     */
    private static boolean fontCanDisplay(Font font, String symbol) {
        if (symbol == null || symbol.isEmpty()) return false;
        return font.canDisplay(symbol.codePointAt(0));
    }
}
