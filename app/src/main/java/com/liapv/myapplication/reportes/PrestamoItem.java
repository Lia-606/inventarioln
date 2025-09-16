package com.liapv.myapplication.reportes;

public class PrestamoItem {
    private String id;              // key en Firebase
    private String equipoId;        // id del equipo
    private String nombreEquipo;    // opcional, se obtiene al consultar "equipos"
    private String solicitante;     // quien pide el préstamo
    private String responsable;     // responsable del préstamo
    private String estado;          // Pendiente, Aprobado, Rechazado, Devuelto
    private String fechaSolicitud;  // yyyy-MM-dd HH:mm:ss
    private String fechaDevolucion; // opcional, puede ser null

    // Constructor vacío (obligatorio para Firebase)
    public PrestamoItem() { }

    // Constructor completo
    public PrestamoItem(String id, String equipoId, String nombreEquipo, String solicitante,
                        String responsable, String estado,
                        String fechaSolicitud, String fechaDevolucion) {
        this.id = id;
        this.equipoId = equipoId;
        this.nombreEquipo = nombreEquipo;
        this.solicitante = solicitante;
        this.responsable = responsable;
        this.estado = estado;
        this.fechaSolicitud = fechaSolicitud;
        this.fechaDevolucion = fechaDevolucion;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEquipoId() { return equipoId; }
    public void setEquipoId(String equipoId) { this.equipoId = equipoId; }

    public String getNombreEquipo() { return nombreEquipo; }
    public void setNombreEquipo(String nombreEquipo) { this.nombreEquipo = nombreEquipo; }

    public String getSolicitante() { return solicitante; }
    public void setSolicitante(String solicitante) { this.solicitante = solicitante; }

    public String getResponsable() { return responsable; }
    public void setResponsable(String responsable) { this.responsable = responsable; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getFechaSolicitud() { return fechaSolicitud; }
    public void setFechaSolicitud(String fechaSolicitud) { this.fechaSolicitud = fechaSolicitud; }

    public String getFechaDevolucion() { return fechaDevolucion; }
    public void setFechaDevolucion(String fechaDevolucion) { this.fechaDevolucion = fechaDevolucion; }

    @Override
    public String toString() {
        return "PrestamoItem{" +
                "equipo='" + (nombreEquipo != null ? nombreEquipo : equipoId) + '\'' +
                ", solicitante='" + solicitante + '\'' +
                ", responsable='" + responsable + '\'' +
                ", estado='" + estado + '\'' +
                ", fechaSolicitud='" + fechaSolicitud + '\'' +
                (fechaDevolucion != null ? ", fechaDevolucion='" + fechaDevolucion + '\'' : "") +
                '}';
    }
}
