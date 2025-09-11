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
        // Parámetros por defecto
        String host = "127.0.0.1"; // Dirección del servidor
        int port = 6002; // Puerto UDP del servidor
        int iterations = 10; // Número de mensajes a enviar
        int payloadSize = 64; // Tamaño de cada payload en bytes
        int interval = 100; // Intervalo entre mensajes (ms)
        int timeout = 2000; // Timeout de recepción (ms)
        String csvFile = "udp_client_metrics.csv"; // Archivo CSV para métricas

        // Parsear argumentos de línea de comando para sobrescribir valores por defecto
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-h": host = args[++i]; break; // Dirección del servidor
                case "-p": port = Integer.parseInt(args[++i]); break; // Puerto UDP
                case "-n": iterations = Integer.parseInt(args[++i]); break; // Número de iteraciones
                case "-size": payloadSize = Integer.parseInt(args[++i]); break; // Tamaño payload
                case "-interval": interval = Integer.parseInt(args[++i]); break; // Intervalo entre mensajes
                case "--timeout": timeout = Integer.parseInt(args[++i]); break; // Timeout recepción
                case "-csv": csvFile = args[++i]; break; // Archivo CSV
            }
        }

        DatagramSocket socket = new DatagramSocket(); // Crear socket UDP
        socket.setSoTimeout(timeout); // Establecer timeout para recepción

        PrintWriter csv = new PrintWriter(new FileWriter(csvFile)); // Crear archivo CSV
        csv.println("seq,payload_bytes,client_send_ns,client_recv_ns,rtt_ms,throughput_bps,lost,jitter_ms"); // Cabecera CSV

        byte[] payload = new byte[payloadSize]; // Array de bytes para payload
        Arrays.fill(payload, (byte) 'A'); // Llenar payload con el carácter 'A'

        long prevRTT = -1; // Variable para calcular jitter (RTT anterior)

        // Bucle principal de prueba: enviar, recibir, medir
        for (int seq = 1; seq <= iterations; seq++) {
            long sendTime = System.nanoTime(); // Timestamp de envío en nanosegundos
            String msg = "MSG|" + seq + "|" + sendTime + "|" + payloadSize; // Formato de mensaje
            byte[] buf = msg.getBytes(StandardCharsets.UTF_8); // Convertir mensaje a bytes UTF-8
            DatagramPacket packet = new DatagramPacket(buf, buf.length, InetAddress.getByName(host), port); // Crear paquete UDP

            socket.send(packet); // Enviar paquete al servidor

            boolean lost = false; // Indicador de pérdida
            long recvTime = -1; // Timestamp de recepción
            long rtt = -1; // Round Trip Time
            double throughput = 0; // Throughput en bits/s
            double jitter = 0; // Jitter en ms

            try {
                byte[] recvBuf = new byte[1024]; // Buffer para recibir respuesta
                DatagramPacket resp = new DatagramPacket(recvBuf, recvBuf.length); // Paquete de respuesta
                socket.receive(resp); // Esperar respuesta del servidor
                recvTime = System.nanoTime(); // Timestamp de recepción

                String ack = new String(resp.getData(), 0, resp.getLength(), StandardCharsets.UTF_8); // Convertir bytes recibidos a String
                long endTime = System.nanoTime(); // Timestamp final para RTT
                rtt = (endTime - sendTime) / 1_000_000; // Calcular RTT en milisegundos

                if (rtt > 0) {
                    throughput = (payloadSize * 8.0) / (rtt / 1000.0); // Calcular throughput en bits/s
                }
                if (prevRTT != -1) {
                    jitter = Math.abs(rtt - prevRTT); // Calcular jitter
                }
                prevRTT = rtt; // Guardar RTT actual para la siguiente iteración

                // Imprimir métricas en consola para cada mensaje
                System.out.println("ACK recibido: " + ack + " | RTT=" + rtt + " ms | Thr=" + throughput + " bps | Jitter=" + jitter + " ms");

            } catch (SocketTimeoutException e) {
                lost = true; // Si se produce timeout, el paquete se considera perdido
                System.out.println("Mensaje " + seq + " PERDIDO (timeout)");
            }

            // Escribir métricas en CSV
            csv.printf(Locale.US, "%d,%d,%d,%d,%d,%.2f,%b,%.2f%n",
                       seq, payloadSize, sendTime, recvTime, rtt, throughput, lost, (prevRTT == -1 ? 0.0 : (double) Math.abs(rtt - prevRTT)));

            Thread.sleep(interval); // Esperar intervalo antes de enviar siguiente mensaje
        }

        csv.close(); // Cerrar archivo CSV
        socket.close(); // Cerrar socket UDP
        System.out.println("Métricas guardadas en " + csvFile); // Mensaje final indicando ubicación de métricas
    }
}
