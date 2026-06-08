# Java Terminal Tetris

A terminal Tetris implementation written in Java with
[Lanterna](https://github.com/mabe02/lanterna). The game includes 7-bag piece
randomization, hold, next-piece preview, hard drop, persistent high score, and a
resize-aware terminal renderer.

## Requirements

- JDK 8 or newer (`java` and `javac` must be available on `PATH`).
- `curl` for the first run, unless `lanterna-3.1.2.jar` is already present.
- A UTF-8 terminal with ANSI color support.
- Recommended terminal size: at least 52 columns by 30 rows.

## Run

```bash
chmod +x run.sh
./run.sh
```

The script downloads Lanterna when missing, compiles sources into
`build/classes`, and skips recompilation when the existing classes are newer
than the Java source files.

Manual compile and run:

```bash
javac -Xlint:all -cp lanterna-3.1.2.jar -d build/classes Config.java Tetris.java Game.java Main.java
java -cp build/classes:lanterna-3.1.2.jar Main
```

## Controls

| Action | Keys |
| --- | --- |
| Move left | `A`, Left arrow |
| Move right | `D`, Right arrow |
| Rotate clockwise | `W`, Up arrow |
| Soft drop | `S`, Down arrow |
| Hard drop | Space |
| Hold piece | `C` |
| Pause or resume | `P` |
| Restart | `R` on pause or game over |
| Quit | `Q` |

## Project Structure

- `Main.java`: terminal setup and shutdown.
- `Game.java`: main loop, input handling, menus, and terminal rendering.
- `Tetris.java`: board state, piece movement, collision detection, scoring, and persistence.
- `Config.java`: board size, timing, colors, and encoded piece definitions.
- `run.sh`: dependency download, incremental compilation, and launch.

## Generated Files

- `build/classes/`: compiled `.class` files.
- `highscore.txt`: persistent best score.
- `lanterna-3.1.2.jar`: downloaded Lanterna dependency.

## Troubleshooting

- If the game reports that the terminal is too small, resize it to at least
  52x30.
- If Lanterna cannot be downloaded, verify network access or place
  `lanterna-3.1.2.jar` in the project root.
- If Unicode borders render incorrectly, use a UTF-8 locale and a terminal font
  with box-drawing character support.
