package com.liapv.myapplication.equipos;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
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
    private List<Equipo> listaEquipos;             // Lista completa
    private List<Equipo> listaEquiposFiltrada;     // Lista filtrada para mostrar

    private DatabaseReference equiposRef;
    private FloatingActionButton btnAgregar;
    private AlertDialog dialogBusqueda;
    private String userRol = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_equipos);

        recyclerView = findViewById(R.id.recyclerEquipos);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        listaEquipos = new ArrayList<>();
        listaEquiposFiltrada = new ArrayList<>();

        // Obtener el rol del intent
        userRol = getIntent().getStringExtra("rol");

        // Pasar el rol al adaptador
        equipoAdapter = new EquipoAdapter(this, listaEquiposFiltrada, userRol);
        recyclerView.setAdapter(equipoAdapter);

        btnAgregar = findViewById(R.id.btnAgregarEquipo);
        btnAgregar.setOnClickListener(v -> {
            startActivity(new Intent(ListaEquiposActivity.this, FormularioEquipoActivity.class));
        });

        equiposRef = FirebaseDatabase.getInstance().getReference("equipos");
        cargarEquipos();

        // Botón buscar
        findViewById(R.id.cardBuscar).setOnClickListener(v -> mostrarDialogBusqueda());

        // Ocultar botón agregar si no es admin
        if (userRol == null || (!userRol.equalsIgnoreCase("Admin"))) {
            btnAgregar.setVisibility(View.GONE);
        }
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

                listaEquiposFiltrada.clear();
                listaEquiposFiltrada.addAll(listaEquipos);
                equipoAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ListaEquiposActivity.this, getString(R.string.equipos_msg_error_cargar), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarDialogBusqueda() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_busqueda, null);
        builder.setView(dialogView);

        EditText etBuscar = dialogView.findViewById(R.id.etBuscar);

        dialogBusqueda = builder.create();
        dialogBusqueda.show();

        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s,int start,int count,int after) {}

            @Override
            public void onTextChanged(CharSequence s,int start,int before,int count) {
                filtrarLista(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filtrarLista(String texto) {
        String textoLower = texto.toLowerCase();
        listaEquiposFiltrada.clear();

        for (Equipo equipo : listaEquipos) {
            boolean coincide = (equipo.getNombre() != null && equipo.getNombre().toLowerCase().contains(textoLower))
                    || (equipo.getCodigo() != null && equipo.getCodigo().toLowerCase().contains(textoLower))
                    || (equipo.getTipo() != null && equipo.getTipo().toLowerCase().contains(textoLower))
                    || (equipo.getMarca() != null && equipo.getMarca().toLowerCase().contains(textoLower))
                    || (equipo.getModelo() != null && equipo.getModelo().toLowerCase().contains(textoLower))
                    || (equipo.getEstado() != null && equipo.getEstado().toLowerCase().contains(textoLower))
                    || (equipo.getUbicacion() != null && equipo.getUbicacion().toLowerCase().contains(textoLower));

            if (coincide) {
                listaEquiposFiltrada.add(equipo);
            }
        }

        equipoAdapter.notifyDataSetChanged();
    }
}
