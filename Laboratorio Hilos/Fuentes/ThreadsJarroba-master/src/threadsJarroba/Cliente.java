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

 /* Representa a un cliente
  Esta clase actúa como modelo de datos usado por las cajeras para simular el proceso */
public class Cliente {

	private String nombre;
	// Cada posición representa un producto y el valor indica el tiempo de procesamiento
	private int[] carroCompra;

	// Constructores, Getters y Setters
	public Cliente() {
	}

	public Cliente(String nombre, int[] carroCompra) {
		this.nombre = nombre;
		this.carroCompra = carroCompra;
	}

	public String getNombre() { return nombre; }

	public void setNombre(String nombre) { this.nombre = nombre; }

	public int[] getCarroCompra() { return carroCompra; }

	public void setCarroCompra(int[] carroCompra) { this.carroCompra = carroCompra; }

}
