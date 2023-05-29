package org.sdg3.mutex;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.LinkedList;
import java.util.Queue;


public class Mutex {
    private static boolean enUso;

    public static void main(String[] args) {
        Integer turno = 0;
        Queue<Integer> colaEspera = new LinkedList<>();

        try(ZContext context = new ZContext()) {
            // Socket de comunicacion con actores
            ZMQ.Socket socketREP = context.createSocket(SocketType.REP);
            socketREP.bind("tcp://10.43.100.191:9999");

            // Publica el siguiente turno
            ZMQ.Socket socketPUB = context.createSocket(SocketType.PUB);
            socketPUB.bind("tcp://10.43.100.191:9998");

            // Atiende solicitudes de actores
            while (!Thread.currentThread().isInterrupted()) {
                String tipo = socketREP.recvStr();

                if(tipo.equals("A")){ // acquire
                    System.out.println("--------------------------------------------");
                    System.out.println("Solicitud de acquire recibida");
                    System.out.println("--------------------------------------------");

                    // Si el recurso no esta en uso publica el siguiente turno
                    if(!enUso){
                        enUso = true;
                        socketREP.send("ok");
                        System.out.println("\t > Recurso libre, consediendo acceso");
                    }

                    // Si esta en uso lo pone en la cola de espera
                    else{
                        // Le envia el turno correspondiente al actor
                        socketREP.send(String.valueOf(turno));
                        colaEspera.add(turno);
                        System.out.println("\t > Recurso en uso, turno " + turno);
                        // prepara siguiente turno
                        turno++;
                    }
                }
                else { // release
                    System.out.println("--------------------------------------------");
                    System.out.println("Solicitud de release recibida");
                    System.out.println("--------------------------------------------");
                    enUso = false;
                    socketREP.send("ok");
                    System.out.println("\t > Recurso liberado");

                    // Si la cola no esta vacia (hay procesos en espera)
                    if(!colaEspera.isEmpty()){
                        String siguiente = String.valueOf(colaEspera.poll());
                        System.out.println("\t > Consediendo acceso al turno " + siguiente);
                        enUso = true;
                        socketPUB.send(siguiente + " ok");
                    }
                }

                System.out.println("--------------------------------------------");
                System.out.println("Estado de la cola de espera");
                System.out.println("--------------------------------------------");
                for(Integer pendiente : colaEspera)
                    System.out.print(pendiente + " ");
                System.out.println();
            }
        }
    }
}



