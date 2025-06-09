package org.example;

import java.awt.HeadlessException;
import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.SwingWorker;

public class Cliente {

    private static final Logger logger = Logger.getLogger(Cliente.class.getName());

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PantallaInicio().setVisible(true));
    }

    public static void iniciarJuego(String nombreUsuario, int nivel) {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                AtomicInteger op = new AtomicInteger(); // Para manejar el tipo de operación (1: descubrir, 2: colocar bandera)

                try {
                    // Crear un socket en modo no bloqueante
                    SocketChannel socketChannel = SocketChannel.open();
                    socketChannel.configureBlocking(false);  // Modo no bloqueante
                    System.out.println("Socket abierto en modo no bloqueante.");

                    // Conectar al servidor
                    SocketAddress socketAddress = new InetSocketAddress("127.0.0.1", 1234);
                    socketChannel.connect(socketAddress);
                    System.out.println("Conectando al servidor...");

                    // Crear un selector para manejar múltiples conexiones
                    Selector selector = Selector.open();
                    socketChannel.register(selector, SelectionKey.OP_CONNECT | SelectionKey.OP_READ | SelectionKey.OP_WRITE);

                    // Esperar hasta que se complete la conexión
                    while (!socketChannel.finishConnect()) {
                        System.out.println("Esperando la conexión...");
                        Thread.sleep(100); // Esperamos un poco antes de verificar nuevamente
                    }
                    System.out.println("Conexión establecida.");

                    // Crear un ByteBuffer para los datos
                    ByteBuffer buffer = ByteBuffer.allocate(1024);

                    // Enviar nombre de usuario y nivel al servidor
                    escribirObjeto(socketChannel, buffer, nombreUsuario);
                    System.out.println("Nombre de Usuario enviado al servidor: " + nombreUsuario);
                    escribirObjeto(socketChannel, buffer, nivel);
                    System.out.println("Nivel enviado al servidor: " + nivel);

                    // Recibir el tablero inicial desde el servidor
                    Tablerousr tablero = (Tablerousr) leerObjeto(socketChannel, buffer);
                    System.out.println("Tablero inicial recibido: " + Arrays.deepToString(tablero.getTab()));
                    TablaGrafica tablaGrafica = new TablaGrafica(tablero.getTab(), 40);
                    tablaGrafica.setVisible(true);

                    SwingUtilities.invokeLater(() -> {
                        tablaGrafica.setCeldaClickListener((int xPos, int yPos, boolean botonDerecho) -> {
                            new SwingWorker<Void, Void>() {
                                private Tablerousr tablero;

                                @Override
                                protected Void doInBackground() {
                                    try {
                                        if (botonDerecho) {
                                            op.set(2); // Colocar bandera
                                        } else {
                                            op.set(1); // Descubrir casilla
                                        }

                                        // Crear una jugada con las coordenadas y el tipo de operación
                                        Jugada jugada = new Jugada(xPos, yPos, op.get());

                                        // Enviar la jugada al servidor
                                        escribirObjeto(socketChannel, buffer, jugada);
                                        System.out.println("Jugada enviada: x = " + xPos + ", y = " + yPos + " Tipo " + op.get());
                                        // Recibir el tablero actualizado desde el servidor
                                        //tablero = (Tablerousr) leerObjeto(socketChannel, buffer);
                                        //System.out.println("Tablero actualizado recibido.");

                                    } catch (IOException e) {
                                        logger.log(Level.SEVERE, "Error al manejar la jugada", e);
                                    }
                                    return null;
                                }

                            }.execute();
                        });
                    });

                    // Bucle principal de selección para manejar la I/O no bloqueante
                    while (true) {
                        // Esperar hasta que haya algo que hacer
                        selector.select();

                        // Obtener las claves seleccionadas
                        Set<SelectionKey> selectedKeys = selector.selectedKeys();
                        Iterator<SelectionKey> iterator = selectedKeys.iterator();

                        while (iterator.hasNext()) {
                            SelectionKey key = iterator.next();
                            iterator.remove();

                            // Si la clave es de lectura, podemos leer los datos del servidor
                            if (key.isReadable()) {
                                try {
                                    // Recibir el tablero actualizado
                                    tablero = (Tablerousr) leerObjeto(socketChannel, buffer);
                                    // Si se ha recibido un tablero, actualizar la interfaz gráfica
                                    if (tablero != null) {
                                        System.out.println("Tablero actualizado recibido desde el servidor.");
                                        tablaGrafica.actualizarTablero(tablero.getTab(), tablero.getMensaje());
                                        if (!Objects.equals(tablero.getEstado(), "Jugando")) {
                                            JOptionPane.showMessageDialog(tablaGrafica, "El juego ha terminado: " + tablero.getEstado());
                                            tablaGrafica.setCeldaClickListener(null);

                                            String mensaje = (String) leerObjeto(socketChannel, buffer);
                                            tablaGrafica.mostrarMensaje(mensaje);

                                            String records = (String) leerObjeto(socketChannel, buffer);
                                            tablaGrafica.habilitarBotones(records);

                                            socketChannel.close();
                                        }
                                    } else {
                                        System.out.println("No se pudo recibir el tablero.");
                                    }
                                } catch (IOException e) {
                                    logger.log(Level.SEVERE, "Error al leer del servidor", e);
                                }
                            }

                            // Si la clave es de conexión, completar la conexión
                            if (key.isConnectable()) {
                                if (socketChannel.isConnectionPending()) {
                                    socketChannel.finishConnect();
                                    System.out.println("Conexión completada.");
                                }
                            }
                        }
                    }
                } catch (IOException | InterruptedException e) {
                    logger.log(Level.SEVERE, "Error al iniciar el cliente", e);
                }
                return null;
            }
        };
        worker.execute();
    }

    // Método para escribir objetos serializados a través del SocketChannel
    private static void escribirObjeto(SocketChannel socketChannel, ByteBuffer buffer, Object objeto) throws IOException {
        // Serializar el objeto
        System.out.println("Intenta scribir objeto: " + objeto);
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {

            objectOutputStream.writeObject(objeto);
            objectOutputStream.flush();

            byte[] byteArray = byteArrayOutputStream.toByteArray();
            System.out.println("Longitud del objeto: " + byteArray.length);
            System.out.println("Datos serializados: " + Arrays.toString(byteArray));


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


    // Método para leer objetos serializados desde el SocketChannel
    private static Object leerObjeto(SocketChannel socketChannel, ByteBuffer buffer) throws IOException {
        //buffer.clear();
        // Paso 1: Leer el tamaño del objeto
        ByteBuffer sizeBuffer = ByteBuffer.allocate(Integer.BYTES); // Buffer para el tamaño
        while (sizeBuffer.hasRemaining()) {
            if (socketChannel.read(sizeBuffer) == -1) {
                throw new EOFException("Conexión cerrada al leer el tamaño del objeto.");
            }
        }
        sizeBuffer.flip(); // Preparar para lectura
        int dataLength = sizeBuffer.getInt(); // Obtener el tamaño

        if (dataLength <= 0) {
            throw new IOException("Tamaño inválido del objeto recibido: " + dataLength);
        }

        // Paso 2: Leer los datos del objeto
        ByteBuffer dataBuffer = ByteBuffer.allocate(dataLength);
        while (dataBuffer.hasRemaining()) {
            if (socketChannel.read(dataBuffer) == -1) {
                throw new EOFException("Conexión cerrada al leer los datos del objeto.");
            }
        }

        // Paso 3: Deserializar el objeto
        dataBuffer.flip(); // Preparar para lectura
        byte[] byteArray = new byte[dataBuffer.remaining()];
        dataBuffer.get(byteArray); // Copiar los datos al array

        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArray);
             ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)) {
            return objectInputStream.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException("Error al deserializar el objeto", e);
        }
    }
}


