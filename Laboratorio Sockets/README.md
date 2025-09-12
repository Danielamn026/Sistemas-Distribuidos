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

- Servidor TCP (sockettcpser.java): Acepta múltiples clientes de manera concurrente utilizando hilos. Cada solicitud recibida es procesada individualmente, registrando marcas de tiempo tanto de recepción como de envío, lo que permite estimar el tiempo de procesamiento en el servidor (server_proc_ms). Gracias al uso de TCP, el servidor garantiza la confiabilidad del transporte y la entrega ordenada de los mensajes.

- Cliente TCP (sockettcpcli.java): Se conecta al servidor y envía mensajes configurables en tamaño y número. A partir de los sellos de tiempo, el cliente mide RTT, calcula jitter y estima throughput, proporcionando una visión completa del desempeño del protocolo en distintos escenarios.

- Servidor UDP (socketudpser.java): Opera sin mantener conexiones persistentes, atendiendo datagramas de manera independiente. Cada mensaje recibido se procesa y se responde con un ACK, mientras se registran marcas de tiempo que permiten evaluar el desempeño del servidor frente a cargas variables.

- Cliente UDP (socketudpcli.java): Envía datagramas al servidor y calcula RTT, jitter y throughput desde la perspectiva del cliente. Este cliente es útil para simular escenarios no ideales, en los que se pueden introducir retrasos, pérdidas de paquetes o congestión de red, evaluando la capacidad de UDP para manejar condiciones adversas.

---

### Métricas y Escenarios:

El sistema ejecuta pruebas en condiciones ideales y no ideales (con degradaciones como retrasos, variación de retardo o pérdidas). Las métricas se registran en archivos CSV y se visualizan en FIG:

<div align="center">
<img width="475" height="250" alt="image" src="https://github.com/user-attachments/assets/5f11d5bb-3463-4269-bd68-38b834c52d62" />
</div>

---

## Objetivos

1. Implementar clientes y servidores TCP y UDP.

2. Ejecutar pruebas en dos condiciones:

    - Ideal: latencias estables y sin pérdidas.

    - No ideal: congestión, retrasos variables y/o pérdidas.

3. Registrar métricas en CSV y visualizar resultados (archivos .fig).

4. Comparar protocolos y discutir compromisos de fiabilidad vs. rendimiento.

---

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

#### CSV

Cada prueba genera archivos CSV separados por protocolo, rol (cliente/servidor) y escenario.

- TCP (ideal): tcp_client_metrics.csv, tcp_server_metrics.csv

- TCP (no ideal): tcp_client_metrics_noIdeal.csv, tcp_server_metricsnoIdeal.csv

- UDP (ideal): udp_client_metrics.csv, udp_server_metrics.csv

- UDP (no ideal): udp_client_metricsNoIdeal.csv, udp_server_metricsnoIdeal.csv

#### Columnas típicas:

<div align="center">
    <img width="461" height="213" alt="image" src="https://github.com/user-attachments/assets/9b582ae8-b891-43bf-8ed3-062a5581e850" />
</div>

#### FIG (gráficas MATLAB/Octave):

- RTT: RTT clientes.fig, RTT ideal vs no ideal Clientes.fig

- Jitter: Jitter Clientes.fig, Jitter ideal vs no idea clientes.fig

- Throughput: Throughput Clientes.fig, Throughput ideal vs no ideal.fig

- Otros: Mensajes recibidos caso ideal.fig, Tiempo de procesamiento del servidor.fig

--- 

 ## Compilación (Linux)
 ```
 javac sockettcpser.java sockettcpcli.java socketudpser.java socketudpcli.java
```
--- 

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
---

## Pruebas

Se ejecutaron pruebas en escenario ideal y no ideal para ambos protocolos (TCP/UDP).
Los resultados numéricos se encuentran en data/*.csv.
Las visualizaciones se encuentran en data/*.fig.

---

## Observaciones y Conclusiones

- TCP presenta RTT más estable y jitter menor por sus mecanismos de control, a costa de overhead y     posibles caídas de throughput bajo pérdidas.

- UDP puede alcanzar latencias menores y throughput más alto, pero es más sensible a pérdidas y        variación de retardo (jitter).

- En no ideal, se incrementan RTT y jitter, con especial impacto en UDP; TCP compensa con              retransmisiones pero puede degradar el throughput.

- El tiempo de procesamiento del servidor depende del número de clientes, del manejo de hilos y de     la E/S; su control evita cuellos de botella.

---

# Análisis de Resultados TCP vs UDP

## Mensajes recibidos (Caso Ideal)

![Mensajes recibidos](data/exported_png/Mensajes%20recibidos%20caso%20ideal.png)

**Análisis:**  
- En el escenario ideal, prácticamente todos los mensajes enviados son recibidos correctamente.  
- Esto refleja que tanto TCP como UDP funcionan de manera confiable en condiciones sin pérdidas ni retrasos.  
- La diferencia principal es que TCP asegura la entrega mediante retransmisiones, mientras que en UDP la recepción depende del estado de la red.  
- En este caso ideal, UDP no presenta pérdidas visibles, lo que permite throughput máximo.  

---

## RTT en clientes

![RTT clientes](data/exported_png/RTT%20clientes.png)

**Análisis:**  
- El RTT (tiempo de ida y vuelta) en condiciones ideales se mantiene bajo y estable.  
- TCP muestra una ligera variación debido al overhead de confirmaciones (ACKs).  
- UDP puede alcanzar un RTT ligeramente menor ya que no requiere establecer conexión ni gestionar retransmisiones.  
- La baja dispersión en los valores indica estabilidad en la red durante la prueba.  

---

## Comparación RTT Ideal vs No Ideal (Clientes)

![RTT Ideal vs No Ideal](data/exported_png/RTT%20ideal%20vs%20no%20ideal Clientes.png)

**Análisis:**  
- En el escenario no ideal, se observa un incremento claro en los valores de RTT.  
- TCP logra mantener estabilidad gracias a sus mecanismos de control de congestión y retransmisión, aunque a costa de mayor latencia promedio.  
- UDP, en contraste, refleja mayor dispersión y valores extremos de RTT cuando hay congestión o pérdidas.  
- **Conclusión**: TCP sacrifica latencia por confiabilidad; UDP ofrece menor latencia solo si la red está libre de pérdidas.  

---

## Throughput en clientes

![Throughput clientes](data/exported_png/Throughput%20Clientes.png)

**Análisis:**  
- En el escenario ideal, ambos protocolos logran un throughput alto.  
- UDP puede llegar a valores ligeramente mayores debido a la ausencia de confirmaciones y menor overhead.  
- TCP mantiene un throughput estable y consistente, pero con un costo de cabecera mayor.  
- La curva muestra que, a medida que aumenta la cantidad de mensajes, la tasa de transferencia tiende a estabilizarse.  

---

## Comparación Throughput Ideal vs No Ideal

![Throughput Ideal vs No Ideal](data/exported_png/Throughput%20ideal%20vs%20no%20ideal.png)

**Análisis:**  
- En el escenario no ideal, el throughput de UDP se degrada significativamente cuando ocurren pérdidas.  
- TCP mantiene mejor throughput en situaciones adversas, pero puede caer abruptamente si el nivel de congestión es alto.  
- La diferencia entre las curvas evidencia el costo de la fiabilidad en TCP frente a la sensibilidad de UDP a las condiciones de la red.  

---

## Tiempo de procesamiento en el servidor

![Tiempo procesamiento servidor](data/exported_png/Tiempo%20de%20procesamiento%20del%20servidor.png)

**Análisis:**  
- El tiempo de procesamiento en el servidor depende directamente de la cantidad de clientes concurrentes y de la eficiencia en el manejo de hilos.  
- En TCP, la sobrecarga de establecer y mantener conexiones puede aumentar el tiempo promedio de procesamiento.  
- En UDP, el servidor procesa datagramas de forma más ligera, pero sin garantías de orden ni confiabilidad.  
- Un tiempo de procesamiento bajo y estable es esencial para evitar cuellos de botella que perjudiquen métricas de RTT y throughput.  

---

## Comparación Jitter Cliente: Ideal vs No Ideal

![Comparación Jitter Cliente: Ideal vs No Ideal](data/exported_png/Jitter%20ideal%20vs%20no%20idea%20clientes.png)

**Análisis:**  
Esta gráfica muestra cómo el jitter varía dramáticamente en escenarios no ideales, especialmente para TCP (línea roja), donde se observan picos de hasta 400 ms. En contraste, tanto TCP como UDP en condiciones ideales mantienen un jitter muy bajo y estable. Esto reafirma que el entorno afecta de manera significativa la variabilidad de la latencia y que los protocolos reaccionan de manera diferente ante condiciones adversas.

---
## Distribución de RTT: TCP vs UDP
![Distribución RTT cliente](data/exported_png/Distribucion%20RTT%20cliente.png)

**Análisis:**  
Aquí se observa la distribución de los tiempos de ida y vuelta (RTT) para TCP y UDP. La mayoría de los mensajes tienen RTT bajo (<10 ms), pero UDP muestra ocasionalmente valores atípicos mucho más altos. TCP tiende a mantener una distribución más concentrada y confiable, debido a sus mecanismos de control y retransmisión.

---
## Jitter por mensaje: TCP vs UDP
![Jitter Clientes](data/exported_png/Jitter%20Clientes.png)

**Análisis:**  
El jitter por mensaje para TCP muestra variaciones ocasionales, pero UDP permanece extremadamente bajo y constante. Esto sugiere que UDP, aunque no garantiza entrega, ofrece consistencia de latencia en estos escenarios, mientras que TCP puede verse afectado por retransmisiones y control de congestión.

---
## Comparación RTT Cliente: Ideal vs No Ideal
![RTT ideal vs no ideal Clientes](data/exported_png/RTT%20ideal%20vs%20no%20ideal%20Clientes.png)

**Análisis:**  
La gráfica evidencia que tanto TCP como UDP presentan RTT significativamente mayores y más variables en condiciones no ideales. Las líneas correspondientes a los casos no ideales muestran picos y fluctuaciones que superan los 400 ms, mientras que los escenarios ideales mantienen RTT bajos y estables. El entorno de red y la calidad de la conexión son factores críticos para el desempeño de ambos protocolos.

---

# Conclusiones Generales

- **TCP**: Ofrece mayor estabilidad y confiabilidad, con RTT y jitter controlados, pero puede sacrificar throughput bajo condiciones de red adversas.  
- **UDP**: Más rápido y ligero en escenarios ideales, pero altamente vulnerable a pérdidas y variaciones de retardo en escenarios no ideales.  
- **Escenarios no ideales**: RTT y jitter se incrementan para ambos protocolos, pero los efectos son más críticos en UDP.  
- **Servidor**: El diseño del servidor (manejo de hilos, eficiencia de E/S) impacta de forma directa en el tiempo de procesamiento y, por ende, en todas las métricas finales.  
