package com.liapv.myapplication.equipos;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.*;
import com.liapv.myapplication.R;
import com.liapv.myapplication.modelos.Equipo;

import java.util.ArrayList;
import java.util.List;

public class SeleccionarEquipoQRActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private List<Equipo> listaEquipos = new ArrayList<>();
    private DatabaseReference equiposRef;
    private EquipoAdapter adaptador;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seleccionar_equipo_qr);

        recyclerView = findViewById(R.id.recyclerEquiposQR);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adaptador = new EquipoAdapter(this, listaEquipos, true); // modoQR = true

        recyclerView.setAdapter(adaptador);

        equiposRef = FirebaseDatabase.getInstance().getReference("equipos");
        cargarEquipos();
    }

    private void cargarEquipos() {
        equiposRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaEquipos.clear();
                for (DataSnapshot dato : snapshot.getChildren()) {
                    Equipo equipo = dato.getValue(Equipo.class);
                    if (equipo != null) {
                        equipo.setId(dato.getKey());
                        listaEquipos.add(equipo);
                    }
                }
                adaptador.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SeleccionarEquipoQRActivity.this, "Error al cargar equipos", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
