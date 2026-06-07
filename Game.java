import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TerminalSize;
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
 * Game engine for the Tetris game.
 * Coordinates game state, input handling, and rendering.
 */
public class Game {
  private Screen screen;
  private Tetris tetris;
  private long lastGravityTick;
  private int frame;

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

  public Game(Screen screen) {
    this.screen = screen;
    this.tetris = new Tetris();
    this.frame = 0;
  }

  public void run() throws IOException, InterruptedException {
    lastGravityTick = System.currentTimeMillis();

    while (true) {
      // Process Input
      KeyStroke keyStroke = screen.pollInput();
      String action = null;
      if (keyStroke != null) {
        action = handleInput(keyStroke);
      }

      if ("quit".equals(action)) break;
      if ("pause".equals(action)) {
        String result = pauseScreen();
        if ("quit".equals(result)) break;
        if ("restart".equals(result)) {
          tetris.initGame();
          lastGravityTick = System.currentTimeMillis();
        }
      }

      // Update Logic
      if (!tetris.isGameOver() && !tetris.isPaused()) {
        long currentInterval = (long) Math.max(
            Config.MIN_GRAVITY_INTERVAL_MS,
            Config.INITIAL_GRAVITY_INTERVAL_MS - tetris.getScore() * Config.SPEED_FACTOR
        );

        long now = System.currentTimeMillis();
        if (now - lastGravityTick >= currentInterval) {
          tetris.step();
          lastGravityTick = now;
        }
      }

      // Draw
      if (tetris.isGameOver()) {
        drawGameOver();
        // Wait for input after game over
        KeyStroke ks = screen.readInput();
        char c = Character.toLowerCase(ks.getCharacter() != null ? ks.getCharacter() : ' ');
        if (c == 'r') {
          tetris.initGame();
          lastGravityTick = System.currentTimeMillis();
        } else if (c == 'q') {
          break;
        }
      } else {
        draw();
      }

      frame++;
      Thread.sleep(Config.LOOP_SLEEP_MS);
    }
  }

  private String handleInput(KeyStroke ks) {
    if (ks.getKeyType() == KeyType.Character) {
      char c = Character.toLowerCase(ks.getCharacter());
      if (c == 'q') return "quit";
      if (c == 'p') return "pause";
      if (c == 'r' && tetris.isGameOver()) {
        tetris.initGame();
        return null;
      }

      if (!tetris.isGameOver() && !tetris.isPaused()) {
        switch (c) {
          case 'a': tetris.moveLateral(-1); break;
          case 'd': tetris.moveLateral(1); break;
          case 's': tetris.step(); break; // Soft drop
          case 'w': tetris.rotate(); break;
          case 'c': tetris.holdPiece(); break;
          case ' ': tetris.hardDrop(); break;
        }
      }
    } else {
      KeyType type = ks.getKeyType();
      if (!tetris.isGameOver() && !tetris.isPaused()) {
        if (type == KeyType.ArrowLeft) tetris.moveLateral(-1);
        else if (type == KeyType.ArrowRight) tetris.moveLateral(1);
        else if (type == KeyType.ArrowUp) tetris.rotate();
        else if (type == KeyType.ArrowDown) tetris.step();
      }
    }
    return null;
  }

  private void drawBorder(int x, int y, int h, int w) {
    screen.setCharacter(x, y, new TextCharacter(Config.BORDER_TL, Config.BORDER_COLOR, TextColor.ANSI.DEFAULT));
    screen.setCharacter(x + w - 1, y, new TextCharacter(Config.BORDER_TR, Config.BORDER_COLOR, TextColor.ANSI.DEFAULT));
    screen.setCharacter(x, y + h - 1, new TextCharacter(Config.BORDER_BL, Config.BORDER_COLOR, TextColor.ANSI.DEFAULT));
    screen.setCharacter(x + w - 1, y + h - 1, new TextCharacter(Config.BORDER_BR, Config.BORDER_COLOR, TextColor.ANSI.DEFAULT));
    
    for (int i = 1; i < w - 1; i++) {
      screen.setCharacter(x + i, y, new TextCharacter(Config.BORDER_HOR, Config.BORDER_COLOR, TextColor.ANSI.DEFAULT));
      screen.setCharacter(x + i, y + h - 1, new TextCharacter(Config.BORDER_HOR, Config.BORDER_COLOR, TextColor.ANSI.DEFAULT));
    }
    for (int i = 1; i < h - 1; i++) {
      screen.setCharacter(x, y + i, new TextCharacter(Config.BORDER_VER, Config.BORDER_COLOR, TextColor.ANSI.DEFAULT));
      screen.setCharacter(x + w - 1, y + i, new TextCharacter(Config.BORDER_VER, Config.BORDER_COLOR, TextColor.ANSI.DEFAULT));
    }
  }

  private void draw() throws IOException {
    screen.doResizeIfNecessary();
    screen.clear();
    TerminalSize size = screen.getTerminalSize();
    int termH = size.getRows();
    int termW = size.getColumns();

    int startY = (termH - Config.BOARD_HEIGHT) / 2;
    if (startY < 3) startY = 3;
    int startX = (termW - Config.BOARD_WIDTH * 2) / 2;

    // Header
    drawText(startX + Config.BOARD_WIDTH - Config.TITLE.length()/2, startY - 3, Config.TITLE, Config.TITLE_COLOR, true);
    drawText(startX, startY - 1, "Score: " + tetris.getScore(), Config.HUD_COLOR, true);
    String best = "Best Score: " + tetris.getHighscore();
    drawText(startX + Config.BOARD_WIDTH * 2 - best.length() + 2, startY - 1, best, Config.HUD_COLOR, true);

    // Border
    drawBorder(startX, startY, Config.BOARD_HEIGHT + 2, Config.BOARD_WIDTH * 2 + 2);

    // Board
    int[][] board = tetris.getBoard();
    for (int i = 0; i < Config.BOARD_HEIGHT; i++) {
      for (int j = 0; j < Config.BOARD_WIDTH; j++) {
        int cell = board[i][j];
        if (cell != 0) {
          TextColor color = PIECE_COLORS[cell];
          TextCharacter block = new TextCharacter(' ', TextColor.ANSI.DEFAULT, color);
          screen.setCharacter(startX + 1 + j * 2, startY + 1 + i, block);
          screen.setCharacter(startX + 2 + j * 2, startY + 1 + i, block);
        }
      }
    }

    // Panels
    drawHold(startX - 14, startY + 1);
    drawNext(startX + Config.BOARD_WIDTH * 2 + 4, startY + 1);

    screen.refresh();
  }

  private void drawHold(int x, int y) {
    drawText(x + 2, y - 1, " HOLD ", Config.HUD_COLOR, false);
    drawBorder(x, y, 5, 10);
    int held = tetris.getHeldPiece();
    if (held != -1) drawPiecePreview(x + 1, y + 1, held);
  }

  private void drawNext(int x, int y) {
    drawText(x + 2, y - 1, " NEXT ", Config.HUD_COLOR, false);
    drawBorder(x, y, 13, 10);
    List<Integer> next = tetris.getNextPieces();
    for (int i = 0; i < next.size(); i++) {
      drawPiecePreview(x + 1, y + 1 + (i * 4), next.get(i));
    }
  }

  private void drawPiecePreview(int x, int y, int pieceType) {
    TextColor color = PIECE_COLORS[pieceType + 1];
    TextCharacter block = new TextCharacter(' ', TextColor.ANSI.DEFAULT, color);
    int rotation = 0;
    for (int i = 0; i < 8; i += 2) {
      int r = 3 & (Config.PIECES[pieceType][rotation] >> (i * 2));
      int c = 3 & (Config.PIECES[pieceType][rotation] >> ((i * 2) + 2));
      screen.setCharacter(x + c * 2, y + r, block);
      screen.setCharacter(x + c * 2 + 1, y + r, block);
    }
  }

  private void drawGameOver() throws IOException {
    draw();
    String[] msgs = {
      "GAME OVER",
      "Score: " + tetris.getScore(),
      "Best: " + tetris.getHighscore(),
      "",
      "Press R to restart",
      "Press Q to quit"
    };
    drawMenu(msgs, Config.GAME_OVER_COLOR);
  }

  private String pauseScreen() throws IOException {
    while (true) {
      draw();
      String[] msgs = {
        "PAUSED",
        "Score: " + tetris.getScore(),
        "Best: " + tetris.getHighscore(),
        "",
        "Press P to resume",
        "Press R to restart",
        "Press Q to quit"
      };
      drawMenu(msgs, Config.PAUSE_COLOR);
      
      KeyStroke ks = screen.readInput();
      char c = Character.toLowerCase(ks.getCharacter() != null ? ks.getCharacter() : ' ');
      if (c == 'p') return null;
      if (c == 'r') return "restart";
      if (c == 'q') return "quit";
    }
  }

  private void drawMenu(String[] lines, TextColor titleColor) throws IOException {
    screen.doResizeIfNecessary();
    TerminalSize size = screen.getTerminalSize();
    int termH = size.getRows();
    int termW = size.getColumns();
    int midY = termH / 2 - lines.length / 2;
    int midX = termW / 2;

    for (int i = 0; i < lines.length; i++) {
      TextColor color = (i == 0) ? titleColor : Config.HUD_COLOR;
      drawText(midX - lines[i].length() / 2, midY + i, lines[i], color, i < 3);
    }
    screen.refresh();
  }

  private void drawText(int x, int y, String text, TextColor color, boolean bold) {
    for (int i = 0; i < text.length(); i++) {
      TextCharacter tc = new TextCharacter(text.charAt(i), color, TextColor.ANSI.DEFAULT);
      if (bold) tc = tc.withModifier(SGR.BOLD);
      screen.setCharacter(x + i, y, tc);
    }
  }
}
