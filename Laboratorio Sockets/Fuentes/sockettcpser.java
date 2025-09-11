/**************************************************************
         		Pontificia Universidad Javeriana
     Autor: Daniela Medina
     Fecha: 12 Septiembre 2025
     Materia: Sistemas Distribuidos
     Tema: Laboratorio de Sockets
****************************************************************/
import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * sockettcpser - Concurrent TCP server with metrics and CSV logging.
 *
 * Usage:
 *   java sockettcpser -p 6001 -csv server_metrics.csv --nodelay --keepalive --backlog 100
 *
 * Protocol:
 * - Client messages (UTF):
 *   "MSG|seq|clientSendNs|payloadLen|<payload>"
 *   "USR|seq|clientSendNs|payloadLen|<user_text>"
 *   "FIN"  => closes the connection
 *
 * - Server response:
 *   "ACK|seq|serverRecvNs|serverSendNs"
 */
public class sockettcpser {

    // Configuración por defecto del servidor
    private static int port = 6001; // Puerto TCP
    private static String csvPath = "metrics_server.csv"; // Archivo CSV para métricas
    private static boolean tcpNoDelay = true; // Desactiva Nagle (envío inmediato de paquetes)
    private static boolean keepAlive = true; // Mantener conexión TCP viva
    private static int backlog = 100; // Número máximo de conexiones en espera
    private static int rcvBuf = 0, sndBuf = 0; // Tamaño de buffer de recepción/envío (0 = default)

    private static final Object CSV_LOCK = new Object(); // Lock para sincronizar escritura CSV

    public static void main(String[] args) {
        // Parsear argumentos de línea de comando
        parseArgs(args);
        try (ServerSocket server = new ServerSocket(port, backlog)) { // Crear servidor TCP
            System.out.println(ts() + " Servidor escuchando en puerto " + port); // Log de inicio
            // Bucle principal: aceptar clientes y manejar cada uno en un hilo nuevo
            while (true) {
                Socket client = server.accept(); // Espera y acepta una conexión entrante
                configureSocket(client); // Configura opciones TCP del cliente
                System.out.println(ts() + " Conexión de " + client.getRemoteSocketAddress()); // Log cliente
                new Thread(new ClientHandler(client)).start(); // Crear hilo para manejar cliente
            }
        } catch (IOException e) {
            System.err.println("Error servidor: " + e.getMessage()); // Manejo de errores
        }
    }

    /**
     * Parsear argumentos de línea de comando y configurar variables.
     */
    private static void parseArgs(String[] argv) {
        for (int i = 0; i < argv.length; i++) {
            String a = argv[i];
            switch (a) {
                case "-p":
                case "--port":
                    port = Integer.parseInt(argv[++i]); // Configura puerto
                    break;
                case "-csv":
                    csvPath = argv[++i]; // Configura ruta CSV
                    break;
                case "--nodelay":
                    tcpNoDelay = true; // Habilita TCP_NODELAY
                    break;
                case "--nodelay=false":
                    tcpNoDelay = false; // Deshabilita TCP_NODELAY
                    break;
                case "--keepalive":
                    keepAlive = true; // Habilita KEEPALIVE
                    break;
                case "--keepalive=false":
                    keepAlive = false; // Deshabilita KEEPALIVE
                    break;
                case "--backlog":
                    backlog = Integer.parseInt(argv[++i]); // Número máximo de conexiones pendientes
                    break;
                case "--rcvbuf":
                    rcvBuf = Integer.parseInt(argv[++i]); // Tamaño buffer de recepción
                    break;
                case "--sndbuf":
                    sndBuf = Integer.parseInt(argv[++i]); // Tamaño buffer de envío
                    break;
                default:
                    // Ignorar argumentos desconocidos
                    break;
            }
        }
    }

    /**
     * Configura opciones TCP del socket según variables actuales.
     */
    private static void configureSocket(Socket s) throws SocketException {
        s.setTcpNoDelay(tcpNoDelay); // Aplica TCP_NODELAY
        s.setKeepAlive(keepAlive); // Aplica KEEPALIVE
        if (rcvBuf > 0) s.setReceiveBufferSize(rcvBuf); // Configura buffer de recepción
        if (sndBuf > 0) s.setSendBufferSize(sndBuf); // Configura buffer de envío
    }

    /**
     * Retorna timestamp para logging.
     */
    private static String ts() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()); // Formato yyyy-MM-dd HH:mm:ss.SSS
    }

    // ================== HANDLER ==================
    /**
     * Clase interna que maneja una conexión de cliente en un hilo separado.
     */
    private static class ClientHandler implements Runnable {
        private final Socket socket; // Socket del cliente
        ClientHandler(Socket s) { this.socket = s; } // Constructor asigna socket
        @Override
        public void run() {
            try (Socket s = socket;
                 DataInputStream in = new DataInputStream(new BufferedInputStream(s.getInputStream())); // Stream de entrada
                 DataOutputStream out = new DataOutputStream(new BufferedOutputStream(s.getOutputStream()))) { // Stream de salida

                writeServerCsvHeaderIfNeeded(); // Escribe cabecera CSV si no existe

                // Bucle principal: leer mensaje, procesar, enviar ACK
                while (true) {
                    String msg;
                    try {
                        msg = in.readUTF(); // Leer mensaje UTF desde cliente
                    } catch (EOFException eof) {
                        System.out.println(ts() + " Cliente " + s.getRemoteSocketAddress() + " cerró conexión.");
                        break;
                    }
                    if (msg == null) break; // Fin de stream
                    if ("FIN".equals(msg)) { // Cliente envía FIN
                        System.out.println(ts() + " FIN recibido de " + s.getRemoteSocketAddress());
                        break;
                    }
                    long recvNs = System.nanoTime(); // Timestamp de recepción
                    String type = msg.length() >= 3 ? msg.substring(0, 3) : ""; // Tipo MSG/USR
                    long seq = -1;
                    if (type.equals("MSG") || type.equals("USR")) {
                        String[] parts = msg.split("\\|", 5); // Separar mensaje en campos
                        if (parts.length >= 5) {
                            // parts: [0]=MSG/USR, [1]=seq, [2]=clientSendNs, [3]=payloadLen, [4]=payload
                            seq = safeParseLong(parts[1], -1L); // Secuencia
                            long clientSendNs = safeParseLong(parts[2], -1L); // Timestamp del cliente
                            int payloadLen = safeParseInt(parts[3], -1); // Largo payload
                            String payload = parts[4]; // Mensaje real
                            // Simulación de procesamiento (puede incluir validación, checksum)
                            long beforeProcNs = System.nanoTime(); // Antes procesamiento
                            // ...processing...
                            long afterProcNs = System.nanoTime(); // Después procesamiento
                            // Registrar métricas en CSV
                            writeServerCsv(seq, type, s.getRemoteSocketAddress().toString(), clientSendNs, recvNs, beforeProcNs, afterProcNs, payloadLen, payload);
                            // Enviar ACK al cliente
                            long sendNs = System.nanoTime();
                            String ack = "ACK|" + seq + "|" + recvNs + "|" + sendNs;
                            out.writeUTF(ack);
                            out.flush();
                            if (type.equals("USR")) {
                                System.out.println(ts() + " [" + s.getRemoteSocketAddress() + "] " + "Mensaje #" + seq + ": " + payload);
                            }
                        } else {
                            System.out.println(ts() + " Mensaje malformado de " + s.getRemoteSocketAddress() + ": " + msg);
                        }
                    } else {
                        // Echo simple para compatibilidad con otros mensajes
                        long sendNs = System.nanoTime();
                        String ack = "ACK|0|" + recvNs + "|" + sendNs;
                        out.writeUTF(ack);
                        out.flush();
                        System.out.println(ts() + " [RAW] " + msg);
                    }
                }
            } catch (IOException e) {
                System.err.println("Error con cliente " + socket.getRemoteSocketAddress() + ": " + e.getMessage());
            }
        }
        /**
         * Escribe una fila de métricas en el CSV del servidor.
         */
        private void writeServerCsv(long seq, String type, String remote, long clientSendNs, long serverRecvNs,
                                    long beforeProcNs, long afterProcNs, int payloadLen, String payload) {
            long procNs = afterProcNs - beforeProcNs; // Tiempo de procesamiento
            int bytes = payload.getBytes(StandardCharsets.UTF_8).length; // Tamaño real del payload
            synchronized (CSV_LOCK) { // Lock para evitar concurrencia
                try (PrintWriter csv = new PrintWriter(new BufferedWriter(new FileWriter(csvPath, true)))) {
                    csv.printf(Locale.US,
                            "%s,%d,%s,%d,%d,%d,%.6f,%d,%d%n",
                            type, seq, remote, clientSendNs, serverRecvNs, bytes,
                            procNs / 1_000_000.0, payloadLen, System.nanoTime());
                } catch (IOException e) {
                    System.err.println("Error escribiendo CSV: " + e.getMessage());
                }
            }
        }
        /**
         * Escribe la cabecera del CSV si no existe o está vacía.
         */
        private void writeServerCsvHeaderIfNeeded() {
            File f = new File(csvPath);
            if (f.exists() && f.length() > 0) return; // Ya existe y tiene datos
            synchronized (CSV_LOCK) {
                try (PrintWriter csv = new PrintWriter(new BufferedWriter(new FileWriter(csvPath, true)))) {
                    csv.println("type,seq,remote,client_send_ns,server_recv_ns,payload_bytes,server_proc_ms,payload_len,server_log_ns");
                } catch (IOException e) {
                    System.err.println("Error escribiendo cabecera CSV: " + e.getMessage());
                }
            }
        }
        /**
         * Parseo seguro de long desde String, retorna valor por defecto si falla.
         */
        private long safeParseLong(String s, long def) {
            try { return Long.parseLong(s); } catch (Exception e) { return def; }
        }
        /**
         * Parseo seguro de int desde String, retorna valor por defecto si falla.
         */
        private int safeParseInt(String s, int def) {
            try { return Integer.parseInt(s); } catch (Exception e) { return def; }
        }
    }
}

