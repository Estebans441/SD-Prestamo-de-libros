package org.sdg3.solicitante;

import org.sdg3.entities.Libro;
import org.sdg3.entities.Prestamo;

import java.io.IOException;
import java.util.ArrayList;
import java.io.File;
import java.util.Date;
import java.util.Objects;
import java.util.Scanner;

import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZContext;

public class Solicitante {
    // Constantes
    private static String[] ipSede = {"10.43.100.191", "10.43.100.187"};
    // Sockets
    private static ZMQ.Socket socketREQ;

    public static void main(String[] args) throws Exception {
        String endpointGestor = "tcp://"+ipSede[Integer.parseInt(args[0])]+":5555"; // Endpoint del gestor con el que se comunica

        try(ZContext context = new ZContext()){
            // Socket de comunicacion con el gestor
            socketREQ = context.createSocket(SocketType.REQ);
            socketREQ.connect(endpointGestor);

            // Lee los requerimientos del archivo
            ArrayList<String[]> requerimientos = leerArchivo("src/main/resources/req.txt");

            // Procesa cada requerimiento
            for(String[] requerimiento : requerimientos){
                // Muestra la informacion del requerimiento por consola
                System.out.println("--------------------------------------------");
                System.out.println("Tipo: " + requerimiento[0]);
                System.out.println("Codigo de libro: " + requerimiento[1]);
                System.out.println("Usuario: " + requerimiento[2]);
                System.out.println("--------------------------------------------");

                // Tipo Solicitud
                switch (requerimiento[0]) {
                    case "S" -> solicitarPrestamo(requerimiento);

                    // Tipo Renovacion
                    case "R" -> renovarPrestamo(requerimiento);

                    // Tipo Devolucion
                    case "D" -> devolverPrestamo(requerimiento);
                }
            }

            socketREQ.close();
        }
    }

    // Metodo que realiza una solicitud de prestamo
    private static void solicitarPrestamo(String[] requerimiento) throws IOException {
        Prestamo prestamo = new Prestamo(new Date(), requerimiento[2], new Libro(requerimiento[1]));
        socketREQ.sendMore("S");
        socketREQ.send(prestamo.serializar());
        if(socketREQ.recvStr().equals("ok"))
            System.out.println("\t> Solicitud aceptada");
        else
            System.out.println("\t> Solicitud rechazada");
    }

    // Metodo que realiza una renovacion de prestamo
    private static void renovarPrestamo(String[] requerimiento) throws Exception{
        // Solicitud de renovacion al gestor
        socketREQ.sendMore("R");
        Prestamo prestamo = new Prestamo(new Date(), requerimiento[2], new Libro(requerimiento[1]));
        socketREQ.send(prestamo.serializar());

        // Recibe respuesta del gestor
        Prestamo p = new Prestamo(socketREQ.recv());
        System.out.println("Nueva fecha de renovacion: " + p.getF_fin());
    }

    // Metodo que realiza una devolucion de prestamo
    private static void devolverPrestamo(String[] requerimiento) throws Exception{
        // Solicitud de renovacion al gestor
        socketREQ.sendMore("D");
        Prestamo prestamo = new Prestamo(new Date(), requerimiento[2], new Libro(requerimiento[1]));
        socketREQ.send(prestamo.serializar());

        // Recibe respuesta del gestor
        String respuesta = new String(socketREQ.recv(), ZMQ.CHARSET);
        if(respuesta.equals("ok"))
            System.out.println("\t> Prestamo devuelto");
        else
            System.out.println("\t> Error devolviendo el prestamo");
    }

    // Lee el archivo con los requerimientos
    private static ArrayList<String[]> leerArchivo(String file) throws Exception{
        ArrayList<String[]> requerimientos = new ArrayList<String[]>();

        File archivo = new File(file);
        Scanner scanner = new Scanner(archivo);

        while (scanner.hasNextLine()) {
            String linea = scanner.nextLine();
            String[] requerimiento = linea.split("-");
            requerimientos.add(requerimiento);
        }
        return requerimientos;
    }
}
