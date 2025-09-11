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

/* Ejecuta la simulación del procesamiento de compras
   Usa la clase Cajera: procesamiento de clientes se realiza de manera secuencial */ 
public class Main {

	public static void main(String[] args) {
		
		// Se crean dos clientes  in carritos de compra, dos cajeras
		Cliente cliente1 = new Cliente("Cliente 1", new int[] { 2, 2, 1, 5, 2, 3 });
		Cliente cliente2 = new Cliente("Cliente 2", new int[] { 1, 3, 5, 1, 1 });

		Cajera cajera1 = new Cajera("Cajera 1");
		Cajera cajera2 = new Cajera("Cajera 2");

		// Tiempo inicial de referencia
		long initialTime = System.currentTimeMillis();

		// Procesamiento secuencial de las compras
		cajera1.procesarCompra(cliente1, initialTime);
		cajera2.procesarCompra(cliente2, initialTime);
	}
}