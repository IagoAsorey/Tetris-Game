import java.io.*;
import java.util.Random;

/**
 * Clase Tetris
 * 
 * Gestiona el estado interno del juego, incluyendo el tablero,
 * la pieza actual, la puntuación y las colisiones.
 */
public class Tetris {
    private int[][] board;
    private int score;
    private int highscore;
    
    // Estado de la pieza actual
    private int curX, curY, curR, curP;
    
    private boolean gameOver;
    private boolean paused;
    private Random random;

    public Tetris() {
        board = new int[Constants.BOARD_HEIGHT][Constants.BOARD_WIDTH];
        random = new Random();
        loadHighscore();
        initGame();
    }

    public void initGame() {
        // Limpiar el tablero lógico
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

    private int getNum(int pieceType, int rotation, int bitOffset) {
        return 3 & (Constants.PIECES[pieceType][rotation] >> bitOffset);
    }

    private void setPieceOnBoard(int x, int y, int r, int p, int value) {
        for (int i = 0; i < 8; i += 2) {
            int row = y + getNum(p, r, i * 2);
            int col = x + getNum(p, r, (i * 2) + 2);
            if (row >= 0 && row < Constants.BOARD_HEIGHT && col >= 0 && col < Constants.BOARD_WIDTH) {
                board[row][col] = value;
            }
        }
    }

    public boolean checkCollision(int x, int y, int r, int p) {
        // Límite inferior
        if (y + getNum(p, r, 18) >= Constants.BOARD_HEIGHT) return true;
        
        for (int i = 0; i < 8; i += 2) {
            int row = y + getNum(p, r, i * 2);
            int col = x + getNum(p, r, (i * 2) + 2);
            
            // Límites laterales
            if (col < 0 || col >= Constants.BOARD_WIDTH) return true;
            
            // Otras piezas fijas en el tablero
            if (row >= 0 && board[row][col] != 0) return true;
        }
        return false;
    }

    private void spawnPiece() {
        curY = 0;
        curP = random.nextInt(7);
        curR = random.nextInt(4);
        curX = random.nextInt(Constants.BOARD_WIDTH - getNum(curP, curR, 16));

        // Si la pieza nueva ya colisiona, se acabó el juego
        if (checkCollision(curX, curY, curR, curP)) {
            gameOver = true;
        } else {
            setPieceOnBoard(curX, curY, curR, curP, curP + 1);
        }
    }

    public void step() {
        if (gameOver || paused) return;

        // 1. Borrar temporalmente la pieza para comprobar el movimiento
        setPieceOnBoard(curX, curY, curR, curP, 0);

        // 2. ¿Puede bajar?
        if (checkCollision(curX, curY + 1, curR, curP)) {
            // No puede bajar, la fijamos y generamos otra
            setPieceOnBoard(curX, curY, curR, curP, curP + 1);
            clearLines();
            spawnPiece();
        } else {
            // Sí puede bajar
            curY++;
            setPieceOnBoard(curX, curY, curR, curP, curP + 1);
        }
    }

    public void moveLateral(int dx) {
        if (gameOver || paused) return;
        setPieceOnBoard(curX, curY, curR, curP, 0);
        if (!checkCollision(curX + dx, curY, curR, curP)) {
            curX += dx;
        }
        setPieceOnBoard(curX, curY, curR, curP, curP + 1);
    }

    public void rotate() {
        if (gameOver || paused) return;
        setPieceOnBoard(curX, curY, curR, curP, 0);
        int nextR = (curR + 1) % 4;
        if (!checkCollision(curX, curY, nextR, curP)) {
            curR = nextR;
        }
        setPieceOnBoard(curX, curY, curR, curP, curP + 1);
    }

    public void hardDrop() {
        if (gameOver || paused) return;
        // Bajar la pieza hasta que choque
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

    private void clearLines() {
        int linesCleared = 0;
        for (int i = 0; i < Constants.BOARD_HEIGHT; i++) {
            boolean full = true;
            for (int j = 0; j < Constants.BOARD_WIDTH; j++) {
                if (board[i][j] == 0) full = false;
            }
            
            if (full) {
                for (int k = i; k > 0; k--) {
                    for (int j = 0; j < Constants.BOARD_WIDTH; j++) {
                        board[k][j] = board[k - 1][j];
                    }
                }
                for (int j = 0; j < Constants.BOARD_WIDTH; j++) board[0][j] = 0;
                linesCleared++;
            }
        }
        if (linesCleared > 0) {
            score += linesCleared;
            if (score > highscore) {
                highscore = score;
                saveHighscore();
            }
        }
    }

    public void togglePause() { paused = !paused; }

    // Getters
    public int[][] getBoard() { return board; }
    public int getScore() { return score; }
    public int getHighscore() { return highscore; }
    public boolean isGameOver() { return gameOver; }
    public boolean isPaused() { return paused; }

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
            e.printStackTrace();
        }
    }
}
