package restaurantecs;

import java.io.*;
import java.net.*;

public class Cliente {
    private static final String DIRECCION_SERVIDOR = "localhost";
    private static final int PUERTO = 1234;

    public static void main(String[] args) {
        try (Socket socket = new Socket(DIRECCION_SERVIDOR, PUERTO);
             BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
             PrintWriter output = new PrintWriter(socket.getOutputStream(), true)) {

            System.out.println("Cliente conectado al servidor en " + DIRECCION_SERVIDOR + ":" + PUERTO);

            // Leer el mensaje de opciones del servidor
            String mensaje;
            while ((mensaje = input.readLine()) != null) {
                System.out.println(mensaje);

                if (mensaje.contains("Seleccione una opcion")) {
                    System.out.print("Ingrese el numero de la opcion (1, 2 o 3): ");
                    String opcion = userInput.readLine();
                    output.println(opcion);  // Enviar la opción seleccionada al servidor
                }
            }

            // Leer el resto de los mensajes del servidor
            while ((mensaje = input.readLine()) != null) {
                System.out.println("Mensaje recibido del servidor: " + mensaje);
            }
        } catch (IOException e) {
        }
    }
}