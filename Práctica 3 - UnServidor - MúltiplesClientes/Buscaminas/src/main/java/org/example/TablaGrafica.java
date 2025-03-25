package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;

/**
 * La clase TablaGrafica representa la interfaz gráfica del tablero del juego de Buscaminas.
 * Extiende JFrame y contiene componentes para mostrar el tablero y manejar la interacción del usuario.
 */

public class TablaGrafica extends JFrame {
    private String[][] tablero; // Matriz que representa el estado del tablero
    private final JButton[][] botones; // Botones que representan cada celda del tablero
    JTextArea mensajeInferior; // Área de texto para mostrar mensajes al usuario
    JButton botonMostrarMensaje; // Botón para mostrar un mensaje adicional
    JButton botonAgain;
    int filas; // Número de filas en el tablero
    int columnas; // Número de columnas en el tablero
    private CeldaClickListener celdaClickListener; // Listener para manejar clics en las celdas

    // Constructor
    /**
     * Constructor de la clase TablaGrafica.
     *
     * @param tablero     Matriz que representa el estado inicial del tablero.
     * @param tamanoCelda Tamaño de cada celda en píxeles.
     */
    public TablaGrafica(String[][] tablero, int tamanoCelda) {
        this.tablero = tablero;
        this.filas = tablero.length;
        this.columnas = tablero[0].length;

        setTitle("Tablero de Buscaminas");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Crear un panel principal con BoxLayout para alinear el tablero y el mensaje inferior verticalmente
        JPanel panelPrincipal = new JPanel();
        panelPrincipal.setLayout(new BoxLayout(panelPrincipal, BoxLayout.Y_AXIS)); // Disposición vertical
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Margen de 20 píxeles

        // Crear un panel para el título
        JPanel panelTitulo = new JPanel();
        JLabel titulo = new JLabel("Buscaminas", SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 24));
        panelTitulo.add(titulo);
        panelPrincipal.add(panelTitulo); // Añadir el título al panel principal

        // Crear un panel para el tablero
        JPanel panelTablero = new JPanel(new GridLayout(filas, columnas));
        botones = new JButton[filas][columnas]; // Inicializar el arreglo de botones
        inicializarTablero(panelTablero, tamanoCelda); // Inicializar el tablero

        // Alinear el panel del tablero al centro horizontalmente
        panelTablero.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelPrincipal.add(panelTablero); // Añadir el tablero al panel principal

        // Crear y agregar el mensaje en la parte inferior
        mensajeInferior = new JTextArea("¡Haz clic en una celda para actualizar el valor o colocar/quitar una bandera!\n\n");
        mensajeInferior.setFont(new Font("Arial", Font.PLAIN, 12));
        mensajeInferior.setLineWrap(true);  // Ajustar el texto en varias líneas
        mensajeInferior.setWrapStyleWord(true);  // Asegurarse de que el ajuste sea entre palabras
        mensajeInferior.setEditable(false);  // No editable
        mensajeInferior.setOpaque(false);  // Fondo transparente
        mensajeInferior.setMaximumSize(new Dimension(panelTablero.getPreferredSize()));  // Igualar tamaño máximo al del tablero
        mensajeInferior.setAlignmentX(Component.CENTER_ALIGNMENT);  // Alinear el mensaje al centro
        mensajeInferior.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

        // Añadir el mensaje al panel principal, justo debajo del tablero
        panelPrincipal.add(mensajeInferior);

        //JPanel para ambos botones
        JPanel panelBotones = new JPanel(new GridLayout(1,2));
        // Botón para mostrar el mensaje largo
        botonMostrarMensaje = new JButton("Mostrar Records");
        botonMostrarMensaje.setBackground(new Color(0xcedfe4));
        botonMostrarMensaje.setForeground(new Color(0x313334));
        botonMostrarMensaje.setAlignmentX(Component.CENTER_ALIGNMENT);
        botonMostrarMensaje.setEnabled(false); // Inicialmente oculto
        botonMostrarMensaje.addActionListener(e -> {
            // Aquí puedes cambiar esto a un JOptionPane o otro método para mostrar el mensaje
            String mensajeLargo = e.getActionCommand();
            JOptionPane.showMessageDialog(this, mensajeLargo, "Records para tu dificultad", JOptionPane.INFORMATION_MESSAGE);
        });

        // Añadir un borde al botón para que tenga espacio
        panelBotones.add(botonMostrarMensaje); // Añadir el botón al panel

        // Botón para VOLVER A JUGAR
        botonAgain = new JButton("Volver a jugar");
        botonAgain.setBackground(new Color(0xcedfe4)); // Establecer el color de fondo
        botonAgain.setForeground(new Color(0x313334)); // Establecer el color del texto
        botonAgain.setAlignmentX(Component.CENTER_ALIGNMENT);
        botonAgain.setEnabled(false); // Inicialmente oculto
        botonAgain.setVisible(false);
        botonAgain.addActionListener(e -> {
            // Cierra la ventana actual
            this.dispose(); // Cierra la ventana de la GUI actual
            limpiarConsola(); //Simular que se limpia la consola
            // Reinicia el juego invocando Cliente.iniciarJuego() con nuevos parámetros
            SwingUtilities.invokeLater(() -> {
                new PantallaInicio().setVisible(true); // O puedes manejar la creación de una nueva partida con Cliente.iniciarJuego()
            });
        });


        // Añadir un borde al botón para que tenga espacio
        panelBotones.add(botonAgain); // Añadir el botón al panel
        // Alinear el panel de los botones centro horizontalmente
        panelBotones.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelPrincipal.add(panelBotones); // Añadir el tablero al panel principal

        // Añadir el panel principal a la ventana
        add(panelPrincipal);

        // Ajustar el tamaño de la ventana para que se ajuste a los componentes
        pack();
        setSize(Math.max(400, getWidth()), Math.max(400, getHeight())); // Asegurar un tamaño mínimo
        setLocationRelativeTo(null);  // Centrar la ventana en la pantalla
    }

    // Método para inicializar el tablero
    /**
     * Inicializa el tablero gráfico y sus botones.
     *
     * @param panelTablero Panel donde se colocan los botones del tablero.
     * @param tamanoCelda  Tamaño de cada celda en píxeles.
     */
    private void inicializarTablero(JPanel panelTablero, int tamanoCelda) {
        System.out.println(" ------     --- --- --- INICIALIZA TABLERO ****");
        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                final int fila = i; // Hacemos que 'fila' sea final
                final int columna = j; // Hacemos que 'columna' sea final
                botones[fila][columna] = new JButton(); // Crear botón
                botones[fila][columna].setPreferredSize(new Dimension(tamanoCelda, tamanoCelda));
                botones[fila][columna].setBackground(Color.LIGHT_GRAY);
                botones[fila][columna].setOpaque(true);
                // Aplicar un borde con márgenes
                botones[fila][columna].setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createEmptyBorder(2, 2, 2, 2), // Margen
                        BorderFactory.createLineBorder(Color.BLACK) // Borde
                ));

                // Agregar un MouseListener para detectar clics
                botones[fila][columna].addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (celdaClickListener != null) {
                            celdaClickListener.onCeldaClick(fila, columna, SwingUtilities.isRightMouseButton(e));
                        }
                    }
                });

                panelTablero.add(botones[fila][columna]); // Añadir celda al panel del tablero
            }
        }
        // Actualizar el tablero visualmente
        actualizarTablero(tablero,"¡Haz clic en una celda para actualizar el valor o colocar/quitar una bandera!\n\n");
    }

    // Método para actualizar el tablero visualmente
    /**
     * Actualiza el estado visual del tablero y muestra un mensaje.
     *
     * @param nuevoTablero Matriz que representa el nuevo estado del tablero.
     * @param mensaje      Mensaje a mostrar en el área de texto inferior.
     */
    public void actualizarTablero(String[][] nuevoTablero, String mensaje) {
        this.tablero = nuevoTablero;
        for (int i = 0; i < tablero.length; i++) {
            for (int j = 0; j < tablero[i].length; j++) {
                JButton celda = botones[i][j]; // Acceder al botón directamente
                // Personalizar el color o el texto según el estado de la celda
                if (Objects.equals(tablero[i][j], "-1")) { // -1 indica mina (MUESTRA LA UBICACIÓN)
                    celda.setText("*");
                    celda.setBackground(Color.RED);
                    celda.setForeground(Color.BLACK);
                } else if (Objects.equals(tablero[i][j], "*")) { // Celda con una mina pisada
                    celda.setText("*");
                    celda.setBackground(Color.pink);
                    celda.setForeground(Color.RED); // Color del texto rojo para contraste
                } else if (Objects.equals(tablero[i][j], "0")) { // Celdas vacías descubiertas
                    celda.setText("");
                    celda.setBackground(Color.WHITE);
                } else if (tablero[i][j] == null) { // Celdas no descubiertas
                    celda.setText("");
                    celda.setBackground(new Color(0xb3b8b5));
                } else if (Objects.equals(tablero[i][j], "B")) { // Celdas con bandera bien posicionada
                    celda.setText("B");
                    celda.setForeground(Color.RED); // Color del texto blanco para contraste
                    celda.setBackground(new Color(0xaee6f5));
                } else if (Objects.equals(tablero[i][j], "x")) { // Celdas con bandera mal posicionada
                    celda.setText("B");
                    celda.setForeground(Color.RED); // Color del texto rojo para contraste
                    celda.setBackground(Color.ORANGE);
                } else if (Objects.equals(tablero[i][j], "2")) {
                    celda.setText(String.valueOf(tablero[i][j]));
                    celda.setForeground(new Color(0x119b0c));
                    celda.setBackground(Color.WHITE);
                } else if (Objects.equals(tablero[i][j], "3")) {
                    celda.setText(String.valueOf(tablero[i][j]));
                    celda.setForeground(new Color(0xd10404));
                    celda.setBackground(Color.WHITE);
                } else if (Objects.equals(tablero[i][j], "4")) {
                    celda.setText(String.valueOf(tablero[i][j]));
                    celda.setForeground(new Color(0xba0afc));
                    celda.setBackground(Color.WHITE);
                } else if (Objects.equals(tablero[i][j], "5")) {
                    celda.setText(String.valueOf(tablero[i][j]));
                    celda.setForeground(new Color(0x6a1811));
                    celda.setBackground(Color.WHITE);
                } else if (Objects.equals(tablero[i][j], "6")) {
                    celda.setText(String.valueOf(tablero[i][j]));
                    celda.setForeground(new Color(0xfccd0a));
                    celda.setBackground(Color.WHITE);
                } else if (Objects.equals(tablero[i][j], "7")) {
                    celda.setText(String.valueOf(tablero[i][j]));
                    celda.setForeground(new Color(0x584c56));
                    celda.setBackground(Color.WHITE);
                } else if (Objects.equals(tablero[i][j], "8")) {
                    celda.setText(String.valueOf(tablero[i][j]));
                    celda.setForeground(new Color(0xfc0a7f));
                    celda.setBackground(Color.WHITE);
                } else {
                    celda.setText(String.valueOf(tablero[i][j])); // Mostrar el número
                    celda.setForeground(new Color(0x0008ff));
                    celda.setBackground(Color.WHITE);
                }
            }
        }
        // Actualizar el mensaje en la parte inferior
        if (mensajeInferior != null) {
            mensajeInferior.setText(mensaje);  // Cambiar el texto del JTextArea
        }
    }

    // Método para establecer el listener de clic en las celdas
    /**
     * Establece el listener para manejar los clics en las celdas del tablero.
     *
     * @param listener Instancia del listener que se encargará de los clics.
     */
    public void setCeldaClickListener(CeldaClickListener listener) {
        this.celdaClickListener = listener;
    }

    // Interfaz para manejar los clics en las celdas
    public interface CeldaClickListener {
        void onCeldaClick(int x, int y, boolean botonDerecho);
    }

    // Método para mostrar un mensaje
    /**
     * Muestra un mensaje en el área de texto inferior.
     *
     * @param mensaje Mensaje a mostrar.
     */
    public void mostrarMensaje(String mensaje) {
        mensajeInferior.setText(mensaje); // Actualiza el área de texto con el mensaje
    }

    // Método para habilitar el botón que muestra el mensaje
    /**
     * Habilita el botón de mostrar mensaje y establece su texto.
     *
     * @param mensaje Mensaje que se mostrará cuando se haga clic en el botón.
     */
    public void habilitarBotones(String mensaje) {
        botonMostrarMensaje.setActionCommand(mensaje); // Establecer el mensaje largo como comando de acción
        botonMostrarMensaje.setEnabled(true); // Hacer el botón visible
        botonAgain.setActionCommand(mensaje); // Establecer el mensaje largo como comando de acción
        botonAgain.setEnabled(true);
        botonAgain.setVisible(true);
    }

    public void limpiarConsola() {
        for (int i = 0; i < 50; i++) { // Ajusta el número según lo que necesites
            System.out.println();
        }
    }


}
