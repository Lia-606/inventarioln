package com.liapv.myapplication.inventario;

import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.*;
import com.liapv.myapplication.R;
import java.util.ArrayList;
import java.util.List;
import com.liapv.myapplication.modelos.Equipo;

public class StockActivity extends AppCompatActivity {

    private RecyclerView rvStock;
    private StockAdapter adapter;
    private List<Equipo> equipoList;
    private DatabaseReference dbEquipos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock);

        rvStock = findViewById(R.id.rvStock);
        rvStock.setLayoutManager(new LinearLayoutManager(this));  // Muy importante

        equipoList = new ArrayList<>();
        adapter = new StockAdapter(equipoList);
        rvStock.setAdapter(adapter);

        dbEquipos = FirebaseDatabase.getInstance().getReference("equipos");
        cargarEquipos();
        rvStock.setLayoutManager(new LinearLayoutManager(this));

    }

    private void cargarEquipos() {
        dbEquipos.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                equipoList.clear();
                for (DataSnapshot equipoSnap : snapshot.getChildren()) {
                    String id = equipoSnap.getKey();
                    String nombre = equipoSnap.child("nombre").getValue(String.class);
                    Integer stock = equipoSnap.child("stock").getValue(Integer.class);
                    if (nombre != null && stock != null && id != null) {
                        equipoList.add(new Equipo(id, nombre, stock));
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(StockActivity.this, "Error al cargar equipos: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
