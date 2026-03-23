package chess.utils;

import chess.position.Position;
import chess.pieces.Piece;

/**
 * Utility class containing helper methods for parsing and validating chess input.
 */
public class Utils {

    /**
     * Validates that a move string is in the correct format.
     * Accepted formats:
     * <ul>
     *   <li>"E2 E4" — standard move</li>
     *   <li>"E7 E8=Q" — pawn promotion</li>
     *   <li>"O-O" — kingside castling</li>
     *   <li>"O-O-O" — queenside castling</li>
     * </ul>
     *
     * @param input the raw input string from the player
     * @return true if the format is valid
     */
    public static boolean isValidMoveFormat(String input) {
        if (input == null) return false;
        String trimmed = input.trim().toUpperCase();

        // Castling
        if (trimmed.equals("O-O") || trimmed.equals("O-O-O")) return true;

        // Standard move or promotion: "XX YY" or "XX YY=P"
        String[] parts = trimmed.split("\\s+");
        if (parts.length != 2) return false;

        if (!isValidSquare(parts[0])) return false;

        // Destination may include promotion suffix like "E8=Q"
        String dest = parts[1];
        if (dest.contains("=")) {
            String[] destParts = dest.split("=");
            if (destParts.length != 2) return false;
            if (!isValidSquare(destParts[0])) return false;
            if (!"QRBN".contains(destParts[1]) || destParts[1].length() != 1) return false;
        } else {
            if (!isValidSquare(dest)) return false;
        }

        return true;
    }

    /**
     * Returns whether a string represents a valid board square (e.g., "E2", "A8").
     *
     * @param square the square string to validate
     * @return true if the square is valid
     */
    public static boolean isValidSquare(String square) {
        if (square == null || square.length() != 2) return false;
        char file = square.charAt(0);
        char rank = square.charAt(1);
        return file >= 'A' && file <= 'H' && rank >= '1' && rank <= '8';
    }

    /**
     * Parses the source position from a move string.
     *
     * @param input the move string (e.g., "E2 E4")
     * @return the source Position, or null if parsing fails
     */
    public static Position parseFrom(String input) {
        if (input == null) return null;
        String[] parts = input.trim().toUpperCase().split("\\s+");
        if (parts.length < 2) return null;
        return Position.fromNotation(parts[0]);
    }

    /**
     * Parses the destination position from a move string, stripping any promotion suffix.
     *
     * @param input the move string (e.g., "E7 E8=Q")
     * @return the destination Position, or null if parsing fails
     */
    public static Position parseTo(String input) {
        if (input == null) return null;
        String[] parts = input.trim().toUpperCase().split("\\s+");
        if (parts.length < 2) return null;
        String dest = parts[1].split("=")[0];
        return Position.fromNotation(dest);
    }

    /**
     * Parses the promotion piece type from a move string (e.g., "E7 E8=Q" returns 'Q').
     *
     * @param input the move string
     * @return the promotion character ('Q', 'R', 'B', 'N'), or null if not a promotion move
     */
    public static Character parsePromotion(String input) {
        if (input == null) return null;
        String[] parts = input.trim().toUpperCase().split("\\s+");
        if (parts.length < 2) return null;
        if (parts[1].contains("=")) {
            String[] destParts = parts[1].split("=");
            if (destParts.length == 2 && destParts[1].length() == 1) {
                return destParts[1].charAt(0);
            }
        }
        return null;
    }

    /**
     * Determines if the input string represents a kingside castling move.
     *
     * @param input the player's input
     * @return true if the input is "O-O" (but not "O-O-O")
     */
    public static boolean isKingsideCastle(String input) {
        if (input == null) return false;
        return input.trim().equalsIgnoreCase("O-O");
    }

    /**
     * Determines if the input string represents a queenside castling move.
     *
     * @param input the player's input
     * @return true if the input is "O-O-O"
     */
    public static boolean isQueensideCastle(String input) {
        if (input == null) return false;
        return input.trim().equalsIgnoreCase("O-O-O");
    }

    /**
     * Returns a simple label for a piece color.
     *
     * @param color the color to label
     * @return "White" or "Black"
     */
    public static String colorName(Piece.Color color) {
        return (color == Piece.Color.WHITE) ? "White" : "Black";
    }
}
