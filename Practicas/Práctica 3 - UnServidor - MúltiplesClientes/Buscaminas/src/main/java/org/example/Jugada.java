package org.example;

import java.io.Serializable;

/**
 * La clase Jugada representa una acción realizada por el jugador en el juego de Minesweeper.
 * Esta clase es serializable, lo que permite que las instancias de Jugada se guarden y recuperen
 * fácilmente, por ejemplo, en un archivo o en una transmisión de red.
 */
public class Jugada implements Serializable {
    private int x; // Coordenada X de la celda en el tablero
    private int y; // Coordenada Y de la celda en el tablero
    private int op; // Opción: 1 para liberar celda, 2 para colocar bandera, 3 para quitar bandera, 0 para el primer tiro

    /**
     * Constructor de la clase Jugada.
     *
     * @param x  La coordenada X de la celda donde se realiza la jugada.
     * @param y  La coordenada Y de la celda donde se realiza la jugada.
     * @param op La opción de la jugada (1: liberar celda, 2: colocar bandera, 3: quitar bandera, 0: primer tiro).
     */
    public Jugada(int x, int y, int op) {
        this.x = x;
        this.y = y;
        this.op = op;
    }

    /**
     * Obtiene la coordenada X de la celda.
     *
     * @return La coordenada X de la celda.
     */
    public int getX() {
        return x;
    }

    /**
     * Obtiene la coordenada Y de la celda.
     *
     * @return La coordenada Y de la celda.
     */
    public int getY() {
        return y;
    }

    /**
     * Obtiene la opción de la jugada.
     *
     * @return La opción de la jugada.
     */
    public int getOption() {
        return op;
    }

    /**
     * Establece la coordenada X de la celda.
     *
     * @param x La nueva coordenada X de la celda.
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * Establece la coordenada Y de la celda.
     *
     * @param y La nueva coordenada Y de la celda.
     */
    public void setY(int y) {
        this.y = y;
    }

    /**
     * Establece la opción de la jugada.
     *
     * @param op La nueva opción de la jugada.
     */
    public void setOption(int op) {
        this.op = op;
    }
}
