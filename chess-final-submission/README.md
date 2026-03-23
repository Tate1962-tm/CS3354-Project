# Java Console Chess

A fully functional two-player chess game playable in the terminal, built in Java using object-oriented design principles.

---

## Prerequisites

| Requirement | Version |
|---|---|
| Java JDK | 11 or higher |
| Operating System | Windows / macOS / Linux |

Verify your Java installation:
```bash
java -version
javac -version
```

---

## Quick Start

### 1. Clone or unzip the project
```bash
unzip chess-project.zip
cd chess
```

### 2. Compile
```bash
javac -d out/ -sourcepath src/ src/Main.java
```

### 3. Run
```bash
java -cp out Main
```

### Alternative: Run the pre-built JAR
```bash
java -jar chess.jar
```

---

## How to Play

The board is displayed after every move. White always goes first.

### Move Format

| Action | Format | Example |
|---|---|---|
| Standard move | `FROM TO` | `E2 E4` |
| Capture | Same as move | `D4 E5` |
| Kingside castle | `O-O` | `O-O` |
| Queenside castle | `O-O-O` | `O-O-O` |
| Pawn promotion | `FROM TO=PIECE` | `E7 E8=Q` |

- Input is **case-insensitive**: `e2 e4` and `E2 E4` both work
- Extra spaces are tolerated: `E2  E4` works fine
- Promotion pieces: `Q` (Queen), `R` (Rook), `B` (Bishop), `N` (Knight)
- If no promotion piece is specified, the pawn auto-promotes to a **Queen**

### Board Legend

```
wp = White Pawn      bp = Black Pawn
wR = White Rook      bR = Black Rook
wN = White Knight    bN = Black Knight
wB = White Bishop    bB = Black Bishop
wQ = White Queen     bQ = Black Queen
wK = White King      bK = Black King
## = Empty light square
   = Empty dark square
```

---

## Example Game Session

```
========================================
        WELCOME TO JAVA CHESS
========================================
  Move format: E2 E4  (from square, to square)
  Castling:    O-O   (kingside)
               O-O-O (queenside)
  Promotion:   E7 E8=Q  (promote to Q/R/B/N)
========================================

     A    B    C    D    E    F    G    H
   +----+----+----+----+----+----+----+----+
 8 | bR | bN | bB | bQ | bK | bB | bN | bR | 8
   +----+----+----+----+----+----+----+----+
 7 | bp | bp | bp | bp | bp | bp | bp | bp | 7
   +----+----+----+----+----+----+----+----+
 6 | ## |    | ## |    | ## |    | ## |    | 6
   +----+----+----+----+----+----+----+----+
 5 |    | ## |    | ## |    | ## |    | ## | 5
   +----+----+----+----+----+----+----+----+
 4 | ## |    | ## |    | ## |    | ## |    | 4
   +----+----+----+----+----+----+----+----+
 3 |    | ## |    | ## |    | ## |    | ## | 3
   +----+----+----+----+----+----+----+----+
 2 | wp | wp | wp | wp | wp | wp | wp | wp | 2
   +----+----+----+----+----+----+----+----+
 1 | wR | wN | wB | wQ | wK | wB | wN | wR | 1
   +----+----+----+----+----+----+----+----+
     A    B    C    D    E    F    G    H

White's move: E2 E4
...
Black's move: e7 e5
...
White's move: D1 H5
...
Black's move: B8 C6
...
White's move: F1 C4
...
Black's move: G8 F6
...
White's move: H5 F7

  *** CHECKMATE! WHITE WINS! ***
  Game over. White wins!

Play again? (yes/no):
```

### Example Invalid Input Handling
```
White's move: hello
  Invalid format. Use 'E2 E4', 'O-O', 'O-O-O', or 'E7 E8=Q'.

White's move: E9 E4
  Invalid format. Use 'E2 E4', 'O-O', 'O-O-O', or 'E7 E8=Q'.

White's move: E1 F1
  Illegal move. Try again.

Black's move: E2 E4
  That is not your piece. Try again.
```

---

## Project Structure

```
chess/
├── src/
│   ├── Main.java                        # Entry point
│   └── chess/
│       ├── board/
│       │   └── Board.java               # 8x8 grid, move execution, check detection
│       ├── game/
│       │   └── Game.java                # Game loop, turn management, end conditions
│       ├── pieces/
│       │   ├── Piece.java               # Abstract base class
│       │   ├── Pawn.java
│       │   ├── Rook.java
│       │   ├── Knight.java
│       │   ├── Bishop.java
│       │   ├── Queen.java
│       │   └── King.java
│       ├── player/
│       │   └── Player.java              # Input handling, move dispatch
│       ├── position/
│       │   └── Position.java            # Algebraic notation, bounds validation
│       └── utils/
│           └── Utils.java               # Parsing helpers, format validation
├── out/                                 # Compiled .class files (after javac)
├── chess.jar                            # Pre-built runnable JAR
└── README.md
```

---

## Features

### Implemented
- ✅ Full 8×8 chessboard with correct initial piece setup
- ✅ CLI board rendering with rank (1–8) and file (A–H) labels
- ✅ Checkerboard `##` pattern for empty light squares
- ✅ All six piece types with correct movement rules
- ✅ Case-insensitive, whitespace-tolerant input parsing
- ✅ Turn enforcement (White then Black, alternating)
- ✅ Cannot move opponent's pieces
- ✅ Cannot capture your own pieces
- ✅ Out-of-bounds move rejection
- ✅ Moves leaving own king in check are rejected
- ✅ Check detection with in-game warning message
- ✅ Checkmate detection with winner announcement
- ✅ Stalemate detection with draw announcement
- ✅ Kingside castling (`O-O`)
- ✅ Queenside castling (`O-O-O`)
- ✅ Pawn promotion with piece selection (`=Q`, `=R`, `=B`, `=N`)
- ✅ Auto-promotion to Queen if no piece specified
- ✅ Play-again prompt after game ends
- ✅ Full Javadoc on all classes, methods, and fields

### Not Implemented
- ❌ En passant capture
- ❌ Draw by repetition or 50-move rule
- ❌ Insufficient material draw detection
- ❌ Move history / undo
- ❌ Graphical UI

---

## Running Javadoc

```bash
javadoc -d docs/ -sourcepath src/ -subpackages chess
```
Then open `docs/index.html` in a browser.

---

## AI Usage Log

All AI usage is documented in each Git commit message following the format:

```
[Prompt] what was asked
[Adopt]  what was used from the response
[Change] what was modified and why
[Test]   how it was verified
```

See `git log` for the full record.
