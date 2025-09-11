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

 /* Demuestra el uso de hilos medinate la aplicación de la interfaz */
public class MainRunnable implements Runnable{
	
	private Cliente cliente;
	private Cajera cajera;
	private long initialTime;
	
	public MainRunnable (Cliente cliente, Cajera cajera, long initialTime){
		this.cajera = cajera;
		this.cliente = cliente;
		this.initialTime = initialTime;
	}

	/* Crea dos clientes, dos cajeras y simula el procesamiento de
       sus compras en paralelo */
	public static void main(String[] args) {
		
		Cliente cliente1 = new Cliente("Cliente 1", new int[] { 2, 2, 1, 5, 2, 3 });
		Cliente cliente2 = new Cliente("Cliente 2", new int[] { 1, 3, 5, 1, 1 });
		
		Cajera cajera1 = new Cajera("Cajera 1");
		Cajera cajera2 = new Cajera("Cajera 2");
		
		// Tiempo inicial de referencia
		long initialTime = System.currentTimeMillis();
		
		// Procesos concurrentes con Runnable
		Runnable proceso1 = new MainRunnable(cliente1, cajera1, initialTime);
		Runnable proceso2 = new MainRunnable(cliente2, cajera2, initialTime);
		
		new Thread(proceso1).start();
		new Thread(proceso2).start();

	}
	/* Ejecuta el proceso de compra de un cliente utilizando la
    cajera asignada. */
	@Override
	public void run() {
		this.cajera.procesarCompra(this.cliente, this.initialTime);
	}
}
