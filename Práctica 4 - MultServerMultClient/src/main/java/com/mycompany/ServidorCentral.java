package com.mycompany;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class ServidorCentral {
    private static final int PUERTO = 5000;
    private static ExecutorService pool;
    private static final Map<Socket, PrintWriter> salidaRestaurantes = new HashMap<>();
    private static final Map<Socket, BufferedReader> entradaRestaurantes = new HashMap<>();
    private static final int MAX_SERVIDORES = 10;  // Tamaño del pool de servidores
    private static int pedidoIdCounter = 0;  // Contador de identificadores de pedidos

    public static void main(String[] args) {
        pool = Executors.newFixedThreadPool(MAX_SERVIDORES);  // Crear un pool de hilos

        try (ServerSocket serverSocket = new ServerSocket(PUERTO)) {
            System.out.println("Servidor central en funcionamiento...");

            while (true) {
                Socket socket = serverSocket.accept();
                salidaRestaurantes.put(socket, new PrintWriter(socket.getOutputStream(), true));
                entradaRestaurantes.put(socket, new BufferedReader(new InputStreamReader(socket.getInputStream())));

                // Crear un hilo para manejar la comunicación con el cliente
                pool.submit(new ClienteHandler(socket));
                System.out.println("Nuevo cliente conectado: " + socket.getRemoteSocketAddress());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClienteHandler implements Runnable {
        private final Socket socketCliente;
        private final int pedidoId;

        public ClienteHandler(Socket socketCliente) {
            this.socketCliente = socketCliente;
            synchronized (ServidorCentral.class) {
                this.pedidoId = ++pedidoIdCounter;  // Generar un identificador único para el pedido
            }
        }

        @Override
        public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(socketCliente.getInputStream()));
                PrintWriter out = new PrintWriter(socketCliente.getOutputStream(), true);
                String pedido;

                // Sincronización de tiempo
                while ((pedido = in.readLine()) != null) {
                    if (pedido.startsWith("PEDIDO:")) {
                        String platillos = pedido.split(":")[1];
                        distribuirPedido(platillos);
                        out.println("Pedido " + pedidoId + " recibido y en proceso.");
                    } else if (pedido.startsWith("SYNC_TIME:")) {
                        long t0 = Long.parseLong(pedido.split(":")[1]);
                        long t1 = System.currentTimeMillis();
                        sincronizarTiempo(t0, t1);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void distribuirPedido(String platillos) {
            // Aquí se realiza la lógica de distribución de los pedidos a los servidores (simulado)
            for (Socket servidor : salidaRestaurantes.keySet()) {
                pool.submit(() -> {
                    PrintWriter salida = salidaRestaurantes.get(servidor);
                    salida.println("PEDIDO:" + platillos);
                    System.out.println("Pedido " + pedidoId + " enviado al servidor de restaurante: " + platillos);
                });
            }
        }

        private void sincronizarTiempo(long t0, long t1) {
            // Enviar la sincronización a los servidores de restaurantes
            for (Socket servidor : salidaRestaurantes.keySet()) {
                PrintWriter salida = salidaRestaurantes.get(servidor);
                salida.println("SYNC_RESPONSE:" + t1 + ":" + t0);
                System.out.println("Sincronización enviada al servidor de restaurante.");
            }
        }
    }
}
