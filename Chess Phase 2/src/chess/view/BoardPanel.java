package chess.view;

import chess.controller.GameController;
import chess.model.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * The main chessboard display panel.
 * Handles click-to-move and drag-and-drop piece movement.
 * Renders the 8x8 grid, pieces, highlights, and optional rank/file labels.
 */
public class BoardPanel extends JPanel {

    private final GameController controller;

    /** Currently selected square for click-to-move (-1 if none) */
    private int selectedRow = -1;
    private int selectedCol = -1;

    /** For drag-and-drop: piece being dragged */
    private Piece draggedPiece   = null;
    private int   dragFromRow    = -1;
    private int   dragFromCol    = -1;
    private int   dragX          = -1;   // current mouse position during drag
    private int   dragY          = -1;

    /**
     * Constructs the BoardPanel and registers mouse listeners.
     * @param controller the game controller managing board state
     */
    public BoardPanel(GameController controller) {
        this.controller = controller;
        setOpaque(true);
        setupMouseListeners();
    }

    // -------------------------------------------------------------------------
    // Size helpers
    // -------------------------------------------------------------------------

    /** @return the pixel size of one board square from current settings */
    private int sq() {
        return controller.getSettings().getSquareSize();
    }

    /** @return total pixel width/height of the board */
    private int boardPx() {
        return sq() * 8;
    }

    /** @return x pixel offset so the board is centered */
    private int offsetX() {
        return (getWidth()  - boardPx()) / 2;
    }

    /** @return y pixel offset so the board is centered */
    private int offsetY() {
        return (getHeight() - boardPx()) / 2;
    }

    /** Converts a pixel x coordinate to a board column (0-7), or -1 if outside */
    private int pixelToCol(int px) {
        int col = (px - offsetX()) / sq();
        return (col >= 0 && col < 8) ? col : -1;
    }

    /** Converts a pixel y coordinate to a board row (0-7), or -1 if outside */
    private int pixelToRow(int py) {
        int row = (py - offsetY()) / sq();
        return (row >= 0 && row < 8) ? row : -1;
    }

    // -------------------------------------------------------------------------
    // Mouse handling
    // -------------------------------------------------------------------------

    /**
     * Registers mouse press, release, and drag listeners for interaction.
     */
    private void setupMouseListeners() {
        MouseAdapter adapter = new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                int row = pixelToRow(e.getY());
                int col = pixelToCol(e.getX());
                if (row < 0 || col < 0) return;

                Board board = controller.getBoard();
                Piece piece = board.getPiece(row, col);

                if (piece != null && piece.getColor() == board.getCurrentTurn()) {
                    // Start drag
                    draggedPiece = piece;
                    dragFromRow  = row;
                    dragFromCol  = col;
                    dragX = e.getX();
                    dragY = e.getY();
                    // Also set as selected (for visual highlight)
                    selectedRow = row;
                    selectedCol = col;
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                int row = pixelToRow(e.getY());
                int col = pixelToCol(e.getX());

                if (draggedPiece != null) {
                    // Drag-and-drop: attempt move if released on a different square
                    if (row >= 0 && col >= 0 && !(row == dragFromRow && col == dragFromCol)) {
                        controller.attemptMove(dragFromRow, dragFromCol, row, col);
                    }
                    // Reset drag state
                    draggedPiece = null;
                    dragFromRow  = -1;
                    dragFromCol  = -1;
                    dragX = -1;
                    dragY = -1;
                    selectedRow = -1;
                    selectedCol = -1;
                    repaint();
                    return;
                }

                // Click-to-move
                if (row < 0 || col < 0) return;
                Board board = controller.getBoard();

                if (selectedRow == -1) {
                    // Select a piece
                    Piece piece = board.getPiece(row, col);
                    if (piece != null && piece.getColor() == board.getCurrentTurn()) {
                        selectedRow = row;
                        selectedCol = col;
                    }
                } else {
                    // Move to clicked square
                    if (row == selectedRow && col == selectedCol) {
                        // Deselect
                        selectedRow = -1;
                        selectedCol = -1;
                    } else {
                        controller.attemptMove(selectedRow, selectedCol, row, col);
                        selectedRow = -1;
                        selectedCol = -1;
                    }
                }
                repaint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (draggedPiece != null) {
                    dragX = e.getX();
                    dragY = e.getY();
                    repaint();
                }
            }
        };

        addMouseListener(adapter);
        addMouseMotionListener(adapter);
    }

    // -------------------------------------------------------------------------
    // Painting
    // -------------------------------------------------------------------------

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Background
        g2.setColor(new Color(40, 40, 40));
        g2.fillRect(0, 0, getWidth(), getHeight());

        int sq  = sq();
        int ox  = offsetX();
        int oy  = offsetY();
        Board board    = controller.getBoard();
        BoardSettings  settings = controller.getSettings();

        // Board border / shadow
        g2.setColor(new Color(0, 0, 0, 120));
        g2.fillRoundRect(ox - 6, oy - 6, boardPx() + 12, boardPx() + 12, 8, 8);
        g2.setColor(new Color(90, 60, 30));
        g2.setStroke(new BasicStroke(4));
        g2.drawRoundRect(ox - 4, oy - 4, boardPx() + 8, boardPx() + 8, 6, 6);

        // Draw squares
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                boolean light = (r + c) % 2 == 0;
                Color sqColor = light ? settings.getLightColor() : settings.getDarkColor();

                // Highlight selected square
                if (r == selectedRow && c == selectedCol) {
                    sqColor = new Color(246, 246, 105);
                }

                g2.setColor(sqColor);
                g2.fillRect(ox + c * sq, oy + r * sq, sq, sq);

                // Subtle grid line
                g2.setColor(new Color(0, 0, 0, 18));
                g2.setStroke(new BasicStroke(1));
                g2.drawRect(ox + c * sq, oy + r * sq, sq, sq);
            }
        }

        // Draw coordinate labels
        if (settings.isShowCoordinates()) {
            drawCoordinates(g2, ox, oy, sq);
        }

        // Draw pieces (skip the square being dragged from)
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                if (draggedPiece != null && r == dragFromRow && c == dragFromCol) continue;
                Piece piece = board.getPiece(r, c);
                if (piece != null) {
                    PieceRenderer.drawPiece(g2, piece, ox + c * sq, oy + r * sq, sq);
                }
            }
        }

        // Draw dragged piece at cursor position
        if (draggedPiece != null && dragX >= 0) {
            PieceRenderer.drawPiece(g2, draggedPiece, dragX - sq / 2, dragY - sq / 2, sq);
        }

        // Turn indicator bar
        drawTurnIndicator(g2);
    }

    /**
     * Draws rank (1-8) and file (a-h) labels around the board edges.
     */
    private void drawCoordinates(Graphics2D g2, int ox, int oy, int sq) {
        Font labelFont = new Font("Monospaced", Font.BOLD, Math.max(9, sq / 7));
        g2.setFont(labelFont);

        for (int i = 0; i < 8; i++) {
            // Rank numbers on the left
            String rank = String.valueOf(8 - i);
            g2.setColor(new Color(200, 180, 140));
            g2.drawString(rank, ox - 14, oy + i * sq + sq / 2 + 5);

            // File letters on the bottom
            String file = String.valueOf((char)('a' + i));
            g2.drawString(file, ox + i * sq + sq / 2 - 4, oy + boardPx() + 14);
        }
    }

    /**
     * Draws a small colored bar at the bottom indicating whose turn it is.
     */
    private void drawTurnIndicator(Graphics2D g2) {
        PlayerColor turn = controller.getBoard().getCurrentTurn();
        String label = turn.name() + "'S TURN";
        Color color  = (turn == PlayerColor.WHITE) ? new Color(255, 255, 255, 210) : new Color(40, 40, 40, 210);
        Color bgColor = (turn == PlayerColor.WHITE) ? new Color(220, 220, 220, 180) : new Color(50, 50, 50, 180);

        int barW = 140, barH = 28;
        int barX = (getWidth() - barW) / 2;
        int barY = offsetY() + boardPx() + 20;

        g2.setColor(bgColor);
        g2.fillRoundRect(barX, barY, barW, barH, 14, 14);
        g2.setColor(color);
        g2.setFont(new Font("Monospaced", Font.BOLD, 12));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(label, barX + (barW - fm.stringWidth(label)) / 2, barY + 19);
    }

    @Override
    public Dimension getPreferredSize() {
        int total = boardPx() + 60;
        return new Dimension(total, total);
    }

    /**
     * Refreshes the board display. Call after any state change.
     */
    public void refresh() {
        revalidate();
        repaint();
    }

    /**
     * Clears the current selection highlight.
     */
    public void clearSelection() {
        selectedRow = -1;
        selectedCol = -1;
        repaint();
    }
}
