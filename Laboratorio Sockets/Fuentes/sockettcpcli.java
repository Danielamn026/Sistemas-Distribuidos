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

    // Default configuration
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
        // Parse command line arguments
        parseArgs(args);
        // Choose mode: interactive or benchmark
        if (iterations <= 0) {
            runInteractive();
        } else {
            runBenchmark();
        }
    }

    /**
     * Parses command line arguments and sets configuration variables.
     */
    private static void parseArgs(String[] argv) {
        for (int i = 0; i < argv.length; i++) {
            String a = argv[i];
            switch (a) {
                case "-h":
                case "--host":
                    host = argv[++i];
                    break;
                case "-p":
                case "--port":
                    port = Integer.parseInt(argv[++i]);
                    break;
                case "-n":
                case "--iterations":
                    iterations = Integer.parseInt(argv[++i]);
                    break;
                case "-size":
                case "--size":
                    payloadSize = Integer.parseInt(argv[++i]);
                    break;
                case "-interval":
                case "--interval":
                    intervalMs = Long.parseLong(argv[++i]);
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
                case "--timeout":
                    soTimeoutMs = Integer.parseInt(argv[++i]);
                    break;
                case "--rcvbuf":
                    rcvBuf = Integer.parseInt(argv[++i]);
                    break;
                case "--sndbuf":
                    sndBuf = Integer.parseInt(argv[++i]);
                    break;
                case "--warmup":
                    warmup = Integer.parseInt(argv[++i]);
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
        if (soTimeoutMs > 0) s.setSoTimeout(soTimeoutMs);
        if (rcvBuf > 0) s.setReceiveBufferSize(rcvBuf);
        if (sndBuf > 0) s.setSendBufferSize(sndBuf);
    }

    /**
     * Prints socket configuration and connection info to the console.
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
     * Runs the client in interactive mode, allowing the user to type messages and receive metrics.
     */
    private static void runInteractive() {
        System.out.println("Prueba de sockets TCP (cliente) - MODO INTERACTIVO");
        try (Socket socket = new Socket()) {
            // Connect to server
            socket.connect(new InetSocketAddress(host, port), 5000);
            configureSocket(socket);
            printSocketInfo(socket);
            try (DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                 DataInputStream in = new DataInputStream(socket.getInputStream());
                 BufferedReader console = new BufferedReader(new InputStreamReader(System.in))) {

                System.out.println("Escribe mensajes. 'fin' para salir.");

                long prevRttNs = -1;
                long seq = 0;

                // Main loop: read user input, send to server, receive ACK, print metrics
                while (true) {
                    System.out.print("> ");
                    String userMsg = console.readLine();
                    if (userMsg == null) break;
                    if (userMsg.trim().equalsIgnoreCase("fin")) {
                        // Notify server to close connection
                        String finMsg = "FIN";
                        out.writeUTF(finMsg);
                        out.flush();
                        break;
                    }

                    long clientSendNs = System.nanoTime();
                    String payload = pad(userMsg, payloadSize); // optional: pad to fixed size
                    String wireMsg = "USR|" + (++seq) + "|" + clientSendNs + "|" + payload.length() + "|" + payload;

                    out.writeUTF(wireMsg);
                    out.flush();

                    String ack = in.readUTF();
                    long clientRecvNs = System.nanoTime();
                    long rttNs = clientRecvNs - clientSendNs;

                    AckFields af = parseAck(ack);
                    double rttMs = rttNs / 1_000_000.0;
                    double serverProcMs = (af.serverSendNs - af.serverRecvNs) / 1_000_000.0;

                    Double jitterMs = null;
                    if (prevRttNs > 0) {
                        jitterMs = Math.abs(rttNs - prevRttNs) / 1_000_000.0;
                    }
                    prevRttNs = rttNs;

                    int bytes = payload.getBytes(StandardCharsets.UTF_8).length;
                    double throughputBps = (bytes * 8.0) / (rttNs / 1_000_000_000.0);

                    // Print metrics for each message
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
     * Runs the client in benchmark mode, sending multiple messages and recording metrics to CSV.
     */
    private static void runBenchmark() {
        System.out.println("Prueba de sockets TCP (cliente) - MODO BENCHMARK");
        System.out.printf("Destino %s:%d, n=%d, size=%d bytes, interval=%d ms, csv=%s%n",
                host, port, iterations, payloadSize, intervalMs, csvPath);

        // CSV
        try (Socket socket = new Socket()) {
            // Connect to server
            socket.connect(new InetSocketAddress(host, port), 5000);
            configureSocket(socket);
            printSocketInfo(socket);
            try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                 DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                 PrintWriter csv = new PrintWriter(new BufferedWriter(new FileWriter(csvPath)))) {

                // Write CSV header
                csv.println("seq,payload_bytes,client_send_ns,client_recv_ns,rtt_ms,server_recv_ns,server_send_ns,server_proc_ms,throughput_bps,jitter_ms");

                long prevRttNs = -1;
                long seq = 0;

                // Warmup phase (not measured)
                for (int w = 0; w < warmup; w++) {
                    String payload = generatePayload(payloadSize);
                    String wireMsg = "MSG|WARMUP|" + System.nanoTime() + "|" + payload.length() + "|" + payload;
                    out.writeUTF(wireMsg);
                    out.flush();
                    in.readUTF(); // discard ack
                    if (intervalMs > 0) Thread.sleep(intervalMs);
                }

                // Measurement phase
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

                    // Write metrics to CSV
                    csv.printf(Locale.US, "%d,%d,%d,%d,%.6f,%d,%d,%.6f,%.2f,%s%n",
                            af.seq, bytes, clientSendNs, clientRecvNs, rttMs, af.serverRecvNs, af.serverSendNs, serverProcMs, throughputBps,
                            (jitterMs == null ? "" : String.format(Locale.US, "%.6f", jitterMs)));

                    rttStats.add(rttMs);
                    thrStats.add(throughputBps);

                    if (intervalMs > 0) Thread.sleep(intervalMs);
                }

                // Print summary statistics
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
     * Pads the input text to the target byte size using 'x' characters.
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
     * Generates a payload string of the specified byte size using 'x' characters.
     */
    private static String generatePayload(int bytes) {
        char[] fill = new char[Math.max(0, bytes)];
        Arrays.fill(fill, 'x');
        return new String(fill);
    }

    /**
     * Parses the ACK message from the server and extracts fields.
     * Format: "ACK|seq|serverRecvNs|serverSendNs"
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
     * Helper class to store ACK fields.
     */
    private static class AckFields {
        final long seq;
        final long serverRecvNs;
        final long serverSendNs;
        AckFields(long s, long r, long t) { this.seq = s; this.serverRecvNs = r; this.serverSendNs = t; }
    }

    /**
     * Simple statistics collector (min, max, average, population stddev).
     */
    private static class StatCollector {
        private long n = 0;
        private double mean = 0;
        private double m2 = 0;
        private double min = Double.POSITIVE_INFINITY;
        private double max = Double.NEGATIVE_INFINITY;
        /**
         * Adds a value to the statistics.
         */
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
