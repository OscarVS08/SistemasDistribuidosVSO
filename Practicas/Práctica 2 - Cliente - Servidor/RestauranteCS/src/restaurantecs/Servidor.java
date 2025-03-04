package restaurantecs;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.*;

public class Servidor {
    private static final int PUERTO = 1234; // Puerto del servidor
    private static final List<String> platosServidos = Collections.synchronizedList(new ArrayList<>()); // Lista sincronizada de platos servidos
    private static final CountDownLatch señalInicio = new CountDownLatch(1); // Latch para sincronizar el inicio de los chefs

    public static void main(String[] args) {
        System.out.println("Restaurante abierto. El chef principal coordina los pedidos.\n");

        try (ServerSocket serverSocket = new ServerSocket(PUERTO)) {
            System.out.println("Servidor esperando conexion...");
            
            // El servidor espera conexiones de clientes de manera continua
            while (true) {
                // Aceptar una conexión de cliente
                Socket clienteSocket = serverSocket.accept();
                System.out.println("Cliente conectado desde: " + clienteSocket.getInetAddress() + ":" + clienteSocket.getPort());

                // Crear un hilo para manejar la comunicación con el cliente
                new Thread(new ClienteHandler(clienteSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Clase para manejar las solicitudes de un cliente
    static class ClienteHandler implements Runnable {
        private final Socket clienteSocket;

        public ClienteHandler(Socket socket) {
            this.clienteSocket = socket;
        }

        @Override
        public void run() {
            try (
                PrintWriter output = new PrintWriter(clienteSocket.getOutputStream(), true);
                BufferedReader input = new BufferedReader(new InputStreamReader(clienteSocket.getInputStream()))
            ) {
                // Reiniciar la lista de platos servidos antes de cada cliente
                synchronized (platosServidos) {
                    platosServidos.clear();
                }

                // Enviar inicio del servicio y opciones del menú
                output.println("Seleccione una opcion del menu:" +
                                "Opcion 1: Sopa de tomate, Salmon a la plancha, Pastel de chocolate" +
                                "Opcion 2: Sopa de verduras, Filete empanizado, Fresas con crema" +
                                "Opcion 3: Ensalada rusa, Filete relleno, Flan napolitano");
                
                // Leer la opción seleccionada por el cliente
                String opcion = input.readLine();
                System.out.println("Opcion seleccionada: " + opcion);

                // Enviar mensaje de inicio
                output.println("Todos los chefs estan listos... A cocinar!!!");
                System.out.println("Mensaje enviado al cliente: 'Todos los chefs estan listos... A cocinar!!!'");

                // Crear los chefs y asignarles los platos dependiendo de la opción seleccionada
                ChefAsistente entrada = null;
                ChefAsistente platoFuerte = null;
                ChefAsistente postre = null;

                // Asignación de platos según la opción seleccionada
                switch (opcion) {
                    case "1" -> {
                        entrada = new ChefAsistente(1, "Sopa de tomate", 2, señalInicio, platosServidos, output);
                        platoFuerte = new ChefAsistente(2, "Salmon a la plancha", 5, señalInicio, platosServidos, output);
                        postre = new ChefAsistente(3, "Pastel de chocolate", 3, señalInicio, platosServidos, output);
                    }
                    case "2" -> {
                        entrada = new ChefAsistente(1, "Sopa de verduras", 3, señalInicio, platosServidos, output);
                        platoFuerte = new ChefAsistente(2, "Filete empanizado", 6, señalInicio, platosServidos, output);
                        postre = new ChefAsistente(3, "Fresas con crema", 2, señalInicio, platosServidos, output);
                    }
                    case "3" -> {
                        entrada = new ChefAsistente(1, "Ensalada rusa", 4, señalInicio, platosServidos, output);
                        platoFuerte = new ChefAsistente(2, "Filete relleno", 7, señalInicio, platosServidos, output);
                        postre = new ChefAsistente(3, "Flan napolitano", 5, señalInicio, platosServidos, output);
                    }
                    default -> {
                        output.println("Opcion no válida. Seleccione entre 1, 2 o 3.");
                        return;
                    }
                }

                // Iniciar los hilos de los chefs
                entrada.start();
                platoFuerte.start();
                postre.start();

                // Comenzar el conteo para que todos los chefs empiecen simultáneamente
                señalInicio.countDown();

                // Esperar hasta que todos los platos hayan sido servidos
                synchronized (platosServidos) {
                    while (platosServidos.size() < 3) {
                        platosServidos.wait(); // Espera mientras los platos son servidos
                    }
                }

                // Responder con los platos listos en orden
                platosServidos.sort(Comparator.comparingInt(o -> Integer.valueOf(o.split("\\|")[0])));
                for (String plato : platosServidos) {
                    String[] datos = plato.split("\\|");
                    output.println("Sirviendo: " + datos[1]);
                    System.out.println("Mensaje enviado al cliente: 'Sirviendo: " + datos[1] + "'");
                }
                output.println("\nTodos los platos han sido servidos. El restaurante ha terminado el servicio.");
                System.out.println("Mensaje enviado al cliente: 'Todos los platos han sido servidos. El restaurante ha terminado el servicio.'");
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            } finally {
                try {
                    clienteSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Clase para representar a los chefs que preparan los platos
    static class ChefAsistente extends Thread {
        private final String nombrePlato;
        private final int tiempoPreparacion;
        private final int orden;
        private final CountDownLatch señalInicio;
        private final List<String> platosServidos;
        private final PrintWriter output;

        public ChefAsistente(int orden, String nombrePlato, int tiempoPreparacion, 
                             CountDownLatch señalInicio, List<String> platosServidos, PrintWriter output) {
            this.orden = orden;
            this.nombrePlato = nombrePlato;
            this.tiempoPreparacion = tiempoPreparacion;
            this.señalInicio = señalInicio;
            this.platosServidos = platosServidos;
            this.output = output;
        }

        @Override
        public void run() {
            try {
                // Esperar a que todos los chefs estén listos
                señalInicio.await();

                long inicio = System.currentTimeMillis();
                System.out.println("Chef comenzando a preparar: " + nombrePlato);
                output.println("Chef esta preparando: " + nombrePlato + " (Tiempo estimado: " + tiempoPreparacion + "s)");

                // Simular el tiempo de preparación del plato
                Thread.sleep(tiempoPreparacion * 1000);

                long fin = System.currentTimeMillis();
                double tiempoReal = (fin - inicio) / 1000.0;

                // Informar que el chef ha terminado de preparar el plato
                System.out.println("Chef ha terminado de preparar: " + nombrePlato);
                output.println("Chef ha terminado: " + nombrePlato + " en " + tiempoReal + " segundos.");

                // Añadir el plato a la lista de platos servidos
                synchronized (platosServidos) {
                    platosServidos.add(orden + "|" + nombrePlato);
                    platosServidos.notify(); // Despertar a los demás chefs
                }
            } catch (InterruptedException e) {
                output.println("La preparacion fue interrumpida.");
            }
        }
    }
}
