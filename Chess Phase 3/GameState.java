import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * GameState.java  (Phase 3 — fully integrated)
 *
 * Holds the complete state of a chess game:
 * <ul>
 *   <li>8×8 board of {@link ChessPiece} objects (null = empty)</li>
 *   <li>Whose turn it is</li>
 *   <li>Castling rights for both players</li>
 *   <li>En-passant target square</li>
 *   <li>Full move history (supports undo)</li>
 * </ul>
 *
 * All move execution is delegated to {@link MoveValidator} for legality checking.
 */
public class GameState {

    // =========================================================================
    // MoveRecord  — everything needed to describe and fully undo one move
    // =========================================================================

    /**
     * An immutable snapshot of a single move and the state it was made from.
     */
    public static class MoveRecord {
        public final ChessPiece piece;            // the piece that moved (copy)
        public final int        fromRow, fromCol;
        public final int        toRow,   toCol;
        public final ChessPiece captured;         // piece captured (copy), or null
        public final boolean    wasEnPassant;
        public final boolean    wasCastle;
        public final int[]      prevEnPassantTarget;
        public final boolean    prevWCK, prevWCQ; // previous castling rights
        public final boolean    prevBCK, prevBCQ;
        public final PieceType  promotedTo;       // non-null only on pawn promotion

        public MoveRecord(ChessPiece piece,
                          int fromRow, int fromCol,
                          int toRow,   int toCol,
                          ChessPiece captured,
                          boolean wasEnPassant, boolean wasCastle,
                          int[] prevEnPassantTarget,
                          boolean prevWCK, boolean prevWCQ,
                          boolean prevBCK, boolean prevBCQ,
                          PieceType promotedTo) {
            this.piece               = piece;
            this.fromRow             = fromRow;
            this.fromCol             = fromCol;
            this.toRow               = toRow;
            this.toCol               = toCol;
            this.captured            = captured;
            this.wasEnPassant        = wasEnPassant;
            this.wasCastle           = wasCastle;
            this.prevEnPassantTarget = prevEnPassantTarget;
            this.prevWCK             = prevWCK;
            this.prevWCQ             = prevWCQ;
            this.prevBCK             = prevBCK;
            this.prevBCQ             = prevBCQ;
            this.promotedTo          = promotedTo;
        }

        /**
         * Returns a human-readable description for the move history panel.
         * Uses algebraic notation where possible (castling as O-O / O-O-O).
         */
        public String describe() {
            String cols = "abcdefgh";
            String from = "" + cols.charAt(fromCol) + (8 - fromRow);
            String to   = "" + cols.charAt(toCol)   + (8 - toRow);

            if (wasCastle) return (toCol == 6) ? "O-O" : "O-O-O";

            String desc = piece.getShortName() + " " + from + "\u2192" + to;
            if (captured != null) desc += " \u2715" + captured.getShortName();
            if (wasEnPassant) desc += " e.p.";
            if (promotedTo != null)
                desc += "=" + promotedTo.name().charAt(0);
            return desc;
        }
    }

    // =========================================================================
    // State fields
    // =========================================================================

    private ChessPiece[][] board;
    private PlayerColor    currentTurn;
    private List<MoveRecord> moveHistory;

    // Castling rights
    private boolean whiteCanCastleKside = true;
    private boolean whiteCanCastleQside = true;
    private boolean blackCanCastleKside = true;
    private boolean blackCanCastleQside = true;

    /**
     * En-passant target square: the square a pawn may move to in order
     * to capture the pawn that just made a double push.  Null if unavailable.
     */
    private int[] enPassantTarget = null;

    // =========================================================================
    // Construction / reset
    // =========================================================================

    /** Creates a new game in the standard starting position. */
    public GameState() {
        board       = new ChessPiece[8][8];
        moveHistory = new ArrayList<>();
        currentTurn = PlayerColor.WHITE;
        setupInitialBoard();
    }

    private void setupInitialBoard() {
        PieceType[] backRank = {
            PieceType.ROOK, PieceType.KNIGHT, PieceType.BISHOP, PieceType.QUEEN,
            PieceType.KING, PieceType.BISHOP, PieceType.KNIGHT, PieceType.ROOK
        };
        for (int c = 0; c < 8; c++) {
            board[0][c] = new ChessPiece(backRank[c], PlayerColor.BLACK, 0, c);
            board[1][c] = new ChessPiece(PieceType.PAWN, PlayerColor.BLACK, 1, c);
            board[6][c] = new ChessPiece(PieceType.PAWN, PlayerColor.WHITE, 6, c);
            board[7][c] = new ChessPiece(backRank[c],    PlayerColor.WHITE, 7, c);
        }
    }

    /** Resets to the standard starting position. */
    public void newGame() {
        board       = new ChessPiece[8][8];
        moveHistory = new ArrayList<>();
        currentTurn = PlayerColor.WHITE;
        whiteCanCastleKside = whiteCanCastleQside = true;
        blackCanCastleKside = blackCanCastleQside = true;
        enPassantTarget = null;
        setupInitialBoard();
    }

    // =========================================================================
    // Accessors
    // =========================================================================

    public ChessPiece   getPiece(int r, int c)   { return board[r][c]; }
    public PlayerColor  getCurrentTurn()          { return currentTurn; }
    public List<MoveRecord> getMoveHistory()      { return moveHistory; }
    public int[]        getEnPassantTarget()      { return enPassantTarget; }

    public boolean canCastleKingside(PlayerColor p) {
        return p == PlayerColor.WHITE ? whiteCanCastleKside : blackCanCastleKside;
    }
    public boolean canCastleQueenside(PlayerColor p) {
        return p == PlayerColor.WHITE ? whiteCanCastleQside : blackCanCastleQside;
    }

    // =========================================================================
    // Legal move / game-state queries (delegated to MoveValidator)
    // =========================================================================

    /**
     * Returns all legal destination squares for the piece at (row, col).
     * Returns an empty list if the square is empty or belongs to the non-active player.
     */
    public List<int[]> getLegalMovesFor(int row, int col) {
        ChessPiece p = board[row][col];
        if (p == null || p.getColor() != currentTurn) return new ArrayList<>();
        return MoveValidator.getLegalMoves(board, row, col, enPassantTarget,
                canCastleKingside(currentTurn), canCastleQueenside(currentTurn));
    }

    /** Returns true if the active player's king is currently in check. */
    public boolean isCurrentPlayerInCheck() {
        return MoveValidator.isInCheck(board, currentTurn);
    }

    /** Returns true if the active player is in checkmate. */
    public boolean isCheckmate() {
        return isCurrentPlayerInCheck()
            && MoveValidator.hasNoLegalMoves(board, currentTurn, enPassantTarget,
                    canCastleKingside(currentTurn), canCastleQueenside(currentTurn));
    }

    /** Returns true if the active player is in stalemate. */
    public boolean isStalemate() {
        return !isCurrentPlayerInCheck()
            && MoveValidator.hasNoLegalMoves(board, currentTurn, enPassantTarget,
                    canCastleKingside(currentTurn), canCastleQueenside(currentTurn));
    }

    // =========================================================================
    // Move execution
    // =========================================================================

    /**
     * Validates and executes a move, automatically promoting any pawn to Queen.
     * Returns the captured piece (or null), or null if the move is illegal.
     */
    public ChessPiece movePiece(int fromRow, int fromCol, int toRow, int toCol) {
        return movePieceWithPromotion(fromRow, fromCol, toRow, toCol, PieceType.QUEEN);
    }

    /**
     * Validates and executes a move with a caller-specified promotion type.
     * Returns the captured piece (may be null even on a legal move), or null
     * if the move is illegal (in that case the board is left unchanged).
     *
     * <p>Handles:</p>
     * <ul>
     *   <li>Normal moves and captures</li>
     *   <li>En-passant capture</li>
     *   <li>Castling (rook repositioned automatically)</li>
     *   <li>Pawn promotion</li>
     *   <li>Castling-rights updates after king / rook moves</li>
     *   <li>En-passant target update after pawn double-push</li>
     * </ul>
     *
     * @param fromRow       source row
     * @param fromCol       source column
     * @param toRow         destination row
     * @param toCol         destination column
     * @param promotionType piece type to promote to (if applicable)
     * @return the captured piece, or null
     */
    public ChessPiece movePieceWithPromotion(int fromRow, int fromCol,
                                              int toRow,   int toCol,
                                              PieceType promotionType) {
        // --- Legality check ---
        boolean legal = false;
        for (int[] sq : getLegalMovesFor(fromRow, fromCol)) {
            if (sq[0] == toRow && sq[1] == toCol) { legal = true; break; }
        }
        if (!legal) return null;

        ChessPiece moving   = board[fromRow][fromCol];
        ChessPiece captured = board[toRow][toCol];
        boolean isEnPassant = false;
        boolean isCastle    = false;
        PieceType promotedTo = null;

        // Save pre-move state for undo
        int[]   prevEP  = enPassantTarget;
        boolean prevWCK = whiteCanCastleKside, prevWCQ = whiteCanCastleQside;
        boolean prevBCK = blackCanCastleKside, prevBCQ = blackCanCastleQside;

        // --- En-passant capture ---
        if (moving.getType() == PieceType.PAWN
                && fromCol != toCol && board[toRow][toCol] == null
                && enPassantTarget != null
                && enPassantTarget[0] == toRow && enPassantTarget[1] == toCol) {
            int capRow = (moving.getColor() == PlayerColor.WHITE) ? toRow + 1 : toRow - 1;
            captured               = board[capRow][toCol];
            board[capRow][toCol]   = null;
            isEnPassant            = true;
        }

        // --- Castling: reposition the rook ---
        if (moving.getType() == PieceType.KING && Math.abs(toCol - fromCol) == 2) {
            isCastle = true;
            if (toCol == 6) {                        // kingside
                board[fromRow][5] = board[fromRow][7];
                board[fromRow][7] = null;
                if (board[fromRow][5] != null) {
                    board[fromRow][5].setRow(fromRow);
                    board[fromRow][5].setCol(5);
                }
            } else {                                 // queenside
                board[fromRow][3] = board[fromRow][0];
                board[fromRow][0] = null;
                if (board[fromRow][3] != null) {
                    board[fromRow][3].setRow(fromRow);
                    board[fromRow][3].setCol(3);
                }
            }
        }

        // --- Apply the move ---
        board[toRow][toCol]     = moving;
        board[fromRow][fromCol] = null;
        moving.setRow(toRow);
        moving.setCol(toCol);

        // --- Pawn promotion ---
        if (moving.getType() == PieceType.PAWN && (toRow == 0 || toRow == 7)) {
            board[toRow][toCol] = new ChessPiece(promotionType,
                                                  moving.getColor(), toRow, toCol);
            promotedTo = promotionType;
        }

        // --- Update en-passant target ---
        enPassantTarget = (moving.getType() == PieceType.PAWN
                           && Math.abs(toRow - fromRow) == 2)
            ? new int[]{ (moving.getColor() == PlayerColor.WHITE)
                         ? toRow + 1 : toRow - 1, toCol }
            : null;

        // --- Update castling rights ---
        if (moving.getType() == PieceType.KING) {
            if (moving.getColor() == PlayerColor.WHITE) {
                whiteCanCastleKside = whiteCanCastleQside = false;
            } else {
                blackCanCastleKside = blackCanCastleQside = false;
            }
        }
        if (moving.getType() == PieceType.ROOK) {
            if (moving.getColor() == PlayerColor.WHITE) {
                if (fromCol == 7) whiteCanCastleKside = false;
                if (fromCol == 0) whiteCanCastleQside = false;
            } else {
                if (fromCol == 7) blackCanCastleKside = false;
                if (fromCol == 0) blackCanCastleQside = false;
            }
        }
        // Revoke rights if the rook's starting square is captured
        if (captured != null && captured.getType() == PieceType.ROOK) {
            if (toRow == 7) {
                if (toCol == 7) whiteCanCastleKside = false;
                if (toCol == 0) whiteCanCastleQside = false;
            } else if (toRow == 0) {
                if (toCol == 7) blackCanCastleKside = false;
                if (toCol == 0) blackCanCastleQside = false;
            }
        }

        // --- Record ---
        moveHistory.add(new MoveRecord(
            moving.copy(), fromRow, fromCol, toRow, toCol,
            captured != null ? captured.copy() : null,
            isEnPassant, isCastle, prevEP,
            prevWCK, prevWCQ, prevBCK, prevBCQ, promotedTo
        ));

        // --- Switch turn ---
        currentTurn = currentTurn.opposite();

        return captured;
    }

    // =========================================================================
    // Undo
    // =========================================================================

    /**
     * Undoes the most recently made move, fully restoring board state,
     * castling rights, and en-passant availability.
     *
     * @return the MoveRecord that was undone, or null if history is empty
     */
    public MoveRecord undoLastMove() {
        if (moveHistory.isEmpty()) return null;
        MoveRecord last = moveHistory.remove(moveHistory.size() - 1);

        // Restore castling rights and en-passant
        whiteCanCastleKside = last.prevWCK;
        whiteCanCastleQside = last.prevWCQ;
        blackCanCastleKside = last.prevBCK;
        blackCanCastleQside = last.prevBCQ;
        enPassantTarget     = last.prevEnPassantTarget;

        // Restore the moved piece (if it was promoted, put a pawn back)
        ChessPiece mover;
        if (last.promotedTo != null) {
            mover = new ChessPiece(PieceType.PAWN,
                                   last.piece.getColor(), last.fromRow, last.fromCol);
        } else {
            mover = board[last.toRow][last.toCol];
            if (mover != null) { mover.setRow(last.fromRow); mover.setCol(last.fromCol); }
        }
        board[last.fromRow][last.fromCol] = mover;
        board[last.toRow][last.toCol]     = null;

        // Restore captured piece
        if (last.wasEnPassant) {
            int capRow = (last.piece.getColor() == PlayerColor.WHITE)
                         ? last.toRow + 1 : last.toRow - 1;
            board[capRow][last.toCol] = last.captured != null ? last.captured.copy() : null;
        } else {
            board[last.toRow][last.toCol] = last.captured != null ? last.captured.copy() : null;
        }

        // Restore rook for castling undo
        if (last.wasCastle) {
            if (last.toCol == 6) {                        // kingside
                board[last.fromRow][7] = board[last.fromRow][5];
                board[last.fromRow][5] = null;
                if (board[last.fromRow][7] != null) {
                    board[last.fromRow][7].setRow(last.fromRow);
                    board[last.fromRow][7].setCol(7);
                }
            } else {                                      // queenside
                board[last.fromRow][0] = board[last.fromRow][3];
                board[last.fromRow][3] = null;
                if (board[last.fromRow][0] != null) {
                    board[last.fromRow][0].setRow(last.fromRow);
                    board[last.fromRow][0].setCol(0);
                }
            }
        }

        // Restore turn
        currentTurn = currentTurn.opposite();
        return last;
    }

    // =========================================================================
    // Save / Load  (plain-text format)
    // =========================================================================

    /**
     * Serializes the current board position, turn, and castling rights
     * to a plain-text string that can be written to a file.
     */
    public String serialize() {
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                ChessPiece p = board[r][c];
                if (p != null) {
                    sb.append(p.getColor()).append(",")
                      .append(p.getType()).append(",")
                      .append(r).append(",").append(c).append("\n");
                }
            }
        }
        sb.append("TURN,").append(currentTurn).append("\n");
        sb.append("CASTLE,")
          .append(whiteCanCastleKside).append(",")
          .append(whiteCanCastleQside).append(",")
          .append(blackCanCastleKside).append(",")
          .append(blackCanCastleQside).append("\n");
        return sb.toString();
    }

    /**
     * Restores the game from a previously serialized string.
     * Move history is not restored (undo will be unavailable after load).
     *
     * @param data the text produced by {@link #serialize()}
     */
    public void deserialize(String data) {
        board       = new ChessPiece[8][8];
        moveHistory = new ArrayList<>();
        enPassantTarget     = null;
        whiteCanCastleKside = whiteCanCastleQside = false;
        blackCanCastleKside = blackCanCastleQside = false;

        for (String line : data.split("\n")) {
            line = line.trim();
            if (line.startsWith("TURN,")) {
                currentTurn = PlayerColor.valueOf(line.split(",")[1].trim());
            } else if (line.startsWith("CASTLE,")) {
                String[] p = line.split(",");
                whiteCanCastleKside = Boolean.parseBoolean(p[1].trim());
                whiteCanCastleQside = Boolean.parseBoolean(p[2].trim());
                blackCanCastleKside = Boolean.parseBoolean(p[3].trim());
                blackCanCastleQside = Boolean.parseBoolean(p[4].trim());
            } else if (!line.isBlank()) {
                try {
                    String[] p    = line.split(",");
                    PlayerColor c = PlayerColor.valueOf(p[0].trim());
                    PieceType   t = PieceType.valueOf(p[1].trim());
                    int row = Integer.parseInt(p[2].trim());
                    int col = Integer.parseInt(p[3].trim());
                    board[row][col] = new ChessPiece(t, c, row, col);
                } catch (Exception ignored) { }
            }
        }
    }
}
