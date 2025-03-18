package com.mycompany;

import java.io.*;
import java.net.*;

public class ServidorRestaurante {
    private static final int PUERTO = 5000;  // Puerto del servidor de restaurante

    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", PUERTO)) {
            System.out.println("Conexión establecida con el servidor central.");

            // Flujo de entrada y salida para comunicarse con el servidor central
            BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter salida = new PrintWriter(socket.getOutputStream(), true);

            // Escuchar mensajes del servidor central
            String mensaje;
            while ((mensaje = entrada.readLine()) != null) {
                if (mensaje.startsWith("PEDIDO:")) {
                    // Recibir el pedido y procesarlo
                    String platillos = mensaje.split(":")[1];
                    System.out.println("Pedido recibido en el restaurante: " + platillos);
                    // Aquí podrías procesar el pedido, pero por ahora solo lo imprimimos
                } 
                else if (mensaje.startsWith("SYNC_RESPONSE:")) {
                    // Sincronización de tiempo
                    String[] partes = mensaje.split(":");
                    long t1 = Long.parseLong(partes[1]);
                    long t0 = Long.parseLong(partes[2]);
                    long tiempoTranscurrido = t1 - t0;
                    System.out.println("Sincronización completada. Latencia estimada: " + tiempoTranscurrido + " ms");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
