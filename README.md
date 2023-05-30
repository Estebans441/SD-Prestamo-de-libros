# SD-Prestamo-de-libros
Sistema distribuido de prestamo de libros con manejo de concurrencia y tolerancia parcial a fallos.

## Instrucciones de ejecucion
Para la ejecucion del sistema es necesario tener en cuenta que se conforma por dos sedes ubicadas en distintos dispositivos. En el codigo y para las pruebas se uso:
- Sede 1: 10.43.100.191
- Sede 2: 10.43.100.187

En caso de querer modificar estas ips es requerido buscarlas en cada clase y modificarlas.

Posteriormente se define una de las sedes como la que contendra el nodo de exclusión mutua y ejecutar este.

 ### Para cada sede:
 En cada nodo de cada sede se debe pasar como argumento el numero de la sede a la que pertenece (0 o 1). El orden de ejecución de los nodos es el siguiente.
1. Conector de la base de datos.
2. Brokers
3. Actores sincronos
4. Actores asicronos (este debe ser ejecutado al menos dos veces, cada una con un segundo argumento que especificara el tipo: D para devolucion o R para renovacion)
5. Gestor y replicas
6. Solicitante

## Tecnologías empleadas ⚒
- Java 19: Lenguaje de programación utilizado para la estructuración e implementación del sistema.
- ZeroMQ: Biblioteca de red integrable. Otorga sockets con patrones de comunicacion entre procesos.
- Maven: Gestor de proyectos y dependencias.
- MySQL: Base de datos relacional open-source.
- Git: Versionamiento del código y flujo de trabajo.

## Desarrolladores 👨‍💻
- [Esteban Salazar Arbelaez](https://github.com/Estebans441)
- [Juan Francisco Ramirez Escobar](https://github.com/juanfra312003)
- [Javier Alejandro Moyano Cipamocha](https://github.com/Moyano1711)
- [Sara Lorena Suarez Villamizar](https://github.com/sara0328)
