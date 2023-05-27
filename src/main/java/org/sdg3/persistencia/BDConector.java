package org.sdg3.persistencia;

import java.rmi.server.UnicastRemoteObject;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

public class BDConector extends UnicastRemoteObject implements IBDConector {
    private final MySQL mysql;

    protected BDConector(String sede) throws RemoteException {
        this.mysql = new MySQL();
        mysql.conectar();
    }

    public static void main(String[] args) throws IOException {
        // Obtener el número de servidor de operación
        String sede = args[0];

        // Crear el objeto cuyos métodos el cliente podrá usar
        BDConector dbconector = new BDConector(sede);

        // Incluir el objeto en el registro del RMI en el puerto 8888.
        Registry registry = LocateRegistry.createRegistry(8888);
        String name = "dbconector"+ sede;
        registry.rebind(name, dbconector);
        System.out.println("Objeto -" + name + "- Registrado en el RMI");
    }


    @Override
    public Boolean validarExistencias(String isbn) throws RemoteException {
        //
        try{
            // Prestamos vigentes con el libro...
            String query = "SELECT COUNT(*) AS vigentes FROM libreria.Prestamo WHERE Libro_ISBN = '"+isbn+"' AND vigente = 1;";
            System.out.println(query);
            Statement stmt = this.mysql.getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(query);
            rs.next();
            int vigentes = rs.getInt("vigentes");
            System.out.println(vigentes);
            stmt.close();

            // Prestamos vigentes con el libro...
            query = "SELECT Existencias AS existencias FROM libreria.libro WHERE ISBN = '"+isbn+"';";
            System.out.println(query);
            stmt = this.mysql.getConnection().createStatement();
            rs = stmt.executeQuery(query);
            if(rs.next()){
                int existencias = rs.getInt("existencias");
                System.out.println(existencias);
                return vigentes < existencias;
            }
            else
                return false;
        }
        catch (SQLException ex){
            System.out.println("Error + " + ex.getMessage());
            Logger.getLogger("");
        }
        return false;
    }
}
