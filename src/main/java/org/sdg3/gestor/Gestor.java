package org.sdg3.gestor;

import org.sdg3.entities.Prestamo;
import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZContext;

public class Gestor {
    // Constantes
    private static String endpoint = "tcp://*:5555"; // endpoint propio
    private static String endpointSolicitudes; // endpoint del actor de solicitudes

    // Sockets
    private static ZMQ.Socket socketPUB; // Publica solicitudes de renovacion y devolucion
    private static ZMQ.Socket socketREP; // Responde a los procesos solicitantes
    private static ZMQ.Socket socketREQ; // Se comunica con el actor de solicitud

    // Prestamo que se esta procesando
    private Prestamo prestamoSolicitante;

    public static void main(String[] args) throws Exception {
        try(ZContext context = new ZContext()){
            // Socket de comunicacion con clientes
            socketREP = context.createSocket(SocketType.REP);
            socketREP.bind(endpoint);

            // Inicia la atencion a solicitudes
            while (!Thread.currentThread().isInterrupted()) {

            }
        }
    }

    // Metodo que procesa una solicitud de prestamo
    private void solicitarPrestamo(Prestamo prestamo){

    }

    // Metodo que procesa una renovacion de prestamo
    private void renovarPrestamo(Prestamo prestamo){

    }

    // Metodo que procesa una devolucion de prestamo
    private void devolverPrestamo(Prestamo prestamo){

    }
}
