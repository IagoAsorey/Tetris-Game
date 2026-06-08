import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Owns all gameplay state: board occupancy, active piece, scoring, hold queue, and collision rules.
 */
public final class Tetris {
  private final int[][] board;
  private int score;
  private int highscore;

  // Current piece state
  private int curX;
  private int curY;
  private int curR;
  private int curP;

  // Advanced features state
  private int heldPiece = -1; // -1 means no piece held
  private boolean canHold = true; // Prevents holding multiple times in one turn
  private final List<Integer> nextPieces = new ArrayList<>();
  private final List<Integer> bag = new ArrayList<>();

  private boolean gameOver;
  private boolean paused;
  private final Random random;

  public Tetris() {
    this.board = new int[Config.BOARD_HEIGHT][Config.BOARD_WIDTH];
    this.random = new Random();
    loadHighscore();
    resetGameState();
  }

  /** Initializes or resets the game state for a new round. */
  public void initGame() {
    resetGameState();
  }

  private void resetGameState() {
    for (int i = 0; i < Config.BOARD_HEIGHT; i++) {
      for (int j = 0; j < Config.BOARD_WIDTH; j++) {
        board[i][j] = 0;
      }
    }

    score = 0;
    gameOver = false;
    paused = false;
    heldPiece = -1;
    canHold = true;
    nextPieces.clear();
    bag.clear();
    
    // Fill the initial queue with 3 pieces
    for (int i = 0; i < 3; i++) {
      nextPieces.add(pullFromBag());
    }
    
    spawnPiece();
  }

  /** Returns the next piece using the standard 7-bag randomization algorithm. */
  private int pullFromBag() {
    if (bag.isEmpty()) {
      for (int i = 0; i < 7; i++) {
        bag.add(i);
      }
      Collections.shuffle(bag, random);
    }
    return bag.remove(0);
  }

  private int getNum(int pieceType, int rotation, int bitOffset) {
    return 3 & (Config.PIECES[pieceType][rotation] >> bitOffset);
  }

  private void setPieceOnBoard(int x, int y, int r, int p, int value) {
    for (int i = 0; i < 8; i += 2) {
      int row = y + getNum(p, r, i * 2);
      int col = x + getNum(p, r, (i * 2) + 2);
      if (row >= 0 && row < Config.BOARD_HEIGHT && col >= 0 && col < Config.BOARD_WIDTH) {
        board[row][col] = value;
      }
    }
  }

  /**
   * Checks whether a piece would collide at the provided board position.
   *
   * @param x target column
   * @param y target row
   * @param r target rotation
   * @param p piece type
   * @return true when the piece would touch a wall, floor, or locked block
   */
  public boolean checkCollision(int x, int y, int r, int p) {
    for (int i = 0; i < 8; i += 2) {
      int row = y + getNum(p, r, i * 2);
      int col = x + getNum(p, r, (i * 2) + 2);

      if (col < 0 || col >= Config.BOARD_WIDTH || row >= Config.BOARD_HEIGHT) {
        return true;
      }

      if (row >= 0 && board[row][col] != 0) {
        return true;
      }
    }
    return false;
  }

  private void spawnPiece() {
    curP = nextPieces.remove(0);
    nextPieces.add(pullFromBag());
    
    curY = 0;
    curR = 0;
    curX = (Config.BOARD_WIDTH / 2) - 1;

    if (checkCollision(curX, curY, curR, curP)) {
      gameOver = true;
    } else {
      setPieceOnBoard(curX, curY, curR, curP, curP + 1);
    }
    canHold = true;
  }

  /** Stores or swaps the current piece, limited to one hold per spawned piece. */
  public void holdPiece() {
    if (gameOver || paused || !canHold) {
      return;
    }

    setPieceOnBoard(curX, curY, curR, curP, 0);

    if (heldPiece == -1) {
      heldPiece = curP;
      spawnPiece();
    } else {
      int temp = curP;
      curP = heldPiece;
      heldPiece = temp;
      
      curY = 0;
      curR = 0;
      curX = (Config.BOARD_WIDTH / 2) - 1;
      
      if (checkCollision(curX, curY, curR, curP)) {
        gameOver = true;
      } else {
        setPieceOnBoard(curX, curY, curR, curP, curP + 1);
      }
    }
    canHold = false;
  }

  /** Advances the active piece by one gravity step. */
  public void step() {
    if (gameOver || paused) {
      return;
    }

    setPieceOnBoard(curX, curY, curR, curP, 0);

    if (checkCollision(curX, curY + 1, curR, curP)) {
      setPieceOnBoard(curX, curY, curR, curP, curP + 1);
      clearLines();
      spawnPiece();
    } else {
      curY++;
      setPieceOnBoard(curX, curY, curR, curP, curP + 1);
    }
  }

  /** Moves the active piece horizontally when the destination is available. */
  public void moveLateral(int dx) {
    if (gameOver || paused) {
      return;
    }
    setPieceOnBoard(curX, curY, curR, curP, 0);
    if (!checkCollision(curX + dx, curY, curR, curP)) {
      curX += dx;
    }
    setPieceOnBoard(curX, curY, curR, curP, curP + 1);
  }

  /** Rotates the active piece clockwise with a small wall-kick fallback. */
  public void rotate() {
    if (gameOver || paused) {
      return;
    }
    setPieceOnBoard(curX, curY, curR, curP, 0);
    int nextR = (curR + 1) % 4;
    
    // Basic Wall Kick
    if (!checkCollision(curX, curY, nextR, curP)) {
      curR = nextR;
    } else if (!checkCollision(curX + 1, curY, nextR, curP)) {
      curX += 1;
      curR = nextR;
    } else if (!checkCollision(curX - 1, curY, nextR, curP)) {
      curX -= 1;
      curR = nextR;
    }
    
    setPieceOnBoard(curX, curY, curR, curP, curP + 1);
  }

  /** Drops the active piece to the lowest valid row and locks it immediately. */
  public void hardDrop() {
    if (gameOver || paused) {
      return;
    }
    setPieceOnBoard(curX, curY, curR, curP, 0);
    while (!checkCollision(curX, curY + 1, curR, curP)) {
      curY++;
    }
    setPieceOnBoard(curX, curY, curR, curP, curP + 1);
    clearLines();
    spawnPiece();
  }

  private void clearLines() {
    int linesClearedCount = 0;
    for (int i = Config.BOARD_HEIGHT - 1; i >= 0; i--) {
      boolean full = true;
      for (int j = 0; j < Config.BOARD_WIDTH; j++) {
        if (board[i][j] == 0) {
          full = false;
          break;
        }
      }

      if (full) {
        for (int k = i; k > 0; k--) {
          System.arraycopy(board[k - 1], 0, board[k], 0, Config.BOARD_WIDTH);
        }
        for (int j = 0; j < Config.BOARD_WIDTH; j++) {
          board[0][j] = 0;
        }
        linesClearedCount++;
        i++;
      }
    }
    if (linesClearedCount > 0) {
      // Classic scoring: 100, 300, 500, 800
      int[] points = {0, 100, 300, 500, 800};
      score += points[Math.min(linesClearedCount, 4)];
      
      if (score > highscore) {
        highscore = score;
        saveHighscore();
      }
    }
  }

  /** Returns the mutable board used by the renderer. */
  public int[][] getBoard() { return board; }

  /** Returns the current score. */
  public int getScore() { return score; }

  /** Returns the persisted high score loaded at startup. */
  public int getHighscore() { return highscore; }

  /** Returns whether no more pieces can be spawned. */
  public boolean isGameOver() { return gameOver; }

  /** Returns whether gameplay updates are paused. */
  public boolean isPaused() { return paused; }

  /** Returns the held piece index, or -1 when no piece is held. */
  public int getHeldPiece() { return heldPiece; }

  /** Returns a read-only view of the next-piece queue. */
  public List<Integer> getNextPieces() { return Collections.unmodifiableList(nextPieces); }

  private void loadHighscore() {
    try (BufferedReader br = new BufferedReader(new InputStreamReader(
        new FileInputStream(Config.HIGHSCORE_FILE),
        StandardCharsets.UTF_8))) {
      String line = br.readLine();
      if (line != null) {
        highscore = Integer.parseInt(line.trim());
      }
    } catch (FileNotFoundException | NumberFormatException e) {
      highscore = 0;
    } catch (IOException e) {
      System.err.println("Unable to read high score: " + e.getMessage());
      highscore = 0;
    }
  }

  private void saveHighscore() {
    try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(
        new FileOutputStream(Config.HIGHSCORE_FILE),
        StandardCharsets.UTF_8))) {
      pw.print(highscore);
    } catch (IOException e) {
      System.err.println("Unable to save high score: " + e.getMessage());
    }
  }
}
