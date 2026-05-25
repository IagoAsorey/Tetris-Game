#include "tetris.h"
#include <stdlib.h>
#include <ctime>
#include <fstream>
#include <string.h>

/**
 * tetris.cpp
 * 
 * Implementación de la lógica del juego.
 * Se ha diseñado siguiendo un estilo limpio y modular.
 */

Tetris::Tetris() {
    srand(time(NULL));
    load_highscore();
    init_game();
}

Tetris::~Tetris() {
    save_highscore();
}

void Tetris::init_game() {
    // Limpiar el tablero (0 = vacío)
    memset(board, 0, sizeof(board));
    
    score = 0;
    game_over = false;
    paused = false;
    spawn_piece();
}

/**
 * Extrae un valor de 2 bits de la codificación de la pieza.
 */
int Tetris::get_num(int piece_type, int rotation, int bit_offset) {
    return 3 & (PIECES[piece_type][rotation] >> bit_offset);
}

/**
 * Dibuja o borra la pieza actual en el tablero lógico.
 * value: 0 para borrar, >0 para el color de la pieza.
 */
void Tetris::set_piece_on_board(int x, int y, int r, int p, int value) {
    for (int i = 0; i < 8; i += 2) {
        int row = y + get_num(p, r, i * 2);
        int col = x + get_num(p, r, (i * 2) + 2);
        if (row >= 0 && row < BOARD_HEIGHT && col >= 0 && col < BOARD_WIDTH) {
            board[row][col] = value;
        }
    }
}

/**
 * Verifica si la pieza en una posición/rotación chocaría con algo.
 */
bool Tetris::check_collision(int x, int y, int r, int p) {
    // Verificar si se sale por abajo
    if (y + get_num(p, r, 18) >= BOARD_HEIGHT) return true;
    
    for (int i = 0; i < 8; i += 2) {
        int row = y + get_num(p, r, i * 2);
        int col = x + get_num(p, r, (i * 2) + 2);
        
        // Colisión con paredes laterales
        if (col < 0 || col >= BOARD_WIDTH) return true;
        
        // Colisión con otras piezas ya fijas (ignorar si está arriba del tablero)
        if (row >= 0 && board[row][col] != 0) return true;
    }
    return false;
}

/**
 * Genera una nueva pieza aleatoria en la parte superior.
 */
void Tetris::spawn_piece() {
    cur_y = 0;
    cur_p = rand() % 7;
    cur_r = rand() % 4;
    cur_x = rand() % (BOARD_WIDTH - get_num(cur_p, cur_r, 16));

    // Si al aparecer ya hay colisión, el juego termina
    if (check_collision(cur_x, cur_y, cur_r, cur_p)) {
        game_over = true;
    } else {
        set_piece_on_board(cur_x, cur_y, cur_r, cur_p, cur_p + 1);
    }
}

/**
 * Avanza la pieza hacia abajo (Gravedad).
 */
void Tetris::step() {
    if (game_over || paused) return;

    // 1. Borrar pieza de la posición actual
    set_piece_on_board(cur_x, cur_y, cur_r, cur_p, 0);

    // 2. Intentar mover abajo
    if (check_collision(cur_x, cur_y + 1, cur_r, cur_p)) {
        // Si choca, volver a ponerla donde estaba y fijarla
        set_piece_on_board(cur_x, cur_y, cur_r, cur_p, cur_p + 1);
        clear_lines();
        spawn_piece();
    } else {
        // Si no choca, bajar y actualizar posición
        cur_y++;
        set_piece_on_board(cur_x, cur_y, cur_r, cur_p, cur_p + 1);
    }
}

void Tetris::move_lateral(int dx) {
    if (game_over || paused) return;
    set_piece_on_board(cur_x, cur_y, cur_r, cur_p, 0);
    if (!check_collision(cur_x + dx, cur_y, cur_r, cur_p)) {
        cur_x += dx;
    }
    set_piece_on_board(cur_x, cur_y, cur_r, cur_p, cur_p + 1);
}

void Tetris::rotate() {
    if (game_over || paused) return;
    set_piece_on_board(cur_x, cur_y, cur_r, cur_p, 0);
    int next_r = (cur_r + 1) % 4;
    if (!check_collision(cur_x, cur_y, next_r, cur_p)) {
        cur_r = next_r;
    }
    set_piece_on_board(cur_x, cur_y, cur_r, cur_p, cur_p + 1);
}

void Tetris::hard_drop() {
    if (game_over || paused) return;
    while (true) {
        set_piece_on_board(cur_x, cur_y, cur_r, cur_p, 0);
        if (check_collision(cur_x, cur_y + 1, cur_r, cur_p)) {
            set_piece_on_board(cur_x, cur_y, cur_r, cur_p, cur_p + 1);
            break;
        }
        cur_y++;
        set_piece_on_board(cur_x, cur_y, cur_r, cur_p, cur_p + 1);
    }
    clear_lines();
    spawn_piece();
}

/**
 * Busca líneas llenas, las elimina y suma puntos.
 */
void Tetris::clear_lines() {
    for (int i = 0; i < BOARD_HEIGHT; i++) {
        bool full = true;
        for (int j = 0; j < BOARD_WIDTH; j++) {
            if (board[i][j] == 0) full = false;
        }
        
        if (full) {
            // Desplazar todo hacia abajo
            for (int k = i; k > 0; k--) {
                for (int j = 0; j < BOARD_WIDTH; j++) {
                    board[k][j] = board[k - 1][j];
                }
            }
            // Limpiar la línea superior
            for (int j = 0; j < BOARD_WIDTH; j++) board[0][j] = 0;
            score++;
        }
    }
    if (score > highscore) {
        highscore = score;
        save_highscore();
    }
}

/**
 * Renderiza el tablero y la interfaz en la ventana de curses.
 */
void Tetris::draw(WINDOW* win) {
    werase(win);
    box(win, 0, 0);
    
    // Dibujar celdas ocupadas
    for (int i = 0; i < BOARD_HEIGHT; i++) {
        for (int j = 0; j < BOARD_WIDTH; j++) {
            if (board[i][j] != 0) {
                wattron(win, COLOR_PAIR(board[i][j]));
                mvwprintw(win, i + 1, j * 2 + 1, "  ");
                wattroff(win, COLOR_PAIR(board[i][j]));
            }
        }
    }
    
    // UI: Puntuaciones
    mvwprintw(win, BOARD_HEIGHT + 1, 1, " Score: %d", score);
    mvwprintw(win, BOARD_HEIGHT + 2, 1, " Best:  %d", highscore);
    
    if (paused) mvwprintw(win, BOARD_HEIGHT / 2, 5, " PAUSED ");
    
    if (game_over) {
        mvwprintw(win, BOARD_HEIGHT / 2 - 1, 4, " GAME OVER ");
        mvwprintw(win, BOARD_HEIGHT / 2, 4, " R:Restart ");
        mvwprintw(win, BOARD_HEIGHT / 2 + 1, 4, " Q:Quit ");
    }
    wrefresh(win);
}

void Tetris::load_highscore() {
    std::ifstream file(HIGHSCORE_FILE);
    if (file.is_open()) {
        file >> highscore;
        file.close();
    } else {
        highscore = 0;
    }
}

void Tetris::save_highscore() {
    std::ofstream file(HIGHSCORE_FILE);
    if (file.is_open()) {
        file << highscore;
        file.close();
    }
}
