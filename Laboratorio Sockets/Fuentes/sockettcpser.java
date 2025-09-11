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

    // Default server configuration
    private static int port = 6001;
    private static String csvPath = "metrics_server.csv";
    private static boolean tcpNoDelay = true;
    private static boolean keepAlive = true;
    private static int backlog = 100;
    private static int rcvBuf = 0, sndBuf = 0;

    private static final Object CSV_LOCK = new Object();

    public static void main(String[] args) {
        // Parse command line arguments
        parseArgs(args);
        try (ServerSocket server = new ServerSocket(port, backlog)) {
            System.out.println(ts() + " Servidor escuchando en puerto " + port);
            // Main accept loop: handle each client in a new thread
            while (true) {
                Socket client = server.accept();
                configureSocket(client);
                System.out.println(ts() + " Conexión de " + client.getRemoteSocketAddress());
                new Thread(new ClientHandler(client)).start();
            }
        } catch (IOException e) {
            System.err.println("Error servidor: " + e.getMessage());
        }
    }

    /**
     * Parses command line arguments and sets configuration variables.
     */
    private static void parseArgs(String[] argv) {
        for (int i = 0; i < argv.length; i++) {
            String a = argv[i];
            switch (a) {
                case "-p":
                case "--port":
                    port = Integer.parseInt(argv[++i]);
                    break;
                case "-csv":
                    csvPath = argv[++i];
                    break;
                case "--nodelay":
                    tcpNoDelay = true;
                    break;
                case "--nodelay=false":
                    tcpNoDelay = false;
                    break;
                case "--keepalive":
                    keepAlive = true;
                    break;
                case "--keepalive=false":
                    keepAlive = false;
                    break;
                case "--backlog":
                    backlog = Integer.parseInt(argv[++i]);
                    break;
                case "--rcvbuf":
                    rcvBuf = Integer.parseInt(argv[++i]);
                    break;
                case "--sndbuf":
                    sndBuf = Integer.parseInt(argv[++i]);
                    break;
                default:
                    // Ignore unknown arguments
                    break;
            }
        }
    }

    /**
     * Configures socket options according to the current settings.
     */
    private static void configureSocket(Socket s) throws SocketException {
        s.setTcpNoDelay(tcpNoDelay);
        s.setKeepAlive(keepAlive);
        if (rcvBuf > 0) s.setReceiveBufferSize(rcvBuf);
        if (sndBuf > 0) s.setSendBufferSize(sndBuf);
    }

    /**
     * Returns a timestamp string for logging.
     */
    private static String ts() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
    }

    // ================== HANDLER ==================
    /**
     * Handles a single client connection in a separate thread.
     */
    private static class ClientHandler implements Runnable {
        private final Socket socket;
        ClientHandler(Socket s) { this.socket = s; }
        @Override
        public void run() {
            try (Socket s = socket;
                 DataInputStream in = new DataInputStream(new BufferedInputStream(s.getInputStream()));
                 DataOutputStream out = new DataOutputStream(new BufferedOutputStream(s.getOutputStream()))) {

                writeServerCsvHeaderIfNeeded();
                // Main message loop: read, process, respond
                while (true) {
                    String msg;
                    try {
                        msg = in.readUTF();
                    } catch (EOFException eof) {
                        System.out.println(ts() + " Cliente " + s.getRemoteSocketAddress() + " cerró conexión.");
                        break;
                    }
                    if (msg == null) break;
                    if ("FIN".equals(msg)) {
                        System.out.println(ts() + " FIN recibido de " + s.getRemoteSocketAddress());
                        break;
                    }
                    long recvNs = System.nanoTime();
                    String type = msg.length() >= 3 ? msg.substring(0, 3) : "";
                    long seq = -1;
                    if (type.equals("MSG") || type.equals("USR")) {
                        String[] parts = msg.split("\\|", 5);
                        if (parts.length >= 5) {
                            // parts: [0]=MSG/USR, [1]=seq, [2]=clientSendNs, [3]=payloadLen, [4]=payload
                            seq = safeParseLong(parts[1], -1L);
                            long clientSendNs = safeParseLong(parts[2], -1L);
                            int payloadLen = safeParseInt(parts[3], -1);
                            String payload = parts[4];
                            // Simulated processing (can add validation, checksum, etc.)
                            long beforeProcNs = System.nanoTime();
                            // ...processing...
                            long afterProcNs = System.nanoTime();
                            // Log metrics to CSV
                            writeServerCsv(seq, type, s.getRemoteSocketAddress().toString(), clientSendNs, recvNs, beforeProcNs, afterProcNs, payloadLen, payload);
                            // Send ACK
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
                        // Basic echo for compatibility
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
         * Writes a metrics row to the server CSV file.
         */
        private void writeServerCsv(long seq, String type, String remote, long clientSendNs, long serverRecvNs,
                                    long beforeProcNs, long afterProcNs, int payloadLen, String payload) {
            long procNs = afterProcNs - beforeProcNs;
            int bytes = payload.getBytes(StandardCharsets.UTF_8).length;
            synchronized (CSV_LOCK) {
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
         * Writes the CSV header if the file is empty or does not exist.
         */
        private void writeServerCsvHeaderIfNeeded() {
            File f = new File(csvPath);
            if (f.exists() && f.length() > 0) return;
            synchronized (CSV_LOCK) {
                try (PrintWriter csv = new PrintWriter(new BufferedWriter(new FileWriter(csvPath, true)))) {
                    csv.println("type,seq,remote,client_send_ns,server_recv_ns,payload_bytes,server_proc_ms,payload_len,server_log_ns");
                } catch (IOException e) {
                    System.err.println("Error escribiendo cabecera CSV: " + e.getMessage());
                }
            }
        }
        /**
         * Safely parses a long from string, returns default if invalid.
         */
        private long safeParseLong(String s, long def) {
            try { return Long.parseLong(s); } catch (Exception e) { return def; }
        }
        /**
         * Safely parses an int from string, returns default if invalid.
         */
        private int safeParseInt(String s, int def) {
            try { return Integer.parseInt(s); } catch (Exception e) { return def; }
        }
    }
}

