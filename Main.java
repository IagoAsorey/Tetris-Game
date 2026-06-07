import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import java.io.IOException;
import java.util.List;

/**
 * Entry point for the Tetris game. Handles the terminal UI and game loop.
 */
public class Main {
  private static final TextColor[] PIECE_COLORS = {
    TextColor.ANSI.BLACK, // Background
    TextColor.ANSI.CYAN, // Piece 1
    TextColor.ANSI.BLUE, // Piece 2
    TextColor.ANSI.YELLOW, // Piece 3
    TextColor.ANSI.GREEN, // Piece 4
    TextColor.ANSI.MAGENTA, // Piece 5
    TextColor.ANSI.RED, // Piece 6
    TextColor.ANSI.WHITE // Piece 7
  };

  private static final long INITIAL_GRAVITY_INTERVAL_MS = 700;
  private static final long MIN_GRAVITY_INTERVAL_MS = 150;
  private static final long LOOP_SLEEP_MS = 10;

  public static void main(String[] args) {
    DefaultTerminalFactory terminalFactory = new DefaultTerminalFactory();
    Screen screen = null;

    try {
      Terminal terminal = terminalFactory.createTerminal();
      screen = new TerminalScreen(terminal);
      screen.startScreen();
      screen.setCursorPosition(null); // Hide cursor for a cleaner UI

      Tetris game = new Tetris();
      long lastGravityTick = System.currentTimeMillis();

      // Main Game Loop
      while (true) {
        // Non-blocking input handling
        KeyStroke keyStroke = screen.pollInput();
        if (keyStroke != null) {
          if (processInput(keyStroke, game)) {
            break; // Exit requested
          }
        }

        // Calculate current gravity speed based on score (Slower progression)
        // Interval decreases by 20ms every 10 points
        long currentGravityInterval = Math.max(
            MIN_GRAVITY_INTERVAL_MS,
            INITIAL_GRAVITY_INTERVAL_MS - (game.getScore() / 5) * 50
        );

        // Automatic downward movement (Gravity)
        if (!game.isGameOver() && !game.isPaused()) {
          long now = System.currentTimeMillis();
          if (now - lastGravityTick >= currentGravityInterval) {
            game.step();
            lastGravityTick = now;
          }
        }

        // Update the visual representation
        draw(screen, game);

        // Control the loop frequency to save CPU resources
        Thread.sleep(LOOP_SLEEP_MS);
      }

    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
    } finally {
      if (screen != null) {
        try {
          screen.stopScreen();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  /**
   * Processes keyboard input. Returns true if the game should exit.
   */
  private static boolean processInput(KeyStroke keyStroke, Tetris game) {
    if (keyStroke.getKeyType() == KeyType.Character) {
      char c = Character.toLowerCase(keyStroke.getCharacter());
      if (c == 'q') {
        return true;
      }
      if (c == 'p') {
        game.togglePause();
      }
      if (c == 'r' && game.isGameOver()) {
        game.initGame();
      }

      if (!game.isGameOver() && !game.isPaused()) {
        switch (c) {
          case 'a':
            game.moveLateral(-1);
            break;
          case 'd':
            game.moveLateral(1);
            break;
          case 'w':
            game.rotate();
            break;
          case 'c':
            game.holdPiece();
            break;
          case ' ':
            game.hardDrop();
            break;
          default:
            break;
        }
      }
    } else {
      KeyType type = keyStroke.getKeyType();
      if (!game.isGameOver() && !game.isPaused()) {
        if (type == KeyType.ArrowLeft) {
          game.moveLateral(-1);
        } else if (type == KeyType.ArrowRight) {
          game.moveLateral(1);
        } else if (type == KeyType.ArrowUp) {
          game.rotate();
        } else if (type == KeyType.ArrowDown) {
          game.step(); // Soft drop: move one step down
        }
      }
    }
    return false;
  }

  /**
   * Renders the game state to the terminal screen.
   */
  private static void draw(Screen screen, Tetris game) throws IOException {
    screen.clear();

    // Shifted right to make room for HOLD panel
    final int startX = 15;
    final int startY = 2;

    // Draw borders: use | for sides and - for top/bottom
    for (int i = 0; i <= Constants.BOARD_HEIGHT + 1; i++) {
      screen.setCharacter(startX, startY + i, new TextCharacter('|'));
      screen.setCharacter(
          startX + Constants.BOARD_WIDTH * 2 + 1, startY + i, new TextCharacter('|'));
    }
    for (int j = 0; j <= Constants.BOARD_WIDTH * 2 + 1; j++) {
      screen.setCharacter(startX + j, startY, new TextCharacter('-'));
      screen.setCharacter(startX + j, startY + Constants.BOARD_HEIGHT + 1, new TextCharacter('-'));
    }

    // Draw active board
    int[][] board = game.getBoard();
    for (int i = 0; i < Constants.BOARD_HEIGHT; i++) {
      for (int j = 0; j < Constants.BOARD_WIDTH; j++) {
        int cell = board[i][j];
        if (cell != 0) {
          TextColor color = PIECE_COLORS[cell];
          TextCharacter block = new TextCharacter(' ', TextColor.ANSI.DEFAULT, color);
          screen.setCharacter(startX + 1 + j * 2, startY + 1 + i, block);
          screen.setCharacter(startX + 2 + j * 2, startY + 1 + i, block);
        }
      }
    }

    // --- HOLD PANEL (Left Side) ---
    drawText(screen, startX - 10, startY, " HOLD ");
    drawBox(screen, startX - 12, startY + 1, 4, 10);
    int held = game.getHeldPiece();
    if (held != -1) {
      drawPiecePreview(screen, startX - 10, startY + 2, held);
    }

    // --- HUD (Right Side) ---
    int hudX = startX + Constants.BOARD_WIDTH * 2 + 4;
    drawText(screen, hudX, startY + 1, "Score: " + game.getScore());
    drawText(screen, hudX, startY + 2, "Best:  " + game.getHighscore());

    // --- NEXT PANEL (Right Side) ---
    drawText(screen, hudX, startY + 5, " NEXT ");
    drawBox(screen, hudX - 1, startY + 6, 12, 10);
    List<Integer> next = game.getNextPieces();
    for (int i = 0; i < next.size(); i++) {
      drawPiecePreview(screen, hudX + 1, startY + 7 + (i * 4), next.get(i));
    }

    // Game state overlays
    if (game.isPaused()) {
      drawText(screen, startX + 5, startY + Constants.BOARD_HEIGHT / 2, "  PAUSED  ");
    }

    if (game.isGameOver()) {
      drawText(screen, startX + 5, startY + 8, " GAME OVER ");
      drawText(screen, startX + 5, startY + 9, " R:Restart ");
      drawText(screen, startX + 5, startY + 10, " Q:Quit    ");
    }

    screen.refresh();
  }

  private static void drawBox(Screen screen, int x, int y, int h, int w) {
    for (int i = 0; i < h; i++) {
      screen.setCharacter(x, y + i, new TextCharacter('|'));
      screen.setCharacter(x + w - 1, y + i, new TextCharacter('|'));
    }
    for (int j = 0; j < w; j++) {
      screen.setCharacter(x + j, y, new TextCharacter('-'));
      screen.setCharacter(x + j, y + h - 1, new TextCharacter('-'));
    }
  }

  private static void drawPiecePreview(Screen screen, int x, int y, int pieceType) {
    TextColor color = PIECE_COLORS[pieceType + 1];
    TextCharacter block = new TextCharacter(' ', TextColor.ANSI.DEFAULT, color);
    int rotation = 0; // Use first rotation for preview
    for (int i = 0; i < 8; i += 2) {
      int rowOffset = 3 & (Constants.PIECES[pieceType][rotation] >> (i * 2));
      int colOffset = 3 & (Constants.PIECES[pieceType][rotation] >> ((i * 2) + 2));
      screen.setCharacter(x + colOffset * 2, y + rowOffset, block);
      screen.setCharacter(x + colOffset * 2 + 1, y + rowOffset, block);
    }
  }

  private static void drawText(Screen screen, int x, int y, String text) {
    for (int i = 0; i < text.length(); i++) {
      screen.setCharacter(x + i, y, new TextCharacter(text.charAt(i)));
    }
  }
}
