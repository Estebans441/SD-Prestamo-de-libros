package org.sdg3.actor;

import org.sdg3.entities.Libro;

import org.sdg3.entities.Prestamo;
import org.sdg3.persistencia.BDConector;
import org.sdg3.persistencia.IBDConector;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Objects;

public class ActorSincrono {
    // Constantes
    private static String[] ipSede = {"10.43.100.191", "10.43.100.187"};

    // Sockets
    private static ZMQ.Socket socketREP;
    private static ZMQ.Socket socketREQ;

    // Bases de datos de las sedes
    private static IBDConector bdc1;
    private static IBDConector bdc2;

    public static void main(String[] args) throws Exception {
        String endpoint = "tcp://"+ipSede[Integer.parseInt(args[0])]+":6667"; // Endpoint del gestor con el que se comunica
        String endpointMutex = "tcp://10.43.100.191:7777"; // Endpoint del gestor con el que se comunica

        try(ZContext context = new ZContext()){
            // Socket de comunicacion con clientes
            socketREP = context.createSocket(SocketType.REP);
            socketREP.connect(endpoint);

            // Socket de comunicacion con mutex
            socketREQ = context.createSocket(SocketType.REQ);
            socketREQ.connect(endpointMutex);


            // Busca el registro del CentralServer
            Registry registry = LocateRegistry.getRegistry(ipSede[0], 8888);
            Registry registry2 = LocateRegistry.getRegistry(ipSede[1], 8888);

            // Buscar el objeto timeServer en el registro y si lo encuentra, crear el objeto local
            bdc1 = (IBDConector) registry.lookup("dbconector0");
            bdc2 = (BDConector) registry2.lookup("dbconector1");


            // Inicia la atencion a solicitudes
            while (!Thread.currentThread().isInterrupted()) {
                // Recibe el prestamo del requerimiento
                Prestamo prestamoSolicitante = new Prestamo(socketREP.recv());
                // TODO: Acquire mutex
                if(validarExistencias(prestamoSolicitante.getLibro())){
                    // TODO: Modificar BD
                    socketREP.send("ok");
                }
                else
                    socketREP.send("nok");
                // TODO: Release mutex
            }
        }
    }

    // Metodo que valida las existencias de cierto libro
    private static boolean validarExistencias(Libro libro) throws RemoteException {
        return bdc1.validarExistencias(libro.getCodigo()) && bdc2.validarExistencias(libro.getCodigo());
    }
}
