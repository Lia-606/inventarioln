package com.liapv.myapplication.prestamos;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.liapv.myapplication.R;
import com.liapv.myapplication.modelos.Prestamo;

import java.util.ArrayList;
import java.util.List;

public class ListaDevolucionesActivity extends AppCompatActivity {

    RecyclerView recyclerDevoluciones;
    DevolucionesAdapter adapter;
    List<Prestamo> listaDevoluciones;

    DatabaseReference prestamosRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_devoluciones);

        recyclerDevoluciones = findViewById(R.id.recyclerDevoluciones);
        recyclerDevoluciones.setLayoutManager(new LinearLayoutManager(this));

        listaDevoluciones = new ArrayList<>();
        adapter = new DevolucionesAdapter(this, listaDevoluciones);
        recyclerDevoluciones.setAdapter(adapter);

        prestamosRef = FirebaseDatabase.getInstance().getReference("prestamos");

        cargarDevoluciones();
        getWindow().setBackgroundDrawableResource(R.drawable.fondo4);
    }

    private void cargarDevoluciones() {
        prestamosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaDevoluciones.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Prestamo prestamo = dataSnapshot.getValue(Prestamo.class);

                    if (prestamo != null &&
                            ("Devuelto".equalsIgnoreCase(prestamo.getEstado()) || prestamo.getFechaDevolucion() != null)) {
                        listaDevoluciones.add(prestamo);
                    }
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Manejo de errores
            }
        });
    }
}

