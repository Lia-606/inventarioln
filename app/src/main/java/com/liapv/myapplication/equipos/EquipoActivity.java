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
        setContentView(R.layout.activity_equipos);

        btnVerLista = findViewById(R.id.btnVerListaEquipos);
        btnRegistrarNuevo = findViewById(R.id.btnRegistrarEquipo);
        btnGenerarQR = findViewById(R.id.btnGenerarQR);

        userRol = getIntent().getStringExtra("rol");

        configurarUI(userRol);

        btnVerLista.setOnClickListener(v -> {
            Intent intent = new Intent(this, ListaEquiposActivity.class);
            intent.putExtra("rol", userRol);
            startActivity(intent);
        });

        btnRegistrarNuevo.setOnClickListener(v -> {
            if (rolPermitido("Admin", "Supervisor")) {
                startActivity(new Intent(this, FormularioEquipoActivity.class));
            } else {
                Toast.makeText(this, "No tienes permiso para registrar equipos", Toast.LENGTH_SHORT).show();
            }
        });

        btnGenerarQR.setOnClickListener(v -> {
            if (rolPermitido("Admin", "Supervisor")) {
                startActivity(new Intent(this, SeleccionarEquipoQRActivity.class)); // ✅ CORRECTO
            } else {
                Toast.makeText(this, "No tienes permiso para generar códigos QR", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void configurarUI(String rol) {
        if (rol == null) return;

        if (!rolPermitido("Admin", "Supervisor")) {
            btnRegistrarNuevo.setVisibility(View.GONE);
            btnGenerarQR.setVisibility(View.GONE); //  Oculta el botón si no tiene permisos
        }

    }

    private boolean rolPermitido(String... roles) {
        for (String rol : roles) {
            if (userRol.equalsIgnoreCase(rol)) return true;
        }
        return false;
    }

    // ✅ Este es el método que te faltaba
    public String getUserRol() {
        return userRol;
    }
}
