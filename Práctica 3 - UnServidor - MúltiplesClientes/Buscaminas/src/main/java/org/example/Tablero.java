package org.example;

import java.io.Serializable;
import java.util.*;

/**
 * La clase Tablero representa un tablero de juego para el juego de Buscaminas.
 * Permite gestionar el estado del juego, la colocación de minas y la interacción del jugador.
 */
public class Tablero implements Serializable {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_YELL = "\u001B[33m";

    int filas; // Número de filas del tablero
    int columnas; // Número de columnas del tablero
    int c_libres; // Contador de celdas destapadas
    int minas; // Número de minas en el tablero
    int c_banderas; // Contador de banderas colocadas

    int totalbanderas; // Total de banderas colocadas
    public String ganador; // Estado del juego: "Jugando", "Winner", "Loser"
    int[][] tablero; // Tablero con las minas y números
    String[][] tablerousr; // Representación visual del tablero para el usuario
    boolean[][] libres; // Estado de las celdas (si están destapadas o no)
    boolean[][] banderas; // Estado de las celdas con banderas
    Queue<int[]> pendientes = new LinkedList<>(); // Cola de celdas pendientes de revelar
    String mensaje; // Mensaje informativo para el usuario

    /**
     * Constructor que inicializa el tablero según el nivel de dificultad.
     * @param nivel El nivel de dificultad (1: principiante, 2: intermedio, 3: avanzado)
     */
    public Tablero(int nivel) {
        // celdas libres o descubiertas
        c_libres = 0;
        switch (nivel) {
            case 1: // nivel principiante 9x9 y 10 minas
                minas = 10;
                filas = 9;
                columnas = 9;
                break;
            case 2: // nivel intermedio 16x16 y 40 minas
                minas = 40;
                filas = 16;
                columnas = 16;
                break;
            case 3: // nivel avanzado 16x30 y 99 minas
                minas = 99;
                filas = 16;
                columnas = 30;
                break;
            default:
                throw new IllegalArgumentException("Nivel no válido");
        }
        ganador = "Jugando";
        tablero = new int[filas][columnas];
        tablerousr = new String[filas][columnas];
        libres = new boolean[filas][columnas];
        banderas = new boolean[filas][columnas];
    }

    /**
     * Rellena el tablero con minas, excluyendo la celda seleccionada y sus adyacentes.
     * @param x Coordenada X de la celda seleccionada.
     * @param y Coordenada Y de la celda seleccionada.
     */
    private void rellenaMinas(int x, int y) {
        // Celdas adyacentes
        int[][] desplazamientos = {
                {0, 0}, // Celda seleccionada
                {1, 0}, // Abajo
                {-1, 0}, // Arriba
                {0, 1}, // Derecha
                {0, -1}, // Izquierda
                {1, 1}, // Abajo derecha
                {-1, -1}, // Arriba izquierda
                {1, -1}, // Abajo izquierda
                {-1, 1}  // Arriba derecha
        };
        // Crear una lista con todas las posiciones posibles (x, y) del tablero
        List<int[]> posiciones = new ArrayList<>();

        // Llenar la lista con las posiciones
        for (int i = 0; i < this.filas; i++) {
            for (int j = 0; j < this.columnas; j++) {
                boolean esExcluida = false;
                // Verificamos si la celda actual es una de las celdas a excluir
                for (int[] desplazamiento : desplazamientos) {
                    int coordX = x + desplazamiento[0];
                    int coordY = y + desplazamiento[1];

                    // Si la celda actual coincide con alguna de las celdas adyacentes
                    if (i == coordX && j == coordY) {
                        esExcluida = true;
                        break;
                    }
                }

                // Solo agrega si no es una celda excluida
                if (!esExcluida) {
                    posiciones.add(new int[]{i, j}); // Agregar cada posición (i, j) a la lista
                }
            }
        }

        // Barajar las posiciones aleatoriamente
        Collections.shuffle(posiciones);

        // Colocar minas en las primeras `minas` posiciones de la lista
        for (int i = 0; i < minas; i++) {
            int[] pos = posiciones.get(i);
            tablero[pos[0]][pos[1]] = -1; // Colocar la mina
        }
    }

    /**
     * Realiza el primer tiro en el tablero.
     * @param x Coordenada X de la celda seleccionada.
     * @param y Coordenada Y de la celda seleccionada.
     */
    public void primertiro(int x, int y){
        rellenaMinas(x,y);
        contarMinasAlrededor(); // cuenta las minas de alrededor de cada celda
        revelarCeldas(x,y);
        mensaje = "¡Haz clic en una celda para actualizar el valor o colocar/quitar una bandera!\n\n";
    }

    /**
     * Revela las celdas del tablero y actualiza el estado del juego.
     * @param x Coordenada X de la celda seleccionada.
     * @param y Coordenada Y de la celda seleccionada.
     */
    public void revelarCeldas(int x, int y) {
        if (tablero[x][y] == -1 && !banderas[x][y]){ //si es una mina y no tiene bandera
            tablerousr[x][y] = "*";
            ganador = "Loser"; // hay mina
            mensaje = "PISASTE UNA MINA, GAME OVER.\n\n";
            generartableroFinal();
        } else if (!libres[x][y] && !banderas[x][y] && tablero[x][y] != 0) {
            // La lista de visitadas son las celdas destapadas
            libres[x][y] = true; // Se libera la celda seleccionada
            c_libres++;
            tablerousr[x][y] = String.valueOf(tablero[x][y]);
            mensaje = "Suerte! esa celda no tenía mina.\n\n";
        } else if (!libres[x][y] && !banderas[x][y] && tablero[x][y] == 0) {
            vaciado(x,y);
            mensaje = "Suerte! esa celda no tenía mina.\n\n";
        } else {
            System.out.println("Esa casilla ya fue visitada o tiene una bandera.\n\n");
            mensaje = "Esa casilla ya fue visitada o tiene una bandera.\n\n";
        }
    }

    /**
     * Genera el tablero final al finalizar el juego.
     */
    private void generartableroFinal() {
        for(int i = 0; i < this.filas; i++){
            for(int j = 0; j < this.columnas; j++){
                if(tablerousr[i][j] != null){ // Si ya tiene un elemento puede ser una Bandera o un número
                    if(Objects.equals(tablerousr[i][j], "B") && tablero[i][j] == -1){ // Ya tiene una bandera puesta y si es una mina
                        tablerousr[i][j] = "B";
                    } else if (Objects.equals(tablerousr[i][j], "B") && tablero[i][j] != -1) { // Ya tiene una bandera puesta y no es una mina
                        tablerousr[i][j] = "x";
                    }
                } else {
                    tablerousr[i][j] = String.valueOf(tablero[i][j]);
                }
            }
        }
    }

    /**
     * Verifica si una celda ya está en la cola de pendientes.
     * @param x Coordenada X de la celda.
     * @param y Coordenada Y de la celda.
     * @return true si la celda no está en la cola, false de lo contrario.
     */
    private boolean verificarCola(int x, int y){
        for (int[] elemento : pendientes) {
            if (Arrays.equals(elemento, new int[]{x, y})) {
                return false; // Si encuentra el elemento
            }
        }
        return true;
    }

    /**
     * Revela las celdas adyacentes cuando se encuentra un cero.
     * @param x Coordenada X de la celda seleccionada.
     * @param y Coordenada Y de la celda seleccionada.
     */
    private void vaciado(int x, int y){
        // Estructura de datos para realizar un recorrido de celdas pendientes por analizar
        pendientes.add(new int[]{x, y});
        while (!pendientes.isEmpty()) {
            int[] celda = pendientes.poll();
            assert celda != null; //asegura que celda no es null
            int i = celda[0];
            int j = celda[1];
            if (tablero[i][j] == 0) { // Si la celda no tiene minas adyacentes, revela todas contando su número de minas alrededor
                libres[i][j] = true; //se libera la celda central con el cero
                c_libres++;
                tablerousr[i][j] = String.valueOf(tablero[i][j]);
                //Y ahora se analizan todas las celdas adyacentes exceptuando la central ya liberada
                for (int k = -1; k <= 1; k++) { //revisa las celdas de alrededor si la selec. no tiene minas alrededor
                    for (int l = -1; l <= 1; l++) {
                        // Evitar agregar la celda actual y verificar límites
                        if ((k != 0 || l != 0) && (i + k >= 0 && i + k < filas) && (j + l >= 0 && j + l < columnas) && !libres[i + k][j + l]) {
                            int vecinoX = i + k;
                            int vecinoY = j + l;
                            // Solo añadir celdas que no han sido reveladas y no tienen minas
                            if (tablero[vecinoX][vecinoY] != -1) {
                                if (tablero[vecinoX][vecinoY] == 0 && verificarCola(vecinoX, vecinoY)) { //si encuentra una con cero minas alrededor, la agrega a la cola para analizar individualmente, pero no se libera
                                    pendientes.add(new int[]{vecinoX, vecinoY}); //si la celda tiene un cero no se libera y se agrega a pendientes
                                } else if (verificarCola(vecinoX, vecinoY)) { //si no es un cero simplemente se libera y sigue
                                    libres[vecinoX][vecinoY] = true; //como es una celda vecina de un 0, se libera
                                    c_libres++;
                                    tablerousr[vecinoX][vecinoY] = String.valueOf(tablero[vecinoX][vecinoY]); //solo es un tablero visual, solo si se liberan, cambia el estado
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Coloca o quita una bandera en la celda seleccionada.
     * @param x Coordenada X de la celda seleccionada.
     * @param y Coordenada Y de la celda seleccionada.
     */
    public void colocarBandera(int x, int y){
        if (totalbanderas >= minas && banderas[x][y]){ //Compruebo que se alcanzó el limite y es una bandera la celda seleccionada
            quitarBandera(x,y);
        } else if (totalbanderas >= minas) { //si se alcanzo el limite y no es una bandera
            mensaje = "Alcanzaste el limite de banderas, recuerda que tienes " + minas + " minas.\n\n";
        } else { //si aún se pueden poner banderas
            if (!libres[x][y] && !banderas[x][y]){ //se puede poner una bandera en cualquier celda no destapada
                banderas[x][y] = true; //Es una celda tapada y no tiene bandera
                totalbanderas++;
                tablerousr[x][y] = "B";
                if(tablero[x][y] == -1){
                    c_banderas++;
                }
                System.out.println("Bandera colocada.");
                mensaje = "Bandera colocada. Tienes " + (minas - totalbanderas) + " banderas disponibles.\n\n";
            }else {
                if(banderas[x][y]){
                    quitarBandera(x,y);
                }else{
                    mensaje = "Esa casilla ya fue visitada y no puede tener una bandera.\n\n";
                }
            }
        }
    }
    // Función para quitar una bandera
    /**
     * Quita una bandera de la celda especificada en las coordenadas (x, y).
     * Si hay una bandera en la celda, se retira y se actualizan los contadores
     * de banderas. También se ajusta el estado de la celda en el tablero del usuario.
     *
     * @param x Coordenada de la fila de la celda de la que se desea quitar la bandera.
     * @param y Coordenada de la columna de la celda de la que se desea quitar la bandera.
     */
    public void quitarBandera(int x, int y) {
        if (banderas[x][y]) {
            banderas[x][y] = false; // Quitar la bandera de la celda
            totalbanderas--; // Decrementar el total de banderas disponibles
            tablerousr[x][y] = null; // Actualizar el tablero del usuario
            if (tablero[x][y] == -1) { // Si había una mina
                c_banderas--; // Decrementar las banderas correctas
            }
            System.out.println("Bandera retirada."); // Mensaje en la consola
            mensaje = "Tienes " + (minas - totalbanderas) + " banderas disponibles. \uD83D\uDEA9 \uD83D\uDEA9";
        } else {
            mensaje = "Esta casilla NO tiene bandera.\n\n"; // Mensaje de error
        }
    }

    /**
     * Cuenta las minas adyacentes a cada celda del tablero.
     * Recorre cada celda y verifica sus ocho vecinos para contar cuántas minas
     * están alrededor, actualizando el valor de la celda correspondiente en el tablero.
     */
    private void contarMinasAlrededor() {
        for (int f = 0; f < filas; f++) {
            for (int c = 0; c < columnas; c++) {
                if (tablero[f][c] != -1) { // Solo contar para celdas que no son minas
                    int minas = 0; // Contador de minas adyacentes
                    for (int i = -1; i <= 1; i++) {
                        for (int j = -1; j <= 1; j++) {
                            int nuevaFila = f + i;
                            int nuevaColumna = c + j;

                            // Verificar límites del tablero
                            if (nuevaFila >= 0 && nuevaFila < filas && nuevaColumna >= 0 && nuevaColumna < columnas) {
                                if (tablero[nuevaFila][nuevaColumna] == -1) { // Contar minas
                                    minas++;
                                }
                            }
                        }
                    }
                    tablero[f][c] = minas; // Actualizar el número de minas en la celda
                }
            }
        }
    }

    /**
     * Devuelve una representación imprimible del tablero.
     *
     * @param interfaz Entero que indica el contexto de la llamada:
     *                 1 para el servidor (tablero sin tapas),
     *                 0 para el cliente (tablero con celdas destapadas).
     * @return Cadena que representa el tablero en formato imprimible.
     */
    public String obtenerTablero(int interfaz) {
        StringBuilder tableroString = new StringBuilder();

        // Encabezado de las columnas
        tableroString.append("    "); // Espacio inicial para alinear con los números de fila
        for (int j = 0; j < columnas; j++) {
            tableroString.append(String.format("%3d ", j)); // Coordenadas de columna
        }
        tableroString.append("\n");

        // Línea superior con guiones bajos alineados
        tableroString.append("    "); // Espacio inicial
        tableroString.append(" ___".repeat(Math.max(0, columnas))); // Guiones bajos
        tableroString.append("\n");

        // Imprimir el tablero con la numeración de filas
        for (int i = 0; i < filas; i++) {
            tableroString.append(String.format("%2d |", i)); // Número de fila
            for (int j = 0; j < columnas; j++) {
                if (interfaz == 1) {
                    tableroString.append(String.format("%3d ", tablero[i][j])); // Servidor
                } else {
                    // Cliente
                    if ((!banderas[i][j] && !libres[i][j] && ganador.equals("Jugando"))) {
                        tableroString.append(ANSI_CYAN).append(String.format("%3s ", "-")).append(ANSI_RESET);
                    } else if (tablero[i][j] == 0 && libres[i][j]) {
                        tableroString.append(String.format("%3s ", " "));
                    } else if (banderas[i][j]) {
                        if (ganador.equals("Loser")) {
                            if (tablero[i][j] == -1) {
                                tableroString.append(ANSI_GREEN).append(String.format("%3s ", "B")).append(ANSI_RESET);
                            } else {
                                tableroString.append(ANSI_YELL).append(String.format("%3s ", "B")).append(ANSI_RESET);
                            }
                        } else {
                            tableroString.append(ANSI_RED).append(String.format("%3s ", "B")).append(ANSI_RESET);
                        }
                    } else if (tablero[i][j] == -1 && ganador.equals("Loser")) {
                        tableroString.append(ANSI_RED).append(String.format("%3s ", "*")).append(ANSI_RESET);
                    } else {
                        tableroString.append(String.format("%3d ", tablero[i][j])); // Mostrar el número
                    }
                }
            }
            tableroString.append("\n"); // Salto de línea después de cada fila
        }

        return tableroString.toString(); // Devuelve el string del tablero
    }

    /**
     * Procesa una jugada en el tablero según la operación especificada.
     *
     * @param x Coordenada de la fila donde se realiza la jugada.
     * @param y Coordenada de la columna donde se realiza la jugada.
     * @param op Operación a realizar:
     *           0 para primer tiro,
     *           1 para revelar celdas,
     *           2 para colocar una bandera,
     *           3 para quitar una bandera.
     */
    public void procesarJugada(int x, int y, int op) {
        System.out.println("Jugada recibida: x = " + x + ", y = " + y + ", tipo = " + op);
        if (op == 0) {
            primertiro(x, y);
        } else if (op == 1) {
            revelarCeldas(x, y);
        } else if (op == 2) {
            colocarBandera(x, y);
        } else if (op == 3) {
            quitarBandera(x, y);
        }
        if (c_banderas == minas || c_libres == (getTam() - minas)) {
            setGanador("Winner");
            mensaje = "GANASTE! DESCUBRISTE TODAS LAS MINAS.\n\n";
        }
    }

    /**
     * Devuelve el estado del ganador del juego.
     *
     * @return Cadena que indica el estado del ganador ("Winner", "Loser", etc.).
     */
    public String getGanador() {
        return ganador;
    }

    /**
     * Devuelve una copia del tablero del usuario.
     *
     * @return Matriz que representa el tablero del usuario.
     */
    public String[][] getTableroUsr() {
        if (tablerousr == null) return null; // Manejo de caso nulo
        String[][] copia = new String[filas][columnas];
        for (int i = 0; i < filas; i++) {
            copia[i] = tablerousr[i].clone(); // Clonamos cada fila
        }
        return copia;
    }

    /**
     * Devuelve el mensaje actual para el usuario.
     *
     * @return Cadena que contiene el mensaje actual.
     */
    public String getMensaje() {
        return mensaje;
    }

    /**
     * Establece el estado del ganador del juego.
     *
     * @param ganador Cadena que indica el nuevo estado del ganador ("Winner", "Loser", etc.).
     */
    public void setGanador(String ganador) {
        this.ganador = ganador;
    }

    /**
     * Devuelve el tamaño total del tablero, calculado como el producto de filas y columnas.
     *
     * @return Entero que representa el tamaño total del tablero.
     */
    public int getTam() {
        return columnas * filas;
    }

}
