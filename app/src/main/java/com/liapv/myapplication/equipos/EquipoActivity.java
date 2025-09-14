package com.liapv.myapplication.equipos;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.liapv.myapplication.R;

public class EquipoActivity extends AppCompatActivity {

    private TextView btnVerLista, btnRegistrarNuevo, btnGenerarQR;
    private String userRol = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_equipo);

        btnVerLista = findViewById(R.id.btnVerListaEquipos);
        btnRegistrarNuevo = findViewById(R.id.btnRegistrarEquipo);
        btnGenerarQR = findViewById(R.id.btnGenerarQR);

        userRol = getIntent().getStringExtra("rol");

        configurarUI(userRol);

        btnVerLista.setOnClickListener(v -> {
            startActivity(new Intent(this, ListaEquiposActivity.class));
        });

        btnRegistrarNuevo.setOnClickListener(v -> {
            if (rolPermitido("admin", "administrador", "supervisor")) {
                startActivity(new Intent(this, FormularioEquipoActivity.class));
            } else {
                Toast.makeText(this, "No tienes permiso para registrar equipos", Toast.LENGTH_SHORT).show();
            }
        });

        btnGenerarQR.setOnClickListener(v -> {
            startActivity(new Intent(this, QRGenerator.class));
        });
    }

    private void configurarUI(String rol) {
        if (rol == null) return;

        if (rol.equalsIgnoreCase("instructor")) {
            btnRegistrarNuevo.setVisibility(View.GONE);
        }
    }

    private boolean rolPermitido(String... roles) {
        for (String rol : roles) {
            if (userRol.equalsIgnoreCase(rol)) return true;
        }
        return false;
    }
}
