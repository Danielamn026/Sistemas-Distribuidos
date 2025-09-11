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

/* Representa a una cajera que procesa las compras de un cliente
No extiende de Thread ni implementa Runnable */
public class Cajera {

	private String nombre;

	// Constructores, Getters y Setters
	public Cajera() {
	}
	
	public Cajera(String nombre) {
		this.nombre = nombre;
	}

	public String getNombre() { return nombre; }

	public void setNombre(String nombre) { this.nombre = nombre; }
	
	/* Procesa la compra de un cliente
	Hay una gran diferencia con respecto a la otra cajera, dado que esta recibe por parametro el cliente y el tiempo inicial
	y no los tiene como atributos de la clase */
	public void procesarCompra(Cliente cliente, long timeStamp) {

		// Imprime en consola el inicio del proceso
		System.out.println("La cajera " + this.nombre + 
				" COMIENZA A PROCESAR LA COMPRA DEL CLIENTE " + cliente.getNombre() + 
				" EN EL TIEMPO: " + (System.currentTimeMillis() - timeStamp) / 1000	+
				"seg");
		
		// Recorre el arreglo de productos del cliente (cada tiempo de procesamiento)
		for (int i = 0; i < cliente.getCarroCompra().length; i++) {
			this.esperarXsegundos(cliente.getCarroCompra()[i]);
			System.out.println("Procesado el producto " + (i + 1) + 
					" ->Tiempo: " + (System.currentTimeMillis() - timeStamp) / 1000 + 
					"seg");
		}
		
		// Indicando que el producto fue procesado y el tiempo total transcurrido desde que empezó la simulación
		System.out.println("La cajera " + this.nombre + " HA TERMINADO DE PROCESAR " + 
							cliente.getNombre() + " EN EL TIEMPO: " + 
							(System.currentTimeMillis() - timeStamp) / 1000 + "seg");

	}

	/* Simula el tiempo de espera de un producto en el proceso de compra
	En caso de que el hilo se interrumpa, se captura la excepción y se reinterrumpe el hilo */
	private void esperarXsegundos(int segundos) {
		try {
			Thread.sleep(segundos * 1000);
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
	}
}