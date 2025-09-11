/**************************************************************
         		Pontificia Universidad Javeriana
     Autor: Daniela Medina
     Fecha: 12 Septiembre 2025
     Materia: Sistemas Distribuidos
     Tema: Laboratorio de Sockets
****************************************************************/
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;


/**
 * socketudpcli - Enhanced UDP client for network testing
 * Metrics: RTT, throughput, jitter, loss
 * Saves results to CSV
 *
 * Usage:
 *   java socketudpcli_enhanced -h 127.0.0.1 -p 6002 -n 100 -size 1024 -interval 5 -csv udp_client_metrics.csv --timeout 2000
 */
public class socketudpcli {

    public static void main(String[] args) throws Exception {
        // Default parameters
        String host = "127.0.0.1";
        int port = 6002;
        int iterations = 10;
        int payloadSize = 64;
        int interval = 100; // ms
        int timeout = 2000; // ms
        String csvFile = "udp_client_metrics.csv";

        // Parse command line arguments
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-h": host = args[++i]; break;
                case "-p": port = Integer.parseInt(args[++i]); break;
                case "-n": iterations = Integer.parseInt(args[++i]); break;
                case "-size": payloadSize = Integer.parseInt(args[++i]); break;
                case "-interval": interval = Integer.parseInt(args[++i]); break;
                case "--timeout": timeout = Integer.parseInt(args[++i]); break;
                case "-csv": csvFile = args[++i]; break;
            }
        }

        DatagramSocket socket = new DatagramSocket();
        socket.setSoTimeout(timeout);

        PrintWriter csv = new PrintWriter(new FileWriter(csvFile));
        csv.println("seq,payload_bytes,client_send_ns,client_recv_ns,rtt_ms,throughput_bps,lost,jitter_ms");

        byte[] payload = new byte[payloadSize];
        Arrays.fill(payload, (byte) 'A');

        long prevRTT = -1;

        // Main test loop: send, receive, measure
        for (int seq = 1; seq <= iterations; seq++) {
            long sendTime = System.nanoTime();
            String msg = "MSG|" + seq + "|" + sendTime + "|" + payloadSize;
            byte[] buf = msg.getBytes(StandardCharsets.UTF_8);
            DatagramPacket packet = new DatagramPacket(buf, buf.length, InetAddress.getByName(host), port);

            socket.send(packet);

            boolean lost = false;
            long recvTime = -1;
            long rtt = -1;
            double throughput = 0;
            double jitter = 0;

            try {
                byte[] recvBuf = new byte[1024];
                DatagramPacket resp = new DatagramPacket(recvBuf, recvBuf.length);
                socket.receive(resp);
                recvTime = System.nanoTime();

                String ack = new String(resp.getData(), 0, resp.getLength(), StandardCharsets.UTF_8);
                long endTime = System.nanoTime();
                rtt = (endTime - sendTime) / 1_000_000; // ms
                if (rtt > 0) {
                    throughput = (payloadSize * 8.0) / (rtt / 1000.0); // bits/s
                }
                if (prevRTT != -1) {
                    jitter = Math.abs(rtt - prevRTT);
                }
                prevRTT = rtt;

                // Print metrics for each message
                System.out.println("ACK recibido: " + ack + " | RTT=" + rtt + " ms | Thr=" + throughput + " bps | Jitter=" + jitter + " ms");

            } catch (SocketTimeoutException e) {
                lost = true;
                System.out.println("Mensaje " + seq + " PERDIDO (timeout)");
            }

            // Write metrics to CSV
            csv.printf(Locale.US, "%d,%d,%d,%d,%d,%.2f,%b,%.2f%n",
                       seq, payloadSize, sendTime, recvTime, rtt, throughput, lost, (prevRTT == -1 ? 0.0 : (double) Math.abs(rtt - prevRTT)));

            Thread.sleep(interval);
        }

        csv.close();
        socket.close();
        System.out.println("Métricas guardadas en " + csvFile);
    }
}

