import java.util.*;

/**
 * ChessAI.java  (Phase 3 — Extra Credit B)
 *
 * A computer chess opponent using the Minimax algorithm with
 * Alpha-Beta pruning. The AI evaluates positions using material
 * count and piece-square tables, and searches to a configurable depth.
 *
 * <p>Difficulty levels:</p>
 * <ul>
 *   <li>EASY   — depth 1 (random-ish, picks any good capture)</li>
 *   <li>MEDIUM — depth 3 (plays reasonable chess)</li>
 *   <li>HARD   — depth 4 (strong, thinks 4 moves ahead)</li>
 * </ul>
 *
 * <p>Usage in ChessFrame:</p>
 * <pre>
 *   ChessAI ai = new ChessAI(ChessAI.Difficulty.MEDIUM);
 *   // After human move, if it is AI's turn:
 *   int[] move = ai.getBestMove(gameState);
 *   if (move != null)
 *       gameState.movePiece(move[0], move[1], move[2], move[3]);
 * </pre>
 */
public class ChessAI {

    // =========================================================================
    // Difficulty
    // =========================================================================

    /** AI difficulty — controls search depth. */
    public enum Difficulty {
        EASY(1, "Easy"),
        MEDIUM(3, "Medium"),
        HARD(4, "Hard");

        public final int depth;
        public final String label;

        Difficulty(int depth, String label) {
            this.depth = depth;
            this.label = label;
        }

        @Override public String toString() { return label; }
    }

    // =========================================================================
    // Fields
    // =========================================================================

    private Difficulty difficulty;
    private PlayerColor aiColor;

    // =========================================================================
    // Construction
    // =========================================================================

    /**
     * Creates an AI that plays as BLACK at the given difficulty.
     * @param difficulty the search depth / strength
     */
    public ChessAI(Difficulty difficulty) {
        this.difficulty = difficulty;
        this.aiColor    = PlayerColor.BLACK;
    }

    /**
     * Creates an AI with a specified color.
     * @param difficulty the search depth / strength
     * @param color      the color the AI plays as
     */
    public ChessAI(Difficulty difficulty, PlayerColor color) {
        this.difficulty = difficulty;
        this.aiColor    = color;
    }

    // =========================================================================
    // Public API
    // =========================================================================

    /** @return the current difficulty setting */
    public Difficulty getDifficulty()              { return difficulty; }

    /** @param d new difficulty */
    public void       setDifficulty(Difficulty d)  { this.difficulty = d; }

    /** @return the color the AI plays as */
    public PlayerColor getColor()                  { return aiColor; }

    /**
     * Returns the best move found by Minimax search.
     * The move is encoded as [fromRow, fromCol, toRow, toCol].
     *
     * @param gameState current game state (not modified)
     * @return best move array, or null if no legal moves exist
     */
    public int[] getBestMove(GameState gameState) {
        // Collect all legal moves for AI color
        List<int[]> moves = getAllLegalMoves(gameState, aiColor);
        if (moves.isEmpty()) return null;

        // EASY: just pick the best capture or a random move (depth 1 still)
        int searchDepth = difficulty.depth;

        int   bestScore = Integer.MIN_VALUE;
        int[] bestMove  = null;

        // Shuffle for variety at same score
        Collections.shuffle(moves);

        for (int[] move : moves) {
            GameState copy = copyState(gameState);
            copy.movePiece(move[0], move[1], move[2], move[3]);

            int score = minimax(copy, searchDepth - 1, Integer.MIN_VALUE,
                                Integer.MAX_VALUE, false);
            if (score > bestScore) {
                bestScore = score;
                bestMove  = move;
            }
        }
        return bestMove;
    }

    // =========================================================================
    // Minimax with Alpha-Beta pruning
    // =========================================================================

    /**
     * Minimax search with alpha-beta pruning.
     *
     * @param state        board state to evaluate
     * @param depth        remaining search depth
     * @param alpha        best score maximiser can guarantee
     * @param beta         best score minimiser can guarantee
     * @param isMaximising true if it is the AI's turn to move
     * @return heuristic evaluation score
     */
    private int minimax(GameState state, int depth,
                        int alpha, int beta, boolean isMaximising) {
        // Terminal conditions
        if (state.isCheckmate()) {
            // Current player is in checkmate — bad for that player
            return isMaximising ? -100000 : 100000;
        }
        if (state.isStalemate()) {
            return 0;   // Draw
        }
        if (depth == 0) {
            return evaluateBoard(state);
        }

        PlayerColor color = isMaximising ? aiColor : aiColor.opposite();
        List<int[]> moves = getAllLegalMoves(state, color);

        if (moves.isEmpty()) return evaluateBoard(state);

        if (isMaximising) {
            int best = Integer.MIN_VALUE;
            for (int[] move : moves) {
                GameState copy = copyState(state);
                copy.movePiece(move[0], move[1], move[2], move[3]);
                int score = minimax(copy, depth - 1, alpha, beta, false);
                best  = Math.max(best,  score);
                alpha = Math.max(alpha, best);
                if (beta <= alpha) break;   // Beta cut-off
            }
            return best;
        } else {
            int best = Integer.MAX_VALUE;
            for (int[] move : moves) {
                GameState copy = copyState(state);
                copy.movePiece(move[0], move[1], move[2], move[3]);
                int score = minimax(copy, depth - 1, alpha, beta, true);
                best = Math.min(best,  score);
                beta = Math.min(beta,  best);
                if (beta <= alpha) break;   // Alpha cut-off
            }
            return best;
        }
    }

    // =========================================================================
    // Board evaluation
    // =========================================================================

    /**
     * Evaluates the board from the AI's perspective.
     * Positive = good for AI, negative = bad for AI.
     *
     * Considers:
     *   - Material balance (piece values)
     *   - Piece-square tables (positional bonuses)
     */
    private int evaluateBoard(GameState state) {
        int score = 0;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                ChessPiece p = state.getPiece(r, c);
                if (p == null) continue;
                int value = pieceValue(p.getType())
                          + positionalBonus(p.getType(), p.getColor(), r, c);
                if (p.getColor() == aiColor) {
                    score += value;
                } else {
                    score -= value;
                }
            }
        }
        return score;
    }

    /** Returns the base material value of a piece type. */
    private int pieceValue(PieceType type) {
        switch (type) {
            case PAWN:   return 100;
            case KNIGHT: return 320;
            case BISHOP: return 330;
            case ROOK:   return 500;
            case QUEEN:  return 900;
            case KING:   return 20000;
            default:     return 0;
        }
    }

    /**
     * Returns a positional bonus for a piece based on piece-square tables.
     * Tables are from White's perspective; they are mirrored for Black.
     */
    private int positionalBonus(PieceType type, PlayerColor color, int row, int col) {
        // Mirror row for Black (Black's "good" squares mirror White's)
        int r = (color == PlayerColor.WHITE) ? row : 7 - row;
        int c = col;

        switch (type) {
            case PAWN:   return PAWN_TABLE[r][c];
            case KNIGHT: return KNIGHT_TABLE[r][c];
            case BISHOP: return BISHOP_TABLE[r][c];
            case ROOK:   return ROOK_TABLE[r][c];
            case QUEEN:  return QUEEN_TABLE[r][c];
            case KING:   return KING_TABLE[r][c];
            default:     return 0;
        }
    }

    // ── Piece-square tables (standard chess engine values) ────────────────────

    private static final int[][] PAWN_TABLE = {
        {  0,  0,  0,  0,  0,  0,  0,  0 },
        { 50, 50, 50, 50, 50, 50, 50, 50 },
        { 10, 10, 20, 30, 30, 20, 10, 10 },
        {  5,  5, 10, 25, 25, 10,  5,  5 },
        {  0,  0,  0, 20, 20,  0,  0,  0 },
        {  5, -5,-10,  0,  0,-10, -5,  5 },
        {  5, 10, 10,-20,-20, 10, 10,  5 },
        {  0,  0,  0,  0,  0,  0,  0,  0 },
    };

    private static final int[][] KNIGHT_TABLE = {
        {-50,-40,-30,-30,-30,-30,-40,-50 },
        {-40,-20,  0,  0,  0,  0,-20,-40 },
        {-30,  0, 10, 15, 15, 10,  0,-30 },
        {-30,  5, 15, 20, 20, 15,  5,-30 },
        {-30,  0, 15, 20, 20, 15,  0,-30 },
        {-30,  5, 10, 15, 15, 10,  5,-30 },
        {-40,-20,  0,  5,  5,  0,-20,-40 },
        {-50,-40,-30,-30,-30,-30,-40,-50 },
    };

    private static final int[][] BISHOP_TABLE = {
        {-20,-10,-10,-10,-10,-10,-10,-20 },
        {-10,  0,  0,  0,  0,  0,  0,-10 },
        {-10,  0,  5, 10, 10,  5,  0,-10 },
        {-10,  5,  5, 10, 10,  5,  5,-10 },
        {-10,  0, 10, 10, 10, 10,  0,-10 },
        {-10, 10, 10, 10, 10, 10, 10,-10 },
        {-10,  5,  0,  0,  0,  0,  5,-10 },
        {-20,-10,-10,-10,-10,-10,-10,-20 },
    };

    private static final int[][] ROOK_TABLE = {
        {  0,  0,  0,  0,  0,  0,  0,  0 },
        {  5, 10, 10, 10, 10, 10, 10,  5 },
        { -5,  0,  0,  0,  0,  0,  0, -5 },
        { -5,  0,  0,  0,  0,  0,  0, -5 },
        { -5,  0,  0,  0,  0,  0,  0, -5 },
        { -5,  0,  0,  0,  0,  0,  0, -5 },
        { -5,  0,  0,  0,  0,  0,  0, -5 },
        {  0,  0,  0,  5,  5,  0,  0,  0 },
    };

    private static final int[][] QUEEN_TABLE = {
        {-20,-10,-10, -5, -5,-10,-10,-20 },
        {-10,  0,  0,  0,  0,  0,  0,-10 },
        {-10,  0,  5,  5,  5,  5,  0,-10 },
        { -5,  0,  5,  5,  5,  5,  0, -5 },
        {  0,  0,  5,  5,  5,  5,  0, -5 },
        {-10,  5,  5,  5,  5,  5,  0,-10 },
        {-10,  0,  5,  0,  0,  0,  0,-10 },
        {-20,-10,-10, -5, -5,-10,-10,-20 },
    };

    private static final int[][] KING_TABLE = {
        {-30,-40,-40,-50,-50,-40,-40,-30 },
        {-30,-40,-40,-50,-50,-40,-40,-30 },
        {-30,-40,-40,-50,-50,-40,-40,-30 },
        {-30,-40,-40,-50,-50,-40,-40,-30 },
        {-20,-30,-30,-40,-40,-30,-30,-20 },
        {-10,-20,-20,-20,-20,-20,-20,-10 },
        { 20, 20,  0,  0,  0,  0, 20, 20 },
        { 20, 30, 10,  0,  0, 10, 30, 20 },
    };

    // =========================================================================
    // Helpers
    // =========================================================================

    /**
     * Returns all legal moves for all pieces of the given color.
     * Each move is [fromRow, fromCol, toRow, toCol].
     */
    private List<int[]> getAllLegalMoves(GameState state, PlayerColor color) {
        List<int[]> moves = new ArrayList<>();
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                ChessPiece p = state.getPiece(r, c);
                if (p != null && p.getColor() == color) {
                    // Temporarily set turn so getLegalMovesFor works
                    for (int[] dest : MoveValidator.getLegalMoves(
                            getBoardSnapshot(state), r, c,
                            state.getEnPassantTarget(),
                            state.canCastleKingside(color),
                            state.canCastleQueenside(color))) {
                        moves.add(new int[]{r, c, dest[0], dest[1]});
                    }
                }
            }
        }
        return moves;
    }

    /**
     * Creates a deep copy of a GameState for simulation.
     * Uses serialize/deserialize to avoid complex copy logic.
     */
    private GameState copyState(GameState original) {
        GameState copy = new GameState();
        copy.deserialize(original.serialize());
        return copy;
    }

    /**
     * Extracts the raw 8x8 board array from GameState for MoveValidator.
     */
    private ChessPiece[][] getBoardSnapshot(GameState state) {
        ChessPiece[][] board = new ChessPiece[8][8];
        for (int r = 0; r < 8; r++)
            for (int c = 0; c < 8; c++)
                board[r][c] = state.getPiece(r, c);
        return board;
    }
}
