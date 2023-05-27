package org.sdg3.actor;

import org.sdg3.entities.Prestamo;
import org.zeromq.ZMQ;

public class ActorAsincrono {
    // Constantes
    private static String endpointGestor = "tcp://*:5557"; // Endpoint del gestor con el que se comunica

    // Sockets
    private static ZMQ.Socket socketSUB;

    public static void main(String[] args) throws Exception {

    }

    // Metodo que se encarga de procesar una devolucion en la base de datos
    private void devolverPrestamo(Prestamo prestamo){

    }
}
