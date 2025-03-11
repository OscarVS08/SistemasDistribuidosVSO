package org.example;

public class ClientInfo {
    private String nombre;
    private int nivel;
    private int indexJugada;
    private Tablero tablero;
    private long startTime;

    public ClientInfo(String nombre, int nivel, int indexJugada, Tablero tablero, long startTime) {
        this.nombre = nombre;
        this.nivel = nivel;
        this.indexJugada = indexJugada;
        this.tablero = tablero;
        this.startTime = startTime;
    }

    public String getNombre() {
        return nombre;
    }

    public int getIndexJugada(){
        return indexJugada;
    }

    public void setIndexJugada(int indexJugada){
        this.indexJugada = indexJugada;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getNivel() {
        return nivel;
    }

    public void setNivel(int nivel) {
        this.nivel = nivel;
    }

    public Tablero getTablero() {
        return tablero;
    }

    public void setTablero(Tablero tablero) {
        this.tablero = tablero;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    @Override
    public String toString() {
        return "ClientInfo{nombre='" + nombre + "', nivel=" + nivel + "}";
    }
}


