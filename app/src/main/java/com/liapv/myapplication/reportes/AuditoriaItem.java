package com.liapv.myapplication.reportes;

public class AuditoriaItem {

    private String id; // Key de Firebase
    private String fecha;
    private String accion;
    private String equipo;
    private Integer cantidad; // ahora puede ser null
    private String responsable;
    private String solicitanteProveedor;

    public AuditoriaItem() {

    }

    public AuditoriaItem(String id, String fecha, String accion, String equipo, Integer cantidad,
                         String responsable, String solicitanteProveedor) {
        this.id = id;
        this.fecha = fecha;
        this.accion = accion;
        this.equipo = equipo;
        this.cantidad = cantidad;
        this.responsable = responsable;
        this.solicitanteProveedor = solicitanteProveedor;
    }

    // Constructor alternativo para Firebase que tenga solo lo que tu fragmento original obtiene
    public AuditoriaItem(String id, String accion, String responsable, String fecha, String detalle) {
        this.id = id;
        this.accion = accion;
        this.responsable = responsable;
        this.fecha = fecha;
        this.solicitanteProveedor = detalle; // reutilizamos "detalle" como solicitante/proveedor
        this.equipo = "-";
        this.cantidad = 0;
    }

    // Getters y setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public String getAccion() { return accion; }
    public void setAccion(String accion) { this.accion = accion; }

    public String getEquipo() { return equipo; }
    public void setEquipo(String equipo) { this.equipo = equipo; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    public String getResponsable() { return responsable; }
    public void setResponsable(String responsable) { this.responsable = responsable; }

    public String getSolicitanteProveedor() { return solicitanteProveedor; }
    public void setSolicitanteProveedor(String solicitanteProveedor) { this.solicitanteProveedor = solicitanteProveedor; }

    @Override
    public String toString() {
        return fecha + " | " + accion + " | " + equipo + " | " + cantidad + " | " + responsable + " | " + solicitanteProveedor;
    }
}
