package com.liapv.myapplication.modelos;

public class Inventario {
    private String id;
    private String equipoId;
    private int cantidad;
    private String tipoMovimiento; // entrada / salida
    private String fecha;
    private String hora;
    private String responsable;
    private String proveedor; // solo en entradas

    public Inventario() {}

    public Inventario(String id, String equipoId, int cantidad, String tipoMovimiento,
                      String fecha, String hora, String responsable, String proveedor) {
        this.id = id;
        this.equipoId = equipoId;
        this.cantidad = cantidad;
        this.tipoMovimiento = tipoMovimiento;
        this.fecha = fecha;
        this.hora = hora;
        this.responsable = responsable;
        this.proveedor = proveedor;
    }

    // Getters y setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getEquipoId() { return equipoId; }
    public void setEquipoId(String equipoId) { this.equipoId = equipoId; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public String getTipoMovimiento() { return tipoMovimiento; }
    public void setTipoMovimiento(String tipoMovimiento) { this.tipoMovimiento = tipoMovimiento; }
    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }
    public String getHora() { return hora; }
    public void setHora(String hora) { this.hora = hora; }
    public String getResponsable() { return responsable; }
    public void setResponsable(String responsable) { this.responsable = responsable; }
    public String getProveedor() { return proveedor; }
    public void setProveedor(String proveedor) { this.proveedor = proveedor; }
}

