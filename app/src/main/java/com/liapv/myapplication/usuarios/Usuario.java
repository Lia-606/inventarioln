package com.liapv.myapplication.usuarios;

public class Usuario {
    private String uid;
    private String nombre;
    private String apellido;
    private String correo;
    private String celular;
    private String direccion;
    private String estado;
    private String fecha_registro;
    private String rol;
    private String sede;
    private String ultimo_login;

    // 🔹 Constructor vacío (requerido por Firebase)
    public Usuario() {}

    // 🔹 Constructor opcional para inicializar rápido
    public Usuario(String uid, String nombre, String apellido, String rol, String sede) {
        this.uid = uid;
        this.nombre = nombre;
        this.apellido = apellido;
        this.rol = rol;
        this.sede = sede;
    }

    // 🔹 Getters y setters
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getCelular() { return celular; }
    public void setCelular(String celular) { this.celular = celular; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getFecha_registro() { return fecha_registro; }
    public void setFecha_registro(String fecha_registro) { this.fecha_registro = fecha_registro; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public String getSede() { return sede; }
    public void setSede(String sede) { this.sede = sede; }

    public String getUltimo_login() { return ultimo_login; }
    public void setUltimo_login(String ultimo_login) { this.ultimo_login = ultimo_login; }

    // 🔹 Para mostrar en listas
    public String getNombreCompleto() {
        return (nombre != null ? nombre : "") + " " + (apellido != null ? apellido : "");
    }
}
