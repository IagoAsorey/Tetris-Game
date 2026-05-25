#ifndef TETRIS_H
#define TETRIS_H

#include <curses.h>
#include <vector>
#include "constants.h"

/**
 * Clase Tetris
 * 
 * Gestiona el estado interno del juego, incluyendo el tablero,
 * la pieza actual, la puntuación y las colisiones.
 */
class Tetris {
public:
    Tetris();
    ~Tetris();

    // Inicializa o reinicia el estado del juego
    void init_game();
    
    // Avanza el juego un paso (gravedad)
    void step();
    
    // Movimientos del jugador
    void move_lateral(int dx);
    void rotate();
    void hard_drop();
    
    // Dibujado
    void draw(WINDOW* win);

    // Getters de estado
    bool is_game_over() const { return game_over; }
    void toggle_pause() { paused = !paused; }
    bool is_paused() const { return paused; }

private:
    int board[BOARD_HEIGHT][BOARD_WIDTH];
    int score;
    int highscore;
    
    // Estado de la pieza actual
    int cur_x, cur_y, cur_r, cur_p;
    
    bool game_over;
    bool paused;

    // Métodos auxiliares internos
    int get_num(int piece_type, int rotation, int bit_offset);
    void set_piece_on_board(int x, int y, int r, int p, int value);
    bool check_collision(int x, int y, int r, int p);
    void spawn_piece();
    void clear_lines();
    void load_highscore();
    void save_highscore();
};

#endif // TETRIS_H
