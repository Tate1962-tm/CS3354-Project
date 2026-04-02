package chess;

import java.io.*;
import java.util.*;

/**
 * Holds the complete state of a chess game including the board,
 * captured pieces, move history, and undo stack.
 * Supports save/load via serialization.
 */
public class GameState implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 8×8 board; null means empty square. */
    private Piece[][] board;

    /** Pieces captured by WHITE (captured from BLACK). */
    private List<Piece> capturedByWhite;

    /** Pieces captured by BLACK (captured from WHITE). */
    private List<Piece> capturedByBlack;

    /** Human-readable move history strings. */
    private List<String> moveHistory;

    /**
     * Snapshot of a single move for undo purposes.
     */
    private record MoveSnapshot(
            Piece[][] boardCopy,
            List<Piece> capturedByWhiteCopy,
            List<Piece> capturedByBlackCopy,
            List<String> moveHistoryCopy
    ) implements Serializable { }

    /** Stack of previous states for undo. */
    private Deque<MoveSnapshot> undoStack;

    /** Whose turn it is. */
    private Piece.Color currentTurn;

    /**
     * Creates a new game state with pieces in starting positions.
     */
    public GameState() {
        reset();
    }

    /**
     * Resets the board to the initial chess setup.
     */
    public void reset() {
        board = new Piece[8][8];
        capturedByWhite = new ArrayList<>();
        capturedByBlack = new ArrayList<>();
        moveHistory = new ArrayList<>();
        undoStack = new ArrayDeque<>();
        currentTurn = Piece.Color.WHITE;
        placePieces();
    }

    /** Places all pieces at their standard starting positions. */
    private void placePieces() {
        // Black back rank (row 0)
        Piece.Type[] backRank = {
            Piece.Type.ROOK, Piece.Type.KNIGHT, Piece.Type.BISHOP,
            Piece.Type.QUEEN, Piece.Type.KING,
            Piece.Type.BISHOP, Piece.Type.KNIGHT, Piece.Type.ROOK
        };
        for (int c = 0; c < 8; c++) {
            board[0][c] = new Piece(Piece.Color.BLACK, backRank[c]);
            board[1][c] = new Piece(Piece.Color.BLACK, Piece.Type.PAWN);
            board[6][c] = new Piece(Piece.Color.WHITE, Piece.Type.PAWN);
            board[7][c] = new Piece(Piece.Color.WHITE, backRank[c]);
        }
    }

    /**
     * Returns the piece at the given board coordinates.
     *
     * @param row 0–7 (top = 0)
     * @param col 0–7
     * @return the Piece at that square, or null if empty
     */
    public Piece getPiece(int row, int col) {
        return board[row][col];
    }

    /**
     * Attempts to move a piece from (fromRow, fromCol) to (toRow, toCol).
     * Saves a snapshot for undo, records the move, and handles captures.
     *
     * @param fromRow source row
     * @param fromCol source column
     * @param toRow   destination row
     * @param toCol   destination column
     * @return the captured Piece, or null if the destination was empty
     * @throws IllegalArgumentException if the source square is empty
     */
    public Piece move(int fromRow, int fromCol, int toRow, int toCol) {
        Piece mover = board[fromRow][fromCol];
        if (mover == null) throw new IllegalArgumentException("No piece at source");

        // Save snapshot for undo
        undoStack.push(snapshot());

        Piece captured = board[toRow][toCol];
        board[toRow][toCol] = mover;
        board[fromRow][fromCol] = null;

        if (captured != null) {
            if (mover.getColor() == Piece.Color.WHITE) capturedByWhite.add(captured);
            else capturedByBlack.add(captured);
        }

        // Record move in algebraic-ish notation
        String notation = mover.getSymbol() + " "
                + colLetter(fromCol) + (8 - fromRow)
                + (captured != null ? "x" : "-")
                + colLetter(toCol) + (8 - toRow);
        moveHistory.add(notation);

        // Flip turn
        currentTurn = (currentTurn == Piece.Color.WHITE) ? Piece.Color.BLACK : Piece.Color.WHITE;

        return captured;
    }

    /**
     * Undoes the last move, restoring the previous game state.
     *
     * @return true if undo succeeded, false if no moves to undo
     */
    public boolean undo() {
        if (undoStack.isEmpty()) return false;
        MoveSnapshot snap = undoStack.pop();
        board = deepCopyBoard(snap.boardCopy());
        capturedByWhite = new ArrayList<>(snap.capturedByWhiteCopy());
        capturedByBlack = new ArrayList<>(snap.capturedByBlackCopy());
        moveHistory = new ArrayList<>(snap.moveHistoryCopy());
        // flip turn back
        currentTurn = (currentTurn == Piece.Color.WHITE) ? Piece.Color.BLACK : Piece.Color.WHITE;
        return true;
    }

    /** @return true if there are moves available to undo */
    public boolean canUndo() { return !undoStack.isEmpty(); }

    /** @return an unmodifiable view of the move history */
    public List<String> getMoveHistory() { return Collections.unmodifiableList(moveHistory); }

    /** @return pieces captured by the white player */
    public List<Piece> getCapturedByWhite() { return Collections.unmodifiableList(capturedByWhite); }

    /** @return pieces captured by the black player */
    public List<Piece> getCapturedByBlack() { return Collections.unmodifiableList(capturedByBlack); }

    /** @return whose turn it currently is */
    public Piece.Color getCurrentTurn() { return currentTurn; }

    // -------------------------------------------------------------------------
    // Save / Load
    // -------------------------------------------------------------------------

    /**
     * Saves the current game state to a file via Java serialization.
     *
     * @param file destination file
     * @throws IOException if writing fails
     */
    public void saveToFile(File file) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(this);
        }
    }

    /**
     * Loads a game state from a serialized file.
     *
     * @param file source file
     * @return the deserialized GameState
     * @throws IOException            if reading fails
     * @throws ClassNotFoundException if the class is not found
     */
    public static GameState loadFromFile(File file) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (GameState) ois.readObject();
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private MoveSnapshot snapshot() {
        return new MoveSnapshot(
                deepCopyBoard(board),
                new ArrayList<>(capturedByWhite),
                new ArrayList<>(capturedByBlack),
                new ArrayList<>(moveHistory)
        );
    }

    private Piece[][] deepCopyBoard(Piece[][] src) {
        Piece[][] copy = new Piece[8][8];
        for (int r = 0; r < 8; r++)
            System.arraycopy(src[r], 0, copy[r], 0, 8);
        return copy;
    }

    private String colLetter(int col) {
        return String.valueOf((char) ('a' + col));
    }
}
