package org.sdg3.gestor;

import org.sdg3.entities.Prestamo;
import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZContext;

import java.io.IOException;

public class Gestor {
    // Constantes
    private static String[] ipSede = {"10.43.100.191", "10.43.100.187"};

    // Sockets
    private static ZMQ.Socket socketPUB; // Publica solicitudes de renovacion y devolucion
    private static ZMQ.Socket socketREP; // Responde a los procesos solicitantes
    private static ZMQ.Socket socketREQ; // Se comunica con el actor de solicitud

    public static void main(String[] args) throws Exception {
        String endpoint = "tcp://"+ipSede[Integer.parseInt(args[0])]+":4444"; // endpoint broker
        String endpointBroker = "tcp://"+ipSede[Integer.parseInt(args[0])]+":5556"; // endpoint broker
        String endpointSolicitudes = "tcp://"+ipSede[Integer.parseInt(args[0])]+":6666"; // endpoint del actor de solicitudes (sincrono)

        try(ZContext context = new ZContext()){
            // Socket de comunicacion con clientes
            socketREP = context.createSocket(SocketType.REP);
            socketREP.connect(endpointBroker);

            // Socket de comunicacion con el actor de solicitudes
            socketREQ = context.createSocket(SocketType.REQ);
            socketREQ.connect(endpointSolicitudes);

            // Socket de comunicacion con los actores asicronos
            socketPUB = context.createSocket(SocketType.PUB);
            socketPUB.bind(endpoint);

            // Inicia la atencion a solicitudes
            while (!Thread.currentThread().isInterrupted()) {
                // Recibe el tipo de requerimiento
                String tipo = socketREP.recvStr();

                // Recibe el prestamo del requerimiento
                Prestamo prestamoSolicitante = new Prestamo(socketREP.recv());

                // Tipo Solicitud
                switch (tipo) {
                    case "S" -> solicitarPrestamo(prestamoSolicitante);

                    // Tipo Renovacion
                    case "R" -> renovarPrestamo(prestamoSolicitante);

                    // Tipo Devolucion
                    case "D" -> devolverPrestamo(prestamoSolicitante);
                }
            }
        }
    }

    // Metodo que procesa una solicitud de prestamo
    private static void solicitarPrestamo(Prestamo prestamo) throws IOException {
        // Muestra la informacion del requerimiento por consola
        System.out.println("--------------------------------------------");
        System.out.println("Tipo: S");
        System.out.println("Codigo de libro: " + prestamo.getLibro().getCodigo());
        System.out.println("Usuario: " + prestamo.getIdCliente());
        System.out.println("--------------------------------------------");
        System.out.println("\t> Solicitando Prestamo...");
        socketREQ.send(prestamo.serializar());
        String respuesta = socketREQ.recvStr();
        socketREP.send(respuesta);
    }

    // Metodo que procesa una renovacion de prestamo
    private static void renovarPrestamo(Prestamo prestamo) throws  Exception{
        // Muestra la informacion del requerimiento por consola
        System.out.println("--------------------------------------------");
        System.out.println("Tipo: R");
        System.out.println("Codigo de libro: " + prestamo.getLibro().getCodigo());
        System.out.println("Usuario: " + prestamo.getIdCliente());
        System.out.println("--------------------------------------------");
        System.out.println("\t> Renovando Prestamo...");

        // Envia respuesta al PS
        socketREP.send("ok");
        System.out.println("\t> Confirmacion renovacion enviada a PS...");
        System.out.println("\t> Publicando requerimiento...");
        socketPUB.sendMore("R");
        socketPUB.send(prestamo.serializar());
    }

    // Metodo que procesa una devolucion de prestamo
    private static void devolverPrestamo(Prestamo prestamo) throws IOException {
        System.out.println("--------------------------------------------");
        System.out.println("Tipo: D");
        System.out.println("Codigo de libro: " + prestamo.getLibro().getCodigo());
        System.out.println("Usuario: " + prestamo.getIdCliente());
        System.out.println("--------------------------------------------");
        // Envia respuesta al PS
        socketREP.send("ok");
        System.out.println("\t> Confirmacion de devolucion enviada a PS...");
        System.out.println("\t> Publicando requerimiento...");
        socketPUB.sendMore("D");
        socketPUB.send(prestamo.serializar());
    }
}
