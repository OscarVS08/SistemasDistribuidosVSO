package org.example;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Clase PantallaInicio que representa la interfaz gráfica de inicio del juego Buscaminas.
 * Permite al usuario ingresar su nombre y seleccionar el nivel de dificultad antes de comenzar el juego.
 */
public class PantallaInicio extends JFrame {
    private JTextField nombreField; // Campo para ingresar el nombre del jugador
    private JRadioButton botonFacil, botonIntermedio, botonAvanzado; // Botones para seleccionar el nivel de dificultad

    /**
     * Constructor de la clase PantallaInicio. Configura la ventana y sus componentes.
     */
    public PantallaInicio() {
        setTitle("Buscaminas - Selección de Nivel");
        setSize(400, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new GridLayout(5, 1));
        setLocationRelativeTo(null); // Centrar la ventana en la pantalla

        // Panel para el título
        JPanel panelTitulo = new JPanel();
        panelTitulo.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Margen superior de 20px
        JLabel titulo = new JLabel("Bienvenido a Buscaminas!", SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 24));
        panelTitulo.add(titulo);
        add(panelTitulo);

        // Panel para el nombre del usuario
        JPanel panelNombre = new JPanel();
        panelNombre.setBorder(BorderFactory.createEmptyBorder(20, 20, 0, 20));
        panelNombre.add(new JLabel("Jugador:"));
        nombreField = new JTextField(15); // Aumentar el ancho del campo
        nombreField.setFont(new Font("Arial", Font.PLAIN, 16)); // Aumentar tamaño del texto
        panelNombre.add(nombreField);
        add(panelNombre);

        // Panel para los botones de selección de nivel
        JPanel panelBotones = new JPanel();
        ButtonGroup grupoNiveles = new ButtonGroup(); // Grupo para los botones de opción

        // Crear botones de selección de nivel
        botonFacil = new JRadioButton("Fácil");
        botonIntermedio = new JRadioButton("Intermedio");
        botonAvanzado = new JRadioButton("Avanzado");

        // Aumentar el tamaño de texto de los botones
        botonFacil.setFont(new Font("Arial", Font.PLAIN, 18));
        botonIntermedio.setFont(new Font("Arial", Font.PLAIN, 18));
        botonAvanzado.setFont(new Font("Arial", Font.PLAIN, 18));

        // Ajustar el borde de los radio buttons
        botonFacil.setBorder(new EmptyBorder(5, 10, 5, 10));
        botonIntermedio.setBorder(new EmptyBorder(5, 10, 5, 10));
        botonAvanzado.setBorder(new EmptyBorder(5, 10, 5, 10));

        // Añadir los botones al grupo
        grupoNiveles.add(botonFacil);
        grupoNiveles.add(botonIntermedio);
        grupoNiveles.add(botonAvanzado);

        // Añadir los botones al panel
        panelBotones.add(botonFacil);
        panelBotones.add(botonIntermedio);
        panelBotones.add(botonAvanzado);
        add(panelBotones);

        // Botón para empezar el juego
        JButton botonEmpezar = new JButton("Empezar Juego");
        botonEmpezar.setFont(new Font("Arial", Font.PLAIN, 16)); // Aumentar tamaño del texto del botón
        botonEmpezar.setMargin(new Insets(10, 20, 10, 20)); // Ajustar márgenes del botón
        botonEmpezar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                empezarJuego(); // Llamar al método para iniciar el juego
            }
        });

        // Añadir el botón al panel con márgenes
        JPanel panelBotonEmpezar = new JPanel();
        panelBotonEmpezar.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20)); // Margen de 20px
        panelBotonEmpezar.add(botonEmpezar);
        add(panelBotonEmpezar);
    }

    /**
     * Método para empezar el juego. Valida la entrada del usuario y llama a la función para iniciar el cliente.
     */
    private void empezarJuego() {
        String nombreUsuario = nombreField.getText().trim();
        if (nombreUsuario.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor ingrese su nombre.");
            return; // Salir del método si no hay nombre
        }

        // Determinar el nivel seleccionado
        int nivelSeleccionado = -1;
        if (botonFacil.isSelected()) {
            nivelSeleccionado = 1; // Nivel fácil
        } else if (botonIntermedio.isSelected()) {
            nivelSeleccionado = 2; // Nivel intermedio
        } else if (botonAvanzado.isSelected()) {
            nivelSeleccionado = 3; // Nivel avanzado
        } else {
            JOptionPane.showMessageDialog(this, "Por favor seleccione un nivel.");
            return; // Salir del método si no hay nivel seleccionado
        }

        // Iniciar cliente con los datos ingresados
        Cliente.iniciarJuego(nombreUsuario, nivelSeleccionado);
        dispose();  // Cerrar la ventana de inicio
    }
}
