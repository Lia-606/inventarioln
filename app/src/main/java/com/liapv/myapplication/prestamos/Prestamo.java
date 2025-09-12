package com.liapv.myapplication.prestamos;

public class Prestamo {
    private String id;
    private String equipoId;
    private String equipoNombre;
    private String usuarioIdSolicitante;
    private String nombreSolicitante;
    private String fechaSolicitud;
    private String estado; // "Pendiente", "Aprobado", "Rechazado", "Devuelto"
    private String fechaDevolucion; // null si no devuelto
    private String observaciones; // opcional

    // Constructor vacío (requerido por Firebase)
    public Prestamo() { }

    // Constructor completo
    public Prestamo(String id,
                    String equipoId,
                    String equipoNombre,
                    String usuarioIdSolicitante,
                    String nombreSolicitante,
                    String fechaSolicitud,
                    String estado,
                    String fechaDevolucion,
                    String observaciones) {
        this.id = id;
        this.equipoId = equipoId;
        this.equipoNombre = equipoNombre;
        this.usuarioIdSolicitante = usuarioIdSolicitante;
        this.nombreSolicitante = nombreSolicitante;
        this.fechaSolicitud = fechaSolicitud;
        this.estado = estado;
        this.fechaDevolucion = fechaDevolucion;
        this.observaciones = observaciones;
    }

    // Getters y setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEquipoId() { return equipoId; }
    public void setEquipoId(String equipoId) { this.equipoId = equipoId; }

    public String getEquipoNombre() { return equipoNombre; }
    public void setEquipoNombre(String equipoNombre) { this.equipoNombre = equipoNombre; }

    public String getUsuarioIdSolicitante() { return usuarioIdSolicitante; }
    public void setUsuarioIdSolicitante(String usuarioIdSolicitante) { this.usuarioIdSolicitante = usuarioIdSolicitante; }

    public String getNombreSolicitante() { return nombreSolicitante; }
    public void setNombreSolicitante(String nombreSolicitante) { this.nombreSolicitante = nombreSolicitante; }

    public String getFechaSolicitud() { return fechaSolicitud; }
    public void setFechaSolicitud(String fechaSolicitud) { this.fechaSolicitud = fechaSolicitud; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getFechaDevolucion() { return fechaDevolucion; }
    public void setFechaDevolucion(String fechaDevolucion) { this.fechaDevolucion = fechaDevolucion; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    @Override
    public String toString() {
        return "Prestamo{" +
                "id='" + id + '\'' +
                ", equipoId='" + equipoId + '\'' +
                ", equipoNombre='" + equipoNombre + '\'' +
                ", usuarioIdSolicitante='" + usuarioIdSolicitante + '\'' +
                ", nombreSolicitante='" + nombreSolicitante + '\'' +
                ", fechaSolicitud='" + fechaSolicitud + '\'' +
                ", estado='" + estado + '\'' +
                ", fechaDevolucion='" + fechaDevolucion + '\'' +
                ", observaciones='" + observaciones + '\'' +
                '}';
    }
}
