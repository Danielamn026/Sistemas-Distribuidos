/**************************************************************
         		Pontificia Universidad Javeriana
     Autor: Daniela Medina
     Fecha: 12 Septiembre 2025
     Materia: Sistemas Distribuidos
     Tema: Laboratorio de Sockets
****************************************************************/
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;

/**
 * sockettcpcli - TCP client with metrics (latency/RTT, throughput, jitter) and CSV output.
 *
 * Usage (benchmark):
 *   java sockettcpcli -h 127.0.0.1 -p 6001 -n 1000 -size 1024 -interval 10 -csv client_metrics.csv --nodelay --timeout 3000
 *
 * Usage (interactive):
 *   java sockettcpcli -h 127.0.0.1 -p 6001
 *   (type messages; 'fin' to exit)
 *
 * Notes:
 * - The client uses writeUTF/readUTF for messages. Size calculations refer to the declared payload,
 *   not including Java framing or TCP/IP overhead.
 * - For UDP tests, you can reuse the same scheme "MSG|seq|timestamp|len|payload".
 */
public class sockettcpcli {

    // Configuración por defecto del cliente TCP
    private static String host = "127.0.0.1";
    private static int port = 6001;
    private static int iterations = 0;         // 0 => modo interactivo
    private static int payloadSize = 64;       // bytes del cuerpo "payload"
    private static long intervalMs = 0;        // delay entre mensajes en benchmark
    private static String csvPath = "metrics_client.csv";
    private static boolean tcpNoDelay = true;
    private static boolean keepAlive = true;
    private static int soTimeoutMs = 0;        // 0 => infinito
    private static int rcvBuf = 0, sndBuf = 0; // 0 => por defecto del SO
    private static int warmup = 5;             // mensajes que no se registran (calentar JVM/TCP)

    public static void main(String[] args) {
        // Analiza los argumentos de línea de comando y configura el cliente
        parseArgs(args);
         // Decide el modo de ejecución según iterations: interactivo, benchmark
        if (iterations <= 0) {
            runInteractive();
        } else {
            runBenchmark();
        }
    }

         /**
         * parseArgs - analiza los argumentos de la línea de comando y
         * asigna valores a las variables de configuración.
         */
    private static void parseArgs(String[] argv) {
        for (int i = 0; i < argv.length; i++) {
            String a = argv[i];
            switch (a) {
                case "-h":
                case "--host":
                    host = argv[++i]; // Siguiente argumento = host
                    break;
                case "-p":
                case "--port":
                    port = Integer.parseInt(argv[++i]); // Puerto
                    break;
                case "-n":
                case "--iterations":
                    iterations = Integer.parseInt(argv[++i]); // Número de iteraciones
                    break;
                case "-size":
                case "--size":
                    payloadSize = Integer.parseInt(argv[++i]); // Tamaño del payload
                    break;
                case "-interval":
                case "--interval":
                    intervalMs = Long.parseLong(argv[++i]); // Intervalo entre mensajes
                    break;
                case "-csv":
                    csvPath = argv[++i]; // Ruta del archivo CSV
                    break;
                case "--nodelay":
                    tcpNoDelay = true; // Habilita TCP_NODELAY
                    break;
                case "--nodelay=false":
                    tcpNoDelay = false; // Desactiva TCP_NODELAY
                    break;
                case "--keepalive":
                    keepAlive = true; // Habilita keep-alive
                    break;
                case "--keepalive=false":
                    keepAlive = false; // Desactiva keep-alive
                    break;
                case "--timeout":
                    soTimeoutMs = Integer.parseInt(argv[++i]); // Timeout socket
                    break;
                case "--rcvbuf":
                    rcvBuf = Integer.parseInt(argv[++i]); // Buffer de recepción
                    break;
                case "--sndbuf":
                    sndBuf = Integer.parseInt(argv[++i]); // Buffer de envío
                    break;
                case "--warmup":
                    warmup = Integer.parseInt(argv[++i]); // Mensajes de calentamiento
                    break;
                default:
                    break; // Ignora argumentos desconocidos
            }
        }
    }

    /**
     * configureSocket - ajusta opciones del socket según la configuración.
     */
    private static void configureSocket(Socket s) throws SocketException {
        s.setTcpNoDelay(tcpNoDelay);
        s.setKeepAlive(keepAlive);
        if (soTimeoutMs > 0) s.setSoTimeout(soTimeoutMs);
        if (rcvBuf > 0) s.setReceiveBufferSize(rcvBuf);
        if (sndBuf > 0) s.setSendBufferSize(sndBuf);
    }

    /**
     * printSocketInfo - muestra información de la conexión y opciones del socket.
     */
    private static void printSocketInfo(Socket s) {
        try {
            System.out.println("[SOCKET] Conectado a " + s.getRemoteSocketAddress());
            System.out.println("[SOCKET] TCP_NODELAY=" + s.getTcpNoDelay() +
                               " KEEPALIVE=" + s.getKeepAlive() +
                               " SO_TIMEOUT=" + s.getSoTimeout());
            System.out.println("[SOCKET] RCVBUF=" + s.getReceiveBufferSize() +
                               " SNDBUF=" + s.getSendBufferSize());
        } catch (Exception e) {
            System.out.println("[SOCKET] Info no disponible: " + e.getMessage());
        }
    }

    // =============== INTERACTIVE MODE ===============
    /**
     * runInteractive - ejecuta el cliente en modo interactivo, donde el usuario
     * puede escribir mensajes y recibir métricas por cada mensaje enviado.
     */
    private static void runInteractive() {
        System.out.println("Prueba de sockets TCP (cliente) - MODO INTERACTIVO");
        try (Socket socket = new Socket()) {
            // Conectar al servidor con timeout de 5 segundos
            socket.connect(new InetSocketAddress(host, port), 5000);
            configureSocket(socket);   // Ajustar opciones del socket
            printSocketInfo(socket);   // Mostrar info de la conexión

            try (DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                 DataInputStream in = new DataInputStream(socket.getInputStream());
                 BufferedReader console = new BufferedReader(new InputStreamReader(System.in))) {

                System.out.println("Escribe mensajes. 'fin' para salir.");
                long prevRttNs = -1; // Para cálculo de jitter
                long seq = 0;         // Contador de secuencia de mensajes

                // Bucle principal de interacción
                while (true) {
                    System.out.print("> ");
                    String userMsg = console.readLine(); // Leer línea del usuario
                    if (userMsg == null) break;
                    if (userMsg.trim().equalsIgnoreCase("fin")) {
                        // Mensaje de cierre al servidor
                        String finMsg = "FIN";
                        out.writeUTF(finMsg);
                        out.flush();
                        break;
                    }

                    long clientSendNs = System.nanoTime(); // Tiempo de envío
                    String payload = pad(userMsg, payloadSize); // Ajustar payload al tamaño
                    String wireMsg = "USR|" + (++seq) + "|" + clientSendNs + "|" + payload.length() + "|" + payload;

                    out.writeUTF(wireMsg); // Enviar mensaje al servidor
                    out.flush();

                    String ack = in.readUTF(); // Leer respuesta
                    long clientRecvNs = System.nanoTime(); // Tiempo de recepción
                    long rttNs = clientRecvNs - clientSendNs; // Calcular RTT

                    AckFields af = parseAck(ack); // Extraer campos del ACK
                    double rttMs = rttNs / 1_000_000.0; // RTT en ms
                    double serverProcMs = (af.serverSendNs - af.serverRecvNs) / 1_000_000.0; // Procesamiento server

                    Double jitterMs = null;
                    if (prevRttNs > 0) {
                        jitterMs = Math.abs(rttNs - prevRttNs) / 1_000_000.0; // Calcular jitter
                    }
                    prevRttNs = rttNs;

                    int bytes = payload.getBytes(StandardCharsets.UTF_8).length; // Tamaño en bytes
                    double throughputBps = (bytes * 8.0) / (rttNs / 1_000_000_000.0); // Throughput

                    // Mostrar métricas por mensaje
                    System.out.printf(Locale.US,
                            "[ACK seq=%d] RTT=%.3f ms, serverProc=%.3f ms, size=%d bytes, throughput=%.2f bps%s%n",
                            af.seq, rttMs, serverProcMs, bytes, throughputBps,
                            (jitterMs != null ? String.format(Locale.US, ", jitter=%.3f ms", jitterMs) : ""));
                }
            }
        } catch (IOException e) {
            System.err.println("Error cliente: " + e.getMessage());
        }
        System.out.println("Cliente finalizado.");
    }

    // =============== BENCHMARK MODE ===============
    /**
     * runBenchmark - ejecuta el cliente en modo benchmark, enviando múltiples mensajes
     * y registrando métricas en un archivo CSV.
     */
    private static void runInteractive() {
        System.out.println("Prueba de sockets TCP (cliente) - MODO INTERACTIVO");
        try (Socket socket = new Socket()) {
            // Conectar al servidor con timeout de 5 segundos
            socket.connect(new InetSocketAddress(host, port), 5000);
            configureSocket(socket);   // Ajustar opciones del socket
            printSocketInfo(socket);   // Mostrar info de la conexión

            try (DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                 DataInputStream in = new DataInputStream(socket.getInputStream());
                 BufferedReader console = new BufferedReader(new InputStreamReader(System.in))) {

                System.out.println("Escribe mensajes. 'fin' para salir.");
                long prevRttNs = -1; // Para cálculo de jitter
                long seq = 0;         // Contador de secuencia de mensajes

                // Bucle principal de interacción
                while (true) {
                    System.out.print("> ");
                    String userMsg = console.readLine(); // Leer línea del usuario
                    if (userMsg == null) break;
                    if (userMsg.trim().equalsIgnoreCase("fin")) {
                        // Mensaje de cierre al servidor
                        String finMsg = "FIN";
                        out.writeUTF(finMsg);
                        out.flush();
                        break;
                    }

                    long clientSendNs = System.nanoTime(); // Tiempo de envío
                    String payload = pad(userMsg, payloadSize); // Ajustar payload al tamaño
                    String wireMsg = "USR|" + (++seq) + "|" + clientSendNs + "|" + payload.length() + "|" + payload;

                    out.writeUTF(wireMsg); // Enviar mensaje al servidor
                    out.flush();

                    String ack = in.readUTF(); // Leer respuesta
                    long clientRecvNs = System.nanoTime(); // Tiempo de recepción
                    long rttNs = clientRecvNs - clientSendNs; // Calcular RTT

                    AckFields af = parseAck(ack); // Extraer campos del ACK
                    double rttMs = rttNs / 1_000_000.0; // RTT en ms
                    double serverProcMs = (af.serverSendNs - af.serverRecvNs) / 1_000_000.0; // Procesamiento server

                    Double jitterMs = null;
                    if (prevRttNs > 0) {
                        jitterMs = Math.abs(rttNs - prevRttNs) / 1_000_000.0; // Calcular jitter
                    }
                    prevRttNs = rttNs;

                    int bytes = payload.getBytes(StandardCharsets.UTF_8).length; // Tamaño en bytes
                    double throughputBps = (bytes * 8.0) / (rttNs / 1_000_000_000.0); // Throughput

                    // Mostrar métricas por mensaje
                    System.out.printf(Locale.US,
                            "[ACK seq=%d] RTT=%.3f ms, serverProc=%.3f ms, size=%d bytes, throughput=%.2f bps%s%n",
                            af.seq, rttMs, serverProcMs, bytes, throughputBps,
                            (jitterMs != null ? String.format(Locale.US, ", jitter=%.3f ms", jitterMs) : ""));
                }
            }
        } catch (IOException e) {
            System.err.println("Error cliente: " + e.getMessage());
        }
        System.out.println("Cliente finalizado.");
    }

    // =============== BENCHMARK MODE ===============
    /**
     * runBenchmark - ejecuta el cliente en modo benchmark, enviando múltiples mensajes
     * y registrando métricas en un archivo CSV.
     */
    private static void runBenchmark() {
        System.out.println("Prueba de sockets TCP (cliente) - MODO BENCHMARK");
        System.out.printf("Destino %s:%d, n=%d, size=%d bytes, interval=%d ms, csv=%s%n",
                host, port, iterations, payloadSize, intervalMs, csvPath);

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), 5000);
            configureSocket(socket);
            printSocketInfo(socket);

            try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                 DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                 PrintWriter csv = new PrintWriter(new BufferedWriter(new FileWriter(csvPath)))) {

                // Escribir encabezado CSV
                csv.println("seq,payload_bytes,client_send_ns,client_recv_ns,rtt_ms,server_recv_ns,server_send_ns,server_proc_ms,throughput_bps,jitter_ms");

                long prevRttNs = -1;
                long seq = 0;

                // Fase de calentamiento (no medida)
                for (int w = 0; w < warmup; w++) {
                    String payload = generatePayload(payloadSize);
                    String wireMsg = "MSG|WARMUP|" + System.nanoTime() + "|" + payload.length() + "|" + payload;
                    out.writeUTF(wireMsg);
                    out.flush();
                    in.readUTF(); // descartar ACK
                    if (intervalMs > 0) Thread.sleep(intervalMs);
                }

                // Fase de medición
                StatCollector rttStats = new StatCollector();
                StatCollector thrStats = new StatCollector();

                for (int i = 0; i < iterations; i++) {
                    String payload = generatePayload(payloadSize);
                    long clientSendNs = System.nanoTime();
                    String wireMsg = "MSG|" + (++seq) + "|" + clientSendNs + "|" + payload.length() + "|" + payload;

                    out.writeUTF(wireMsg);
                    out.flush();

                    String ack = in.readUTF();
                    long clientRecvNs = System.nanoTime();
                    long rttNs = clientRecvNs - clientSendNs;

                    AckFields af = parseAck(ack);
                    int bytes = payload.getBytes(StandardCharsets.UTF_8).length;

                    double rttMs = rttNs / 1_000_000.0;
                    double serverProcMs = (af.serverSendNs - af.serverRecvNs) / 1_000_000.0;
                    double throughputBps = (bytes * 8.0) / (rttNs / 1_000_000_000.0);

                    Double jitterMs = null;
                    if (prevRttNs > 0) jitterMs = Math.abs(rttNs - prevRttNs) / 1_000_000.0;
                    prevRttNs = rttNs;

                    // Guardar métricas en CSV
                    csv.printf(Locale.US, "%d,%d,%d,%d,%.6f,%d,%d,%.6f,%.2f,%s%n",
                            af.seq, bytes, clientSendNs, clientRecvNs, rttMs, af.serverRecvNs, af.serverSendNs, serverProcMs, throughputBps,
                            (jitterMs == null ? "" : String.format(Locale.US, "%.6f", jitterMs)));

                    rttStats.add(rttMs);
                    thrStats.add(throughputBps);

                    if (intervalMs > 0) Thread.sleep(intervalMs);
                }

                // Resumen de estadísticas
                System.out.println("\n=== RESUMEN ===");
                System.out.println("RTT (ms): " + rttStats);
                System.out.println("Throughput (bps): " + thrStats);
                System.out.println("CSV guardado en: " + csvPath);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        } catch (IOException e) {
            System.err.println("Error cliente: " + e.getMessage());
        }
        System.out.println("Cliente finalizado.");
    }

    /**
     * pad - completa el texto hasta el tamaño deseado con 'x'.
     */
    private static String pad(String text, int targetBytes) {
        byte[] original = text.getBytes(StandardCharsets.UTF_8);
        if (original.length >= targetBytes) return text;
        int need = targetBytes - original.length;
        char[] fill = new char[need];
        Arrays.fill(fill, 'x');
        return text + new String(fill);
    }

    /**
     * generatePayload - genera un payload de bytes 'x' de tamaño especificado.
     */
    private static String generatePayload(int bytes) {
        char[] fill = new char[Math.max(0, bytes)];
        Arrays.fill(fill, 'x');
        return new String(fill);
    }

    /**
     * parseAck - parsea un mensaje ACK del servidor.
     * Formato: "ACK|seq|serverRecvNs|serverSendNs"
     */
    private static AckFields parseAck(String ack) throws IOException {
        if (ack == null || !ack.startsWith("ACK|")) {
            throw new IOException("ACK inválido: " + ack);
        }
        String[] parts = ack.split("\\|");
        if (parts.length < 4) throw new IOException("ACK incompleto: " + ack);
        long seq = Long.parseLong(parts[1]);
        long serverRecvNs = Long.parseLong(parts[2]);
        long serverSendNs = Long.parseLong(parts[3]);
        return new AckFields(seq, serverRecvNs, serverSendNs);
    }

    /**
     * Clase interna para almacenar campos del ACK.
     */
    private static class AckFields {
        final long seq;
        final long serverRecvNs;
        final long serverSendNs;
        AckFields(long s, long r, long t) { this.seq = s; this.serverRecvNs = r; this.serverSendNs = t; }
    }

    /**
     * StatCollector - recolector de estadísticas simples (min, max, promedio, desviación estándar).
     */
    private static class StatCollector {
        private long n = 0;
        private double mean = 0;
        private double m2 = 0;
        private double min = Double.POSITIVE_INFINITY;
        private double max = Double.NEGATIVE_INFINITY;

        void add(double x) {
            n++;
            double delta = x - mean;
            mean += delta / n;
            m2 += delta * (x - mean);
            if (x < min) min = x;
            if (x > max) max = x;
        }

        double mean() { return mean; }
        double variance() { return n > 0 ? m2 / n : 0; }
        double stddev() { return Math.sqrt(variance()); }
        double min() { return (n > 0 ? min : 0); }
        double max() { return (n > 0 ? max : 0); }

        @Override public String toString() {
            return String.format(Locale.US, "n=%d, min=%.3f, avg=%.3f, max=%.3f, std=%.3f", n, min(), mean(), max(), stddev());
        }
    }
}
