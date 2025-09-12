# Taller de Hilos y Sockets (TCP/UDP en Java)

Este laboratorio implementa y evalúa servidores y clientes TCP y UDP en Java. El objetivo es medir y comparar RTT, jitter, throughput y tiempos de procesamiento del servidor en escenarios ideal y no ideal, y documentar el comportamiento observado.

Presentado por:

- Jorge Gomez
- Valeria Caycedo
- Jeisson Ruiz
- Daniela Medina

---

## Estructura del proyecto
``` 
udp-tcp/
├── sockettcpser.java           # Servidor TCP
├── sockettcpcli.java           # Cliente TCP
├── socketudpser.java           # Servidor UDP
├── socketudpcli.java           # Cliente UDP
└── data/
    ├── tcp_client_metrics.csv
    ├── tcp_client_metrics_noIdeal.csv
    ├── tcp_server_metrics.csv
    ├── tcp_server_metricsnoIdeal.csv
    ├── udp_client_metrics.csv
    ├── udp_client_metricsNoIdeal.csv
    ├── udp_server_metrics.csv
    ├── udp_server_metricsnoIdeal.csv
    ├── RTT clientes.fig
    ├── RTT ideal vs no ideal Clientes.fig
    ├── Jitter Clientes.fig
    ├── Jitter ideal vs no idea clientes.fig
    ├── Throughput Clientes.fig
    ├── Throughput ideal vs no ideal.fig
    ├── Mensajes recibidos caso ideal.fig
    └── Tiempo de procesamiento del servidor.fig
```
## ¿Qué hace?
Se implementan cuatro programas:

- Servidor TCP (sockettcpser.java): acepta múltiples clientes, procesa solicitudes y responde.

- Cliente TCP (sockettcpcli.java): envía mensajes, mide RTT, estima jitter y calcula throughput.

- Servidor UDP (socketudpser.java): recibe datagramas y responde sin mantener sesión.

- Cliente UDP (socketudpcli.java): envía datagramas y registra tiempos para RTT, jitter y throughput.

### Métricas y escenarios:
El sistema ejecuta pruebas en condiciones ideales y no ideales (con degradaciones como retrasos, variación de retardo o pérdidas). Las métricas se registran en archivos CSV y se visualizan en FIG:

- RTT (ms): tiempo de ida y vuelta desde cliente.

- Jitter (ms): variación entre retardos consecutivos.

- Throughput (bps): tasa efectiva de transferencia.

- Tiempo de procesamiento del servidor (ms): diferencia entre envío y recepción en el servidor.

## Objetivos

1. Implementar clientes y servidores TCP y UDP.

2. Ejecutar pruebas en dos condiciones:

    - Ideal: latencias estables y sin pérdidas.

    - No ideal: congestión, retrasos variables y/o pérdidas.

3. Registrar métricas en CSV y visualizar resultados (archivos .fig).

4. Comparar protocolos y discutir compromisos de fiabilidad vs. rendimiento.
## Documentación del Código

### Programas (Java)

- sockettcpser.java
  Levanta un servidor TCP. Recibe conexiones concurrentes, procesa cada solicitud y registra marcas     de tiempo del lado servidor (recepción y envío) para estimar server_proc_ms.

- sockettcpcli.java
  Cliente TCP de consola. Se conecta al servidor, envía mensajes con payload_bytes configurables,       mide RTT y deriva jitter y throughput a partir de los sellos de tiempo.

- socketudpser.java
  Servidor UDP. Opera sin conexión persistente; atiende datagramas y responde. Registra                 server_recv_ns (y, si aplica, server_send_ns) para analizar carga.

- socketudpcli.java
  Cliente UDP. Envía datagramas y, cuando corresponde, calcula RTT; además estima jitter y throughput   según los tiempos de recepción.

  ### Datos (/data)
  #### CSV (por protocolo, rol y escenario):

    - TCP (ideal): tcp_client_metrics.csv, tcp_server_metrics.csv

    - TCP (no ideal): tcp_client_metrics_noIdeal.csv, tcp_server_metricsnoIdeal.csv

    - UDP (ideal): udp_client_metrics.csv, udp_server_metrics.csv

    - UDP (no ideal): udp_client_metricsNoIdeal.csv, udp_server_metricsnoIdeal.csv

    #### Columnas típicas:

    - seq: número de mensaje.

    - payload_bytes: tamaño útil por mensaje.

    - client_send_ns, client_recv_ns: marcas de tiempo en cliente.

    - server_recv_ns, server_send_ns: marcas en servidor.

    - rtt_ms: latencia ida y vuelta estimada por el cliente.

    - server_proc_ms: tiempo de procesamiento del servidor.

    - throughput_bps: tasa efectiva estimada.

    - jitter_ms: variación de retardo entre mensajes consecutivos.

    #### FIG (gráficas MATLAB/Octave):

    - RTT: RTT clientes.fig, RTT ideal vs no ideal Clientes.fig

    - Jitter: Jitter Clientes.fig, Jitter ideal vs no idea clientes.fig

    - Throughput: Throughput Clientes.fig, Throughput ideal vs no ideal.fig

    - Otros: Mensajes recibidos caso ideal.fig, Tiempo de procesamiento del servidor.fig
 ## Compilación (Linux)
 ```
 javac sockettcpser.java sockettcpcli.java socketudpser.java socketudpcli.java
```
## Ejecución (Linux)
```
# Terminal 1
<h2>Servidor TCP</h2>
```
```
# Terminal 2
java sockettcpcli
```
```
# Terminal 1
java socketudpser
```
```
# Terminal 2
java socketudpcli
```
## Pruebas
Se ejecutaron pruebas en escenario ideal y no ideal para ambos protocolos (TCP/UDP).
Los resultados numéricos se encuentran en data/*.csv.
Las visualizaciones se encuentran en data/*.fig.

### Sugerencia para reporte:

- Estadísticos: media, mediana, p95 y p99 de rtt_ms, jitter_ms, throughput_bps, server_proc_ms.

- Series temporales: métricas vs. seq para detectar picos y colas.

- Comparaciones: ideal vs. no ideal por protocolo.
  
## Observaciones y Conclusiones

- TCP presenta RTT más estable y jitter menor por sus mecanismos de control, a costa de overhead y     posibles caídas de throughput bajo pérdidas.

- UDP puede alcanzar latencias menores y throughput más alto, pero es más sensible a pérdidas y        variación de retardo (jitter).

- En no ideal, se incrementan RTT y jitter, con especial impacto en UDP; TCP compensa con              retransmisiones pero puede degradar el throughput.

- El tiempo de procesamiento del servidor depende del número de clientes, del manejo de hilos y de     la E/S; su control evita cuellos de botella.

## Resultados (Imagenes de las graficas)
