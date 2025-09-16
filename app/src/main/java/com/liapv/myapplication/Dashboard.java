package com.liapv.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
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
    private View ivPerfil;

    private FirebaseAuth mAuth;
    private String userRol = "", userNombre = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);

        // Ajuste de padding para sistemas con barras
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();

        // Referencias UI
        cvUsuarios = findViewById(R.id.cvUsuarios);
        cvEquipos = findViewById(R.id.cvEquipos);
        cvInventario = findViewById(R.id.cvInventario);
        cvPrestamos = findViewById(R.id.cvPrestamos);
        cvReportes = findViewById(R.id.cvReportes);
        tvNombreApellido = findViewById(R.id.tvNombreApellido);
        ivPerfil = findViewById(R.id.btnPerfil);

        // Listener de Mi Perfil
        ivPerfil.setOnClickListener(v -> startActivity(
                new Intent(Dashboard.this, com.liapv.myapplication.perfil.MiPerfilActivity.class)
        ));

        // Verificar que haya un usuario logueado
        if (mAuth.getCurrentUser() == null) {
            // Si no hay usuario logueado, redirigir al login
            Toast.makeText(this, "No hay usuario logueado", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(Dashboard.this, MainActivity.class));
            finish();
            return;
        }

        // Obtener datos del usuario desde Firebase
        String uid = mAuth.getCurrentUser().getUid();
        FirebaseDatabase.getInstance().getReference("usuarios").child(uid)
                .get().addOnSuccessListener(snapshot -> {
                    if(snapshot.exists()) {
                        userNombre = snapshot.child("nombre").getValue(String.class) + " " +
                                snapshot.child("apellido").getValue(String.class);
                        userRol = snapshot.child("rol").getValue(String.class);

                        // Mostrar nombre en Dashboard
                        tvNombreApellido.setText(userNombre + " (" + userRol + ")");

                        // Configurar módulos según rol
                        configurarModuloSegunRol();
                    }
                }).addOnFailureListener(e ->
                        Toast.makeText(this, "Error al cargar datos", Toast.LENGTH_SHORT).show()
                );

    }

    // Configuración de módulos según rol
    private void configurarModuloSegunRol() {
        // Usuarios: solo Admin
        mostrarOCultarModulo(cvUsuarios, new String[]{"Administrador", "Admin"});
        // Equipos: solo Admin puede modificar
        mostrarOCultarModulo(cvEquipos, new String[]{"Administrador", "Admin"});
        // Inventario: Admin y Supervisor pueden registrar
        mostrarOCultarModulo(cvInventario, new String[]{"Administrador", "Admin", "Supervisor"});
        // Préstamos: Instructor puede solicitar, Admin/Supervisor validan
        mostrarOCultarModulo(cvPrestamos, new String[]{"Administrador", "Admin", "Supervisor", "Instructor"});
        // Reportes: Admin y Supervisor
        mostrarOCultarModulo(cvReportes, new String[]{"Administrador", "Admin", "Supervisor"});
    }

    // Mostrar u ocultar módulo según rol
    private void mostrarOCultarModulo(CardView modulo, String[] rolesPermitidos){
        boolean permitido = false;
        for(String rolPermitido : rolesPermitidos){
            if(userRol.equalsIgnoreCase(rolPermitido)){
                permitido = true;
                break;
            }
        }

        if(permitido){
            modulo.setVisibility(View.VISIBLE);
            modulo.setAlpha(1f);
            modulo.setOnClickListener(v -> abrirModulo(modulo.getId()));
        } else {
            modulo.setVisibility(View.GONE); // Módulo oculto para roles no permitidos
        }
    }

    // Abrir actividad correspondiente
    private void abrirModulo(int id) {
        if (id == R.id.cvUsuarios) {
            startActivity(new Intent(this, com.liapv.myapplication.usuarios.UsuariosActivity.class));
        } else if (id == R.id.cvEquipos) {
            startActivity(new Intent(this, com.liapv.myapplication.equipos.ListaEquiposActivity.class));
        } else if (id == R.id.cvInventario) {
            Toast.makeText(this, "Módulo Inventario aún no implementado", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.cvPrestamos) {
            startActivity(new Intent(this, com.liapv.myapplication.prestamos.ListaSolicitudesActivity.class));
        } else if (id == R.id.cvReportes) {
            startActivity(new Intent(this, com.liapv.myapplication.reportes.ReportesActivity.class));
        }
    }


}
