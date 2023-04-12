package org.sdg3.solicitante;

import org.sdg3.entities.Prestamo;
import java.util.ArrayList;

import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZContext;

public class Solicitante {
    // Constantes
    private static String endpointGestor = "tcp://*:5555"; // Endpoint del gestor con el que se comunica

    // Sockets
    private static ZMQ.Socket socketREQ;

    // Prestamos realizados
    ArrayList<Prestamo> prestamosVig = new ArrayList<Prestamo>();

    public static void main(String[] args) throws Exception {
        try(ZContext context = new ZContext()){
            // Socket de comunicacion con el gestor
            socketREQ = context.createSocket(SocketType.REQ);
            socketREQ.connect(endpointGestor);


        }
    }

    // Metodo que realiza una solicitud de prestamo
    private void solicitarPrestamo(){

    }

    // Metodo que realiza una renovacion de prestamo
    private void renovarPrestamo(){

    }

    // Metodo que realiza una devolucion de prestamo
    private void devolverPrestamo(){

    }
}
