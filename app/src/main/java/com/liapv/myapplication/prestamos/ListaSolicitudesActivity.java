package com.liapv.myapplication.prestamos;
import com.liapv.myapplication.modelos.Equipo;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.*;
import com.liapv.myapplication.R;

import java.util.ArrayList;
import java.util.List;

public class ListaSolicitudesActivity extends AppCompatActivity {

    private RecyclerView rvSolicitudes;
    private SolicitudesAdapter adapter;

    private DatabaseReference dbPrestamos;
    private List<Prestamo> listaSolicitudes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_solicitudes);

        rvSolicitudes = findViewById(R.id.rvSolicitudes);
        rvSolicitudes.setLayoutManager(new LinearLayoutManager(this));

        adapter = new SolicitudesAdapter(listaSolicitudes, new SolicitudesAdapter.OnSolicitudActionListener() {
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

        dbPrestamos = FirebaseDatabase.getInstance().getReference("prestamos");

        cargarSolicitudes();
    }

    private void cargarSolicitudes() {
        dbPrestamos.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaSolicitudes.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Prestamo p = ds.getValue(Prestamo.class);
                    if (p != null) {
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
        if (idPrestamo == null) return;

        DatabaseReference prestamoRef = dbPrestamos.child(idPrestamo);
        prestamo.setEstado(nuevoEstado);

        if (nuevoEstado.equals(getString(R.string.prestamo_estado_aprobado))) {
            // Reducir stock del equipo
            DatabaseReference equipoRef = FirebaseDatabase.getInstance().getReference("equipos").child(prestamo.getEquipoId());

            equipoRef.runTransaction(new Transaction.Handler() {
                @NonNull
                @Override
                public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                    Equipo equipo = currentData.getValue(Equipo.class);
                    if (equipo == null) {
                        return Transaction.abort();
                    }

                    int stockActual = equipo.getStock();
                    if (stockActual <= 0) {
                        return Transaction.abort();
                    }

                    equipo.setStock(stockActual - 1);
                    currentData.setValue(equipo);
                    return Transaction.success(currentData);
                }

                @Override
                public void onComplete(@Nullable DatabaseError error, boolean committed, DataSnapshot snapshot) {
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
            // Estado Rechazado
            prestamoRef.setValue(prestamo).addOnSuccessListener(aVoid -> {
                Toast.makeText(this, getString(R.string.prestamo_msg_rechazado), Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(e -> {
                Toast.makeText(this, getString(R.string.prestamo_msg_error_actualizar), Toast.LENGTH_SHORT).show();
            });
        }
    }
}
