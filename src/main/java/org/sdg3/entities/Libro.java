package org.sdg3.entities;

import java.io.*;

public class Libro implements Serializable {
    private String codigo; // Codigo ISBN del libro
    private Integer disponibles; // Existencias disponibles


    // CONSTRUCTORES
    public Libro(String codigo, String nombre, Integer disponibles) {
        this.codigo = codigo;
        this.disponibles = disponibles;
    }

    public Libro(String codigo) {
        this.codigo = codigo;
    }

    public Libro(Libro libro){
        this.codigo = libro.codigo;
        this.disponibles = libro.disponibles;
    }

    // Metodo constructor que construye el objeto a partir de su serializacion
    public Libro(byte[] serializado) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bis = new ByteArrayInputStream(serializado);
        ObjectInputStream entrada = new ObjectInputStream(bis);
        Libro objeto = (Libro) entrada.readObject();

        this.codigo = objeto.codigo;
        this.disponibles = objeto.disponibles;
    }

    // Metodo que retorna el objeto serializado
    public byte[] serializar() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream salida = new ObjectOutputStream(bos);
        Libro x = new Libro(this);
        salida.writeObject(x);

        byte[] objetoSerializado = bos.toByteArray();
        return objetoSerializado;
    }

    // GETTERS Y SETTERS
    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public Integer getDisponibles() {
        return disponibles;
    }

    public void setDisponibles(Integer disponibles) {
        this.disponibles = disponibles;
    }
}
