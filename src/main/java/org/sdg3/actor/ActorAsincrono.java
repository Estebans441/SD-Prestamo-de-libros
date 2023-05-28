package org.sdg3.actor;

import org.sdg3.entities.Prestamo;
import org.sdg3.persistencia.IBDConector;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.StringTokenizer;

public class ActorAsincrono {
    // Constantes
    private static String[] ipSede = {"10.43.100.191", "10.43.100.187"};

    // Bases de datos de las sedes
    private static IBDConector bdc1;
    private static IBDConector bdc2;

    // Sockets
    private static ZMQ.Socket socketSUB;

    public static void main(String[] args) throws Exception {
        try (ZContext context = new ZContext()) {
            //  Socket suscriptor al topico indicado en argumentos
            socketSUB = context.createSocket(SocketType.SUB);
            socketSUB.connect("tcp://"+ipSede[Integer.parseInt(args[0])]+":4444");
            String filtro = args[1];
            socketSUB.subscribe(filtro.getBytes(ZMQ.CHARSET));

            // Busca el registro del CentralServer
            Registry registry = LocateRegistry.getRegistry(ipSede[0], 8888);
            Registry registry2 = LocateRegistry.getRegistry(ipSede[1], 8888);

            // Buscar el objeto timeServer en el registro y si lo encuentra, crear el objeto local
            bdc1 = (IBDConector) registry.lookup("dbconector0");
            bdc2 = (IBDConector) registry2.lookup("dbconector1");

            while (!Thread.currentThread().isInterrupted()) {
                String tipo = socketSUB.recvStr();
                Prestamo prestamo = new Prestamo(socketSUB.recv());

                System.out.println("--------------------------------------------");
                System.out.println("Tipo: "+filtro);
                System.out.println("Codigo de libro: " + prestamo.getLibro().getCodigo());
                System.out.println("Usuario: " + prestamo.getIdCliente());
                System.out.println("--------------------------------------------");

                if(tipo.equals("R"))
                    renovarPrestamo(prestamo);
                else
                    devolverPrestamo(prestamo);
            }
        }
    }

    // Metodo que se encarga de procesar una devolucion en la base de datos
    private static void devolverPrestamo(Prestamo prestamo) throws RemoteException {
        // TODO: acquire
        if(bdc1.devolverPrestamo(prestamo) && bdc2.devolverPrestamo(prestamo))
            System.out.println("Prestamo devuelto");
        else
            System.out.println("Error devolviendo el prestamo");
        // TODO: release
    }

    // Metodo que se encarga de procesar una renovacion en la base de datos
    private static void renovarPrestamo(Prestamo prestamo) throws RemoteException {
        // TODO: acquire
        if(bdc1.renovarPrestamo(prestamo) && bdc2.renovarPrestamo(prestamo))
            System.out.println("Prestamo renovado");
        else
            System.out.println("Error renovando el prestamo");
        // TODO: release
    }
}
