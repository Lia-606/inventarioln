package com.liapv.myapplication.usuarios;

public class Usuario {
    private String nombreApellido;
    private String rol;
    private String sede;
    private int imagenResId; // opcional (puedes usar un drawable)

    public Usuario(String nombreApellido, String rol, String sede, int imagenResId) {
        this.nombreApellido = nombreApellido;
        this.rol = rol;
        this.sede = sede;
        this.imagenResId = imagenResId;
    }

    public String getNombreApellido() { return nombreApellido; }
    public String getRol() { return rol; }
    public String getSede() { return sede; }
    public int getImagenResId() { return imagenResId; }
}
