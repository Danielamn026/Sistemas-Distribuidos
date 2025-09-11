/**************************************************************
         		Pontificia Universidad Javeriana
     Autor: Daniela Medina
     Fecha: 12 Septiembre 2025
     Materia: Sistemas Distribuidos
     Tema: Laboratorio de Sockets
****************************************************************/
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * socketudpser - Enhanced UDP server
 * Responds with ACK and logs metrics
 *
 * Usage:
 *   java socketudpser_enhanced -p 6002 -csv udp_server_metrics.csv
 */
public class socketudpser {

    public static void main(String[] args) throws Exception {
        int port = 6002;
        String csvFile = "udp_server_metrics.csv";
        // Parse command line arguments
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-p": port = Integer.parseInt(args[++i]); break;
                case "-csv": csvFile = args[++i]; break;
            }
        }
        DatagramSocket socket = new DatagramSocket(port);
        PrintWriter csv = new PrintWriter(new FileWriter(csvFile));
        csv.println("seq,payload_bytes,server_recv_ns,server_proc_ms");
        System.out.println("Servidor UDP escuchando en puerto " + port);
        byte[] buf = new byte[8192];
        // Main server loop: receive, process, respond
        while (true) {
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet);
            long recvTime = System.nanoTime();
            String data = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
            String[] parts = data.split("\\|", 4);
            if (parts.length < 4) continue;
            int seq = Integer.parseInt(parts[1]);
            long clientSend = Long.parseLong(parts[2]);
            int payloadLen = Integer.parseInt(parts[3]);
            long procStart = System.nanoTime();
            // Simulate processing if needed (e.g., Thread.sleep(1))
            long procEnd = System.nanoTime();
            long procMs = (procEnd - procStart) / 1_000_000;
            // Respond with ACK
            String ack = "ACK|" + seq + "|" + recvTime + "|" + System.nanoTime();
            byte[] ackBuf = ack.getBytes(StandardCharsets.UTF_8);
            DatagramPacket ackPkt = new DatagramPacket(ackBuf, ackBuf.length,
                                                      packet.getAddress(), packet.getPort());
            socket.send(ackPkt);
            // Log metrics to CSV
            csv.printf(Locale.US, "%d,%d,%d,%d%n", seq, payloadLen, recvTime, procMs);
            csv.flush();
            System.out.println("Recibido seq=" + seq + " (" + payloadLen + " bytes), ACK enviado");
        }
    }
}

