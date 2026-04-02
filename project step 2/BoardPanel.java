package chess;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.function.Consumer;

/**
 * A Swing panel that renders an 8×8 chessboard and handles
 * click-to-move and drag-and-drop piece movement.
 *
 * <p>Movement validation is intentionally omitted per Phase 2 spec —
 * any piece may move to any square.</p>
 */
public class BoardPanel extends JPanel {

    // ---- state ----
    private final GameState state;
    private BoardTheme theme;

    /** Square that is currently selected (click-to-move first click), or null. */
    private int[] selectedSquare = null;

    /** Piece being dragged, or null. */
    private Piece dragPiece = null;
    /** Square the drag started from. */
    private int[] dragFrom = null;
    /** Current cursor position while dragging. */
    private Point dragCursor = null;

    /** Called after every legal move with the captured piece (may be null). */
    private Consumer<Piece> moveListener;

    // ---- fonts ----
    private Font pieceFont;

    /**
     * Constructs the board panel.
     *
     * @param state  the game state to render
     * @param theme  visual theme
     */
    public BoardPanel(GameState state, BoardTheme theme) {
        this.state = state;
        this.theme = theme;
        buildPieceFont();
        setPreferredSize(new Dimension(8 * squarePx(), 8 * squarePx()));
        attachMouseListeners();
    }

    /** Replaces the theme and repaints. */
    public void setTheme(BoardTheme theme) {
        this.theme = theme;
        buildPieceFont();
        int sz = 8 * squarePx();
        setPreferredSize(new Dimension(sz, sz));
        revalidate();
        repaint();
    }

    /** Registers a callback invoked after every move. */
    public void setMoveListener(Consumer<Piece> listener) {
        this.moveListener = listener;
    }

    // -------------------------------------------------------------------------
    // Painting
    // -------------------------------------------------------------------------

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int sq = squarePx();

        // Draw squares
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                boolean light = (r + c) % 2 == 0;
                Color bg = light
                        ? theme.getBoardStyle().getLightSquare()
                        : theme.getBoardStyle().getDarkSquare();

                // Highlight selected square
                if (selectedSquare != null && selectedSquare[0] == r && selectedSquare[1] == c) {
                    bg = bg.brighter().brighter();
                }

                g2.setColor(bg);
                g2.fillRect(c * sq, r * sq, sq, sq);
            }
        }

        // Draw rank/file labels
        g2.setFont(new Font("SansSerif", Font.PLAIN, sq / 6));
        for (int i = 0; i < 8; i++) {
            // rank numbers on left edge
            boolean lightRank = (i + 0) % 2 == 0;
            g2.setColor(lightRank
                    ? theme.getBoardStyle().getDarkSquare()
                    : theme.getBoardStyle().getLightSquare());
            g2.drawString(String.valueOf(8 - i), 3, i * sq + sq / 4);
            // file letters on bottom edge
            boolean lightFile = (7 + i) % 2 == 0;
            g2.setColor(lightFile
                    ? theme.getBoardStyle().getDarkSquare()
                    : theme.getBoardStyle().getLightSquare());
            g2.drawString(String.valueOf((char) ('a' + i)), i * sq + sq - sq / 5, 8 * sq - 3);
        }

        // Draw pieces (skip dragged piece at its origin)
        g2.setFont(pieceFont);
        FontMetrics fm = g2.getFontMetrics();
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                if (dragFrom != null && dragFrom[0] == r && dragFrom[1] == c) continue;
                Piece p = state.getPiece(r, c);
                if (p == null) continue;
                drawPiece(g2, p, c * sq, r * sq, sq, fm);
            }
        }

        // Draw dragged piece at cursor
        if (dragPiece != null && dragCursor != null) {
            drawPiece(g2, dragPiece,
                    dragCursor.x - sq / 2,
                    dragCursor.y - sq / 2,
                    sq, fm);
        }
    }

    private void drawPiece(Graphics2D g2, Piece piece, int x, int y, int sq, FontMetrics fm) {
        String sym = piece.getSymbol();
        int tw = fm.stringWidth(sym);
        int th = fm.getAscent();
        int px = x + (sq - tw) / 2;
        int py = y + (sq + th) / 2 - fm.getDescent();

        // Shadow / outline
        Color pieceColor = piece.getColor() == Piece.Color.WHITE
                ? theme.getPieceStyle().getWhiteColor()
                : theme.getPieceStyle().getBlackColor();
        Color outline = piece.getColor() == Piece.Color.WHITE
                ? new Color(0, 0, 0, 150)
                : new Color(255, 255, 255, 100);

        g2.setColor(outline);
        g2.drawString(sym, px + 1, py + 1);
        g2.setColor(pieceColor);
        g2.drawString(sym, px, py);
    }

    // -------------------------------------------------------------------------
    // Mouse interaction
    // -------------------------------------------------------------------------

    private void attachMouseListeners() {
        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int[] sq = toSquare(e.getPoint());
                if (sq == null) return;
                Piece p = state.getPiece(sq[0], sq[1]);

                if (p != null) {
                    // Start drag
                    dragPiece = p;
                    dragFrom  = sq;
                    dragCursor = e.getPoint();
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (dragFrom == null) return;
                int[] to = toSquare(e.getPoint());

                if (to != null && !(to[0] == dragFrom[0] && to[1] == dragFrom[1])) {
                    // It's a drag-drop move
                    applyMove(dragFrom, to);
                } else if (to != null && to[0] == dragFrom[0] && to[1] == dragFrom[1]) {
                    // Dropped back — treat as click selection
                    selectedSquare = dragFrom;
                    repaint();
                }
                dragPiece  = null;
                dragFrom   = null;
                dragCursor = null;
                repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (dragPiece != null) return; // handled by drag
                int[] clicked = toSquare(e.getPoint());
                if (clicked == null) return;

                if (selectedSquare == null) {
                    // First click — select if our piece
                    Piece p = state.getPiece(clicked[0], clicked[1]);
                    if (p != null) {
                        selectedSquare = clicked;
                    }
                } else {
                    // Second click — move
                    if (clicked[0] == selectedSquare[0] && clicked[1] == selectedSquare[1]) {
                        selectedSquare = null; // deselect
                    } else {
                        applyMove(selectedSquare, clicked);
                        selectedSquare = null;
                    }
                }
                repaint();
            }
        };

        MouseMotionAdapter mma = new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragFrom != null) {
                    dragCursor = e.getPoint();
                    repaint();
                }
            }
        };

        addMouseListener(ma);
        addMouseMotionListener(mma);
    }

    /**
     * Applies a move from {@code from} to {@code to} using the game state,
     * then notifies the move listener.
     */
    private void applyMove(int[] from, int[] to) {
        Piece mover = state.getPiece(from[0], from[1]);
        if (mover == null) return;

        Piece captured = state.move(from[0], from[1], to[0], to[1]);
        repaint();

        if (moveListener != null) {
            moveListener.accept(captured);
        }
    }

    // -------------------------------------------------------------------------
    // Utilities
    // -------------------------------------------------------------------------

    /** Converts a pixel point to [row, col], or null if outside board. */
    private int[] toSquare(Point p) {
        int sq = squarePx();
        int col = p.x / sq;
        int row = p.y / sq;
        if (row < 0 || row > 7 || col < 0 || col > 7) return null;
        return new int[]{row, col};
    }

    private int squarePx() {
        return theme.getBoardSize().getSquarePx();
    }

    private void buildPieceFont() {
        int fontSize = (int) (squarePx() * 0.72);
        pieceFont = new Font("Serif", Font.PLAIN, fontSize);
    }
}
