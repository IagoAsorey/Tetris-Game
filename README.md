# Java Terminal Tetris

A classic Tetris implementation for the terminal using the [Lanterna](https://github.com/mabe02/lanterna) library. This version features smooth speed progression, high-score persistence, and modern gameplay mechanics like "Hold" and "Next" piece previews.

## Features

- **Dynamic Speed:** The game gets faster with every point you score, with no hard limit.
- **Classic Mechanics:** Includes "Hold Piece" (C), "Hard Drop" (Space), and "Next Piece" preview.
- **Visuals:** Modern terminal borders and colorful pieces.
- **Persistence:** Keeps track of your best score in `highscore.txt`.
- **Responsive:** Centered menus and support for terminal resizing.

## Controls

| Action | Key |
| :--- | :--- |
| **Move Left** | `A` or `Left Arrow` |
| **Move Right** | `D` or `Right Arrow` |
| **Rotate** | `W` or `Up Arrow` |
| **Soft Drop** | `S` or `Down Arrow` |
| **Hard Drop** | `Space` |
| **Hold Piece** | `C` |
| **Pause** | `P` |
| **Quit** | `Q` |

## Requirements

- Java Development Kit (JDK) 8 or higher.
- A terminal with ANSI color support.

## How to Run

Simply execute the provided run script:

```bash
chmod +x run.sh
./run.sh
```

The script will automatically download the Lanterna library, compile the source code, and start the game.

## Customization

You can adjust game settings in `Config.java`:
- **Board Size:** Change `BOARD_WIDTH` and `BOARD_HEIGHT`.
- **Speed:** Tweak `INITIAL_GRAVITY_INTERVAL_MS`, `SPEED_FACTOR`, and `MIN_GRAVITY_INTERVAL_MS`.
- **Colors:** Modify piece and HUD colors.

## License

MIT License - feel free to use and modify!
