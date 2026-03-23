package chess.game;

import chess.board.Board;
import chess.pieces.Piece;
import chess.player.Player;
import chess.utils.Utils;

import java.util.Scanner;

/**
 * Manages the overall flow of a chess game between two players.
 * Controls turn alternation, check/checkmate/stalemate detection, and game termination.
 */
public class Game {

    /** The chess board for this game. */
    private Board board;

    /** The white player. */
    private Player whitePlayer;

    /** The black player. */
    private Player blackPlayer;

    /** The color whose turn it currently is. */
    private Piece.Color currentTurn;

    /** Scanner used to read player input from the console. */
    private Scanner scanner;

    /** Whether the game is still ongoing. */
    private boolean running;

    /**
     * Constructs a new Game with two players and initializes the board.
     */
    public Game() {
        board = new Board();
        whitePlayer = new Player(Piece.Color.WHITE);
        blackPlayer = new Player(Piece.Color.BLACK);
        currentTurn = Piece.Color.WHITE;
        scanner = new Scanner(System.in);
        running = false;
    }

    /**
     * Initializes and starts the game.
     * Resets the board and sets White as the first player.
     */
    public void start() {
        board = new Board();
        currentTurn = Piece.Color.WHITE;
        running = true;
        printWelcome();
        play();
    }

    /**
     * Main game loop. Alternates turns between players, displays the board,
     * processes moves, and checks for game-ending conditions after each move.
     */
    public void play() {
        while (running) {
            board.display();

            // Show check warning
            if (board.isCheck(currentTurn)) {
                System.out.println("  *** " + Utils.colorName(currentTurn).toUpperCase() + " IS IN CHECK! ***");
            }

            // Get and execute move
            Player currentPlayer = (currentTurn == Piece.Color.WHITE) ? whitePlayer : blackPlayer;
            currentPlayer.makeMove(board, scanner);

            // Switch turns
            Piece.Color opponent = (currentTurn == Piece.Color.WHITE) ? Piece.Color.BLACK : Piece.Color.WHITE;

            // Check for game-ending conditions for the opponent
            if (board.isCheckmate(opponent)) {
                board.display();
                System.out.println("  *** CHECKMATE! " + Utils.colorName(currentTurn).toUpperCase() + " WINS! ***");
                end(currentTurn);
                return;
            }

            if (board.isStalemate(opponent)) {
                board.display();
                System.out.println("  *** STALEMATE! The game is a draw. ***");
                end(null);
                return;
            }

            currentTurn = opponent;
        }
    }

    /**
     * Ends the game and either declares a winner or announces a draw.
     *
     * @param winner the color of the winning player, or null for a draw
     */
    public void end(Piece.Color winner) {
        running = false;
        if (winner != null) {
            System.out.println("  Game over. " + Utils.colorName(winner) + " wins!");
        } else {
            System.out.println("  Game over. It's a draw!");
        }
        System.out.println();
        askPlayAgain();
    }

    /**
     * Prompts the players to start a new game or exit.
     */
    private void askPlayAgain() {
        System.out.print("Play again? (yes/no): ");
        String input = scanner.nextLine().trim().toLowerCase();
        if (input.equals("yes") || input.equals("y")) {
            start();
        } else {
            System.out.println("Thanks for playing! Goodbye.");
        }
    }

    /**
     * Prints a welcome message and basic usage instructions to the console.
     */
    private void printWelcome() {
        System.out.println("========================================");
        System.out.println("        WELCOME TO JAVA CHESS");
        System.out.println("========================================");
        System.out.println("  Move format: E2 E4  (from square, to square)");
        System.out.println("  Castling:    O-O   (kingside)");
        System.out.println("               O-O-O (queenside)");
        System.out.println("  Promotion:   E7 E8=Q  (promote to Q/R/B/N)");
        System.out.println("========================================");
        System.out.println();
    }
}
