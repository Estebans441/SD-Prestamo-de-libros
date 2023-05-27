package org.sdg3.persistencia;
import org.sdg3.entities.Prestamo;

import java.rmi.Remote;

public interface IBDConector extends Remote {
    // Metodos del conector
    Boolean validarExistencias(String isbn) throws java.rmi.RemoteException;
    Boolean crearPrestamo(Prestamo prestamo) throws java.rmi.RemoteException;
    Boolean renovarPrestamo(Prestamo prestamo) throws  java.rmi.RemoteException;
    Boolean devolverPrestamo(Prestamo prestamo) throws java.rmi.RemoteException;
}