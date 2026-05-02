# ♟ Chess Game — Phase 3 (Integrated GUI)

A fully integrated, rule-enforcing GUI chess game built in Java Swing.  
Phase 3 connects the backend logic from Phase 1 with the GUI framework from Phase 2 into one complete, playable application.

## Preview

The board highlights legal moves as green dots when you select a piece.  
The king's square turns red when in check.  
Move history and captured pieces are displayed in the panel on the right.


## Features

### Core Chess Rules
- ✅ Legal move enforcement for all 6 piece types
- ✅ Check detection — king highlighted red, status bar shows CHECK!
- ✅ Checkmate and stalemate detection with game-over dialog
- ✅ En passant capture
- ✅ Castling (kingside and queenside) with full rights tracking
- ✅ Pawn promotion — dialog lets player choose Queen, Rook, Bishop, or Knight

### GUI
- ✅ Click-to-move and drag-and-drop piece movement
- ✅ Legal move hints (green dots on valid squares, rings on capturable pieces)
- ✅ Move history panel with algebraic notation
- ✅ Captured pieces display
- ✅ Undo (Ctrl+Z) — restores board, castling rights, en passant fully
- ✅ Save / Load game (text file format)
- ✅ Customizable board colors via Settings dialog

### Extra Credit B — AI Chess Opponent
- ✅ Computer opponent using Minimax algorithm with Alpha-Beta pruning
- ✅ Three difficulty levels: Easy (depth 1), Medium (depth 3), Hard (depth 4)
- ✅ Material evaluation with piece-square positional tables
- ✅ Enable via Game → Play vs Computer
- ✅ Change strength via AI Difficulty menu

## Project Structure

├── ChessGame.java         # Entry point — launches the GUI
├── ChessFrame.java        # Main window, menu bar, game controls, AI wiring
├── ChessBoardPanel.java   # Board rendering, mouse input, move hints
├── ChessAI.java           # AI opponent — Minimax with Alpha-Beta pruning
├── GameState.java         # Game model — board state, move execution, undo
├── MoveValidator.java     # Rule engine — legal move generation, check detection
├── HistoryPanel.java      # Move history and captured pieces side panel
├── SettingsDialog.java    # Board color customization dialog
├── ChessPiece.java        # Piece data model with Unicode symbol support
├── PieceType.java         # Enum: PAWN, ROOK, KNIGHT, BISHOP, QUEEN, KING
└── PlayerColor.java       # Enum: WHITE, BLACK

---

## How to Run

**Requirements:** Java 11 or later

```bash
# 1. Compile all source files
javac *.java

# 2. Run the application
java ChessGame
```

## How to Play

1. **Select a piece** — click on any of your pieces. Legal destinations appear as green dots.
2. **Move** — click a highlighted square, or drag and drop the piece directly.
3. **Undo** — press Ctrl+Z or use Game → Undo Move.
4. **New game** — Game → New Game (or Ctrl+N).
5. **Save / Load** — Game → Save Game / Load Game to save progress as a text file.
6. **Settings** — View → Settings to change board colors.
7. **Play vs Computer** — Game → Play vs Computer to enable the AI opponent.
8. **AI Difficulty** — use the AI Difficulty menu to choose Easy, Medium, or Hard.

---

## Architecture

The project follows a clean layered design:

| Layer | Classes | Responsibility |
|-------|---------|---------------|
| **GUI** | `ChessFrame`, `ChessBoardPanel`, `HistoryPanel`, `SettingsDialog` | Rendering, user input, display |
| **Backend** | `GameState`, `MoveRecord` | Board state, move execution, undo, serialization |
| **Rule Engine** | `MoveValidator` | Stateless legal-move generation, check/checkmate detection |
| **AI Engine** | `ChessAI` | Minimax search, position evaluation, difficulty levels |
| **Domain Model** | `ChessPiece`, `PieceType`, `PlayerColor` | Data representation |

---

## AI Usage Log

| Commit | Prompt / Topic | Adopted | Changes Made |
|--------|---------------|---------|--------------|
| Phase 3 integration | Asked Claude to design a stateless `MoveValidator` that computes legal moves without modifying `GameState` | Yes | Created `MoveValidator.java` as a pure utility class |
| Check/checkmate | Asked how to detect check by simulating each move on a copied board | Yes | `moveLeavesKingInCheck()` simulates on `copyBoard()` before accepting any move |
| En passant | Explained en-passant rules and asked for correct target-square tracking | Yes | `enPassantTarget` field in `GameState`, reset every move |
| Castling undo | Asked how to fully restore rook position and castling rights on undo | Yes | `MoveRecord` stores `prevWCK/prevWCQ/prevBCK/prevBCQ` and rook is repositioned in `undoLastMove()` |
| GUI integration | Asked how `ChessBoardPanel` should handle illegal move attempts | Partially | Used legal-move pre-check in `attemptMove()` instead of relying on return value |
| AI opponent | Asked Claude to implement Minimax with Alpha-Beta pruning and piece-square tables | Yes | Created `ChessAI.java` with 3 difficulty levels integrated into `ChessFrame` |


## Phase History

| Phase | Focus | Location |
|-------|-------|----------|
| Phase 1 | Console chess logic | `chess-final-submission/` folder |
| Phase 2 | GUI framework (no rule enforcement) | `Chess Phase 2/` folder |
| Phase 3 | Full integration + AI opponent | `Chess Phase 3/` folder |
