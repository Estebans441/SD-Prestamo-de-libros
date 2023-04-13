package org.sdg3.solicitante;

import org.sdg3.entities.Libro;
import org.sdg3.entities.Prestamo;
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
    private static String endpointGestor = "tcp://*:5555"; // Endpoint del gestor con el que se comunica

    // Sockets
    private static ZMQ.Socket socketREQ;

    // Prestamos realizados
    private static ArrayList<Prestamo> prestamosVig = new ArrayList<Prestamo>();

    public static void main(String[] args) throws Exception {
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
                if(requerimiento[0].equals("S")) {
                    solicitarPrestamo(requerimiento);
                }
                // Tipo Renovacion
                else if(requerimiento[0].equals("R")) {
                    for (Prestamo prestamo : prestamosVig) {
                        if (Objects.equals(prestamo.getIdCliente(), Integer.valueOf(requerimiento[2])) && prestamo.getLibro().getCodigo().equals(requerimiento[1])) {
                            renovarPrestamo(prestamo);
                            break;
                        }
                    }
                }
                // Tipo Devolucion
                else if(requerimiento[0].equals("D")) {
                    for (Prestamo prestamo : prestamosVig) {
                        if (Objects.equals(prestamo.getIdCliente(), Integer.valueOf(requerimiento[2])) && prestamo.getLibro().getCodigo().equals(requerimiento[1])) {
                            devolverPrestamo(prestamo);
                            break;
                        }
                    }
                }
            }

            // Informacion de los prestamos vigentes luego de procesar todos los requerimientos
            System.out.println("--------------------------------------------");
            System.out.println("Estado actual: prestamos vigentes " + prestamosVig.size());
            for (Prestamo prestamo : prestamosVig) {
                System.out.println(prestamosVig.indexOf(prestamo) +
                        " - Cliente: " + prestamo.getIdCliente() +
                        " - Codigo: " + prestamo.getLibro().getCodigo() +
                        " - Inicio: " + prestamo.getF_inicio() +
                        " - Fin: " + prestamo.getF_fin());
            }
            System.out.println("--------------------------------------------");

            socketREQ.close();
        }
    }

    // Metodo que realiza una solicitud de prestamo
    private static void solicitarPrestamo(String[] requerimiento){
        prestamosVig.add(new Prestamo(new Date(), 0, Integer.valueOf(requerimiento[2]), new Libro(requerimiento[1])));
        // La aceptacion del prestamo depende de la respuesta del gestor, sin embargo, todavia no se ha implementado
        System.out.println("\t> Solicitud aceptada");
    }

    // Metodo que realiza una renovacion de prestamo
    private static void renovarPrestamo(Prestamo prestamo) throws Exception{
        // Solicitud de renovacion al gestor
        socketREQ.sendMore("R");
        socketREQ.send(prestamo.serializar());

        // Recibe respuesta del gestor
        Prestamo prestamo1 = new Prestamo(socketREQ.recv());

        // Actualiza la lista de prestamos vigentes
        prestamosVig.set(prestamosVig.indexOf(prestamo), prestamo1);

        // Muestra la informacion por consola
        System.out.println("\t> Prestamo renovado...");
        System.out.println("\t\t Fecha de entrega anterior: " + prestamo.getF_fin());
        System.out.println("\t\t Fecha de entrega renovada: " + prestamo1.getF_fin());
    }

    // Metodo que realiza una devolucion de prestamo
    private static void devolverPrestamo(Prestamo prestamo) throws Exception{
        // Solicitud de renovacion al gestor
        socketREQ.sendMore("D");
        socketREQ.send(prestamo.serializar());

        // Recibe respuesta del gestor
        String respuesta = new String(socketREQ.recv(), ZMQ.CHARSET);
        if(respuesta.equals("D")) {
            prestamosVig.remove(prestamo);
            // Muestra la informacion por consola
            System.out.println("\t> Prestamo devuelto");
        }
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
