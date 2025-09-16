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
import com.liapv.myapplication.equipos.ListaEquiposActivity;
import com.liapv.myapplication.inventario.StockActivity;
import com.liapv.myapplication.prestamos.DevolucionesBottomSheet;
import com.liapv.myapplication.prestamos.ListaSolicitudesActivity;

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
            if ("supervisor".equalsIgnoreCase(userRol)) {
                // 👉 Supervisor solo ve la lista
                Intent intent = new Intent(this, com.liapv.myapplication.equipos.EquipoActivity.class);
                intent.putExtra("rol", userRol);
                startActivity(intent);
            } else {
                // 👉 Admin o instructor van al módulo completo
                Intent intent = new Intent(this, com.liapv.myapplication.equipos.EquipoActivity.class);
                intent.putExtra("rol", userRol);
                startActivity(intent);
            }
        });

        cvInventario.setOnClickListener(v -> {
            if ("supervisor".equalsIgnoreCase(userRol)) {
                // 👉 Supervisor va directamente a StockActivity
                Intent intent = new Intent(this, com.liapv.myapplication.equipos.EquipoActivity.class);
                startActivity(intent);
            } else {
                // 👉 Admin o instructor van al módulo completo
                Intent intent = new Intent(this, com.liapv.myapplication.inventario.InventarioActivity.class);
                intent.putExtra("rol", userRol);
                startActivity(intent);
            }
        });

        cvPrestamos.setOnClickListener(v -> {
            if ("supervisor".equalsIgnoreCase(userRol)) {
                // 👉 Supervisor puede ver lista de solicitudes y devoluciones
                Intent intent = new Intent(this, com.liapv.myapplication.equipos.EquipoActivity.class);
                intent.putExtra("rol", userRol);
                startActivity(intent);

                // 👇 También abrir lista de devoluciones (si quieres en otra CardView, cámbialo)
                // startActivity(new Intent(this, ListaDevolucionesActivity.class));
            } else {
                // 👉 Otros van al módulo principal de préstamos
                Intent intent = new Intent(this, com.liapv.myapplication.prestamos.PrestamoActivity.class);
                intent.putExtra("rol", userRol);
                startActivity(intent);
            }
        });

        cvReportes.setOnClickListener(v -> {
            if (rolPermitido("administrador", "supervisor", "admin")) {
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
