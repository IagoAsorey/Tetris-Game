#include <curses.h>
#include <unistd.h>
#include "tetris.h"

/**
 * main.cpp
 * 
 * Punto de entrada del programa.
 * Configura el entorno de curses y gestiona el bucle principal de tiempo.
 */

int main() {
    // Inicialización de Curses
    initscr();
    start_color();
    
    // Configuración de colores (igual que en los otros juegos)
    for (int i = 1; i < 8; i++) {
        init_pair(i, i, 0);
    }
    
    noecho();           // No mostrar teclas pulsadas
    curs_set(0);        // Ocultar cursor
    keypad(stdscr, TRUE); // Habilitar flechas del teclado
    timeout(50);        // getch() espera 50ms (entrada no bloqueante)

    Tetris game;
    
    // Crear ventana centrada
    int win_h = BOARD_HEIGHT + 4;
    int win_w = BOARD_WIDTH * 2 + 2;
    WINDOW* win = newwin(win_h, win_w, (LINES - win_h) / 2, (COLS - win_w) / 2);
    
    int gravity_timer = 0;
    
    // Bucle principal del juego
    while (true) {
        int key = getch();
        
        // Salir del programa
        if (key == 'q' || key == 'Q') break;
        
        // Pausa
        if (key == 'p' || key == 'P') game.toggle_pause();
        
        // Reiniciar si el juego ha terminado
        if ((key == 'r' || key == 'R') && game.is_game_over()) {
            game.init_game();
        }
        
        // Controles activos si no hay pausa ni game over
        if (!game.is_game_over() && !game.is_paused()) {
            if (key == 'a' || key == KEY_LEFT)  game.move_lateral(-1);
            if (key == 'd' || key == KEY_RIGHT) game.move_lateral(1);
            if (key == 'w' || key == KEY_UP)    game.rotate();
            if (key == 's' || key == KEY_DOWN)  game.hard_drop();
        }

        // Gestión de la Gravedad
        // Cada ciclo de 50ms incrementamos el timer.
        // Cuando llega a 10 (aprox. 500ms), la pieza baja.
        if (!game.is_game_over() && !game.is_paused()) {
            gravity_timer++;
            if (gravity_timer >= 10) {
                game.step();
                gravity_timer = 0;
            }
        }

        // Renderizado
        game.draw(win);
    }

    // Finalización limpia de curses
    delwin(win);
    endwin();
    
    return 0;
}
