import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
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

  private boolean gameOver;
  private boolean paused;
  private final Random random;

  public Tetris() {
    this.board = new int[Constants.BOARD_HEIGHT][Constants.BOARD_WIDTH];
    this.random = new Random();
    loadHighscore();
    initGame();
  }

  /**
   * Initializes or resets the game state.
   */
  public void initGame() {
    for (int i = 0; i < Constants.BOARD_HEIGHT; i++) {
      for (int j = 0; j < Constants.BOARD_WIDTH; j++) {
        board[i][j] = 0;
      }
    }

    score = 0;
    gameOver = false;
    paused = false;
    spawnPiece();
  }

  /**
   * Extracts a specific 2-bit value from the encoded piece integer.
   */
  private int getNum(int pieceType, int rotation, int bitOffset) {
    return 3 & (Constants.PIECES[pieceType][rotation] >> bitOffset);
  }

  /**
   * Places or removes a piece on the board.
   */
  private void setPieceOnBoard(int x, int y, int r, int p, int value) {
    // Each piece consists of 4 blocks, each defined by an (r, c) pair in the bitmask
    for (int i = 0; i < 8; i += 2) {
      int row = y + getNum(p, r, i * 2);
      int col = x + getNum(p, r, (i * 2) + 2);
      if (row >= 0 && row < Constants.BOARD_HEIGHT && col >= 0 && col < Constants.BOARD_WIDTH) {
        board[row][col] = value;
      }
    }
  }

  /**
   * Checks if a piece collides with boundaries or other pieces.
   */
  public boolean checkCollision(int x, int y, int r, int p) {
    // Fast floor collision check using the encoded max height offset
    if (y + getNum(p, r, 18) >= Constants.BOARD_HEIGHT) {
      return true;
    }

    // Detailed check for every block of the piece
    for (int i = 0; i < 8; i += 2) {
      int row = y + getNum(p, r, i * 2);
      int col = x + getNum(p, r, (i * 2) + 2);

      // Boundary check (walls)
      if (col < 0 || col >= Constants.BOARD_WIDTH) {
        return true;
      }

      // Check collision with fixed blocks already on the board
      if (row >= 0 && board[row][col] != 0) {
        return true;
      }
    }
    return false;
  }

  private void spawnPiece() {
    curY = 0;
    curP = random.nextInt(7);
    curR = random.nextInt(4);
    // Calculate random X ensuring the piece starts within the side walls
    curX = random.nextInt(Constants.BOARD_WIDTH - getNum(curP, curR, 16));

    if (checkCollision(curX, curY, curR, curP)) {
      gameOver = true;
    } else {
      setPieceOnBoard(curX, curY, curR, curP, curP + 1);
    }
  }

  /**
   * Advances the game logic by one gravity tick.
   */
  public void step() {
    if (gameOver || paused) {
      return;
    }

    // Temporarily remove piece to check if it can move down
    setPieceOnBoard(curX, curY, curR, curP, 0);

    if (checkCollision(curX, curY + 1, curR, curP)) {
      // Piece landed: lock it, clear lines, and spawn next
      setPieceOnBoard(curX, curY, curR, curP, curP + 1);
      clearLines();
      spawnPiece();
    } else {
      // Move one step down
      curY++;
      setPieceOnBoard(curX, curY, curR, curP, curP + 1);
    }
  }

  /**
   * Moves the current piece horizontally.
   */
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

  /**
   * Rotates the current piece.
   */
  public void rotate() {
    if (gameOver || paused) {
      return;
    }
    setPieceOnBoard(curX, curY, curR, curP, 0);
    int nextR = (curR + 1) % 4;
    if (!checkCollision(curX, curY, nextR, curP)) {
      curR = nextR;
    }
    setPieceOnBoard(curX, curY, curR, curP, curP + 1);
  }

  /**
   * Drops the piece to the bottom instantly.
   */
  public void hardDrop() {
    if (gameOver || paused) {
      return;
    }
    // Continuous loop: move down until a collision is detected
    while (true) {
      setPieceOnBoard(curX, curY, curR, curP, 0);
      if (checkCollision(curX, curY + 1, curR, curP)) {
        setPieceOnBoard(curX, curY, curR, curP, curP + 1);
        break;
      }
      curY++;
      setPieceOnBoard(curX, curY, curR, curP, curP + 1);
    }
    clearLines();
    spawnPiece();
  }

  /**
   * Scans the board for full lines and removes them by shifting rows down.
   */
  private void clearLines() {
    int linesClearedCount = 0;
    for (int i = 0; i < Constants.BOARD_HEIGHT; i++) {
      boolean full = true;
      // Check if the entire row is non-zero
      for (int j = 0; j < Constants.BOARD_WIDTH; j++) {
        if (board[i][j] == 0) {
          full = false;
          break;
        }
      }

      if (full) {
        // Shift all rows above this one down by one position
        for (int k = i; k > 0; k--) {
          System.arraycopy(board[k - 1], 0, board[k], 0, Constants.BOARD_WIDTH);
        }
        // Top row becomes empty after the shift
        for (int j = 0; j < Constants.BOARD_WIDTH; j++) {
          board[0][j] = 0;
        }
        linesClearedCount++;
      }
    }
    // Update score and highscore if lines were cleared
    if (linesClearedCount > 0) {
      score += linesClearedCount;
      if (score > highscore) {
        highscore = score;
        saveHighscore();
      }
    }
  }

  public void togglePause() {
    paused = !paused;
  }

  public int[][] getBoard() {
    return board;
  }

  public int getScore() {
    return score;
  }

  public int getHighscore() {
    return highscore;
  }

  public boolean isGameOver() {
    return gameOver;
  }

  public boolean isPaused() {
    return paused;
  }

  private void loadHighscore() {
    try (BufferedReader br = new BufferedReader(new FileReader(Constants.HIGHSCORE_FILE))) {
      String line = br.readLine();
      if (line != null) {
        highscore = Integer.parseInt(line.trim());
      }
    } catch (Exception e) {
      highscore = 0;
    }
  }

  private void saveHighscore() {
    try (PrintWriter pw = new PrintWriter(new FileWriter(Constants.HIGHSCORE_FILE))) {
      pw.print(highscore);
    } catch (Exception e) {
      // In a real application, consider using a logger.
    }
  }
}
