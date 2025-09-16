package com.liapv.myapplication.prestamos;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.liapv.myapplication.R;
import androidx.cardview.widget.CardView;


public class PrestamoActivity extends AppCompatActivity {

    private TextView btnSolicitar, btnListaSolicitudes, btnListaDevoluciones, btnRegistrarDevolucion, btnVerMisSolicitudes;
    private CardView cardSolicitarPrestamo, cardVerMisSolicitudes, cardRegistrarDevolucion;
    private String userRol = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prestamo);

        btnSolicitar = findViewById(R.id.btnSolicitarPrestamo);
        btnListaSolicitudes = findViewById(R.id.btnListaSolicitudes);
        btnRegistrarDevolucion = findViewById(R.id.btnRegistrarDevolucion);
        btnListaDevoluciones = findViewById(R.id.btnListaDevoluciones);
        btnVerMisSolicitudes = findViewById(R.id.btnVerMisSolicitudes);

        cardSolicitarPrestamo = findViewById(R.id.cardSolicitarPrestamo);
        cardVerMisSolicitudes = findViewById(R.id.cardVerMisSolicitudes);
        cardRegistrarDevolucion = findViewById(R.id.cardRegistrarDevolucion);


        userRol = getIntent().getStringExtra("rol");

        configurarUI(userRol);

        btnSolicitar.setOnClickListener(v -> {
            if (rolPermitido("Instructor")) {
                startActivity(new Intent(this, SolicitarPrestamoActivity.class));
            } else {
                Toast.makeText(this, "Solo los instructores pueden solicitar préstamos", Toast.LENGTH_SHORT).show();
            }
        });

        btnListaSolicitudes.setOnClickListener(v -> {
            if (rolPermitido("Supervisor", "Admin")) {
                startActivity(new Intent(this, ListaSolicitudesActivity.class));
            } else {
                Toast.makeText(this, "No tienes permiso para ver solicitudes", Toast.LENGTH_SHORT).show();
            }
        });

        btnRegistrarDevolucion.setOnClickListener(v -> {
            if (rolPermitido("Instructor", "Supervisor", "Admin")) {
                startActivity(new Intent(this, RegistrarDevolucionActivity.class));
            } else {
                Toast.makeText(this, "No tienes permiso para registrar devoluciones", Toast.LENGTH_SHORT).show();
            }
        });

        btnListaDevoluciones.setOnClickListener(v -> {
            if (rolPermitido("Instructor", "Supervisor", "Admin")) {
                DevolucionesBottomSheet bottomSheet = new DevolucionesBottomSheet();
                bottomSheet.show(getSupportFragmentManager(), "DevolucionesBottomSheet");
            } else {
                Toast.makeText(this, "No tienes permiso para ver devoluciones", Toast.LENGTH_SHORT).show();
            }
        });

        btnVerMisSolicitudes.setOnClickListener(v -> {
            MisSolicitudesBottomSheet dialog = new MisSolicitudesBottomSheet();
            dialog.show(getSupportFragmentManager(), "MisSolicitudesBottomSheet");
        });

        getWindow().setBackgroundDrawableResource(R.drawable.fondo4);
    }


    private void configurarUI(String rol) {
        if (rol == null) return;

        // Solo el Instructor puede solicitar préstamo y ver sus solicitudes
        if (!rol.equalsIgnoreCase("Instructor")) {
            cardSolicitarPrestamo.setVisibility(View.GONE);
            cardVerMisSolicitudes.setVisibility(View.GONE);
        }

        // Solo Supervisor y Admin pueden ver la lista de solicitudes (para validar o controlar)
        if (!rol.equalsIgnoreCase("Supervisor") && !rol.equalsIgnoreCase("Admin")) {
            findViewById(R.id.cardListaSolicitudes).setVisibility(View.GONE);
        }

        // Solo Instructor puede registrar devoluciones
        if (!rol.equalsIgnoreCase("Instructor")) {
            findViewById(R.id.cardRegistrarDevolucion).setVisibility(View.GONE);
        }
    }



    private boolean rolPermitido(String... roles) {
        for (String rol : roles) {
            if (userRol.equalsIgnoreCase(rol)) return true;
        }
        return false;
    }
}
