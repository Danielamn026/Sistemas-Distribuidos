# Taller de Sistemas Distribuidos – Hadoop HDFS

Presentado por:
- Daniela Medina
- Isabella Palacio
- Sergio Ortiz

---

## 1. Introducción

En este taller se implementó un sistema de archivos distribuidos utilizando Hadoop HDFS con tres computadores:  
- Uno configurado como **NameNode** (nodo maestro).  
- Dos configurados como **DataNodes** (nodos esclavos).

El **NameNode** cumple la función de controlador central del sistema, encargado de gestionar los metadatos y la ubicación de los archivos dentro del clúster, mientras que los **DataNodes** se encargan de almacenar físicamente los bloques de datos que componen dichos archivos.

El objetivo principal de la práctica fue comprender el funcionamiento de un entorno distribuido de almacenamiento, los mecanismos de replicación de datos y tolerancia a fallos, así como la interacción entre los nodos que conforman el sistema.

El proceso incluyó:  
- Instalación de Java y Hadoop.  
- Configuración de variables de entorno.  
- Edición de archivos de configuración (`core-site.xml`, `hdfs-site.xml`, `hadoop-env.sh`, `workers`).  
- Conexión SSH entre nodos.  
- Verificación mediante `start-dfs.sh` y la interfaz web del HDFS.

---

## 2. Configuración del DataNode

#### sudo apt update
Actualiza la lista de paquetes disponibles en los repositorios del sistema, para que se instalen las versiones mas recientes. 
