package org.sdg3.persistencia;

import org.sdg3.entities.Libro;
import org.sdg3.entities.Prestamo;

import java.rmi.server.UnicastRemoteObject;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
        BDConector dbconector = new BDConector(args[0]);

        // Incluir el objeto en el registro del RMI en el puerto 8888.
        Registry registry = LocateRegistry.createRegistry(8888);
        String name = "dbconector"+ sede;
        registry.rebind(name, dbconector);
        System.out.println("--------------------------------------------");
        System.out.println("Objeto -" + name + "- Registrado en el RMI");
        System.out.println("Sede " + sede);
        System.out.println("--------------------------------------------");
    }


    @Override
    public Boolean validarExistencias(String isbn) throws RemoteException {
        //
        try{
            System.out.println("--------------------------------------------");
            System.out.println("Validando existencias para el libro" + isbn);
            System.out.println("--------------------------------------------");

            // Prestamos vigentes con el libro...
            String query = "SELECT COUNT(*) AS vigentes FROM libreria.Prestamo WHERE Libro_ISBN = '"+isbn+"' AND vigente = 1;";
            System.out.println("\t"+query);

            Statement stmt = this.mysql.getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(query);

            rs.next();
            int vigentes = rs.getInt("vigentes");
            System.out.println("\tVigentes: "+vigentes);

            stmt.close();

            // Existencias del libro...
            query = "SELECT Existencias AS existencias FROM libreria.libro WHERE ISBN = '"+isbn+"';";
            System.out.println("\t"+query);

            stmt = this.mysql.getConnection().createStatement();
            rs = stmt.executeQuery(query);

            int existencias = 0;
            if(rs.next())
                existencias = rs.getInt("existencias");

            System.out.println("\tExistencias: "+existencias);
            return vigentes < existencias;
        }
        catch (SQLException ex){
            System.out.println("\tError + " + ex.getMessage());
            Logger.getLogger("");
            return false;
        }
    }

    @Override
    public Boolean crearPrestamo(Prestamo prestamo, String sede) throws RemoteException {
        try{
            System.out.println("--------------------------------------------");
            System.out.println("Creando prestamo");
            System.out.println("Sede: " + sede);
            System.out.println("Codigo de libro: " + prestamo.getLibro().getCodigo());
            System.out.println("Usuario: " + prestamo.getIdCliente());
            System.out.println("Fecha inicio: " + prestamo.getF_inicio());
            System.out.println("Fecha fin: " + prestamo.getF_fin());
            System.out.println("--------------------------------------------");

            // formateo de fecha
            String pattern = "dd/MM/YYYY";
            DateFormat df = new SimpleDateFormat(pattern);
            String fechaInicio = df.format(prestamo.getF_inicio());
            String fechaFin = df.format(prestamo.getF_fin());

            String query = "INSERT INTO libreria.prestamo(idUsuario, Libro_ISBN, fechaInicio, fechaFin, vigente, sede) VALUES(" +
                    "'" + prestamo.getIdCliente() + "'," +
                    "'" + prestamo.getLibro().getCodigo() + "'," +
                    "STR_TO_DATE('" + fechaInicio + "','%d/%m/%Y')," +
                    "STR_TO_DATE('" + fechaFin + "','%d/%m/%Y')," +
                    "'1'," +
                    "'"+sede+"');";
            System.out.println("\t"+query);

            Statement stmt = this.mysql.getConnection().createStatement();
            int code = stmt.executeUpdate(query);
            stmt.close();


            if (code == 1) {
                System.out.println("\t > Se creo el prestamo!");
                return true;
            }
            System.out.println("\t > Error creando el prestamo!");
            return false;
        }
        catch (SQLException ex){
            System.out.println("\t Error + " + ex.getMessage());
            Logger.getLogger("");
            return false;
        }
    }

    @Override
    public Boolean renovarPrestamo(Prestamo prestamo) throws RemoteException {
        try{
            System.out.println("--------------------------------------------");
            System.out.println("Renovando prestamo");
            System.out.println("Codigo de libro: " + prestamo.getLibro().getCodigo());
            System.out.println("Usuario: " + prestamo.getIdCliente());
            System.out.println("Fecha inicio: " + prestamo.getF_inicio());
            System.out.println("--------------------------------------------");
            System.out.println("Para la fecha: " + prestamo.getF_fin());
            System.out.println("--------------------------------------------");

            System.out.println("\t > Buscando el prestamo a renovar");
            String query = "SELECT * FROM prestamo " +
                    "WHERE Libro_ISBN = '" + prestamo.getLibro().getCodigo() + "' AND " +
                    "idUsuario = '" + prestamo.getIdCliente() +"';";
            System.out.println("\t"+query);

            Statement stmt = this.mysql.getConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = stmt.executeQuery(query);

            if(rs.first()){
                System.out.println("\t > Prestamo encontrando, realizando renovacion...");
                rs.close();
                stmt.close();

                String pattern = "dd/MM/YYYY";
                DateFormat df = new SimpleDateFormat(pattern);
                String fechaFin = df.format(prestamo.getF_fin());

                query = "UPDATE  libreria.prestamo SET " +
                        "fechaFin = STR_TO_DATE('" + fechaFin + "','%d/%m/%Y') " +
                        "WHERE Libro_ISBN = '" + prestamo.getLibro().getCodigo() + "' AND " +
                        "idUsuario = '" + prestamo.getIdCliente() +"';";
                System.out.println("\t"+query);

                stmt = this.mysql.getConnection().createStatement();
                int code = stmt.executeUpdate(query);
                stmt.close();

                if(code == 1){
                    System.out.println("\t > Prestamo renovado para la fecha " + fechaFin);
                    return true;
                }
                System.out.println("\t > Error renovando el prestamo");
                return false;
            }
            else {
                rs.close();
                stmt.close();
                return false;
            }
        }
        catch (SQLException ex){
            System.out.println("Error + " + ex.getMessage());
            Logger.getLogger("");
            return null;
        }
    }

    @Override
    public Boolean devolverPrestamo(Prestamo prestamo) throws RemoteException {
        try{
            System.out.println("--------------------------------------------");
            System.out.println("Eliminando prestamo");
            System.out.println("Codigo de libro: " + prestamo.getLibro().getCodigo());
            System.out.println("Usuario: " + prestamo.getIdCliente());
            System.out.println("--------------------------------------------");

            String queryDelete = "DELETE FROM libreria.prestamo WHERE " +
                    "Libro_ISBN = '" + prestamo.getLibro().getCodigo() + "' AND " +
                    "idUsuario = "+ prestamo.getIdCliente() +";";

            Statement stmtDelete = this.mysql.getConnection().createStatement();
            int codeDelete = stmtDelete.executeUpdate(queryDelete);
            System.out.println("\t > Se elimino el prestamo!");
            stmtDelete.close();
            return true;
        }
        catch (SQLException ex) {
            System.out.println("Error + " + ex.getMessage());
            Logger.getLogger("");
            return false;
        }
    }

    @Override
    public ArrayList<Prestamo> findAllSede() throws RemoteException {
        try{
            System.out.println("--------------------------------------------");
            System.out.println("Enviando prestamos vigentes");
            System.out.println("--------------------------------------------");

            ArrayList<Prestamo> prestamos = new ArrayList<>();

            String query = "SELECT * FROM prestamo;";
            System.out.println(query);

            Statement stmt = this.mysql.getConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = stmt.executeQuery(query);

            while(rs.next())
                prestamos.add(new Prestamo(rs.getString("idUsuario"), new Libro(rs.getString("Libro_ISBN")),rs.getDate("fechaInicio"),rs.getDate("fechaFin")));

            rs.close();
            stmt.close();

            return prestamos;
        }
        catch (SQLException ex) {
            System.out.println("Error + " + ex.getMessage());
            Logger.getLogger("");
            return null;
        }
    }
}
