package com.liapv.myapplication.inventario;

import android.os.Bundle;
import android.widget.SearchView;
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

public class StockActivity extends AppCompatActivity {

    private RecyclerView rvStock;
    private SearchView searchView;
    private StockAdapter adapter;

    private List<Equipo> equipoList;           // Lista completa
    private List<Equipo> equipoListFiltrada;   // Lista filtrada para mostrar

    private DatabaseReference dbEquipos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock);

        rvStock = findViewById(R.id.rvStock);
        searchView = findViewById(R.id.searchView);

        equipoList = new ArrayList<>();
        equipoListFiltrada = new ArrayList<>();

        adapter = new StockAdapter(equipoListFiltrada);
        rvStock.setLayoutManager(new LinearLayoutManager(this));
        rvStock.setAdapter(adapter);

        dbEquipos = FirebaseDatabase.getInstance().getReference("equipos");

        cargarEquipos();

        // Listener para el campo de búsqueda
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filtrarLista(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filtrarLista(newText);
                return true;
            }
        });
    }

    private void cargarEquipos() {
        dbEquipos.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                equipoList.clear();
                for (DataSnapshot equipoSnap : snapshot.getChildren()) {
                    String id = equipoSnap.getKey();
                    String nombre = equipoSnap.child("nombre").getValue(String.class);
                    String codigo = equipoSnap.child("codigo").getValue(String.class); // Asegúrate que 'codigo' exista en Firebase
                    Integer stock = equipoSnap.child("stock").getValue(Integer.class);

                    if (id != null && nombre != null && stock != null) {
                        Equipo equipo = new Equipo(id, nombre, stock);
                        equipo.setCodigo(codigo);  // Solo si tu modelo tiene campo código
                        equipoList.add(equipo);
                    }
                }

                filtrarLista(searchView.getQuery().toString());  // Aplica filtro si hay texto escrito
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(StockActivity.this, "Error al cargar equipos: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filtrarLista(String texto) {
        String textoLower = texto.toLowerCase();
        equipoListFiltrada.clear();

        for (Equipo equipo : equipoList) {
            boolean coincide = (equipo.getNombre() != null && equipo.getNombre().toLowerCase().contains(textoLower))
                    || (equipo.getCodigo() != null && equipo.getCodigo().toLowerCase().contains(textoLower))
                    || (equipo.getTipo() != null && equipo.getTipo().toLowerCase().contains(textoLower))
                    || (equipo.getMarca() != null && equipo.getMarca().toLowerCase().contains(textoLower))
                    || (equipo.getModelo() != null && equipo.getModelo().toLowerCase().contains(textoLower))
                    || (equipo.getEstado() != null && equipo.getEstado().toLowerCase().contains(textoLower))
                    || (equipo.getUbicacion() != null && equipo.getUbicacion().toLowerCase().contains(textoLower));

            if (coincide) {
                equipoListFiltrada.add(equipo);
            }
        }

        adapter.notifyDataSetChanged();
    }

}
