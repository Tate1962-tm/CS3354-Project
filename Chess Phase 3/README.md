# Chess Game - Phase 3

## Overview
Phase 3 integrates the backend chess rule engine with the Phase 2 GUI to create a fully functional chess game enforcing all standard chess rules.

## How to Run
```java
javac *.java
java ChessGame
```

Requires Java 11 or later. No external libraries needed.

## Features
- Full chess rule enforcement (legal moves, captures, check, checkmate, stalemate)
- Check detection - king square highlighted red when in check
- Checkmate and stalemate detection with game-over dialog
- Castling (kingside and queenside) with full rights tracking
- En passant capture
- Pawn promotion dialog
- Legal move hints - green dots on valid destination squares
- Full undo (Ctrl+Z) restoring all game state
- Save and load game to plain-text file
- Board customisation - 4 colour themes, 3 square sizes
- Drag-and-drop and click-to-move input
- Move history panel with captured pieces display
