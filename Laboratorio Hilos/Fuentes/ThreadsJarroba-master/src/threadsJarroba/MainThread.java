/**************************************************************
         		Pontificia Universidad Javeriana
     Autor: Daniela Medina
     Fecha: 12 Septiembre 2025
     Materia: Sistemas Distribuidos
     Tema: Laboratorio de Hilos
****************************************************************/
package threadsJarroba;

/**
 * 
 * @author Daniela Medina
 */

 /* Demuestra el uso de hilos mediante la simulación mediante la simulacion de cajerasc*/
public class MainThread {

	public static void main(String[] args) {

		// Se crean dos clientes con sus listas de productos representad
		Cliente cliente1 = new Cliente("Cliente 1", new int[] { 2, 2, 1, 5, 2, 3 });
		Cliente cliente2 = new Cliente("Cliente 2", new int[] { 1, 3, 5, 1, 1 });

		// Tiempo inicial de referencia
		long initialTime = System.currentTimeMillis();
		
		/* Cada cajera es un hilo que procesa la compra de un cliente
		//Se crea una instancia de CajeraThread para cada cajera, pasando el nombre de la cajera, el cliente y el tiempo inicial */
		CajeraThread cajera1 = new CajeraThread("Cajera 1", cliente1, initialTime);
		CajeraThread cajera2 = new CajeraThread("Cajera 2", cliente2, initialTime);
		
		// Se inician los hilos con start()
		cajera1.start();
		cajera2.start();
	}
}