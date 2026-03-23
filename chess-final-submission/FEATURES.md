# Feature Summary

## Implemented Features

### Core Gameplay
| Feature | Status | Notes |
|---|---|---|
| 8×8 board representation | ✅ Done | `Piece[][]` 2D array in `Board.java` |
| Correct initial piece setup | ✅ Done | All 32 pieces in standard starting positions |
| CLI board rendering | ✅ Done | Rank/file labels, `##` checkerboard, borders |
| Turn alternation (White → Black) | ✅ Done | Enforced in `Game.play()` loop |
| All 6 piece movement rules | ✅ Done | Each piece has its own `possibleMoves()` |

### Input & Validation
| Feature | Status | Notes |
|---|---|---|
| Standard move format (`E2 E4`) | ✅ Done | |
| Case-insensitive input | ✅ Done | `e2 e4` = `E2 E4` |
| Extra whitespace tolerance | ✅ Done | `split("\\s+")` handles multiple spaces |
| Out-of-bounds rejection | ✅ Done | `Position.isValid()` |
| Wrong player piece rejection | ✅ Done | "That is not your piece" |
| Own-piece capture rejection | ✅ Done | Filtered in `getLegalMoves()` |
| Moving into check rejection | ✅ Done | `wouldLeaveInCheck()` simulation |
| Invalid format — no crash | ✅ Done | Loop continues with error message |

### Advanced Rules
| Feature | Status | Notes |
|---|---|---|
| Check detection | ✅ Done | Warning displayed each turn |
| Checkmate detection | ✅ Done | Ends game, announces winner |
| Stalemate detection | ✅ Done | Ends game, announces draw |
| Kingside castling (`O-O`) | ✅ Done | Checks hasMoved, clear path, not in check |
| Queenside castling (`O-O-O`) | ✅ Done | Same checks as kingside |
| Pawn promotion (`E7 E8=Q`) | ✅ Done | Q/R/B/N; auto-Queen if unspecified |

---

## Not Implemented / Known Limitations

| Feature | Status | Reason |
|---|---|---|
| En passant | ❌ Not implemented | Out of scope for Phase 1 |
| Draw by repetition | ❌ Not implemented | Out of scope for Phase 1 |
| 50-move rule | ❌ Not implemented | Out of scope for Phase 1 |
| Insufficient material draw | ❌ Not implemented | Out of scope for Phase 1 |
| Move history / undo | ❌ Not implemented | Out of scope for Phase 1 |
| Graphical UI | ❌ Not implemented | Console-based per spec |

---

## OOP Design Summary

| Class | Package | Responsibility |
|---|---|---|
| `Piece` | `chess.pieces` | Abstract base: color, position, `possibleMoves()`, `getSymbol()` |
| `Pawn/Rook/Knight/Bishop/Queen/King` | `chess.pieces` | Concrete movement rules |
| `Board` | `chess.board` | 8×8 state, move execution, legality, check/checkmate |
| `Player` | `chess.player` | Console input, move dispatch, castling/promotion handling |
| `Game` | `chess.game` | Main loop, turn management, game-end conditions |
| `Position` | `chess.position` | Algebraic notation parsing, bounds validation |
| `Utils` | `chess.utils` | Stateless helpers: format validation, input parsing |
