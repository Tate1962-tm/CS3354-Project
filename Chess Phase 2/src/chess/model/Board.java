package chess.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the state of the chess board, including piece positions,
 * move history, and captured pieces. Implements Serializable for save/load.
 */
public class Board implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 8x8 grid; null means empty square */
    private Piece[][] grid;

    /** Ordered list of all moves made this game */
    private List<Move> moveHistory;

    /** Pieces captured by white */
    private List<Piece> capturedByWhite;

    /** Pieces captured by black */
    private List<Piece> capturedByBlack;

    /** Whose turn it currently is */
    private PlayerColor currentTurn;

    /**
     * Constructs a new Board and sets up the initial piece positions.
     */
    public Board() {
        grid = new Piece[8][8];
        moveHistory = new ArrayList<>();
        capturedByWhite = new ArrayList<>();
        capturedByBlack = new ArrayList<>();
        currentTurn = PlayerColor.WHITE;
        initPieces();
    }

    /**
     * Places all pieces in their standard starting positions.
     */
    private void initPieces() {
        // Black back rank
        grid[0][0] = new Piece(PieceType.ROOK,   PlayerColor.BLACK);
        grid[0][1] = new Piece(PieceType.KNIGHT, PlayerColor.BLACK);
        grid[0][2] = new Piece(PieceType.BISHOP, PlayerColor.BLACK);
        grid[0][3] = new Piece(PieceType.QUEEN,  PlayerColor.BLACK);
        grid[0][4] = new Piece(PieceType.KING,   PlayerColor.BLACK);
        grid[0][5] = new Piece(PieceType.BISHOP, PlayerColor.BLACK);
        grid[0][6] = new Piece(PieceType.KNIGHT, PlayerColor.BLACK);
        grid[0][7] = new Piece(PieceType.ROOK,   PlayerColor.BLACK);
        for (int c = 0; c < 8; c++) {
            grid[1][c] = new Piece(PieceType.PAWN, PlayerColor.BLACK);
        }

        // White back rank
        grid[7][0] = new Piece(PieceType.ROOK,   PlayerColor.WHITE);
        grid[7][1] = new Piece(PieceType.KNIGHT, PlayerColor.WHITE);
        grid[7][2] = new Piece(PieceType.BISHOP, PlayerColor.WHITE);
        grid[7][3] = new Piece(PieceType.QUEEN,  PlayerColor.WHITE);
        grid[7][4] = new Piece(PieceType.KING,   PlayerColor.WHITE);
        grid[7][5] = new Piece(PieceType.BISHOP, PlayerColor.WHITE);
        grid[7][6] = new Piece(PieceType.KNIGHT, PlayerColor.WHITE);
        grid[7][7] = new Piece(PieceType.ROOK,   PlayerColor.WHITE);
        for (int c = 0; c < 8; c++) {
            grid[6][c] = new Piece(PieceType.PAWN, PlayerColor.WHITE);
        }
    }

    /**
     * Gets the piece at the given board position.
     * @param row row index (0 = top/black side)
     * @param col column index (0 = left/a-file)
     * @return the Piece at that position, or null if empty
     */
    public Piece getPiece(int row, int col) {
        return grid[row][col];
    }

    /**
     * Returns whether a board position is empty.
     * @param row row index
     * @param col column index
     * @return true if no piece is at that position
     */
    public boolean isEmpty(int row, int col) {
        return grid[row][col] == null;
    }

    /**
     * Executes a move on the board, recording it in history and handling captures.
     * Does not validate legality—any square is a valid destination.
     * @param fromRow source row
     * @param fromCol source column
     * @param toRow   destination row
     * @param toCol   destination column
     * @return the captured Piece, or null if the destination was empty
     */
    public Piece movePiece(int fromRow, int fromCol, int toRow, int toCol) {
        Piece moving  = grid[fromRow][fromCol];
        Piece captured = grid[toRow][toCol];

        // Record move
        Move move = new Move(moving, fromRow, fromCol, toRow, toCol, captured);
        moveHistory.add(move);

        // Update captured lists
        if (captured != null) {
            if (moving.getColor() == PlayerColor.WHITE) {
                capturedByWhite.add(captured);
            } else {
                capturedByBlack.add(captured);
            }
        }

        // Apply move
        grid[toRow][toCol]     = moving;
        grid[fromRow][fromCol] = null;

        // Advance turn
        currentTurn = currentTurn.opposite();

        return captured;
    }

    /**
     * Undoes the most recent move, restoring the board and captured lists.
     * @return the Move that was undone, or null if history is empty
     */
    public Move undoLastMove() {
        if (moveHistory.isEmpty()) return null;

        Move last = moveHistory.remove(moveHistory.size() - 1);

        // Reverse the move
        grid[last.getFromRow()][last.getFromCol()] = last.getPiece();
        grid[last.getToRow()][last.getToCol()]     = last.getCapturedPiece();

        // Remove from captured list if a capture was undone
        if (last.getCapturedPiece() != null) {
            if (last.getPiece().getColor() == PlayerColor.WHITE) {
                capturedByWhite.remove(capturedByWhite.size() - 1);
            } else {
                capturedByBlack.remove(capturedByBlack.size() - 1);
            }
        }

        // Revert turn
        currentTurn = currentTurn.opposite();

        return last;
    }

    /**
     * Resets the board to the initial game state.
     */
    public void reset() {
        grid = new Piece[8][8];
        moveHistory.clear();
        capturedByWhite.clear();
        capturedByBlack.clear();
        currentTurn = PlayerColor.WHITE;
        initPieces();
    }

    /** @return whose turn it currently is */
    public PlayerColor getCurrentTurn() { return currentTurn; }

    /** @return an unmodifiable view of the move history */
    public List<Move> getMoveHistory() { return moveHistory; }

    /** @return pieces captured by white */
    public List<Piece> getCapturedByWhite() { return capturedByWhite; }

    /** @return pieces captured by black */
    public List<Piece> getCapturedByBlack() { return capturedByBlack; }

    /** @return the raw 8x8 grid (for serialization purposes) */
    public Piece[][] getGrid() { return grid; }
}
