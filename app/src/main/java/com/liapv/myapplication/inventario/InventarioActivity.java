package com.liapv.myapplication.inventario;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.liapv.myapplication.R;

public class InventarioActivity extends AppCompatActivity {

    private TextView btnVerStock, btnRegistrarEntrada, btnRegistrarSalida;
    private String userRol = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventario);

        btnVerStock = findViewById(R.id.btnVerStock);
        btnRegistrarEntrada = findViewById(R.id.btnRegistrarEntrada);
        btnRegistrarSalida = findViewById(R.id.btnRegistrarSalida);

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseDatabase.getInstance().getReference("usuarios")
                .child(uid)
                .child("rol")
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        userRol = snapshot.getValue(String.class);
                        configurarUI(userRol);
                    } else {
                        Toast.makeText(this, "Rol no encontrado", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error al obtener el rol", Toast.LENGTH_SHORT).show());

        btnVerStock.setOnClickListener(v ->
                startActivity(new Intent(this, StockActivity.class)));

        btnRegistrarEntrada.setOnClickListener(v -> {
            if (rolPermitido("Admin", "Supervisor")) {
                startActivity(new Intent(this, RegistrarEntradaActivity.class));
            } else {
                Toast.makeText(this, "No tienes permiso para registrar entradas", Toast.LENGTH_SHORT).show();
            }
        });

        btnRegistrarSalida.setOnClickListener(v -> {
            if (rolPermitido("Admin", "Supervisor")) {
                startActivity(new Intent(this, RegistrarSalidaActivity.class));
            } else {
                Toast.makeText(this, "No tienes permiso para registrar salidas", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void configurarUI(String rol) {
        if (rol == null) return;

        if (!rolPermitido("Admin", "Supervisor")) {
            btnRegistrarEntrada.setVisibility(View.GONE);
            btnRegistrarSalida.setVisibility(View.GONE);
        }
    }

    private boolean rolPermitido(String... roles) {
        for (String rol : roles) {
            if (userRol.equalsIgnoreCase(rol)) return true;
        }
        return false;
    }
}
