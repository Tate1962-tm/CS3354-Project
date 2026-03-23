package chess.player;

import chess.board.Board;
import chess.pieces.Piece;
import chess.position.Position;
import chess.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Represents a human chess player.
 * Handles input prompting, move parsing, and move execution on the board.
 */
public class Player {

    /** The color assigned to this player (WHITE or BLACK). */
    private Piece.Color color;

    /** The list of pieces this player still has on the board. */
    private List<Piece> availablePieces;

    /**
     * Constructs a Player with the given color.
     *
     * @param color the color of this player
     */
    public Player(Piece.Color color) {
        this.color = color;
        this.availablePieces = new ArrayList<>();
    }

    /**
     * Returns the color of this player.
     *
     * @return the player's color
     */
    public Piece.Color getColor() {
        return color;
    }

    /**
     * Returns the list of pieces currently available to this player (not captured).
     *
     * @return list of active pieces
     */
    public List<Piece> getAvailablePieces() {
        return availablePieces;
    }

    /**
     * Prompts the player to enter a move via the console and attempts to execute it on the board.
     * Validates format and legality, re-prompting on invalid input.
     * Supports standard moves (e.g., "E2 E4"), castling ("O-O", "O-O-O"),
     * and pawn promotion (e.g., "E7 E8=Q").
     *
     * @param board   the current board state
     * @param scanner the Scanner object used to read player input
     * @return true once a valid move has been successfully made
     */
    public boolean makeMove(Board board, Scanner scanner) {
        while (true) {
            System.out.print(Utils.colorName(color) + "'s move: ");
            String input = scanner.nextLine().trim();

            if (!Utils.isValidMoveFormat(input)) {
                System.out.println("  Invalid format. Use 'E2 E4', 'O-O', 'O-O-O', or 'E7 E8=Q'.");
                continue;
            }

            // Handle castling
            if (Utils.isQueensideCastle(input)) {
                return handleCastle(board, false);
            }
            if (Utils.isKingsideCastle(input)) {
                return handleCastle(board, true);
            }

            Position from = Utils.parseFrom(input);
            Position to = Utils.parseTo(input);

            if (from == null || to == null) {
                System.out.println("  Could not parse positions. Try again.");
                continue;
            }

            Piece piece = board.getPiece(from);

            if (piece == null) {
                System.out.println("  No piece at " + from + ". Try again.");
                continue;
            }
            if (piece.getColor() != this.color) {
                System.out.println("  That is not your piece. Try again.");
                continue;
            }

            // Check if destination is in the piece's legal moves
            List<Position[]> legalMoves = board.getLegalMoves(color);
            boolean isLegal = false;
            for (Position[] move : legalMoves) {
                if (move[0].equals(from) && move[1].equals(to)) {
                    isLegal = true;
                    break;
                }
            }

            if (!isLegal) {
                System.out.println("  Illegal move. Try again.");
                continue;
            }

            board.movePiece(from, to);

            // Handle custom promotion piece if specified
            Character promotionChar = Utils.parsePromotion(input);
            if (promotionChar != null) {
                handlePromotion(board, to, promotionChar);
            }

            return true;
        }
    }

    /**
     * Handles a castling move for this player.
     *
     * @param board     the current board
     * @param kingside  true for kingside (O-O), false for queenside (O-O-O)
     * @return true if castling was executed successfully, false otherwise
     */
    private boolean handleCastle(Board board, boolean kingside) {
        int row = (color == Piece.Color.WHITE) ? 0 : 7;
        Position from = new Position(row, 4);
        Position to = new Position(row, kingside ? 6 : 2);

        List<Position[]> legalMoves = board.getLegalMoves(color);
        for (Position[] move : legalMoves) {
            if (move[0].equals(from) && move[1].equals(to)) {
                board.movePiece(from, to);
                System.out.println("  Castled " + (kingside ? "kingside" : "queenside") + "!");
                return true;
            }
        }
        System.out.println("  Castling not available. Try again.");
        return false;
    }

    /**
     * Replaces a promoted pawn with the player's chosen piece.
     *
     * @param board         the current board
     * @param pos           the promotion square
     * @param promotionChar the character representing the desired piece ('Q', 'R', 'B', 'N')
     */
    private void handlePromotion(Board board, Position pos, char promotionChar) {
        Piece promoted;
        switch (promotionChar) {
            case 'R': promoted = new chess.pieces.Rook(color, pos); break;
            case 'B': promoted = new chess.pieces.Bishop(color, pos); break;
            case 'N': promoted = new chess.pieces.Knight(color, pos); break;
            default:  promoted = new chess.pieces.Queen(color, pos); break;
        }
        board.getSquares()[pos.getRow()][pos.getCol()] = promoted;
        System.out.println("  Pawn promoted to " + promoted.getSymbol() + "!");
    }
}
