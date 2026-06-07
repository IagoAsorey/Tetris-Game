import com.googlecode.lanterna.TerminalPosition;
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

public class Main {
    private static final TextColor[] PIECE_COLORS = {
        TextColor.ANSI.BLACK,   // Fondo
        TextColor.ANSI.CYAN,    // Pieza 1
        TextColor.ANSI.BLUE,    // Pieza 2
        TextColor.ANSI.YELLOW,  // Pieza 3
        TextColor.ANSI.GREEN,   // Pieza 4
        TextColor.ANSI.MAGENTA, // Pieza 5
        TextColor.ANSI.RED,     // Pieza 6
        TextColor.ANSI.WHITE    // Pieza 7
    };

    public static void main(String[] args) {
        DefaultTerminalFactory terminalFactory = new DefaultTerminalFactory();
        Screen screen = null;

        try {
            Terminal terminal = terminalFactory.createTerminal();
            screen = new TerminalScreen(terminal);
            screen.startScreen();
            screen.setCursorPosition(null);

            Tetris game = new Tetris();
            long lastGravityTick = System.currentTimeMillis();
            long gravityInterval = 500;

            while (true) {
                // Manejo de entrada
                KeyStroke keyStroke = screen.pollInput();
                if (keyStroke != null) {
                    if (keyStroke.getKeyType() == KeyType.Character) {
                        char c = Character.toLowerCase(keyStroke.getCharacter());
                        if (c == 'q') break;
                        if (c == 'p') game.togglePause();
                        if (c == 'r' && game.isGameOver()) game.initGame();
                        
                        if (!game.isGameOver() && !game.isPaused()) {
                            if (c == 'a') game.moveLateral(-1);
                            if (c == 'd') game.moveLateral(1);
                            if (c == 'w') game.rotate();
                            if (c == 's') game.hardDrop();
                        }
                    } else if (keyStroke.getKeyType() == KeyType.ArrowLeft) {
                        game.moveLateral(-1);
                    } else if (keyStroke.getKeyType() == KeyType.ArrowRight) {
                        game.moveLateral(1);
                    } else if (keyStroke.getKeyType() == KeyType.ArrowUp) {
                        game.rotate();
                    } else if (keyStroke.getKeyType() == KeyType.ArrowDown) {
                        game.hardDrop();
                    }
                }

                // Lógica de gravedad
                if (!game.isGameOver() && !game.isPaused()) {
                    long now = System.currentTimeMillis();
                    if (now - lastGravityTick >= gravityInterval) {
                        game.step();
                        lastGravityTick = now;
                    }
                }

                // Dibujado
                draw(screen, game);
                
                Thread.sleep(10);
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

    private static void draw(Screen screen, Tetris game) throws IOException {
        screen.clear();
        
        int startX = 5;
        int startY = 2;
        
        // Dibujar marco
        for (int i = 0; i <= Constants.BOARD_HEIGHT + 1; i++) {
            screen.setCharacter(startX, startY + i, new TextCharacter('|'));
            screen.setCharacter(startX + Constants.BOARD_WIDTH * 2 + 1, startY + i, new TextCharacter('|'));
        }
        for (int j = 0; j <= Constants.BOARD_WIDTH * 2 + 1; j++) {
            screen.setCharacter(startX + j, startY, new TextCharacter('-'));
            screen.setCharacter(startX + j, startY + Constants.BOARD_HEIGHT + 1, new TextCharacter('-'));
        }

        // Dibujar tablero
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

        // Información lateral
        String scoreStr = "Score: " + game.getScore();
        String highStr = "Best:  " + game.getHighscore();
        
        for (int i = 0; i < scoreStr.length(); i++) {
            screen.setCharacter(startX + Constants.BOARD_WIDTH * 2 + 4, startY + 2, new TextCharacter(scoreStr.charAt(i)));
        }
        for (int i = 0; i < highStr.length(); i++) {
            screen.setCharacter(startX + Constants.BOARD_WIDTH * 2 + 4, startY + 3, new TextCharacter(highStr.charAt(i)));
        }

        if (game.isPaused()) {
            String pausedStr = "  PAUSED  ";
            for (int i = 0; i < pausedStr.length(); i++) {
                screen.setCharacter(startX + 5, startY + Constants.BOARD_HEIGHT / 2, new TextCharacter(pausedStr.charAt(i)));
            }
        }

        if (game.isGameOver()) {
            String goStr = " GAME OVER ";
            String rStr  = " R:Restart ";
            String qStr  = " Q:Quit    ";
            for (int i = 0; i < goStr.length(); i++) screen.setCharacter(startX + 5, startY + 8, new TextCharacter(goStr.charAt(i)));
            for (int i = 0; i < rStr.length(); i++)  screen.setCharacter(startX + 5, startY + 9, new TextCharacter(rStr.charAt(i)));
            for (int i = 0; i < qStr.length(); i++)  screen.setCharacter(startX + 5, startY + 10, new TextCharacter(qStr.charAt(i)));
        }

        screen.refresh();
    }
}
