package org.sdg3.entities;

import java.io.*;
import java.util.Calendar;
import java.util.Date;

public class Prestamo implements Serializable {
    Date f_inicio; // Fecha de inicio del prestamo
    Date f_fin; // Fecha de finalizacion del prestamo
    String idCliente; // Id del solicitante que realiza la solicitud
    Libro libro; // Libro que se tiene prestado

    // CONSTRUCTORES

    public Prestamo(String idCliente, Libro libro, Date f_inicio, Date f_fin) {
        this.f_inicio = f_inicio;
        this.f_fin = f_fin;
        this.idCliente = idCliente;
        this.libro = libro;
    }

    public Prestamo(Date f_inicio, String idCliente, Libro libro) {
        this.f_inicio = f_inicio;

        this.f_fin = f_inicio;
        Calendar c = Calendar.getInstance();
        c.setTime(this.f_fin);
        c.add(Calendar.WEEK_OF_YEAR, 1);
        this.f_fin = c.getTime();

        this.idCliente = idCliente;
        this.libro = libro;
    }

    public Prestamo(Prestamo p) {
        this.f_inicio = p.f_inicio;
        this.f_fin = p.f_fin;
        this.idCliente = p.idCliente;
        this.libro = p.libro;
    }

    // Metodo constructor que construye el objeto a partir de su serializacion
    public Prestamo(byte[] serializado) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bis = new ByteArrayInputStream(serializado);
        ObjectInputStream entrada = new ObjectInputStream(bis);
        Prestamo p = (Prestamo) entrada.readObject();

        this.f_inicio = p.f_inicio;
        this.f_fin = p.f_fin;
        this.idCliente = p.idCliente;
        this.libro = p.libro;
    }

    // Metodo que retorna el objeto serializado
    public byte[] serializar() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream salida = new ObjectOutputStream(bos);
        Prestamo x = new Prestamo(this);
        salida.writeObject(x);

        return bos.toByteArray();
    }

    // Metodo que renueva un prestamo en una semana
    public Date renovar(){
        Calendar c = Calendar.getInstance();
        c.setTime(this.f_fin);
        c.add(Calendar.WEEK_OF_YEAR, 1);
        this.f_fin = c.getTime();
        return f_fin;
    }

    // GETTERS Y SETTERS
    public Date getF_inicio() {
        return f_inicio;
    }

    public void setF_inicio(Date f_inicio) {
        this.f_inicio = f_inicio;
    }

    public Date getF_fin() {
        return f_fin;
    }

    public void setF_fin(Date f_fin) {
        this.f_fin = f_fin;
    }



    public String getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(String idCliente) {
        this.idCliente = idCliente;
    }

    public Libro getLibro() {
        return libro;
    }

    public void setLibro(Libro libro) {
        this.libro = libro;
    }
}
