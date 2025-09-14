package com.liapv.myapplication.modelos;

import java.io.Serializable;

public class Equipo implements Serializable {

    @Override
    public String toString() {
        return (nombre != null ? nombre : "N/A") + " - " + (codigo != null ? codigo : "N/A");
    }
    private String id;
    private String nombre;
    private String tipo;
    private String marca;
    private String modelo;
    private String estado;
    private String ubicacion;
    private String codigo;
    private int stock;

    // Constructor vacío requerido por Firebase
    public Equipo() {
    }

    // Constructor sin ID (para crear nuevos equipos)
    public Equipo(String nombre, String tipo, String marca, String modelo, String estado, String ubicacion, String codigo, int stock) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.marca = marca;
        this.modelo = modelo;
        this.estado = estado;
        this.ubicacion = ubicacion;
        this.codigo = codigo;
        this.stock = stock;
    }

    // Constructor con ID y stock
    public Equipo(String id, String nombre, int stock) {
        this.id = id;
        this.nombre = nombre;
        this.stock = stock;
    }

    // Getters y setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }

    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
}
