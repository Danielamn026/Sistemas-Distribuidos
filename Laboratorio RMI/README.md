# Sistema de préstamos de libros (Java RMI + PostgreSQL)

Proyecto que implementa un servicio remoto de biblioteca usando Java RMI en Java 21.
Permite préstamos, consultas y devoluciones de libros persistidos en PostgreSQL.
Está pensado para ejecutarse en dos computadores: uno con Servidor RMI + Base de Datos y otro(s) con Clientes.

Presentado por:
- Jorge Gomez
- Valeria Caycedo
- Jeisson Ruiz
- Daniela Medina
  
---

## 1. ¿Qué hace?

Objeto remoto `LibraryService` con operaciones:

- **Préstamo por ISBN**: `loanByIsbn(isbn, userId)` → autoriza o rechaza; si autoriza, retorna fecha de entrega (hoy + 1 semana).
- **Préstamo por título**: `loanByTitle(title, userId)` → igual que por ISBN pero buscando por título.
- **Consulta por ISBN**: `queryByIsbn(isbn)` → si existe y cuántos ejemplares disponibles hay.
- **Devolución**: `returnByIsbn(isbn, userId?)` → registra devolución (si `userId` es nulo, devuelve el préstamo más antiguo del ISBN).

**Concurrencia**: el servidor usa transacciones y bloqueos `SELECT ... FOR UPDATE` para evitar carreras cuando hay múltiples clientes.

---

## 2. Estructura
``` 
├── pom.xml
├── README.md
├── scripts
│   ├── ejecucion-cliente.sh
│   └── ejecucion-servidor.sh
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com
│   │   │       └── puj
│   │   │           ├── client
│   │   │           │   └── client.java
│   │   │           ├── dto
│   │   │           │   ├── Peticiones.java
│   │   │           │   ├── Respuesta.java
│   │   │           │   └── RespuestaPrestamo.java
│   │   │           ├── server
│   │   │           │   └── server.java
│   │   │           └── services
│   │   │               ├── ServicioBibliotecaImpl.java
│   │   │               └── ServicioBiblioteca.java
│   │   └── resources
│   │       └── schema.sql
│   └── test
│       └── java

```
---
## 3. Documentación del Código 

### 3.1 Scripts

Puente entre el código y el usuario, automatizando la configuración y arranque de los componentes para ejecutar el sistema. Simplifica la interacción con el proyecto, permitiendo levantar de forma fácil tanto el servisor como el cliente con una configuración adecuada. 

-  `ejecución-servidor.sh`:
Se encarga de iniciar el servidor RMI que ofrece el servicio de la biblioteca. Su propósito es configurar automáticamente la IP, el puerto, el nombre con el que se publicará el servicio y las credenciales de la BD PostgreSQL. Así, el servidor queda listo para recibir peticiones de los clientes.

- `ejecución-cliente.sh`:
Levantar el cliente RMI y conecta al servidor de la biblioteca. Su función es tomar los parámetros necesarios como la IP del servidor, el puerto de comunicación y el nombre del servicio registrado, para que el cliente pueda interactuar directamente con el servidor. Permite que el cliente invoque operaciones de préstamos, consultas y devoluciones.

### 3.2 Src/main > java/com/puj

#### 3.2.1 Client

-  `cliente.java`: Implementa el programa de consola que actúa como cliente del sistema. Su propósito es conectarse al servidor RMI, mostrar un menú interactivo al usuario y permitirle realizar operaciones.

#### 3.2.2 Dto

Contiene clases que sirven para transportar datos entre el cliente y el servidor en el sistema RMI. Como RMI debe enviar información a través de la red, se usan objetos serializables que organizan de manera clara y ordenada las peticiones y respuestas.

-  `Peticiones.java`: Respuesta a una consulta de un libro por ISBN. Indica si el libro existe, su nombre y cuántas copias están disponibles.
-  `Respuesta.java`: Respuestas a operaciones. Informa si la operación fue exitosa y un mensaje.
-  `RespuestaPrestamo.java`: Respuesta específica a una solicitud de préstamo. Indica si el libro fue prestado con éxito, un mensaje de estado y la fecha de vencimiento para la devolución.

#### 3.2.3 Server

-  `server.java`: Levanta el registro de RMI en un puerto específico y publicar en él la implementación del servicio ServicioBibliotecaImpl. Además, esta clase configura los parámetros de conexión a la base de datos (URL, usuario y contraseña) que usará el servicio.

#### 3.2.4 Services

Lógica principal del sistema, ya que contiene la definición del servicio remoto y su implementación.

-  `ServicioBiblioteca.java`: Interfaz remota RMI de la biblioteca.
-  `ServicioBibliotecaImpl.java`: Implementación del servicio. Se ejecuta la lógica: conexión a la BD, validaciones, manejo de préstamos, control de copias disponibles y registro de devoluciones.

### 3.3 Src/main > resourses

-  `schemas.sql`: Esquema de base de datos para el sistema de biblioteca. Define las tablas necesarias para almacenar los libros y los préstamos.

---

4. Base de datos

<div align="center">
  ![82a70904-ca3e-4dea-87f6-bd886e54d993](https://github.com/user-attachments/assets/07b5d39c-7d76-4ac2-94bf-baa5dcad0681)
</div>

La imagen corresponde a la **tabla books** de una base de datos en PostgreSQL. Esta tabla fue creada a partir del esquema definido en el código SQL y contiene tres columnas principales: **isbn**, **title** y **total_copies**. La primera funciona como clave primaria y almacena un identificador único para cada libro en formato de texto de hasta 20 caracteres. La segunda columna guarda el título del libro y la tercera refleja la cantidad de copias disponibles en la biblioteca, siempre con la condición de que no sea un valor negativo. Además de estas tres columnas, se muestra una cuarta llamada **loans**, que en realidad no forma parte física de la tabla books. Este campo extra aparece porque existe una relación entre books y la **tabla loans**, donde cada préstamo registrado en loans referencia el ISBN del libro correspondiente. En la imagen se observa como un acceso directo que permite navegar entre ambas tablas. En el contenido de la tabla se listan distintos libros de literatura clásica y contemporánea, como Cien años de soledad, Don Quijote de la Mancha o Rayuela, junto con su ISBN y el número de ejemplares disponibles, siendo una tabla pensada como el catálogo base sobre el cual se gestionan los préstamos del sistema de biblioteca.

## 5. Requisitos (Linux)

- Java 21+ (JDK)
- Maven 3.8+
- PostgreSQL 13+ (o compatible)
- Abrir el puerto TCP 1099 (RMI Registry)

---
## 6. Compilación (Linux)

```
chmod +x scripts/ejecucion-servidor.sh scripts/ejecucion-cliente.sh
mvn -q clean package
```
## 7. Ejecución (Linux)

- <h2>Servidor</h2>

```
# 1) Exporta variables

export RMI_HOST="10.10.10.5"
export RMI_PORT="1099"
export BIND_NAME="ServicioBiblioteca"
export DB_URL='jdbc:postgresql://127.0.0.1:5432/library'
export DB_USER='postgres'
export DB_PASS='postgres'

# 2) Abre el puerto del RMI Registry (Ubuntu con UFW)

sudo ufw allow 1099/tcp

# 3) Compila y arranca

mvn -q clean package
./scripts/ejecucion-servidor.sh
```

- <h2>Cliente</h2>

```
export SERVER_HOST="10.10.10.5"   # misma IP que RMI_HOST del servidor
export RMI_PORT="1099"
export BIND_NAME="ServicioBiblioteca"

./scripts/ejecucion-cliente.sh  

```
** No se exponen las credenciales de la base de datos por seguridad

## 8. Pruebas 

https://youtu.be/qbmjzDZDkIQ?si=D4bN__lURMrWpfNe

Se realizaron distintos casos de prueba con el propósito de asegurar el correcto envio de las peticiones y la llegada de las consultas al servidor. Así mismo, se realizaron pruebas de control de lógica (buen funcionamiento del sistema de biblioteca).

## 9. Observaciones y Conclusiones

El taller fue una buena forma de aprender cómo aplicar la comunicación remota entre procesos utilizando Java RMI (Remote Method Invocation). Este último permitió que los métodos en un servidor pudieran ser invocados desde un cliente, simulando el comportamiento de una aplicación local. 

El uso de Java RMI mostró cómo se puede crear un sistema cliente-servidor robusto, donde el cliente no tiene que preocuparse por los detalles internos del servidor. Esto refleja la idea de transparencia de localización, que es una característica principal de este tipo de arquitecturas.
Aunque RMI puede manejar múltiples clientes, no está diseñado para entornos de alta concurrencia a gran escala. Por lo tanto, se concluye que, para situaciones más complejas, sería necesario considerar arquitecturas basadas en microservicios, colas de mensajes o frameworks de comunicación distribuida más modernos.

Hay que resaltar que el uso de objetos serializables es muy importante para asegurar un flujo adecuado de datos en la red, evitando inconsistencias de comunicación. Por otro lado, se evidenció que las interfaces son de gran ayuda para separar la lógica de negocio de la lógica de comunicación. En general, la separación entre el cliente, el servidor y las interfaces remotas fue fundamental para lograr la modularidad del sistema, ya que esto no solo facilita su escalabilidad, sino que también permite agregar nuevos clientes o crear nuevas funcionalidades del servidor sin tener que modificar mucho la lógica del sistema. 

Otro aspecto clave fue la necesidad de implementar un esquema de control de concurrencia y sincronización en el servidor para manejar varias solicitudes concurrentes. Esto permitió observar la importancia de evitar situaciones de carrera y controlar la integridad de los datos, sobre todo en operaciones de préstamo y devolución. 

















