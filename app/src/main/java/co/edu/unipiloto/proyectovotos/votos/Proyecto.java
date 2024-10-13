package co.edu.unipiloto.proyectovotos.votos;

public class Proyecto {
    private String id;
    private String titulo;
    private String descripcion;
    private String direccion;
    private String entidad;

    // Constructor
    public Proyecto(String id, String titulo, String descripcion, String direccion, String entidad) {
        this.id = id;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.direccion = direccion;
        this.entidad = entidad;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getDireccion() {
        return direccion;
    }

    public String getEntidad() {
        return entidad;
    }
}

