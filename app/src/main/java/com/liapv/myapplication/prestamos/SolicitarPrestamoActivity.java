package com.liapv.myapplication.prestamos;

import com.liapv.myapplication.modelos.Equipo;

import android.os.Bundle;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import com.liapv.myapplication.R;

import java.text.SimpleDateFormat;
import java.util.*;

public class SolicitarPrestamoActivity extends AppCompatActivity {

    private Spinner spinnerEquipos;
    private EditText edtObservaciones;
    private Button btnSolicitar;

    private DatabaseReference dbEquipos;
    private DatabaseReference dbPrestamos;

    private List<Equipo> listaEquipos = new ArrayList<>();
    private ArrayAdapter<String> adapterSpinner;
    private Map<String, Equipo> mapEquipos = new HashMap<>();

    private FirebaseAuth auth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solicitar_prestamo);

        spinnerEquipos = findViewById(R.id.spinnerEquipos);
        edtObservaciones = findViewById(R.id.edtObservaciones);
        btnSolicitar = findViewById(R.id.btnSolicitar);

        auth = FirebaseAuth.getInstance();
        dbEquipos = FirebaseDatabase.getInstance().getReference("equipos");
        dbPrestamos = FirebaseDatabase.getInstance().getReference("prestamos");

        cargarEquipos();

        btnSolicitar.setOnClickListener(v -> solicitarPrestamo());
    }

    private void cargarEquipos() {
        dbEquipos.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                listaEquipos.clear();
                List<String> nombresEquipos = new ArrayList<>();
                mapEquipos.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    Equipo equipo = ds.getValue(Equipo.class);
                    if (equipo != null && equipo.getStock() > 0) {
                        String key = equipo.getNombre() + " - " + equipo.getId();
                        listaEquipos.add(equipo);
                        nombresEquipos.add(key);
                        mapEquipos.put(key, equipo);
                    }
                }

                adapterSpinner = new ArrayAdapter<>(SolicitarPrestamoActivity.this,
                        android.R.layout.simple_spinner_item, nombresEquipos);
                adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerEquipos.setAdapter(adapterSpinner);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(SolicitarPrestamoActivity.this,
                        getString(R.string.prestamo_msg_error_cargar),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void solicitarPrestamo() {
        if (spinnerEquipos.getSelectedItem() == null) {
            Toast.makeText(this,
                    getString(R.string.prestamo_msg_error_campos_vacios),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String keySeleccionado = spinnerEquipos.getSelectedItem().toString();
        Equipo equipo = mapEquipos.get(keySeleccionado);

        if (equipo == null) {
            Toast.makeText(this,
                    getString(R.string.prestamo_msg_error_solicitud),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String observaciones = edtObservaciones.getText().toString().trim();

        Prestamo prestamo = crearPrestamo(equipo, observaciones);

        if (prestamo == null) {
            Toast.makeText(this,
                    getString(R.string.prestamo_msg_error_solicitud),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        dbPrestamos.child(prestamo.getId()).setValue(prestamo)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this,
                            getString(R.string.prestamo_msg_solicitud_exitosa),
                            Toast.LENGTH_SHORT).show();
                    finish(); // O limpiar campos si no deseas cerrar
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this,
                            getString(R.string.prestamo_msg_error_solicitud),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private Prestamo crearPrestamo(Equipo equipo, String observaciones) {
        String idPrestamo = dbPrestamos.push().getKey();
        if (idPrestamo == null) return null;

        FirebaseUser user = auth.getCurrentUser();
        String usuarioId = user != null ? user.getUid() : "desconocido";
        String nombreSolicitante = (user != null && user.getDisplayName() != null)
                ? user.getDisplayName()
                : getString(R.string.rol_instructor); // o "Instructor" directo

        String fechaSolicitud = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date());

        return new Prestamo(
                idPrestamo,
                equipo.getId(),
                equipo.getNombre(),
                usuarioId,
                nombreSolicitante,
                fechaSolicitud,
                "Pendiente",
                null,
                observaciones
        );
    }
}
