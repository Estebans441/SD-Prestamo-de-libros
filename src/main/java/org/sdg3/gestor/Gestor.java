package org.sdg3.gestor;

import org.sdg3.entities.Prestamo;
import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZContext;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class Gestor {
    // Constantes
    private static String[] ipSede = {"10.43.100.191", "10.43.100.187"};

    static ArrayList<Prestamo> prestamos;

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


            // Obtiene los prestamos vigentes de la Base de datos
            socketREQ.send("A");
            ByteArrayInputStream bis = new ByteArrayInputStream(socketREQ.recv());
            ObjectInputStream entrada = new ObjectInputStream(bis);
            prestamos = (ArrayList<Prestamo>) entrada.readObject();

            System.out.println("Prestamos vigentes: ");
            for(Prestamo prestamo : prestamos){
                System.out.println("--------------------------------------------");
                System.out.println("Codigo de libro: " + prestamo.getLibro().getCodigo());
                System.out.println("Usuario: " + prestamo.getIdCliente());
                System.out.println("Fecha inicio: " + prestamo.getF_inicio());
                System.out.println("Fecha fin: " + prestamo.getF_fin());
                System.out.println("--------------------------------------------");
            }


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
        socketREQ.sendMore("S");
        socketREQ.send(prestamo.serializar());
        String respuesta = socketREQ.recvStr();
        if(respuesta.equals("ok"))
            prestamos.add(prestamo);
        socketREP.send(respuesta);
    }

    // Metodo que procesa una renovacion de prestamo
    private static void renovarPrestamo(Prestamo prestamo) throws  Exception{
        // Busca el prestamo
        int index = 0;
        boolean encontrado = false;
        for(Prestamo prestamo1 : prestamos){
            if(Objects.equals(prestamo1.getIdCliente(), prestamo.getIdCliente()) && prestamo1.getLibro().getCodigo().equals(prestamo.getLibro().getCodigo())){
                prestamo = prestamo1;
                index = prestamos.indexOf(prestamo);
                encontrado = true;
            }
        }

        if(!encontrado){
            prestamo.setF_inicio(new Date());
            prestamo.setF_fin(new Date());
            prestamo.renovar();
        }

        // Muestra la informacion del requerimiento por consola
        System.out.println("--------------------------------------------");
        System.out.println("Tipo: R");
        System.out.println("Codigo de libro: " + prestamo.getLibro().getCodigo());
        System.out.println("Usuario: " + prestamo.getIdCliente());
        System.out.println("--------------------------------------------");
        System.out.println("\t> Renovando Prestamo...");
        System.out.println("Fecha anterior: " + prestamo.getF_fin());
        System.out.println("Nueva fecha: " + prestamo.renovar());

        // Actualiza la lista de prestamos vigentes
        if(encontrado)
            prestamos.set(index, prestamo);

        // Envia respuesta al PS
        socketREP.send(prestamo.serializar());
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

        prestamos.removeIf(prestamo1 -> Objects.equals(prestamo1.getIdCliente(), prestamo.getIdCliente()) && prestamo1.getLibro().getCodigo().equals(prestamo.getLibro().getCodigo()));

        // Envia respuesta al PS
        socketREP.send("ok");
        System.out.println("\t> Confirmacion de devolucion enviada a PS...");
        System.out.println("\t> Publicando requerimiento...");
        socketPUB.sendMore("D");
        socketPUB.send(prestamo.serializar());
    }
}
