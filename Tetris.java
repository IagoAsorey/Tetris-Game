import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Manages the internal game state, including the board, pieces, score, and collisions.
 */
public class Tetris {
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
    initGame();
  }

  /**
   * Initializes or resets the game state.
   */
  public void initGame() {
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

  /**
   * Implements the 7-bag randomization algorithm.
   */
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
    for (int i = 0; i < Config.BOARD_HEIGHT; i++) {
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

  public void togglePause() {
    paused = !paused;
  }

  public int[][] getBoard() { return board; }
  public int getScore() { return score; }
  public int getHighscore() { return highscore; }
  public boolean isGameOver() { return gameOver; }
  public boolean isPaused() { return paused; }
  public int getHeldPiece() { return heldPiece; }
  public List<Integer> getNextPieces() { return nextPieces; }

  private void loadHighscore() {
    try (BufferedReader br = new BufferedReader(new FileReader(Config.HIGHSCORE_FILE))) {
      String line = br.readLine();
      if (line != null) {
        highscore = Integer.parseInt(line.trim());
      }
    } catch (Exception e) {
      highscore = 0;
    }
  }

  private void saveHighscore() {
    try (PrintWriter pw = new PrintWriter(new FileWriter(Config.HIGHSCORE_FILE))) {
      pw.print(highscore);
    } catch (Exception e) {
    }
  }
}
