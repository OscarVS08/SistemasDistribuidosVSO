package restaurante;

import java.util.concurrent.*;
import java.util.*;

// Clase que representa a un chef asistente en la cocina (hilo de trabajo)
class ChefAsistente extends Thread {
    private final String nombrePlato;
    private final int tiempoPreparacion;
    private final int orden;
    private final CountDownLatch señalInicio;
    private final List<String> platosServidos;

    // Constructor
    public ChefAsistente(int orden, String nombrePlato, int tiempoPreparacion, 
                         CountDownLatch señalInicio, List<String> platosServidos) {
        this.orden = orden;
        this.nombrePlato = nombrePlato;
        this.tiempoPreparacion = tiempoPreparacion;
        this.señalInicio = señalInicio;
        this.platosServidos = platosServidos;
    }

    @Override
    public void run() {
        try {
            señalInicio.await(); // Espera a que todos los chefs estén listos
            long inicio = System.currentTimeMillis();

            System.out.println("Chef esta preparando: " + nombrePlato + " (Tiempo estimado: " + tiempoPreparacion + "s)");
            Thread.sleep(tiempoPreparacion * 1000);

            long fin = System.currentTimeMillis();
            double tiempoReal = (fin - inicio) / 1000.0;

            System.out.println("Chef ha terminado: " + nombrePlato + " en " + tiempoReal + " segundos.");
            
            // Almacena el plato con el formato "orden|nombre"
            synchronized (platosServidos) {
                platosServidos.add(orden + "|" + nombrePlato);
                platosServidos.notify(); // Despierta al chef principal
            }

        } catch (InterruptedException e) {
            System.out.println("La preparacion de " + nombrePlato + " fue interrumpida.");
        }
    }
}

// Clase principal que coordina los hilos (chef principal)
public class Restaurante {
    public static void main(String[] args) {
        System.out.println("Restaurante abierto. El chef principal coordina los pedidos.\n");

        int numChefs = 3;
        CountDownLatch señalInicio = new CountDownLatch(1); // Señal para que todos comiencen
        List<String> platosServidos = Collections.synchronizedList(new ArrayList<>()); // Lista sincronizada para los platos terminados

        // Creación de chefs asistentes (en orden de servicio)
        ChefAsistente entrada = new ChefAsistente(1, "Sopa de tomate", 2, señalInicio, platosServidos);
        ChefAsistente platoFuerte = new ChefAsistente(2, "Salmon a la plancha", 5, señalInicio, platosServidos);
        ChefAsistente postre = new ChefAsistente(3, "Pastel de chocolate", 3, señalInicio, platosServidos);

        entrada.start();
        platoFuerte.start();
        postre.start();

        System.out.println("Todos los chefs estan listos... A cocinar!!!");
        señalInicio.countDown(); // Da la señal para que comiencen

        try {
            // Espera hasta que todos los platos hayan terminado
            for (int i = 0; i < numChefs; i++) {
                synchronized (platosServidos) {
                    while (platosServidos.size() <= i) {
                        platosServidos.wait(); // Espera a que el siguiente plato se haya preparado
                    }
                }
            }

            // Servir platos en el orden de la lista, primero la sopa, luego el salmón, y por último el pastel
            platosServidos.sort(Comparator.comparingInt(o -> Integer.valueOf(o.split("\\|")[0]))); // Ordena por el primer número (orden)

            for (String plato : platosServidos) {
                String[] datos = plato.split("\\|"); // Separa orden y nombre
                System.out.println("Sirviendo: " + datos[1]);
            }

        } catch (InterruptedException e) {
            System.out.println("El chef principal fue interrumpido.");
        }

        System.out.println("\nTodos los platos han sido servidos. El restaurante ha terminado el servicio.");
    }
}


