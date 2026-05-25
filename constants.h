#ifndef CONSTANTS_H
#define CONSTANTS_H

/**
 * constants.h
 * 
 * Este archivo contiene las configuraciones globales y los datos lógicos
 * de las piezas del juego. Se ha organizado para ser fácilmente modificable.
 */

// Dimensiones del tablero de juego
const int BOARD_WIDTH = 10;
const int BOARD_HEIGHT = 20;

// Nombre del archivo donde se guarda el récord
const char* const HIGHSCORE_FILE = "highscore.txt";

// Datos lógicos de las 7 piezas originales del Tetris.
// Cada pieza tiene 4 rotaciones codificadas en números enteros de 32 bits.
// La lógica interna extrae las coordenadas de estos bits.
const int PIECES[7][4] = {
    {431424, 598356, 431424, 598356},
    {427089, 615696, 427089, 615696},
    {348480, 348480, 348480, 348480},
    {599636, 431376, 598336, 432192},
    {411985, 610832, 415808, 595540},
    {247872, 799248, 247872, 799248},
    {614928, 399424, 615744, 428369}
};

#endif // CONSTANTS_H
