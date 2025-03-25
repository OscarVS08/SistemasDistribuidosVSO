package org.example;

import java.io.Serializable;

/**
 * La clase Tablerousr representa el estado visual del tablero del juego para el jugador.
 * Contiene el estado del juego, la representación visual del tablero y mensajes informativos.
 */
public class Tablerousr implements Serializable {
    String estado; // Estado del juego (ej: "Jugando", "Winner", "Loser")
    String[][] tablerovisual; // Representación visual del tablero para el jugador
    String mensaje; // Mensaje informativo para el usuario

    /**
     * Constructor que inicializa el estado del juego, el tablero visual y un mensaje.
     * @param estado Estado del juego.
     * @param tablerovisual Representación visual del tablero.
     * @param mensaje Mensaje informativo para el usuario.
     */
    public Tablerousr(String estado, String[][] tablerovisual, String mensaje) {
        this.estado = estado;
        this.mensaje = mensaje;
        this.tablerovisual = tablerovisual;
    }

    /**
     * Obtiene el estado del juego.
     * @return Estado del juego.
     */
    public String getEstado(){
        return estado;
    }

    /**
     * Obtiene el mensaje informativo para el usuario.
     * @return Mensaje informativo.
     */
    public String getMensaje(){
        return mensaje;
    }

    /**
     * Obtiene la representación visual del tablero.
     * @return Tablero visual.
     */
    public String[][] getTab(){
        return tablerovisual;
    }

}

