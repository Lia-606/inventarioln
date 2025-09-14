package com.liapv.myapplication.prestamos;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.liapv.myapplication.R;

public class PrestamoActivity extends AppCompatActivity {

    private TextView btnSolicitar, btnListaSolicitudes, btnListaDevoluciones, btnRegistrarDevolucion;
    private String userRol = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prestamo);

        // UI
        btnSolicitar = findViewById(R.id.btnSolicitarPrestamo);
        btnListaSolicitudes = findViewById(R.id.btnListaSolicitudes);
        btnRegistrarDevolucion = findViewById(R.id.btnRegistrarDevolucion);
        btnListaDevoluciones = findViewById(R.id.btnListaDevoluciones);  // nuevo botón

        // Recibir el rol desde el Dashboard
        userRol = getIntent().getStringExtra("rol");

        configurarUI(userRol);

        btnSolicitar.setOnClickListener(v -> {
            startActivity(new Intent(this, SolicitarPrestamoActivity.class));
        });

        btnListaSolicitudes.setOnClickListener(v -> {
            if (rolPermitido("Admin", "Instructor", "Supervisor")) {
                startActivity(new Intent(this, ListaSolicitudesActivity.class));
            } else {
                Toast.makeText(this, "No tienes permiso para ver solicitudes", Toast.LENGTH_SHORT).show();
            }
        });

        btnRegistrarDevolucion.setOnClickListener(v -> {
            startActivity(new Intent(this, RegistrarDevolucionActivity.class));
        });

        // Nuevo listener para lista devoluciones
        btnListaDevoluciones.setOnClickListener(v -> {
            startActivity(new Intent(this, ListaDevolucionesActivity.class));
        });
        getWindow().setBackgroundDrawableResource(R.drawable.fondo4);
    }

    private void configurarUI(String rol) {
        if (rol == null) return;

        if (rol.equalsIgnoreCase("instructor")) {
            btnListaSolicitudes.setVisibility(View.GONE);
        }
    }

    private boolean rolPermitido(String... roles) {
        for (String rol : roles) {
            if (userRol.equalsIgnoreCase(rol)) return true;
        }
        return false;
    }
}
