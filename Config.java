import com.googlecode.lanterna.TextColor;

/**
 * Central configuration for board geometry, timing, controls, visuals, and pieces.
 */
public final class Config {
  private Config() {}

  /** Number of logical columns in the board. */
  public static final int BOARD_WIDTH = 12;

  /** Number of logical rows in the board. */
  public static final int BOARD_HEIGHT = 22;

  /** Header displayed above the board. */
  public static final String TITLE = "TETRIS";
  public static final char BORDER_TL = '╭';
  public static final char BORDER_TR = '╮';
  public static final char BORDER_BL = '╰';
  public static final char BORDER_BR = '╯';
  public static final char BORDER_HOR = '─';
  public static final char BORDER_VER = '│';

  /** File used for persistent high score storage. */
  public static final String HIGHSCORE_FILE = "highscore.txt";

  /** Initial gravity interval in milliseconds. */
  public static final long INITIAL_GRAVITY_INTERVAL_MS = 1000;

  /** Lowest gravity interval allowed by speed progression. */
  public static final long MIN_GRAVITY_INTERVAL_MS = 100;

  /** Milliseconds removed from gravity interval per score point. */
  public static final double SPEED_FACTOR = 0.1;

  /** Main loop sleep in milliseconds to avoid busy-waiting. */
  public static final long LOOP_SLEEP_MS = 10;

  /** Minimum terminal columns needed for the board and side panels. */
  public static final int MIN_TERM_W = 52;

  /** Minimum terminal rows needed for the board and menus. */
  public static final int MIN_TERM_H = 30;

  /** Border color. */
  public static final TextColor BORDER_COLOR = TextColor.ANSI.WHITE;
  public static final TextColor TITLE_COLOR = TextColor.ANSI.CYAN;
  public static final TextColor HUD_COLOR = TextColor.ANSI.WHITE;
  public static final TextColor GAME_OVER_COLOR = TextColor.ANSI.RED;
  public static final TextColor PAUSE_COLOR = TextColor.ANSI.CYAN;

  /**
   * Logical data for the 7 original Tetris pieces.
   */
  public static final int[][] PIECES = {
    {431424, 598356, 431424, 598356},
    {427089, 615696, 427089, 615696},
    {348480, 348480, 348480, 348480},
    {599636, 431376, 598336, 432192},
    {411985, 610832, 415808, 595540},
    {247872, 799248, 247872, 799248},
    {614928, 399424, 615744, 428369}
  };
}
