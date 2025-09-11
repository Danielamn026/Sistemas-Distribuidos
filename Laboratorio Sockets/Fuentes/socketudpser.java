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
        int port = 6002; // Puerto UDP por defecto
        String csvFile = "udp_server_metrics.csv"; // Archivo CSV por defecto

        // Parsear argumentos de línea de comando
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-p": port = Integer.parseInt(args[++i]); break; // Cambiar puerto si se pasa -p
                case "-csv": csvFile = args[++i]; break; // Cambiar archivo CSV si se pasa -csv
            }
        }

        DatagramSocket socket = new DatagramSocket(port); // Crear socket UDP en el puerto indicado
        PrintWriter csv = new PrintWriter(new FileWriter(csvFile)); // Crear archivo CSV para métricas
        csv.println("seq,payload_bytes,server_recv_ns,server_proc_ms"); // Cabecera del CSV
        System.out.println("Servidor UDP escuchando en puerto " + port); // Mensaje inicial

        byte[] buf = new byte[8192]; // Buffer para recibir paquetes UDP

        // Bucle principal del servidor: recibir, procesar y responder
        while (true) {
            DatagramPacket packet = new DatagramPacket(buf, buf.length); // Crear paquete vacío para recibir
            socket.receive(packet); // Esperar a recibir un paquete UDP
            long recvTime = System.nanoTime(); // Timestamp de recepción en nanosegundos

            String data = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8); // Convertir bytes a String
            String[] parts = data.split("\\|", 4); // Separar mensaje en partes: tipo|seq|clientSend|payloadLen
            if (parts.length < 4) continue; // Ignorar mensajes malformados

            int seq = Integer.parseInt(parts[1]); // Número de secuencia del mensaje
            long clientSend = Long.parseLong(parts[2]); // Timestamp de envío del cliente
            int payloadLen = Integer.parseInt(parts[3]); // Longitud del payload

            long procStart = System.nanoTime(); // Inicio de procesamiento
            // Simular procesamiento si se desea (ej. Thread.sleep(1))
            long procEnd = System.nanoTime(); // Fin de procesamiento
            long procMs = (procEnd - procStart) / 1_000_000; // Tiempo de procesamiento en ms

            // Preparar respuesta ACK
            String ack = "ACK|" + seq + "|" + recvTime + "|" + System.nanoTime(); // Formato ACK: seq|recvTime|sendTime
            byte[] ackBuf = ack.getBytes(StandardCharsets.UTF_8); // Convertir ACK a bytes UTF-8
            DatagramPacket ackPkt = new DatagramPacket(ackBuf, ackBuf.length,
                                                      packet.getAddress(), packet.getPort()); // Crear paquete de respuesta
            socket.send(ackPkt); // Enviar ACK al cliente

            // Registrar métricas en CSV
            csv.printf(Locale.US, "%d,%d,%d,%d%n", seq, payloadLen, recvTime, procMs);
            csv.flush(); // Asegurarse de que se escriba inmediatamente

            // Imprimir información en consola
            System.out.println("Recibido seq=" + seq + " (" + payloadLen + " bytes), ACK enviado");
        }
    }
}

