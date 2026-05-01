import java.util.ArrayList;
import java.util.List;

/**
 * MoveValidator.java  (Phase 3 — pure rule engine)
 *
 * A stateless utility class that computes legal chess moves and detects
 * check / checkmate / stalemate without modifying any GameState.
 *
 * <p>Supported rules:</p>
 * <ul>
 *   <li>All six piece movement patterns</li>
 *   <li>Captures and blocking</li>
 *   <li>En-passant capture</li>
 *   <li>Castling (kingside and queenside) with path-clear and no-check constraints</li>
 *   <li>Move legality: a move is only legal if it does NOT leave the mover's king in check</li>
 *   <li>Check, checkmate, and stalemate detection</li>
 * </ul>
 */
public class MoveValidator {

    // =========================================================================
    // Public API
    // =========================================================================

    /**
     * Returns all fully legal destination squares for the piece at (fromRow, fromCol).
     * Illegal moves (those that leave the moving king in check) are filtered out.
     * Castling moves are appended for the king when the rights flags permit.
     *
     * @param board           8x8 piece array (null = empty)
     * @param fromRow         row of the piece to move
     * @param fromCol         column of the piece to move
     * @param enPassantTarget the [row,col] square that can be captured en passant,
     *                        or null if unavailable
     * @param canCastleKside  whether the current player retains kingside castling rights
     * @param canCastleQside  whether the current player retains queenside castling rights
     * @return list of [row, col] pairs representing legal destination squares
     */
    public static List<int[]> getLegalMoves(ChessPiece[][] board,
                                             int fromRow, int fromCol,
                                             int[] enPassantTarget,
                                             boolean canCastleKside,
                                             boolean canCastleQside) {
        ChessPiece piece = board[fromRow][fromCol];
        if (piece == null) return new ArrayList<>();

        List<int[]> pseudo = getPseudoLegalMoves(board, fromRow, fromCol, enPassantTarget);
        List<int[]> legal  = new ArrayList<>();

        for (int[] dest : pseudo) {
            if (!moveLeavesKingInCheck(board, fromRow, fromCol,
                                       dest[0], dest[1], enPassantTarget)) {
                legal.add(dest);
            }
        }

        // Castling (king only) — added after check-filtering
        if (piece.getType() == PieceType.KING) {
            addCastlingMoves(board, fromRow, fromCol, legal,
                             canCastleKside, canCastleQside);
        }

        return legal;
    }

    /**
     * Returns true if the given player's king is currently under attack.
     *
     * @param board board state
     * @param color the player to test
     * @return true if that player is in check
     */
    public static boolean isInCheck(ChessPiece[][] board, PlayerColor color) {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                ChessPiece p = board[r][c];
                if (p != null && p.getType() == PieceType.KING && p.getColor() == color) {
                    return isSquareAttacked(board, r, c, color);
                }
            }
        }
        return false;
    }

    /**
     * Returns true if the given player has zero legal moves.
     * Combined with isInCheck this distinguishes checkmate from stalemate.
     *
     * @param board           board state
     * @param color           the player whose turn it is
     * @param enPassantTarget en-passant target or null
     * @param canCastleKside  kingside castling right
     * @param canCastleQside  queenside castling right
     * @return true if the player cannot make any legal move
     */
    public static boolean hasNoLegalMoves(ChessPiece[][] board,
                                           PlayerColor color,
                                           int[] enPassantTarget,
                                           boolean canCastleKside,
                                           boolean canCastleQside) {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                ChessPiece p = board[r][c];
                if (p != null && p.getColor() == color) {
                    List<int[]> moves = getLegalMoves(board, r, c, enPassantTarget,
                                                      canCastleKside, canCastleQside);
                    if (!moves.isEmpty()) return false;
                }
            }
        }
        return true;
    }

    // =========================================================================
    // Pseudo-legal move generators (ignore check)
    // =========================================================================

    /**
     * Generates all moves for a piece without checking if they leave the king in check.
     */
    public static List<int[]> getPseudoLegalMoves(ChessPiece[][] board,
                                                    int fromRow, int fromCol,
                                                    int[] enPassantTarget) {
        ChessPiece piece = board[fromRow][fromCol];
        if (piece == null) return new ArrayList<>();

        List<int[]> moves = new ArrayList<>();
        switch (piece.getType()) {
            case PAWN:   addPawnMoves(board, fromRow, fromCol, moves, enPassantTarget); break;
            case KNIGHT: addKnightMoves(board, fromRow, fromCol, moves);                break;
            case BISHOP: addSlidingMoves(board, fromRow, fromCol, moves, true,  false); break;
            case ROOK:   addSlidingMoves(board, fromRow, fromCol, moves, false, true);  break;
            case QUEEN:  addSlidingMoves(board, fromRow, fromCol, moves, true,  true);  break;
            case KING:   addKingMoves(board, fromRow, fromCol, moves);                  break;
        }
        return moves;
    }

    // -------------------------------------------------------------------------
    // Per-piece generators
    // -------------------------------------------------------------------------

    private static void addPawnMoves(ChessPiece[][] board, int r, int c,
                                     List<int[]> moves, int[] enPassantTarget) {
        ChessPiece pawn = board[r][c];
        int dir      = (pawn.getColor() == PlayerColor.WHITE) ? -1 : 1;
        int startRow = (pawn.getColor() == PlayerColor.WHITE) ? 6 : 1;

        // One step forward
        int nr = r + dir;
        if (inBounds(nr, c) && board[nr][c] == null) {
            moves.add(new int[]{nr, c});
            // Two steps from starting row
            if (r == startRow && board[r + 2 * dir][c] == null) {
                moves.add(new int[]{r + 2 * dir, c});
            }
        }

        // Diagonal captures and en-passant
        for (int dc : new int[]{-1, 1}) {
            int nc = c + dc;
            if (!inBounds(nr, nc)) continue;
            ChessPiece target = board[nr][nc];
            if (target != null && target.getColor() != pawn.getColor()) {
                moves.add(new int[]{nr, nc});
            }
            if (enPassantTarget != null
                    && enPassantTarget[0] == nr && enPassantTarget[1] == nc) {
                moves.add(new int[]{nr, nc});
            }
        }
    }

    private static void addKnightMoves(ChessPiece[][] board, int r, int c,
                                       List<int[]> moves) {
        int[][] offsets = {{-2,-1},{-2,1},{-1,-2},{-1,2},{1,-2},{1,2},{2,-1},{2,1}};
        ChessPiece knight = board[r][c];
        for (int[] off : offsets) {
            int nr = r + off[0], nc = c + off[1];
            if (inBounds(nr, nc)) {
                ChessPiece t = board[nr][nc];
                if (t == null || t.getColor() != knight.getColor()) {
                    moves.add(new int[]{nr, nc});
                }
            }
        }
    }

    /**
     * Adds sliding piece moves (bishop, rook, or queen).
     *
     * @param diag     include diagonal directions
     * @param straight include straight (rank/file) directions
     */
    private static void addSlidingMoves(ChessPiece[][] board, int r, int c,
                                        List<int[]> moves,
                                        boolean diag, boolean straight) {
        int[][] dirs;
        if (diag && straight) {
            dirs = new int[][]{{-1,-1},{-1,0},{-1,1},{0,-1},{0,1},{1,-1},{1,0},{1,1}};
        } else if (diag) {
            dirs = new int[][]{{-1,-1},{-1,1},{1,-1},{1,1}};
        } else {
            dirs = new int[][]{{-1,0},{1,0},{0,-1},{0,1}};
        }

        ChessPiece piece = board[r][c];
        for (int[] dir : dirs) {
            int nr = r + dir[0], nc = c + dir[1];
            while (inBounds(nr, nc)) {
                ChessPiece t = board[nr][nc];
                if (t == null) {
                    moves.add(new int[]{nr, nc});
                } else {
                    if (t.getColor() != piece.getColor()) moves.add(new int[]{nr, nc});
                    break;
                }
                nr += dir[0];
                nc += dir[1];
            }
        }
    }

    private static void addKingMoves(ChessPiece[][] board, int r, int c,
                                     List<int[]> moves) {
        ChessPiece king = board[r][c];
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                int nr = r + dr, nc = c + dc;
                if (inBounds(nr, nc)) {
                    ChessPiece t = board[nr][nc];
                    if (t == null || t.getColor() != king.getColor()) {
                        moves.add(new int[]{nr, nc});
                    }
                }
            }
        }
    }

    /**
     * Appends kingside and/or queenside castling destinations.
     * Castling is only added when:
     * <ol>
     *   <li>The king is not currently in check.</li>
     *   <li>All squares between king and rook are empty.</li>
     *   <li>The king does not pass through or land on an attacked square.</li>
     * </ol>
     */
    private static void addCastlingMoves(ChessPiece[][] board,
                                          int r, int c,
                                          List<int[]> legal,
                                          boolean canCastleKside,
                                          boolean canCastleQside) {
        ChessPiece king = board[r][c];
        if (king == null || king.getType() != PieceType.KING) return;
        PlayerColor color = king.getColor();

        // King must not be in check right now
        if (isSquareAttacked(board, r, c, color)) return;

        // Kingside: f and g must be empty and unattacked
        if (canCastleKside
                && board[r][5] == null && board[r][6] == null
                && !isSquareAttacked(board, r, 5, color)
                && !isSquareAttacked(board, r, 6, color)) {
            legal.add(new int[]{r, 6});
        }

        // Queenside: b, c, d must be empty; c and d unattacked
        if (canCastleQside
                && board[r][3] == null && board[r][2] == null && board[r][1] == null
                && !isSquareAttacked(board, r, 3, color)
                && !isSquareAttacked(board, r, 2, color)) {
            legal.add(new int[]{r, 2});
        }
    }

    // =========================================================================
    // Check / attack detection
    // =========================================================================

    /**
     * Returns true if the square (r, c) is attacked by any opponent of friendColor.
     */
    public static boolean isSquareAttacked(ChessPiece[][] board,
                                            int r, int c,
                                            PlayerColor friendColor) {
        PlayerColor enemy = friendColor.opposite();
        for (int er = 0; er < 8; er++) {
            for (int ec = 0; ec < 8; ec++) {
                ChessPiece p = board[er][ec];
                if (p == null || p.getColor() != enemy) continue;
                for (int[] sq : getPseudoLegalMoves(board, er, ec, null)) {
                    if (sq[0] == r && sq[1] == c) return true;
                }
            }
        }
        return false;
    }

    /**
     * Simulates moving a piece from (fr,fc) to (tr,tc) on a cloned board and
     * returns true if that move would leave the mover's king in check.
     */
    public static boolean moveLeavesKingInCheck(ChessPiece[][] board,
                                                 int fr, int fc,
                                                 int tr, int tc,
                                                 int[] enPassantTarget) {
        ChessPiece[][] sim = copyBoard(board);
        PlayerColor mover  = sim[fr][fc].getColor();

        // Handle en-passant capture on the simulated board
        if (sim[fr][fc].getType() == PieceType.PAWN
                && fc != tc && sim[tr][tc] == null
                && enPassantTarget != null
                && enPassantTarget[0] == tr && enPassantTarget[1] == tc) {
            int capRow = (mover == PlayerColor.WHITE) ? tr + 1 : tr - 1;
            sim[capRow][tc] = null;
        }

        sim[tr][tc] = sim[fr][fc];
        sim[fr][fc] = null;

        return isInCheck(sim, mover);
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    /**
     * Creates a deep copy of the board (each non-null piece is copied).
     */
    public static ChessPiece[][] copyBoard(ChessPiece[][] board) {
        ChessPiece[][] copy = new ChessPiece[8][8];
        for (int r = 0; r < 8; r++)
            for (int c = 0; c < 8; c++)
                if (board[r][c] != null)
                    copy[r][c] = board[r][c].copy();
        return copy;
    }

    private static boolean inBounds(int r, int c) {
        return r >= 0 && r < 8 && c >= 0 && c < 8;
    }
}
