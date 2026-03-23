package chess.board;

import chess.pieces.*;
import chess.position.Position;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the chess board and manages the state of all pieces.
 * Handles move execution, check/checkmate detection, and board display.
 */
public class Board {

    /** The 8x8 grid of chess pieces. A null value indicates an empty square. */
    private Piece[][] squares;

    /** List of pieces that have been captured during the game. */
    private List<Piece> capturedPieces;

    /**
     * Constructs a new Board and initializes pieces to their starting positions.
     */
    public Board() {
        squares = new Piece[8][8];
        capturedPieces = new ArrayList<>();
        initialize();
    }

    /**
     * Sets up all pieces in their standard starting positions.
     * White pieces occupy rows 0–1 (ranks 1–2); black pieces occupy rows 6–7 (ranks 7–8).
     */
    public void initialize() {
        // White pieces (row 0 = rank 1, row 1 = rank 2)
        squares[0][0] = new Rook(Piece.Color.WHITE, new Position(0, 0));
        squares[0][1] = new Knight(Piece.Color.WHITE, new Position(0, 1));
        squares[0][2] = new Bishop(Piece.Color.WHITE, new Position(0, 2));
        squares[0][3] = new Queen(Piece.Color.WHITE, new Position(0, 3));
        squares[0][4] = new King(Piece.Color.WHITE, new Position(0, 4));
        squares[0][5] = new Bishop(Piece.Color.WHITE, new Position(0, 5));
        squares[0][6] = new Knight(Piece.Color.WHITE, new Position(0, 6));
        squares[0][7] = new Rook(Piece.Color.WHITE, new Position(0, 7));
        for (int c = 0; c < 8; c++) {
            squares[1][c] = new Pawn(Piece.Color.WHITE, new Position(1, c));
        }

        // Black pieces (row 7 = rank 8, row 6 = rank 7)
        squares[7][0] = new Rook(Piece.Color.BLACK, new Position(7, 0));
        squares[7][1] = new Knight(Piece.Color.BLACK, new Position(7, 1));
        squares[7][2] = new Bishop(Piece.Color.BLACK, new Position(7, 2));
        squares[7][3] = new Queen(Piece.Color.BLACK, new Position(7, 3));
        squares[7][4] = new King(Piece.Color.BLACK, new Position(7, 4));
        squares[7][5] = new Bishop(Piece.Color.BLACK, new Position(7, 5));
        squares[7][6] = new Knight(Piece.Color.BLACK, new Position(7, 6));
        squares[7][7] = new Rook(Piece.Color.BLACK, new Position(7, 7));
        for (int c = 0; c < 8; c++) {
            squares[6][c] = new Pawn(Piece.Color.BLACK, new Position(6, c));
        }
    }

    /**
     * Returns the piece at the given position, or null if the square is empty.
     *
     * @param position the position to query
     * @return the piece at that position, or null
     */
    public Piece getPiece(Position position) {
        return squares[position.getRow()][position.getCol()];
    }

    /**
     * Moves a piece from one position to another on the board.
     * Handles captures, pawn promotion (auto-promotes to Queen),
     * castling, and marks pieces as having moved.
     *
     * @param from the starting position
     * @param to   the destination position
     * @return true if the move was executed successfully, false otherwise
     */
    public boolean movePiece(Position from, Position to) {
        Piece piece = getPiece(from);
        if (piece == null) return false;

        Piece target = getPiece(to);
        if (target != null) {
            capturedPieces.add(target);
        }

        // Execute move
        squares[to.getRow()][to.getCol()] = piece;
        squares[from.getRow()][from.getCol()] = null;
        piece.setPosition(to);

        // Track movement for pawns, rooks, and king (used for castling/promotion)
        if (piece instanceof Pawn) {
            ((Pawn) piece).setHasMoved();
            // Pawn promotion: auto-promote to Queen
            int promotionRow = (piece.getColor() == Piece.Color.WHITE) ? 7 : 0;
            if (to.getRow() == promotionRow) {
                squares[to.getRow()][to.getCol()] = new Queen(piece.getColor(), to);
            }
        } else if (piece instanceof Rook) {
            ((Rook) piece).setHasMoved();
        } else if (piece instanceof King) {
            ((King) piece).setHasMoved();
            // Handle castling: if king moves two squares, also move the rook
            int colDiff = to.getCol() - from.getCol();
            if (Math.abs(colDiff) == 2) {
                int row = from.getRow();
                if (colDiff == 2) {
                    // Kingside
                    Piece rook = squares[row][7];
                    squares[row][5] = rook;
                    squares[row][7] = null;
                    if (rook != null) rook.setPosition(new Position(row, 5));
                } else {
                    // Queenside
                    Piece rook = squares[row][0];
                    squares[row][3] = rook;
                    squares[row][0] = null;
                    if (rook != null) rook.setPosition(new Position(row, 3));
                }
            }
        }

        return true;
    }

    /**
     * Returns all legal moves for a given color that do not leave their own king in check.
     *
     * @param color the color whose legal moves to compute
     * @return list of {from, to} position pairs as two-element arrays
     */
    public List<Position[]> getLegalMoves(Piece.Color color) {
        List<Position[]> legalMoves = new ArrayList<>();
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece piece = squares[r][c];
                if (piece != null && piece.getColor() == color) {
                    for (Position dest : piece.possibleMoves(squares)) {
                        if (!wouldLeaveInCheck(new Position(r, c), dest, color)) {
                            legalMoves.add(new Position[]{new Position(r, c), dest});
                        }
                    }
                }
            }
        }
        // Add castling moves
        legalMoves.addAll(getCastlingMoves(color));
        return legalMoves;
    }

    /**
     * Checks whether a given color is currently in check.
     *
     * @param color the color to check
     * @return true if the specified color's king is in check
     */
    public boolean isCheck(Piece.Color color) {
        Position kingPos = findKing(color);
        if (kingPos == null) return false;
        Piece.Color opponentColor = (color == Piece.Color.WHITE) ? Piece.Color.BLACK : Piece.Color.WHITE;
        return isAttackedBy(kingPos, opponentColor);
    }

    /**
     * Checks whether a given color is in checkmate (in check with no legal moves).
     *
     * @param color the color to check
     * @return true if the specified color is in checkmate
     */
    public boolean isCheckmate(Piece.Color color) {
        return isCheck(color) && getLegalMoves(color).isEmpty();
    }

    /**
     * Checks whether a given color is in stalemate (not in check but no legal moves).
     *
     * @param color the color to check
     * @return true if the specified color is in stalemate
     */
    public boolean isStalemate(Piece.Color color) {
        return !isCheck(color) && getLegalMoves(color).isEmpty();
    }

    /**
     * Determines whether moving a piece from one position to another would leave
     * the moving player's king in check (making the move illegal).
     *
     * @param from  the starting position
     * @param to    the destination position
     * @param color the color of the moving player
     * @return true if the move would result in the player's king being in check
     */
    public boolean wouldLeaveInCheck(Position from, Position to, Piece.Color color) {
        // Simulate the move on a copy of the board
        Piece[][] copy = copyBoard();
        Piece piece = copy[from.getRow()][from.getCol()];
        copy[to.getRow()][to.getCol()] = piece;
        copy[from.getRow()][from.getCol()] = null;
        if (piece != null) piece.setPosition(to);

        // Find king's position on simulated board
        Position kingPos = null;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = copy[r][c];
                if (p instanceof King && p.getColor() == color) {
                    kingPos = new Position(r, c);
                }
            }
        }
        if (kingPos == null) return false;

        // Check if any opponent piece can attack the king
        Piece.Color opponent = (color == Piece.Color.WHITE) ? Piece.Color.BLACK : Piece.Color.WHITE;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = copy[r][c];
                if (p != null && p.getColor() == opponent) {
                    for (Position move : p.possibleMoves(copy)) {
                        if (move.equals(kingPos)) {
                            // Restore moved piece position
                            if (piece != null) piece.setPosition(from);
                            return true;
                        }
                    }
                }
            }
        }
        if (piece != null) piece.setPosition(from);
        return false;
    }

    /**
     * Checks whether a castling move is valid for the given color,
     * and returns legal castling moves as a list.
     *
     * @param color the color to check castling for
     * @return list of legal castling destination positions paired with king start
     */
    private List<Position[]> getCastlingMoves(Piece.Color color) {
        List<Position[]> castlingMoves = new ArrayList<>();
        int row = (color == Piece.Color.WHITE) ? 0 : 7;
        Piece king = squares[row][4];
        if (!(king instanceof King) || ((King) king).hasMoved()) return castlingMoves;
        if (isCheck(color)) return castlingMoves;

        // Kingside castling
        Piece kingsideRook = squares[row][7];
        if (kingsideRook instanceof Rook && !((Rook) kingsideRook).hasMoved()) {
            if (squares[row][5] == null && squares[row][6] == null) {
                if (!wouldLeaveInCheck(new Position(row, 4), new Position(row, 5), color)
                        && !wouldLeaveInCheck(new Position(row, 4), new Position(row, 6), color)) {
                    castlingMoves.add(new Position[]{new Position(row, 4), new Position(row, 6)});
                }
            }
        }

        // Queenside castling
        Piece queensideRook = squares[row][0];
        if (queensideRook instanceof Rook && !((Rook) queensideRook).hasMoved()) {
            if (squares[row][1] == null && squares[row][2] == null && squares[row][3] == null) {
                if (!wouldLeaveInCheck(new Position(row, 4), new Position(row, 3), color)
                        && !wouldLeaveInCheck(new Position(row, 4), new Position(row, 2), color)) {
                    castlingMoves.add(new Position[]{new Position(row, 4), new Position(row, 2)});
                }
            }
        }

        return castlingMoves;
    }

    /**
     * Determines whether a given position is under attack by any piece of the specified color.
     *
     * @param pos            the position to check
     * @param attackingColor the color of potential attackers
     * @return true if the position is attacked
     */
    private boolean isAttackedBy(Position pos, Piece.Color attackingColor) {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = squares[r][c];
                if (p != null && p.getColor() == attackingColor) {
                    for (Position move : p.possibleMoves(squares)) {
                        if (move.equals(pos)) return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Locates the king of the given color on the board.
     *
     * @param color the color of the king to find
     * @return the position of the king, or null if not found
     */
    private Position findKing(Piece.Color color) {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = squares[r][c];
                if (p instanceof King && p.getColor() == color) {
                    return new Position(r, c);
                }
            }
        }
        return null;
    }

    /**
     * Creates a shallow copy of the board grid for simulation purposes.
     *
     * @return a copy of the squares array
     */
    private Piece[][] copyBoard() {
        Piece[][] copy = new Piece[8][8];
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                copy[r][c] = squares[r][c];
            }
        }
        return copy;
    }

    /**
     * Returns the list of all captured pieces so far.
     *
     * @return the list of captured pieces
     */
    public List<Piece> getCapturedPieces() {
        return capturedPieces;
    }

    /**
     * Returns the raw 8x8 board array.
     *
     * @return the squares array
     */
    public Piece[][] getSquares() {
        return squares;
    }

    /**
     * Prints the current state of the chessboard to the console.
     * Displays file labels (A–H) across the top and rank numbers (8–1) down the left side.
     * Empty squares are shown as "##" on light squares and "  " on dark squares.
     */
    public void display() {
        System.out.println();
        System.out.println("     A    B    C    D    E    F    G    H");
        System.out.println("   +----+----+----+----+----+----+----+----+");
        for (int row = 7; row >= 0; row--) {
            System.out.print(" " + (row + 1) + " |");
            for (int col = 0; col < 8; col++) {
                Piece piece = squares[row][col];
                if (piece != null) {
                    System.out.printf(" %-2s |", piece.getSymbol());
                } else {
                    // Checkerboard pattern: light squares = ##
                    boolean light = (row + col) % 2 == 1;
                    System.out.print(light ? " ## |" : "    |");
                }
            }
            System.out.println(" " + (row + 1));
            System.out.println("   +----+----+----+----+----+----+----+----+");
        }
        System.out.println("     A    B    C    D    E    F    G    H");
        System.out.println();
    }
}
