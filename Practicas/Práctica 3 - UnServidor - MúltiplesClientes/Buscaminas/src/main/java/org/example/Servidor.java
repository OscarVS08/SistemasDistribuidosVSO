package org.example;

import java.io.*;
import java.net.*;
import java.nio.channels.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.lang.*;
import java.util.logging.*;

public class Servidor {
    private static final Logger logger = Logger.getLogger(Servidor.class.getName());
    private static final int MAX_RECORDS = 10;
    private static final int PUERTO = 1234;

    private static final Set<SocketChannel> clientesConectados = new HashSet<>();
    private static final Map<SocketChannel, ClientInfo> infoClientes = new HashMap<>();

    private static Selector selector;

    public static void main(String[] args) {
        try {
            // Inicializa el selector
            selector = Selector.open();

            // Configura el ServerSocketChannel
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(PUERTO));
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            logger.info("Servidor iniciado en el puerto " + PUERTO);

            while (true) {
                // Espera hasta que haya algo para procesar
                int readyChannels = selector.select();
                if (readyChannels == 0) continue;

                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

                while (keys.hasNext()) {
                    SelectionKey key = keys.next();
                    keys.remove();

                    try {
                        if (key.isAcceptable()) {
                            aceptarConexion(serverSocketChannel);
                        } else if (key.isReadable()) {
                            procesarCliente(key);
                        }
                    } catch (IOException e) {
                        logger.log(Level.SEVERE, "Error al manejar una clave de selección", e);
                        key.cancel();
                        if (key.channel() != null) {
                            key.channel().close();
                        }
                    }
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error crítico en el servidor", e);
        }
    }

    private static void aceptarConexion(ServerSocketChannel serverSocketChannel) throws IOException {
        SocketChannel clientChannel = serverSocketChannel.accept();
        if (clientChannel != null) {
            clientChannel.configureBlocking(false);
            clientChannel.register(selector, SelectionKey.OP_READ);
            clientesConectados.add(clientChannel);
            // Agrega el cliente al mapa con datos iniciales
            infoClientes.put(clientChannel, new ClientInfo("Desconocido", -1, 0, null, 0));

            logger.info("Cliente conectado desde " + clientChannel.getRemoteAddress());
        }
    }


    private static void procesarCliente(SelectionKey key) {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        try {
            // Leer los primeros 4 bytes que contienen la longitud del objeto
            ByteBuffer lengthBuffer = ByteBuffer.allocate(4);
            int bytesRead = clientChannel.read(lengthBuffer);

            if (bytesRead == -1) {
                logger.info("Cliente desconectado: " + clientChannel.getRemoteAddress());
                clientesConectados.remove(clientChannel);
                infoClientes.remove(clientChannel);
                clientChannel.close();
                return;
            }

            if (bytesRead < 4) {
                logger.warning("No se recibió suficiente información para determinar la longitud.");
                return; // Asegurarse de recibir al menos 4 bytes
            }

            lengthBuffer.flip();
            int objectLength = lengthBuffer.getInt(); // Obtener la longitud del objeto
            if (objectLength < 0) {
                logger.warning("Invalid size.");
                return;
            }
            // Leer el objeto serializado basado en la longitud
            ByteBuffer objectBuffer = ByteBuffer.allocate(objectLength);
            while (objectBuffer.hasRemaining()) {
                clientChannel.read(objectBuffer);
            }

            objectBuffer.flip();
            byte[] objectBytes = new byte[objectBuffer.remaining()];
            objectBuffer.get(objectBytes);

            // Deserializar el objeto
            ByteArrayInputStream byteInputStream = new ByteArrayInputStream(objectBytes);
            ObjectInputStream objectInputStream = new ObjectInputStream(byteInputStream);
            Object objetoRecibido = objectInputStream.readObject();

            //logger.log(Level.INFO, "Objeto recibido de {0}: {1}", new Object[]{clientChannel.getRemoteAddress(), objetoRecibido});
            // Declaración de las variables
            // Verifica el tipo de objeto recibido
            if (objetoRecibido instanceof String nombreUsuario) {

                // Asignar el valor a la variable
                infoClientes.get(clientChannel).setNombre(nombreUsuario);
                logger.log(Level.INFO, "Nombre del usuario recibido: {0} ", nombreUsuario);
                logger.log(Level.INFO, "Channel: {0}", clientChannel);

            } else if (objetoRecibido instanceof Integer) {

                int nivel = (Integer) objetoRecibido; // Aquí defines la variable nivel
                infoClientes.get(clientChannel).setNivel(nivel);
                // Aquí defines la variable nivel
                logger.log(Level.INFO, "Nivel recibido: {0}", nivel);

                // Crear el objeto Tablero con el nivel
                Tablero t = new Tablero(nivel);
                Tablerousr tablerousr = new Tablerousr(t.getGanador(), t.getTableroUsr(), t.getMensaje());
                System.out.print(t.obtenerTablero(1)); // obtiene el primer tablero vacio
                System.out.println("Objeto enviado : " + tablerousr.getEstado() + Arrays.deepToString(tablerousr.getTab()));
                System.out.println("Enviando el tablero al cliente...");
                logger.log(Level.INFO, "Channel: {0}", clientChannel);
                long startTime = System.currentTimeMillis(); // Inicia marca de tiempo
                infoClientes.get(clientChannel).setStartTime(startTime);
                enviarTablero(clientChannel, tablerousr);
                infoClientes.get(clientChannel).setTablero(t);

            } else if (objetoRecibido instanceof Jugada) {

                int indexJugada = infoClientes.get(clientChannel).getIndexJugada();
                Tablero t = infoClientes.get(clientChannel).getTablero();
                String nombreUsuario = infoClientes.get(clientChannel).getNombre();
                int nivel = infoClientes.get(clientChannel).getNivel();
                long startTime = infoClientes.get(clientChannel).getStartTime();
                Tablerousr tabusr;
                if (Objects.equals(t.getGanador(), "Jugando")) {
                    Jugada jugada = (Jugada) objetoRecibido;
                    if (indexJugada != 0) {
                        t.procesarJugada(jugada.getX(), jugada.getY(), jugada.getOption());
                    } else {
                        t.procesarJugada(jugada.getX(), jugada.getY(), 0);
                        infoClientes.get(clientChannel).setIndexJugada(indexJugada + 1);
                    }
                    tabusr = new Tablerousr(t.getGanador(), t.getTableroUsr(), t.getMensaje());
                    System.out.println("Enviando el tablero al cliente...");
                    logger.log(Level.INFO, "Channel: {0}", clientChannel);
                    enviarTablero(clientChannel, tabusr);
                    infoClientes.get(clientChannel).setTablero(t);
                }

                // Verificar si el juego ha terminado
                if (!Objects.equals(t.getGanador(), "Jugando")) {
                    // Guardar duración y estado final del juego
                    long endTime = System.currentTimeMillis();
                    long duration = endTime - startTime;
                    long minutes = (duration / 1000) / 60;
                    long seconds = (duration / 1000) % 60;
                    String marcatiempo = minutes + ":" + seconds;


                    if (Objects.equals(t.getGanador(), "Winner")) {
                        guardarEnArchivo(marcatiempo, nombreUsuario, nivel);
                    }

                    // Finalizar partida
                    enviarMensajeFinal(clientChannel, "Duración: " + minutes + " minutos y " + seconds + " segundos.\n\n");
                    enviarRecords(clientChannel, nivel);
                    try {
                        clientesConectados.remove(clientChannel);
                        infoClientes.remove(clientChannel);
                        clientChannel.close();
                    } catch (IOException ex) {
                        logger.log(Level.SEVERE, "Error al cerrar la conexión con el cliente", ex);
                    }
                }

            }
        } catch (IOException | ClassNotFoundException e) {
            logger.log(Level.WARNING, "Error al procesar cliente: " + clientChannel, e);
            try {
                clientesConectados.remove(clientChannel);
                infoClientes.remove(clientChannel);
                clientChannel.close();
            } catch (IOException ex) {
                logger.log(Level.SEVERE, "Error al cerrar la conexión con el cliente", ex);
            }
        }
    }

    /**
     * Método para guardar registros en el archivo, ordenándolos y limitándolos a 10.
     *
     * @param marcatiempo El tiempo que duró la partida en formato [minutos:segundos].
     * @param nombreUsuario El nombre del usuario que jugó.
     * @param nivel El nivel de dificultad del juego.
     */
    private static void guardarEnArchivo(String marcatiempo, String nombreUsuario, int nivel) {
        // Leer los registros actuales del archivo
        List<String> records = leerRecords(nivel);

        // Agregar el nuevo record al final de la lista
        records.add(marcatiempo + " - - - " + nombreUsuario);

        // Ordenar los records por tiempo
        records.sort((record1, record2) -> {
            int[] tiempo1 = extraerTiempo(record1); // Extraemos tiempo del primer registro
            int[] tiempo2 = extraerTiempo(record2); // Extraemos tiempo del segundo registro

            // Comparar minutos; si son diferentes, usamos esa comparación
            if (tiempo1[0] != tiempo2[0]) {
                return Integer.compare(tiempo1[0], tiempo2[0]); // Ordenar por minutos
            } else {
                return Integer.compare(tiempo1[1], tiempo2[1]); // Si los minutos son iguales, ordenar por segundos
            }
        });

        // Si hay más de 10 registros, limitamos la lista a los 10 mejores
        if (records.size() > MAX_RECORDS) {
            records = records.subList(0, MAX_RECORDS); // Mantener solo los primeros 10 registros
        }

        // Sobrescribir el archivo con los nuevos records
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("records" + nivel + ".txt"))) {
            for (int i = 0; i < MAX_RECORDS; i++) { // Iterar hasta 10 para escribir los registros
                if (i < records.size()) {
                    // Si hay un registro, lo escribimos con el nombre justificado a 15 caracteres
                    String[] partes = records.get(i).split(" - - - "); // Separar el tiempo del nombre
                    String tiempo = partes[0].trim(); // Tiempo en formato [minutos:segundos]
                    tiempo = tiempo.replaceAll("[\\[\\]]", ""); // Quitar los corchetes del tiempo
                    String nombre = String.format("%-15s", partes[1].trim()); // Nombre del jugador justificado a 15 caracteres
                    writer.write(String.format("%02d [%s]  - - -  %s", i + 1, tiempo, nombre)); // Escribir el índice y el record alineados
                } else {
                    // Si no hay más registros, rellenamos con líneas vacías
                    writer.write(String.format("%02d ----------------", i + 1)); // Línea vacía para mantener el formato
                }
                writer.newLine(); // Agregar un salto de línea después de cada registro
            }
        } catch (IOException e) {
            System.err.println("Error al guardar el record en el archivo: " + e.getMessage());
        }
    }

    /**
     * Método para leer los records del archivo según el nivel.
     *
     * @param nivel El nivel de dificultad de los records a leer.
     * @return Lista de registros leídos del archivo.
     */
    private static List<String> leerRecords(int nivel) {
        List<String> records = new ArrayList<>(); // Crear una lista para almacenar los registros leídos

        try (BufferedReader reader = new BufferedReader(new FileReader("records" + nivel + ".txt"))) {
            String linea; // Variable para leer línea por línea
            while ((linea = reader.readLine()) != null) {
                if (!linea.contains("---")) { // Ignorar líneas que son solo separadores
                    records.add(linea.substring(3)); // Eliminar el número del record (01, 02, etc.) y agregar a la lista
                }
            }
        } catch (IOException e) {
            System.err.println("Error al leer el archivo de records: " + e.getMessage());
        }

        return records; // Devolver la lista de registros leídos
    }

    /**
     * Método para extraer minutos y segundos de un record.
     *
     * @param record El registro del que se extraerán los minutos y segundos.
     * @return Un arreglo con dos elementos: el primer elemento es minutos y el segundo es segundos.
     */
    private static int[] extraerTiempo(String record) {
        try {
            // Dividir el record para extraer el tiempo en formato [minutos:segundos]
            String[] partes = record.split(" - - - "); // Dividir el registro en tiempo y nombre
            String tiempo = partes[0].trim(); // Mantener solo la parte del tiempo
            tiempo = tiempo.replaceAll("[\\[\\]]", ""); // Quitar los corchetes del tiempo
            String[] tiempoArray = tiempo.split(":"); // Dividir tiempo en minutos y segundos
            int minutos = Integer.parseInt(tiempoArray[0].trim()); // Parsear minutos
            int segundos = Integer.parseInt(tiempoArray[1].trim()); // Parsear segundos
            return new int[]{minutos, segundos}; // Devolver como un array
        } catch (Exception e) {
            return new int[]{Integer.MAX_VALUE, Integer.MAX_VALUE}; // Si hay un error, asignar tiempo máximo
        }
    }

    /**
     * Método para obtener los records y mostrarlos en formato de texto.
     *
     * @param nivel El nivel de dificultad de los records a obtener.
     * @return Una cadena que representa la lista de records.
     */
    private static String obtenerRecords(int nivel) {
        String archivoRecords = "records" + nivel + ".txt";
        StringBuilder recordsBuilder = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new FileReader(archivoRecords))) {
            String linea;
            String dificultad;
            if (nivel == 1) {
                dificultad = "Fácil";
            } else if (nivel == 2) {
                dificultad = "Intermedia";
            } else {
                dificultad = "Difícil";
            }
            recordsBuilder.append("----- Lista de Records (").append(dificultad).append(") -----\n");
            while ((linea = reader.readLine()) != null) {
                recordsBuilder.append(linea).append("\n");
            }
            recordsBuilder.append("----------------------------");
        } catch (IOException e) {
            return "No existen registros de records para esta dificultad.";
        }

        return recordsBuilder.toString();
    }

    private static void enviarTablero(SocketChannel clientChannel, Tablerousr tablerousr) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(tablerousr);
        System.out.println("tablero enviado: " + Arrays.deepToString(tablerousr.getTab()));
        objectOutputStream.flush();
        byte[] data = byteArrayOutputStream.toByteArray();

        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES + data.length);
        buffer.putInt(data.length); // Primero, escribir el tamaño de los datos
        buffer.put(data);           // Luego, escribir los datos serializados
        buffer.flip();              // Preparar el buffer para lectura

        while (buffer.hasRemaining()) {
            clientChannel.write(buffer);
        }
    }


    private static void enviarMensajeFinal(SocketChannel clientChannel, String mensaje) throws IOException {
        escribirObjeto(clientChannel, ByteBuffer.allocate(1024), mensaje);
    }

    private static void enviarRecords(SocketChannel clientChannel, int nivel) throws IOException {
        escribirObjeto(clientChannel, ByteBuffer.allocate(1024), obtenerRecords(nivel));
    }


    private static void escribirObjeto(SocketChannel socketChannel, ByteBuffer buffer, Object objeto) throws IOException {
        // Serializar el objeto
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {

            objectOutputStream.writeObject(objeto);
            objectOutputStream.flush();

            byte[] byteArray = byteArrayOutputStream.toByteArray();

            // Escribir longitud del objeto en el buffer
            buffer.clear();
            buffer.putInt(byteArray.length);  // Longitud del objeto
            buffer.put(byteArray);            // El objeto serializado
            buffer.flip();

            // Enviar los datos a través del canal
            while (buffer.hasRemaining()) {
                socketChannel.write(buffer);
            }

            System.out.println("Escribir objeto /byteArray : " + byteArrayOutputStream.toString());
        }catch (IOException e) {
            System.out.println("Error al escribir el objeto" + e.toString());
        }
    }

}
