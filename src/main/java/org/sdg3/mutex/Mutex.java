package org.sdg3.mutex;


import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.LinkedList;
import java.util.Queue;


public class Mutex {
    private static boolean enUso;

    public static void main(String[] args) throws Exception {
        Integer turno = 0;
        Queue<Integer> colaEspera = new LinkedList<>();

        try(ZContext context = new ZContext()) {
            // Socket de comunicacion con clientes
            ZMQ.Socket socketREP = context.createSocket(SocketType.REP);
            socketREP.bind("tcp://10.43.100.191:9999");

            // Publica solicitudes de renovacion y devolucion
            ZMQ.Socket socketPUB = context.createSocket(SocketType.PUB);
            socketPUB.bind("tcp://10.43.100.191:9998");

            // Atiende solicitudes de actores
            while (!Thread.currentThread().isInterrupted()) {
                // Recibe la solicitud (A = acquire, R = release)
                String tipo = socketREP.recvStr();
                // acquire
                if(tipo.equals("A")){
                    System.out.println("Solicitud de acquire recibida");
                    // Si el recurso no esta en uso publica el siguiente turno
                    if(!enUso){
                        socketREP.send("ok");
                        System.out.println("Consediendo acceso");
                        enUso = true;
                    }
                    // Si esta en uso lo pone en la cola de espera
                    else{
                        // Le envia el turno correspondiente al actor
                        socketREP.send(String.valueOf(turno));
                        colaEspera.add(turno);
                    }
                    // Espera el siguiente turno
                    turno++;
                }
                // release
                else {
                    enUso = false;
                    socketREP.send("ok");
                    // si la cola no esta vacia (hay procesos en espera)
                    if(!colaEspera.isEmpty()){
                        System.out.println("Consediendo acceso");
                        socketPUB.send(colaEspera.poll() + " ok");
                        enUso = true;
                    }
                }
            }
        }
    }
}



