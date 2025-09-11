# Simulación de Cajeras en Supermercado (Java Threads)

Proyecto que implementa la simulación de un supermercado donde cajeras procesan las compras de clientes.  
El objetivo es comparar la diferencia entre la ejecución **secuencial** y la ejecución **paralela** mediante el uso de **hilos en Java** (`Thread` y `Runnable`).  

Presentado por:  
- Jorge Gomez
- Valeria Caycedo
- Jeisson Ruiz
- Daniela Medina
---

## 1. ¿Qué hace?

El programa simula un escenario en el que clientes llegan con un carrito de compras, y cada cajera procesa los productos del cliente.  

- Ejecución secuencial: Una cajera atiende a un cliente y, cuando termina, comienza con el siguiente.  
- Ejecución concurrente con **Thread**: Cada cajera trabaja en su propio hilo, atendiendo clientes al mismo tiempo.  
- Ejecución concurrente con **Runnable**: Se crea un Runnable para representar el trabajo de cada cajera y se lanza en hilos independientes.  

El sistema mide el tiempo transcurrido para mostrar la diferencia entre procesamiento secuencial y paralelo.  

---

## 2. Estructura

```
├── src
│ └── threadsJarroba
│ ├── Cajera.java
│ ├── CajeraThread.java
│ ├── Cliente.java
│ ├── Main.java
│ ├── MainRunnable.java
│ └── MainThread.java
└── README.md
```
---

## 3. Documentación del Código

- La clase `Cajera.java` representa a una cajera que procesa compras de manera secuencial. A través de su método procesarCompra(), recibe un cliente y el tiempo inicial de la simulación, recorriendo el carrito de productos y simulando con pausas (Thread.sleep()) el tiempo que tardaría en atender cada uno. Esta clase no hace uso de hilos.

- La clase `CajeraThread.java` extiende de Thread y permite que cada cajera se ejecute como un hilo independiente. Al iniciar con start(), se invoca el método run(), que procesa la compra de un cliente en paralelo, simulando igualmente los tiempos de atención de cada producto.

- La clase `Cliente.java` representa a los clientes del supermercado. Cada cliente tiene un nombre y un carrito de compras representado como un arreglo de enteros, donde cada número corresponde al tiempo necesario para procesar un producto. Esta clase se utiliza tanto en la ejecución secuencial como en la concurrente.

En cuanto a la ejecución, `la clase Main.java` es la encargada de simular el proceso secuencial. Allí se crean clientes y cajeras, y las compras se procesan una tras otra, mostrando el tiempo total que tarda el sistema sin utilizar hilos.

Para la ejecución concurrente, se presentan dos formas. La clase `MainRunnable.java` utiliza la interfaz Runnable, asociando cada cajera a un objeto que procesa en paralelo la compra de un cliente mediante hilos (Thread). En cambio, la clase `MainThread.java` emplea herencia de la clase Thread, creando instancias de CajeraThread que son lanzadas de manera concurrente para atender a los clientes.

## 4. Pruebas

Se realizaron diferentes pruebas con variación en el número de productos que cada cliente lleva en su carrito, para observar cómo cambia el comportamiento de la ejecución secuencial y concurrente.

### 4.1 Prueba 1 - PC1 y PC2 (valores por defecto en el código):
En esta primera prueba, se ejecutó el programa con los valores que vienen por defecto en el código. Los clientes tenían carritos con distinta cantidad de productos y se pudo evidenciar que la ejecución concurrente resulta más rápida en comparación con la secuencial, ya que las cajeras pueden trabajar al mismo tiempo. Cabe resaltar que los valores de tiempo de procesamiento en ambos PC´s fueron los mismos.
Se utilizaron los valores originales definidos en el código. 

Cliente 1: `[2, 2, 1, 5, 2, 3]`

Cliente 2: `[1, 3, 5, 1, 1]`

- Ejecución secuencial:
El procesamiento se hizo uno tras otro. La Cajera 1 atendió a Cliente 1 con un tiempo acumulado de aproximadamente 15 segundos, y luego la Cajera 2 atendió a Cliente 2 con unos 11 segundos. En total, el tiempo de la simulación fue cercano a 26 segundos.

- Ejecución concurrente:
Utilizando la implementación `Runnable`, la secuencia de procesamiento de los productos de los clientes es muy similar a la observada en la implementación con `Thread`. Ambas cajeras inician la atención de sus respectivos clientes de manera casi simultánea, lo que permite que el trabajo se realice en paralelo. En este escenario, Cliente 2 completa su compra en 11 segundos, mientras que Cliente 1 termina en 15 segundos, resultando en un tiempo total de ejecución para todo el sistema de 15 segundos, que corresponde al tiempo del cliente más lento. Esto demuestra que la concurrencia con Runnable mantiene un comportamiento consistente y eficiente, aprovechando la ejecución paralela para reducir el tiempo global.

La concurrencia permitió reducir el tiempo final en un 42% respecto al secuencial, mostrando  la ventaja de usar hilos cuando varias tareas independientes pueden ejecutarse en paralelo.

- Prueba PC2
<div align="center">
<img width="1300" height="361" alt="image" src="https://github.com/user-attachments/assets/623c4d62-0853-4587-9210-e0b53bcd5424" />
</div>
<div align="center">
<img width="1300" height="261" alt="image" src="https://github.com/user-attachments/assets/511e5f9d-d1bb-45df-bf78-a0d650933a2a" />
</div>


### 4.2 Prueba 2 - PC1 (mismo número de productos para ambos clientes):
En este caso, se modificaron los datos para que los dos clientes tuvieran la misma cantidad de productos en su carrito. Esto permitió observar un balance más uniforme en la carga de trabajo de las cajeras cuando se utilizó concurrencia, ya que ambas cajeras procesaron aproximadamente la misma cantidad de tiempo, optimizando mejor los recursos.
Se configuró que Cliente 1 y Cliente 2 tuvieran la misma cantidad de productos, por ejemplo, 6 productos cada uno. Los tiempos asignados fueron:

Cliente 1: `[7, 12, 6, 25, 20, 13]` (total de 17 segundos)

Cliente 2: `[7, 12, 6, 25, 20, 13]` (total de 15 segundos)

- Ejecución secuencial:
El proceso tardó en total 166 segundos (83 + 83), ya que un cliente se atendía completamente antes de iniciar con el otro.

- Ejecución concurrente:
En la ejecución concurrente utilizando `Thread`, ambos clientes comienzan a ser atendidos al mismo tiempo, desde el segundo 0, y logran finalizar sus compras simultáneamente en 83 segundos, demostrando que la concurrencia permite que los procesos se desarrollen de manera paralela sin que uno dependa del otro. De manera similar, al emplear la implementación basada en `Runnable`, se observa el mismo patrón: ambos clientes finalizan sus compras al mismo tiempo, también en 83 segundo.

Este caso muestra que cuando los clientes tienen carritos de tamaño similar, la concurrencia aprovecha mucho mejor los recursos disponibles. Con cargas idénticas, la concurrencia maximiza la eficiencia. El tiempo se reduce exactamente a la mitad frente a la secuencial, y ambas formas de concurrencia (Thread y Runnable) se comportan de manera idéntica.

### 4.3 Prueba 3 - PC2 (diferente número de productos para los clientes):
Aquí se probaron nuevos valores en los que los clientes no tenían la misma cantidad de productos. El resultado evidenció que, aunque la concurrencia sigue siendo más eficiente que la ejecución secuencial, una cajera finaliza antes que la otra debido a la diferencia en la carga de trabajo. Este escenario refleja cómo la distribución desigual de tareas puede afectar el aprovechamiento de la concurrencia.
Se probó una situación con desbalance en el número de productos de cada cliente.

Cliente 1: `[10, 5, 8, 1, 1, 22]` → 6 productos
Cliente 2: `[7, 27, 2, 13, 3]` → 5 productos

- Ejecución secuencial:
El tiempo total fue la suma de ambos, es decir, 112 segundos. Ya que, la Cajera 1 termina Cliente 1 en 60s, luego Cajera 2 termina Cliente 2 en 52s.

- Ejecución concurrente:
Tanto en la implementación concurrente utilizando `Thread` como en la que utiliza `Runnable`, ambos clientes inician su atención casi que al mismo tiempo. En estas ejecuciones, la Cajera 1 completa la compra de Cliente 1 en 60 segundos, mientras que la Cajera 2 termina con Cliente 2 en 52 segundos, resultando en un tiempo global de 60 segundos, que corresponde al tiempo del cliente que tarda más. Esto indica que, independientemente de si se usa Thread o Runnable, la concurrencia permite que las tareas se realicen de manera paralela y eficiente, reduciendo el tiempo total de atención.

La concurrencia sigue siendo más eficiente (reducción de 46%). Sin embargo, la diferencia de carga de trabajo provoca que una cajera quede inactiva antes que la otra. Esto refleja que la concurrencia mejora los tiempos, pero no garantiza que los recursos estén balanceados si los clientes tienen cargas muy diferentes, es decir, la eficiencia máxima se logra cuando los clientes tienen cargas similares.

- Prueba PC2
<div align="center">
<img width="1300" height="420" alt="image" src="https://github.com/user-attachments/assets/95c33d08-267f-44d8-8bb0-3b308127f513" />
</div>
<div align="center">
<img width="1300" height="256" alt="image" src="https://github.com/user-attachments/assets/db5b70e3-91a5-41ae-bf02-3e35dd737df9" />
</div>

--- 

## Conclusiones

Los resultados demuestran que el uso de concurrencia en Java permite optimizar tiempos de ejecución en sistemas donde existen tareas independientes (en este caso, la atención de distintos clientes). Mientras que en la ejecución secuencial el tiempo total es la suma de todas las atenciones, en la ejecución concurrente el tiempo global queda determinado por la tarea más larga.

Cuando los clientes tienen cargas similares (Prueba 2 PC1), la concurrencia muestra su máximo potencial, ya que los tiempos se reducen casi a la mitad y las cajeras trabajan de forma balanceada. Sin embargo, cuando las cargas son muy distintas (Prueba 3 PC2), aunque la concurrencia sigue siendo más eficiente que el proceso secuencial, algunos recursos no se aprovechan completamente, ya que una cajera termina antes que la otra.

En conclusión, se evidenció de manera práctica los beneficios de la concurrencia, a la vez que resalta la importancia de considerar la distribución de cargas de trabajo para aprovechar al máximo los recursos del sistema.


