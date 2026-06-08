import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import java.io.IOException;

/**
 * Creates the terminal screen and starts the Tetris application.
 */
public final class Main {
  private Main() {}

  /**
   * Application entry point.
   *
   * @param args command-line arguments, currently unused
   */
  public static void main(String[] args) {
    DefaultTerminalFactory terminalFactory = new DefaultTerminalFactory();
    Screen screen = null;
    int exitCode = 0;

    try {
      Terminal terminal = terminalFactory.createTerminal();
      screen = new TerminalScreen(terminal);
      screen.startScreen();

      new Game(screen).run();

    } catch (IOException e) {
      System.err.println("Unable to initialize or refresh the terminal: " + e.getMessage());
      exitCode = 1;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      System.err.println("Game interrupted.");
      exitCode = 1;
    } finally {
      if (screen != null) {
        try {
          screen.stopScreen();
        } catch (IOException e) {
          System.err.println("Unable to restore the terminal cleanly: " + e.getMessage());
        }
      }
    }

    if (exitCode != 0) {
      System.exit(exitCode);
    }
  }
}
