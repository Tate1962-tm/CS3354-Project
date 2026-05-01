import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ChessBoardPanel.java  (Phase 3 — integrated with GameState + MoveValidator)
 *
 * Renders the 8×8 chessboard and handles all user input.
 *
 * <p>Phase 3 additions over Phase 2:</p>
 * <ul>
 *   <li>Legal-move destinations highlighted as green dots / capture rings</li>
 *   <li>All moves are validated — illegal moves are silently rejected</li>
 *   <li>King square is tinted red when the active player is in check</li>
 *   <li>Pawn promotion dialog lets the player choose a piece</li>
 *   <li>Castling is handled transparently by the backend</li>
 *   <li>Game-ending events (checkmate, stalemate) propagated via {@link GameEventListener}</li>
 * </ul>
 */
public class ChessBoardPanel extends JPanel {

    // =========================================================================
    // GameEventListener interface
    // =========================================================================

    /**
     * Callback interface so the parent frame can react to game events.
     */
    public interface GameEventListener {
        /** Called after every successfully completed move. */
        void onMoveMade();
        /** Called when checkmate is detected. */
        void onCheckmate(PlayerColor winner);
        /** Called when stalemate is detected. */
        void onStalemate();
    }

    // =========================================================================
    // Fields
    // =========================================================================

    private int           squareSize = 80;
    private final GameState           gameState;
    private final GameEventListener   listener;

    // Selection / click-to-move
    private int         selectedRow = -1;
    private int         selectedCol = -1;
    private List<int[]> legalMovesForSelected = new ArrayList<>();

    // Drag-and-drop
    private ChessPiece draggingPiece = null;
    private int        dragX, dragY;
    private int        dragFromRow = -1;
    private int        dragFromCol = -1;

    // Colour theme (changeable via SettingsDialog)
    private Color lightSquare   = new Color(240, 217, 181);
    private Color darkSquare    = new Color(181, 136,  99);
    private Color selectedColor = new Color(106, 168,  79, 160);
    private Color legalDotColor = new Color(  0,   0,   0,  55);
    private Color checkColor    = new Color(220,  50,  50, 140);

    // =========================================================================
    // Construction
    // =========================================================================

    /**
     * @param gameState the integrated game model
     * @param listener  event sink for move notifications
     */
    public ChessBoardPanel(GameState gameState, GameEventListener listener) {
        this.gameState = gameState;
        this.listener  = listener;
        setPreferredSize(new Dimension(squareSize * 8, squareSize * 8));
        setupMouseHandlers();
    }

    // =========================================================================
    // Theme / size setters (called by SettingsDialog)
    // =========================================================================

    public int  getSquareSize()         { return squareSize; }
    public void setSquareSize(int size) {
        this.squareSize = size;
        setPreferredSize(new Dimension(size * 8, size * 8));
        revalidate();
        repaint();
    }
    public void setLightSquare(Color c) { lightSquare = c; repaint(); }
    public void setDarkSquare(Color c)  { darkSquare  = c; repaint(); }

    // =========================================================================
    // Mouse input
    // =========================================================================

    private void setupMouseHandlers() {
        addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                int col = e.getX() / squareSize;
                int row = e.getY() / squareSize;
                if (!inBounds(row, col)) return;

                ChessPiece piece = gameState.getPiece(row, col);
                if (piece != null && piece.getColor() == gameState.getCurrentTurn()) {
                    // Begin drag and also pre-select for visual legal-move hints
                    draggingPiece = piece;
                    dragFromRow   = row;
                    dragFromCol   = col;
                    dragX = e.getX();
                    dragY = e.getY();

                    selectedRow = row;
                    selectedCol = col;
                    legalMovesForSelected = gameState.getLegalMovesFor(row, col);
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                int col = e.getX() / squareSize;
                int row = e.getY() / squareSize;

                if (draggingPiece != null) {
                    if (inBounds(row, col)
                            && (row != dragFromRow || col != dragFromCol)) {
                        attemptMove(dragFromRow, dragFromCol, row, col);
                    }
                    draggingPiece = null;
                    dragFromRow   = -1;
                    dragFromCol   = -1;
                    clearSelection();
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                // The drag handler already consumed the event if dragging occurred
                if (draggingPiece != null) return;

                int col = e.getX() / squareSize;
                int row = e.getY() / squareSize;
                if (!inBounds(row, col)) return;

                if (selectedRow == -1) {
                    // No piece selected — try to select one
                    ChessPiece piece = gameState.getPiece(row, col);
                    if (piece != null && piece.getColor() == gameState.getCurrentTurn()) {
                        selectedRow = row;
                        selectedCol = col;
                        legalMovesForSelected = gameState.getLegalMovesFor(row, col);
                        repaint();
                    }
                } else {
                    if (row == selectedRow && col == selectedCol) {
                        clearSelection();           // deselect on second click
                    } else {
                        ChessPiece target = gameState.getPiece(row, col);
                        if (target != null
                                && target.getColor() == gameState.getCurrentTurn()) {
                            // Switch selection to a different friendly piece
                            selectedRow = row;
                            selectedCol = col;
                            legalMovesForSelected = gameState.getLegalMovesFor(row, col);
                            repaint();
                        } else {
                            attemptMove(selectedRow, selectedCol, row, col);
                            clearSelection();
                        }
                    }
                }
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (draggingPiece != null) {
                    dragX = e.getX();
                    dragY = e.getY();
                    repaint();
                }
            }
        });
    }

    private void clearSelection() {
        selectedRow = -1;
        selectedCol = -1;
        legalMovesForSelected = new ArrayList<>();
        repaint();
    }

    /**
     * Validates and executes a move from (fromRow,fromCol) to (toRow,toCol).
     * Handles pawn-promotion dialog, board repaint, and game-ending detection.
     */
    private void attemptMove(int fromRow, int fromCol, int toRow, int toCol) {
        if (!inBounds(toRow, toCol)) return;

        // Confirm the destination is in the pre-computed legal-move list
        boolean legal = false;
        for (int[] sq : legalMovesForSelected) {
            if (sq[0] == toRow && sq[1] == toCol) { legal = true; break; }
        }
        if (!legal) return;

        ChessPiece moving = gameState.getPiece(fromRow, fromCol);
        if (moving == null) return;

        // Pawn promotion: ask the player which piece they want
        PieceType promotionChoice = PieceType.QUEEN;
        boolean isPromotion = moving.getType() == PieceType.PAWN
                && ((moving.getColor() == PlayerColor.WHITE && toRow == 0)
                ||  (moving.getColor() == PlayerColor.BLACK && toRow == 7));
        if (isPromotion) {
            promotionChoice = askPromotionChoice(moving.getColor());
        }

        gameState.movePieceWithPromotion(fromRow, fromCol, toRow, toCol, promotionChoice);

        repaint();
        listener.onMoveMade();

        // Detect game-ending conditions after turn switch
        if (gameState.isCheckmate()) {
            PlayerColor winner = gameState.getCurrentTurn().opposite();
            listener.onCheckmate(winner);
        } else if (gameState.isStalemate()) {
            listener.onStalemate();
        }
    }

    /** Shows a dialog and returns the player's pawn-promotion choice. */
    private PieceType askPromotionChoice(PlayerColor color) {
        String[] options = {"Queen", "Rook", "Bishop", "Knight"};
        int choice = JOptionPane.showOptionDialog(
            this, "Promote pawn to:", "Pawn Promotion",
            JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
            null, options, options[0]
        );
        switch (choice) {
            case 1: return PieceType.ROOK;
            case 2: return PieceType.BISHOP;
            case 3: return PieceType.KNIGHT;
            default: return PieceType.QUEEN;
        }
    }

    private boolean inBounds(int row, int col) {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }

    // =========================================================================
    // Painting
    // =========================================================================

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        drawSquares(g2);
        drawCoordinates(g2);
        drawLegalMoveHints(g2);
        drawPieces(g2);
        drawDragPiece(g2);
    }

    /** Fills every square with the appropriate theme colour; overlays highlights. */
    private void drawSquares(Graphics2D g2) {
        // Locate king if in check (so we can tint its square red)
        int checkKingRow = -1, checkKingCol = -1;
        if (gameState.isCurrentPlayerInCheck()) {
            outer:
            for (int r = 0; r < 8; r++)
                for (int c = 0; c < 8; c++) {
                    ChessPiece p = gameState.getPiece(r, c);
                    if (p != null && p.getType() == PieceType.KING
                            && p.getColor() == gameState.getCurrentTurn()) {
                        checkKingRow = r;
                        checkKingCol = c;
                        break outer;
                    }
                }
        }

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                boolean light = (r + c) % 2 == 0;
                g2.setColor(light ? lightSquare : darkSquare);
                g2.fillRect(c * squareSize, r * squareSize, squareSize, squareSize);

                // Selected-square highlight (green tint)
                if (r == selectedRow && c == selectedCol) {
                    g2.setColor(selectedColor);
                    g2.fillRect(c * squareSize, r * squareSize, squareSize, squareSize);
                }

                // King-in-check highlight (red tint)
                if (r == checkKingRow && c == checkKingCol) {
                    g2.setColor(checkColor);
                    g2.fillRect(c * squareSize, r * squareSize, squareSize, squareSize);
                }
            }
        }
    }

    /**
     * Draws legal-move indicators:
     * <ul>
     *   <li>Filled dot on empty squares</li>
     *   <li>Hollow ring on capturable squares</li>
     * </ul>
     */
    private void drawLegalMoveHints(Graphics2D g2) {
        if (legalMovesForSelected.isEmpty()) return;
        g2.setColor(legalDotColor);
        int dot    = squareSize / 3;
        int offset = (squareSize - dot) / 2;

        for (int[] sq : legalMovesForSelected) {
            int r = sq[0], c = sq[1];
            if (gameState.getPiece(r, c) != null) {
                // Capture ring
                int ring    = squareSize - 8;
                int ringOff = 4;
                g2.setStroke(new BasicStroke(5));
                g2.drawOval(c * squareSize + ringOff,
                            r * squareSize + ringOff, ring, ring);
                g2.setStroke(new BasicStroke(1));
            } else {
                // Movement dot
                g2.fillOval(c * squareSize + offset,
                            r * squareSize + offset, dot, dot);
            }
        }
    }

    /** Draws rank (1–8) and file (a–h) labels inside the board edges. */
    private void drawCoordinates(Graphics2D g2) {
        g2.setFont(new Font("SansSerif", Font.BOLD, 11));
        String files = "abcdefgh";
        for (int c = 0; c < 8; c++) {
            boolean light = (7 + c) % 2 == 0;
            g2.setColor(light ? darkSquare : lightSquare);
            g2.drawString(String.valueOf(files.charAt(c)),
                          c * squareSize + squareSize - 10,
                          squareSize * 8 - 3);
        }
        for (int r = 0; r < 8; r++) {
            boolean light = r % 2 == 0;
            g2.setColor(light ? darkSquare : lightSquare);
            g2.drawString(String.valueOf(8 - r), 3, r * squareSize + 13);
        }
    }

    /** Draws all pieces that are not currently being dragged. */
    private void drawPieces(Graphics2D g2) {
        Font       pieceFont = new Font("Serif", Font.PLAIN, (int)(squareSize * 0.72));
        g2.setFont(pieceFont);
        FontMetrics fm = g2.getFontMetrics();

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                if (r == dragFromRow && c == dragFromCol
                        && draggingPiece != null) continue;
                ChessPiece piece = gameState.getPiece(r, c);
                if (piece != null)
                    drawPieceAt(g2, fm, piece,
                                c * squareSize, r * squareSize);
            }
        }
    }

    private void drawPieceAt(Graphics2D g2, FontMetrics fm,
                              ChessPiece piece, int x, int y) {
        String sym = piece.getSymbol();
        int px = x + (squareSize - fm.stringWidth(sym)) / 2;
        int py = y + (squareSize + fm.getAscent() - fm.getDescent()) / 2 - 2;

        // Drop shadow
        g2.setColor(new Color(0, 0, 0, 60));
        g2.drawString(sym, px + 2, py + 2);

        // Piece colour: white = bright, black = near-black
        g2.setColor(piece.getColor() == PlayerColor.WHITE
                    ? new Color(255, 255, 255)
                    : new Color(25,  25,  25));
        g2.drawString(sym, px, py);
    }

    /** Draws the piece currently being dragged at the cursor position. */
    private void drawDragPiece(Graphics2D g2) {
        if (draggingPiece == null) return;
        Font pieceFont = new Font("Serif", Font.PLAIN, (int)(squareSize * 0.82));
        g2.setFont(pieceFont);
        FontMetrics fm  = g2.getFontMetrics();
        String sym      = draggingPiece.getSymbol();
        int x = dragX - fm.stringWidth(sym) / 2;
        int y = dragY + fm.getAscent() / 2;

        g2.setColor(new Color(0, 0, 0, 80));
        g2.drawString(sym, x + 3, y + 3);
        g2.setColor(draggingPiece.getColor() == PlayerColor.WHITE
                    ? new Color(255, 255, 255)
                    : new Color(25,  25,  25));
        g2.drawString(sym, x, y);
    }

    // =========================================================================
    // Public refresh
    // =========================================================================

    /** Forces a full repaint of the board. */
    public void refresh() {
        repaint();
    }
}
