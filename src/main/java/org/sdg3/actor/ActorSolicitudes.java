package org.sdg3.actor;

import org.sdg3.entities.Libro;

import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZContext;

public class ActorSolicitudes {
    // Constantes
    private static String endpoint = "tcp://*:5556"; // Endpoint del gestor con el que se comunica

    // Sockets
    private static ZMQ.Socket socketREP;

    public static void main(String[] args) throws Exception {

    }

    // Metodo que valida las existencias de cierto libro
    private boolean validarExistencia(Libro libro){
        return true;
    }
}
