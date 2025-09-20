package com.liapv.myapplication.prestamos;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.liapv.myapplication.R;
import com.liapv.myapplication.modelos.Equipo;
import com.liapv.myapplication.modelos.Prestamo;

import java.util.ArrayList;
import java.util.List;

public class ListaSolicitudesActivity extends AppCompatActivity {

    private RecyclerView rvSolicitudes;
    private SolicitudesAdapter adapter;
    private DatabaseReference dbPrestamos;

    private List<Prestamo> listaSolicitudes = new ArrayList<>();
    private boolean puedeAprobar = false; // ✅ Solo uno

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_solicitudes);

        rvSolicitudes = findViewById(R.id.rvSolicitudes);
        rvSolicitudes.setLayoutManager(new LinearLayoutManager(this));

        dbPrestamos = FirebaseDatabase.getInstance().getReference("prestamos");

        // ✅ Obtener el UID del usuario actual
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference refRol = FirebaseDatabase.getInstance().getReference("usuarios").child(uid).child("rol");

        // ✅ Verificar el rol y asignar permisos
        refRol.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                String rol = snapshot.getValue(String.class);
                if ("Supervisor".equalsIgnoreCase(rol)) {
                    puedeAprobar = true;
                }
            }

            // ✅ Configurar adapter con permisos correctos
            adapter = new SolicitudesAdapter(listaSolicitudes, puedeAprobar, new SolicitudesAdapter.OnSolicitudActionListener() {
                @Override
                public void onAprobar(Prestamo prestamo) {
                    cambiarEstadoPrestamo(prestamo, getString(R.string.prestamo_estado_aprobado));
                }

                @Override
                public void onRechazar(Prestamo prestamo) {
                    cambiarEstadoPrestamo(prestamo, getString(R.string.prestamo_estado_rechazado));
                }
            });

            rvSolicitudes.setAdapter(adapter);
            cargarSolicitudes();

        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error al obtener el rol del usuario", Toast.LENGTH_SHORT).show();
        });

        getWindow().setBackgroundDrawableResource(R.drawable.fondo4);
    }

    private void cargarSolicitudes() {
        dbPrestamos.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaSolicitudes.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Prestamo p = ds.getValue(Prestamo.class);
                    if (p != null && !"Devuelto".equalsIgnoreCase(p.getEstado())) {
                        listaSolicitudes.add(p);
                    }
                }
                adapter.actualizarLista(listaSolicitudes);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ListaSolicitudesActivity.this, getString(R.string.prestamo_msg_error_cargar), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cambiarEstadoPrestamo(Prestamo prestamo, String nuevoEstado) {
        String idPrestamo = prestamo.getId();
        if (idPrestamo == null) {
            Toast.makeText(this, "ID del préstamo inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        String equipoId = prestamo.getEquipoId();
        if (equipoId == null || equipoId.isEmpty()) {
            Toast.makeText(this, "El préstamo no tiene un equipo asignado.", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference prestamoRef = dbPrestamos.child(idPrestamo);
        prestamo.setEstado(nuevoEstado);

        if (nuevoEstado.equals(getString(R.string.prestamo_estado_aprobado))) {
            // ✅ Reducir stock
            DatabaseReference equipoRef = FirebaseDatabase.getInstance().getReference("equipos").child(equipoId);

            equipoRef.runTransaction(new Transaction.Handler() {
                @NonNull
                @Override
                public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                    Equipo equipo = currentData.getValue(Equipo.class);
                    if (equipo == null) {
                        return Transaction.abort();
                    }

                    int stockActual = equipo.getStock();
                    int cantidad = prestamo.getCantidad();

                    if (stockActual < cantidad) {
                        return Transaction.abort(); // No hay stock suficiente
                    }

                    equipo.setStock(stockActual - cantidad);
                    currentData.setValue(equipo);
                    return Transaction.success(currentData);
                }

                @Override
                public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot snapshot) {
                    if (committed) {
                        prestamoRef.setValue(prestamo).addOnSuccessListener(aVoid -> {
                            Toast.makeText(ListaSolicitudesActivity.this, getString(R.string.prestamo_msg_aprobado), Toast.LENGTH_SHORT).show();
                        }).addOnFailureListener(e -> {
                            Toast.makeText(ListaSolicitudesActivity.this, getString(R.string.prestamo_msg_error_actualizar), Toast.LENGTH_SHORT).show();
                        });
                    } else {
                        Toast.makeText(ListaSolicitudesActivity.this, getString(R.string.prestamo_msg_stock_insuficiente), Toast.LENGTH_SHORT).show();
                    }
                }
            });

        } else {
            // ✅ Estado Rechazado
            prestamoRef.setValue(prestamo).addOnSuccessListener(aVoid -> {
                Toast.makeText(this, getString(R.string.prestamo_msg_rechazado), Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(e -> {
                Toast.makeText(this, getString(R.string.prestamo_msg_error_actualizar), Toast.LENGTH_SHORT).show();
            });
        }
    }
}
