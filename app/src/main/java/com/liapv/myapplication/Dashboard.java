package com.liapv.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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

        // Inicializar Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Ajustar padding por barras de sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Referencias UI
        cvUsuarios = findViewById(R.id.cvUsuarios);
        cvEquipos = findViewById(R.id.cvEquipos);
        cvInventario = findViewById(R.id.cvInventario);
        cvPrestamos = findViewById(R.id.cvPrestamos);
        cvReportes = findViewById(R.id.cvReportes);
        tvNombreApellido = findViewById(R.id.tvNombreApellido);

        // Obtener usuario actual
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

    /**
     * Mostrar u ocultar módulos según el rol del usuario.
     */
    private void configurarModuloSegunRol(String rol) {
        if (rol == null) return;

        switch (rol.toLowerCase()) {
            case "administrador":
            case "admin":
                // Todo visible
                break;

            case "supervisor":
                cvUsuarios.setVisibility(View.GONE); // no puede ver usuarios
                break;

            case "instructor":
                // Acceso solo a préstamos
                cvUsuarios.setVisibility(View.GONE);
                cvEquipos.setVisibility(View.GONE);
                cvInventario.setVisibility(View.GONE);
                cvReportes.setVisibility(View.GONE);
                break;

            default:
                // Rol no reconocido: ocultar todo excepto préstamos
                cvUsuarios.setVisibility(View.GONE);
                cvEquipos.setVisibility(View.GONE);
                cvInventario.setVisibility(View.GONE);
                cvReportes.setVisibility(View.GONE);
                break;
        }
    }

    /**
     * Configura los clicks para los módulos según permisos.
     */
    private void configurarNavegacion() {
        cvUsuarios.setOnClickListener(v -> {
            if (rolPermitido("admin", "administrador")) {
                Intent intent = new Intent(this, com.liapv.myapplication.usuarios.UsuariosActivity.class);
                intent.putExtra("rol", userRol);
                startActivity(intent);
            } else {
                mostrarAccesoDenegado();
            }
        });

        cvEquipos.setOnClickListener(v -> {
            if (rolPermitido("admin", "administrador", "supervisor")) {
                Intent intent = new Intent(this, com.liapv.myapplication.equipos.EquipoActivity.class);
                intent.putExtra("rol", userRol);
                startActivity(intent);
            } else {
                mostrarAccesoDenegado();
            }
        });

        cvInventario.setOnClickListener(v -> {
            if (rolPermitido("admin", "administrador", "supervisor")) {
                Intent intent = new Intent(this, com.liapv.myapplication.inventario.InventarioActivity.class);
                intent.putExtra("rol", userRol);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Solo puedes ver stock", Toast.LENGTH_SHORT).show();
                // Aquí podrías abrir solo la vista de stock si quieres
            }
        });

        cvPrestamos.setOnClickListener(v -> {
            Intent intent = new Intent(this, com.liapv.myapplication.prestamos.PrestamoActivity.class);
            intent.putExtra("rol", userRol);
            startActivity(intent);
        });

        cvReportes.setOnClickListener(v -> {
            if (rolPermitido("admin", "administrador", "supervisor")) {
            }
        });
    }

    private boolean rolPermitido(String... rolesPermitidos) {
        for (String rolPermitido : rolesPermitidos) {
            if (userRol != null && userRol.equalsIgnoreCase(rolPermitido)) {
                return true;
            }
        }
        return false;
    }

    private void mostrarAccesoDenegado() {
        Toast.makeText(this, "Acceso denegado", Toast.LENGTH_SHORT).show();
    }
}
