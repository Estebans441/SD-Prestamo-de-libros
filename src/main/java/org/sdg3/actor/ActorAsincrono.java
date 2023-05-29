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
    private static ZMQ.Socket socketREQMutex;
    private static ZMQ.Socket socketSUBMutex;

    public static void main(String[] args) throws Exception {
        try (ZContext context = new ZContext()) {
            //  Socket suscriptor al topico indicado en argumentos
            socketSUB = context.createSocket(SocketType.SUB);
            socketSUB.connect("tcp://"+ipSede[Integer.parseInt(args[0])]+":4444");
            String filtro = args[1];
            socketSUB.subscribe(filtro.getBytes(ZMQ.CHARSET));

            System.out.println("--------------------------------------------");
            System.out.println("Actor asincrono suscrito al topico " + filtro);
            System.out.println("Sede " + args[0]);
            System.out.println("--------------------------------------------");

            // Sockets para comunicacion con mutex
            socketREQMutex = context.createSocket(SocketType.REQ);
            socketREQMutex.connect("tcp://10.43.100.191:9999");

            socketSUBMutex = context.createSocket(SocketType.SUB);
            socketSUBMutex.connect("tcp://10.43.100.191:9998");

            // Busca el registro de los conectores de base de datos
            Registry registry = LocateRegistry.getRegistry(ipSede[0], 8888);
            Registry registry2 = LocateRegistry.getRegistry(ipSede[1], 8888);

            // Busca el objeto conector con la base de datos de cada sede
            bdc1 = (IBDConector) registry.lookup("dbconector0");
            bdc2 = (IBDConector) registry2.lookup("dbconector1");

            // Inicia atencion a solicitudes
            while (!Thread.currentThread().isInterrupted()) {
                // recibe el tipo
                String tipo = socketSUB.recvStr();
                // recibe el prestamo
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
        acquire(); // mutex
        if(bdc1.devolverPrestamo(prestamo) && bdc2.devolverPrestamo(prestamo))
            System.out.println("\t > Prestamo devuelto");
        else
            System.out.println("\t > Error devolviendo el prestamo");
        release(); // mutex
    }

    // Metodo que se encarga de procesar una renovacion en la base de datos
    private static void renovarPrestamo(Prestamo prestamo) throws RemoteException {
        acquire(); // mutex
        if(bdc1.renovarPrestamo(prestamo) && bdc2.renovarPrestamo(prestamo))
            System.out.println("\t > Prestamo renovado");
        else
            System.out.println("\t > Error renovando el prestamo");
        release(); // mutex
    }

    private static void acquire(){
        socketREQMutex.send("A");
        System.out.println(" - Solicitando acceso bd");
        String turno = socketREQMutex.recvStr();
        System.out.println("\t - Turno "+ turno);
        if(!turno.equals("ok")){ // El recurso no estaba libre espera su turno
            socketSUBMutex.subscribe(turno.getBytes(ZMQ.CHARSET));
            socketSUBMutex.recvStr();
        }
        System.out.println(" - Acceso adquirido");
    }

    private static void release(){
        socketREQMutex.send("R");
        socketREQMutex.recvStr();
        System.out.println(" - Acceso bd soltado");
    }
}
