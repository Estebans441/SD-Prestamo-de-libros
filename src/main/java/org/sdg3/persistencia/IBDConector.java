package org.sdg3.persistencia;
import java.rmi.Remote;

public interface IBDConector extends Remote {
    // Metodos del conector
    Boolean validarExistencias(String isbn) throws java.rmi.RemoteException;
}