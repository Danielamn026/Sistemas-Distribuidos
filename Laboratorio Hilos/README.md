# Simulación de Cajeras en Supermercado (Java Threads)

Proyecto que implementa la simulación de un supermercado donde cajeras procesan las compras de clientes.  
El objetivo es comparar la diferencia entre la ejecución **secuencial** y la ejecución **paralela** mediante el uso de **hilos en Java** (`Thread` y `Runnable`).  

Presentado por:  
- Daniela Medina  

---

## 1. ¿Qué hace?

El programa simula un escenario en el que clientes llegan con un carrito de compras, y cada cajera procesa los productos del cliente.  

- **Ejecución secuencial**: Una cajera atiende a un cliente y, cuando termina, comienza con el siguiente.  
- **Ejecución concurrente con `Thread`**: Cada cajera trabaja en su propio hilo, atendiendo clientes al mismo tiempo.  
- **Ejecución concurrente con `Runnable`**: Se crea un `Runnable` para representar el trabajo de cada cajera y se lanza en hilos independientes.  

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
## 3. Documentación del Código

- La clase `Cajera.java` representa a una cajera que procesa compras de manera secuencial. A través de su método procesarCompra(), recibe un cliente y el tiempo inicial de la simulación, recorriendo el carrito de productos y simulando con pausas (Thread.sleep()) el tiempo que tardaría en atender cada uno. Esta clase no hace uso de hilos.

- La clase `CajeraThread.java` extiende de Thread y permite que cada cajera se ejecute como un hilo independiente. Al iniciar con start(), se invoca el método run(), que procesa la compra de un cliente en paralelo, simulando igualmente los tiempos de atención de cada producto.

- Por su parte, la clase `Cliente.java` representa a los clientes del supermercado. Cada cliente tiene un nombre y un carrito de compras representado como un arreglo de enteros, donde cada número corresponde al tiempo necesario para procesar un producto. Esta clase se utiliza tanto en la ejecución secuencial como en la concurrente.

En cuanto a la ejecución, la clase `Main.java` es la encargada de simular el proceso secuencial. Allí se crean clientes y cajeras, y las compras se procesan una tras otra, mostrando el tiempo total que tarda el sistema sin utilizar hilos.  

Para la ejecución concurrente, se presentan dos formas. La clase `MainRunnable.java` utiliza la interfaz Runnable, asociando cada cajera a un objeto que procesa en paralelo la compra de un cliente mediante hilos (Thread). En cambio, la clase `MainThread.java` hace uso de herencia de la clase Thread, creando instancias de CajeraThread que son lanzadas de manera concurrente para atender a los clientes.



