package com.liapv.myapplication.equipos;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.*;

import com.liapv.myapplication.R;
import com.liapv.myapplication.modelos.Equipo;

import java.util.ArrayList;
import java.util.List;

public class ListaEquiposActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EquipoAdapter equipoAdapter;
    private List<Equipo> listaEquipos;
    private DatabaseReference equiposRef;
    private FloatingActionButton btnAgregar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_equipos);

        recyclerView = findViewById(R.id.recyclerEquipos);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        listaEquipos = new ArrayList<>();
        equipoAdapter = new EquipoAdapter(this, listaEquipos);
        recyclerView.setAdapter(equipoAdapter);

        btnAgregar = findViewById(R.id.btnAgregarEquipo);
        btnAgregar.setOnClickListener(v -> {
            startActivity(new Intent(ListaEquiposActivity.this, FormularioEquipoActivity.class));
        });

        // Firebase
        equiposRef = FirebaseDatabase.getInstance().getReference("equipos");
        cargarEquipos();
    }

    private void cargarEquipos() {
        equiposRef.addValueEventListener(new ValueEventListener() {
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
                equipoAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ListaEquiposActivity.this, getString(R.string.equipos_msg_error_cargar), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
