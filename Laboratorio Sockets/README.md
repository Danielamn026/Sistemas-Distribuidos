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

La clase `Cajera.java` representa a una cajera que procesa compras de manera secuencial. A través de su método procesarCompra(), recibe un cliente y el tiempo inicial de la simulación, recorriendo el carrito de productos y simulando con pausas (Thread.sleep()) el tiempo que tardaría en atender cada uno. Esta clase no hace uso de hilos.

La clase `CajeraThread.java` extiende de Thread y permite que cada cajera se ejecute como un hilo independiente. Al iniciar con start(), se invoca el método run(), que procesa la compra de un cliente en paralelo, simulando igualmente los tiempos de atención de cada producto.

Por su parte, la clase `Cliente.java` represneta a los clientes del supermercado. Cada cliente tiene un nombre y un carrito de compras representado como un arreglo de enteros, donde cada número corresponde al tiempo necesario para procesar un producto. Esta clase se utiliza tanto en la ejecución secuencial como en la concurrente.

En cuanto a la ejecución, `la clase Main.java` es la encargada de simular el proceso secuencial. Allí se crean clientes y cajeras, y las compras se procesan una tras otra, mostrando el tiempo total que tarda el sistema sin utilizar hilos.

Para la ejecución concurrente, se presentan dos formas. La clase `MainRunnable.java` utiliza la interfaz Runnable, asociando cada cajera a un objeto que procesa en paralelo la compra de un cliente mediante hilos (Thread). En cambio, la clase `MainThread.java` emplea herencia de la clase Thread, creando instancias de CajeraThread que son lanzadas de manera concurrente para atender a los clientes.

## 4. Pruebas

Se realizaron diferentes pruebas con variación en el número de productos que cada cliente lleva en su carrito, para observar cómo cambia el comportamiento de la ejecución secuencial y concurrente.

### 4.1 Prueba 1 - PC1 y PC2 (valores por defecto en el código):
En esta primera prueba, se ejecutó el programa con los valores que vienen por defecto en el código. Los clientes tenían carritos con distinta cantidad de productos y se pudo evidenciar que la ejecución concurrente resulta más rápida en comparación con la secuencial, ya que las cajeras pueden trabajar al mismo tiempo. Cabe resaltar que los valores de tiempo de procesamiento en ambos PC´s fueron los mismos.
Se utilizaron los valores originales definidos en el código. 

Cliente 1 tenía un carrito con 6 productos de tiempos `[2, 2, 1, 5, 2, 3]`
Cliente 2 tenía 5 productos con tiempos `[1, 3, 5, 1, 1]`

-Ejecución secuencial:
El procesamiento se hizo uno tras otro. La Cajera 1 atendió a Cliente 1 con un tiempo acumulado de aproximadamente 15 segundos, y luego la Cajera 2 atendió a Cliente 2 con unos 11 segundos. En total, el tiempo de la simulación fue cercano a 26 segundos.

-Ejecución concurrente:
Al correr la simulación con hilos, ambas cajeras comenzaron al mismo tiempo. Cliente 1 tardó alrededor de 15 segundos y Cliente 2 aproximadamente 11 segundos. Sin embargo, como se ejecutaron en paralelo, el tiempo total estuvo determinado por el cliente más lento, es decir, 15 segundos.


- Prueba PC2
<div align="center">
<img width="1300" height="361" alt="image" src="https://github.com/user-attachments/assets/623c4d62-0853-4587-9210-e0b53bcd5424" />
</div>
<div align="center">
<img width="1300" height="261" alt="image" src="https://github.com/user-attachments/assets/511e5f9d-d1bb-45df-bf78-a0d650933a2a" />
</div>

La concurrencia permitió reducir el tiempo final en un 42% respecto al secuencial, mostrando  la ventaja de usar hilos cuando varias tareas independientes pueden ejecutarse en paralelo.

### 4.2 Prueba 2 - PC1 (mismo número de productos para ambos clientes):
En este caso, se modificaron los datos para que los dos clientes tuvieran la misma cantidad de productos en su carrito. Esto permitió observar un balance más uniforme en la carga de trabajo de las cajeras cuando se utilizó concurrencia, ya que ambas cajeras procesaron aproximadamente la misma cantidad de tiempo, optimizando mejor los recursos.
Se configuró que Cliente 1 y Cliente 2 tuvieran la misma cantidad de productos, por ejemplo, 6 productos cada uno. Los tiempos asignados fueron:

Cliente 1: `[2, 4, 3, 2, 5, 1]` (total de 17 segundos)

Cliente 2: `[3, 2, 2, 4, 1, 3]` (total de 15 segundos)

- Ejecución secuencial:
El proceso tardó en total 32 segundos (17 + 15), ya que un cliente se atendía completamente antes de iniciar con el otro.

- Ejecución concurrente:
Al ejecutarse de manera paralela, el tiempo dependió por el cliente más lento. En este caso, la Cajera de Cliente 1 tardó 17 segundos, mientras que la de Cliente 2 solo 15. Por lo tanto, el tiempo final fue de 17 segundos.

Este caso muestra que cuando los clientes tienen carritos de tamaño similar, la concurrencia aprovecha mucho mejor los recursos disponibles. Se redujo el tiempo en un 47%, y además la carga de trabajo estuvo balanceada entre ambas cajeras.

### 4.3 Prueba 3 - PC2 (diferente número de productos para los clientes):
Aquí se probaron nuevos valores en los que los clientes no tenían la misma cantidad de productos. El resultado evidenció que, aunque la concurrencia sigue siendo más eficiente que la ejecución secuencial, una cajera finaliza antes que la otra debido a la diferencia en la carga de trabajo. Este escenario refleja cómo la distribución desigual de tareas puede afectar el aprovechamiento de la concurrencia.
Se probó una situación con desbalance en el número de productos de cada cliente.

Cliente 1: `[2, 1, 3, 4]` (total de 10 segundos)
Cliente 2: `[5, 2, 3, 2, 4, 1, 3, 2]` (total de 22 segundos)

- Ejecución secuencial:
El tiempo total fue la suma de ambos, es decir, 32 segundos.

- Ejecución concurrente:
Como ambos clientes fueron atendidos en paralelo, el tiempo final dependió del cliente con mayor carga. Cliente 1 terminó en solo 10 segundos, pero Cliente 2 tardó 22, por lo que el tiempo total fue de 22 segundos.

Aunque la concurrencia mejoró la eficiencia en un 31%, se evidenció que la distribución desigual de trabajo afecta el aprovechamiento. Mientras una cajera quedaba desocupada después de 10 segundos, la otra seguía trabajando por 12 segundos más. Esto refleja que la concurrencia mejora los tiempos, pero no garantiza que los recursos estén balanceados si los clientes tienen cargas muy diferentes.

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


