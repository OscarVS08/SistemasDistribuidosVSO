package com.mycompany;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.util.List;

public class Cliente {
    private final JFrame frame;
    private final JList<String> menuList;
    private final DefaultListModel<String> menuModel;
    private final JTextArea areaPedidos;
    private final JButton enviarButton;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    private final String[] platillos = {
        "Tacos al Pastor",
        "Enchiladas Verdes",
        "Pozole Rojo",
        "Burritos de Carne Asada",
        "Quesadillas de Pollo",
        "Tamales de Elote",
        "Sopes de Chicharrón",
        "Tortas de Chorizo",
        "Gorditas de Puerco",
        "Papas Fritas con Salsa"
    };

    public Cliente() {
        frame = new JFrame("Cliente - Pedido de Restaurante");
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        menuModel = new DefaultListModel<>();
        for (String platillo : platillos) {
            menuModel.addElement(platillo);
        }

        menuList = new JList<>(menuModel);
        menuList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane scrollPane = new JScrollPane(menuList);

        enviarButton = new JButton("Enviar Pedido");
        areaPedidos = new JTextArea();
        areaPedidos.setEditable(false);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(new JScrollPane(areaPedidos), BorderLayout.SOUTH);
        panel.add(enviarButton, BorderLayout.NORTH);

        frame.add(panel);
        frame.setVisible(true);

        enviarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                enviarPedido();
            }
        });

        conectarServidor();
    }

    private void conectarServidor() {
        try {
            socket = new Socket("localhost", 5000);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            System.out.println("Conexión establecida con el servidor central en el puerto: " + socket.getPort());
            
            new Thread(() -> recibirRespuestas()).start();
        } catch (IOException e) {
            areaPedidos.append("Error al conectar con el servidor\n");
            System.err.println("Error al conectar con el servidor: " + e.getMessage());
        }
    }

    private void enviarPedido() {
        List<String> selectedItems = menuList.getSelectedValuesList();
        if (selectedItems.isEmpty()) {
            areaPedidos.append("Por favor, seleccione al menos un platillo.\n");
            return;
        }

        StringBuilder pedido = new StringBuilder();
        for (String item : selectedItems) {
            pedido.append(item).append(",");
        }

        if (pedido.length() > 0) {
            pedido.setLength(pedido.length() - 1);
        }

        out.println("PEDIDO:" + pedido.toString());

        long t0 = System.currentTimeMillis();
        out.println("SYNC_TIME:" + t0);  // Para sincronización (mensaje de depuración)

        System.out.println("Pedido enviado: " + pedido.toString());
        areaPedidos.append("Pedido enviado: " + pedido.toString() + "\n");

        menuList.clearSelection();
    }

    private void recibirRespuestas() {
        try {
            String mensaje;
            while ((mensaje = in.readLine()) != null) {
                System.out.println("Respuesta recibida: " + mensaje);
                areaPedidos.append(mensaje + "\n");
            }
        } catch (IOException e) {
            System.err.println("Error al recibir mensajes: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new Cliente();
    }
}
