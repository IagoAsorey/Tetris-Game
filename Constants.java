/**
 * Global game constants and piece definitions.
 */
public final class Constants {
  private Constants() {}

  // Grid dimensions (standard Tetris size is 10x20)
  public static final int BOARD_WIDTH = 10;
  public static final int BOARD_HEIGHT = 20;

  // File used for persistent high score storage
  public static final String HIGHSCORE_FILE = "highscore.txt";

  /**
   * Logical data fot the 7 origianl Tetis pieces.
   * Each piece has 4 rotations encoded in 32-bit integers.
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
