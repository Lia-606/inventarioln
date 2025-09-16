package com.liapv.myapplication.reportes;

import android.util.Log;

public class InventarioItem {
    private static final String TAG = "InventarioItem";

    private String id; // opcional, key de Firebase
    private String nombreEquipo;
    private int stock; // cantidad
    private String fecha; // formato: "yyyy-MM-dd"
    private String tipoMovimiento; // "Entrada" o "Salida"
    private String responsable;

    public InventarioItem() { }

    public InventarioItem(String id, String nombreEquipo, int stock, String fecha, String tipoMovimiento, String responsable) {
        this.id = id;
        this.nombreEquipo = nombreEquipo;
        this.stock = stock;
        this.fecha = fecha;
        this.tipoMovimiento = tipoMovimiento;
        this.responsable = responsable;
        Log.i(TAG, "Nuevo InventarioItem creado: " + this);
    }

    // Getters y setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombreEquipo() { return nombreEquipo; }
    public void setNombreEquipo(String nombreEquipo) { this.nombreEquipo = nombreEquipo; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public String getTipoMovimiento() { return tipoMovimiento; }
    public void setTipoMovimiento(String tipoMovimiento) { this.tipoMovimiento = tipoMovimiento; }

    public String getResponsable() { return responsable; }
    public void setResponsable(String responsable) { this.responsable = responsable; }

    @Override
    public String toString() {
        return tipoMovimiento + " | " + nombreEquipo + " | " + stock + " | " + fecha + " | " + responsable;
    }
}
