package com.liapv.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class Dashboard extends AppCompatActivity {

    private CardView cvUsuarios, cvEquipos, cvInventario, cvPrestamos, cvReportes;
    private TextView tvNombreApellido;

    private FirebaseAuth mAuth;
    private String userRol = "", userNombre = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        mAuth = FirebaseAuth.getInstance();

        cvUsuarios = findViewById(R.id.cvUsuarios);
        cvEquipos = findViewById(R.id.cvEquipos);
        cvInventario = findViewById(R.id.cvInventario);
        cvPrestamos = findViewById(R.id.cvPrestamos);
        cvReportes = findViewById(R.id.cvReportes);
        tvNombreApellido = findViewById(R.id.tvNombreApellido);

        String uid = mAuth.getCurrentUser().getUid();

        FirebaseDatabase.getInstance().getReference("usuarios").child(uid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        String nombre = snapshot.child("nombre").getValue(String.class);
                        String apellido = snapshot.child("apellido").getValue(String.class);
                        userRol = snapshot.child("rol").getValue(String.class);

                        userNombre = nombre + " " + apellido;
                        tvNombreApellido.setText(userNombre + " (" + userRol + ")");

                        configurarModuloSegunRol(userRol);
                        configurarNavegacion();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error al cargar datos del usuario", Toast.LENGTH_SHORT).show());
    }

    private void configurarModuloSegunRol(String rol) {
        if (rol == null) return;

        switch (rol.toLowerCase()) {
            case "administrador":
            case "admin":
                // Todo habilitado
                break;
            case "supervisor":
                cvUsuarios.setVisibility(View.GONE);
                break;
            case "instructor":
                cvUsuarios.setVisibility(View.GONE);
                cvEquipos.setVisibility(View.GONE);
                cvInventario.setVisibility(View.GONE);
                cvReportes.setVisibility(View.GONE);
                break;
            default:
                // Ocultar todo excepto préstamos
                cvUsuarios.setVisibility(View.GONE);
                cvEquipos.setVisibility(View.GONE);
                cvInventario.setVisibility(View.GONE);
                cvReportes.setVisibility(View.GONE);
                break;
        }
    }

    private void configurarNavegacion() {
        cvUsuarios.setOnClickListener(v -> {
            Intent intent = new Intent(this, com.liapv.myapplication.usuarios.UsuariosActivity.class);
            intent.putExtra("rol", userRol);
            startActivity(intent);
        });

        cvEquipos.setOnClickListener(v -> {
            // Todos acceden a InventarioActivity, pero ahí se restringe qué pueden hacer
            Intent intent = new Intent(this, com.liapv.myapplication.equipos.EquipoActivity.class);
            intent.putExtra("rol", userRol);
            startActivity(intent);
        });

        cvInventario.setOnClickListener(v -> {
            // Todos acceden a InventarioActivity, pero ahí se restringe qué pueden hacer
            Intent intent = new Intent(this, com.liapv.myapplication.inventario.InventarioActivity.class);
            intent.putExtra("rol", userRol);
            startActivity(intent);
        });

        cvPrestamos.setOnClickListener(v -> {
            Intent intent = new Intent(this, com.liapv.myapplication.prestamos.PrestamoActivity.class);
            intent.putExtra("rol", userRol);
            startActivity(intent);
        });

        cvReportes.setOnClickListener(v -> {
            if (rolPermitido("administrador", "supervisor", "admin")) {
                // TODO: Cambiar por tu actividad real de reportes
                Toast.makeText(this, "Módulo de reportes aún no implementado", Toast.LENGTH_SHORT).show();
            } else {
                mostrarAccesoDenegado();
            }
        });
    }

    private boolean rolPermitido(String... rolesPermitidos) {
        for (String rolPermitido : rolesPermitidos) {
            if (userRol.equalsIgnoreCase(rolPermitido)) {
                return true;
            }
        }
        return false;
    }

    private void mostrarAccesoDenegado() {
        Toast.makeText(this, "Acceso denegado", Toast.LENGTH_SHORT).show();
    }
}
