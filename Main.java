import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import java.io.IOException;

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

  private static final long GRAVITY_INTERVAL_MS = 500;
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

        // Automatic downward movement (Gravity)
        if (!game.isGameOver() && !game.isPaused()) {
          long now = System.currentTimeMillis();
          if (now - lastGravityTick >= GRAVITY_INTERVAL_MS) {
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
          case 's':
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
          game.hardDrop();
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

    // Base offset for the game board rendering
    final int startX = 5;
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

    // Draw active board: iterate through the logical grid
    int[][] board = game.getBoard();
    for (int i = 0; i < Constants.BOARD_HEIGHT; i++) {
      for (int j = 0; j < Constants.BOARD_WIDTH; j++) {
        int cell = board[i][j];
        if (cell != 0) {
          TextColor color = PIECE_COLORS[cell];
          // Use two spaces to create a square-like block in the terminal
          TextCharacter block = new TextCharacter(' ', TextColor.ANSI.DEFAULT, color);
          screen.setCharacter(startX + 1 + j * 2, startY + 1 + i, block);
          screen.setCharacter(startX + 2 + j * 2, startY + 1 + i, block);
        }
      }
    }

    // Draw HUD: Score and Best score
    drawText(screen, startX + Constants.BOARD_WIDTH * 2 + 4, startY + 2, "Score: " + game.getScore());
    drawText(screen, startX + Constants.BOARD_WIDTH * 2 + 4, startY + 3, "Best:  " + game.getHighscore());

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

  /**
   * Helper method to print strings to the Lanterna screen character by character.
   */
  private static void drawText(Screen screen, int x, int y, String text) {
    for (int i = 0; i < text.length(); i++) {
      screen.setCharacter(x + i, y, new TextCharacter(text.charAt(i)));
    }
  }
}
