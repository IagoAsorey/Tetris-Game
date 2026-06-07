import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

/**
 * Main entry point for the Tetris game.
 */
public class Main {
  public static void main(String[] args) {
    DefaultTerminalFactory terminalFactory = new DefaultTerminalFactory();
    Screen screen = null;

    try {
      Terminal terminal = terminalFactory.createTerminal();
      screen = new TerminalScreen(terminal);
      screen.startScreen();
      
      new Game(screen).run();

    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (screen != null) {
        try {
          screen.stopScreen();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
  }
}
