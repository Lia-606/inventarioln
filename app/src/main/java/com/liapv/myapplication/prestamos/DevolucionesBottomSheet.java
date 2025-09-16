package com.liapv.myapplication.prestamos;

import android.os.Bundle;
import android.view.*;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import androidx.recyclerview.widget.*;

import com.google.firebase.database.*;
import com.liapv.myapplication.R;
import com.liapv.myapplication.modelos.Prestamo;

import java.util.ArrayList;
import java.util.List;

public class DevolucionesBottomSheet extends BottomSheetDialogFragment {

    private RecyclerView recyclerDevoluciones;
    private DevolucionesAdapter adapter;
    private List<Prestamo> listaDevoluciones = new ArrayList<>();
    private DatabaseReference prestamosRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_lista_devoluciones, container, false);

        recyclerDevoluciones = v.findViewById(R.id.recyclerDevoluciones);
        recyclerDevoluciones.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new DevolucionesAdapter(getContext(), listaDevoluciones);
        recyclerDevoluciones.setAdapter(adapter);

        prestamosRef = FirebaseDatabase.getInstance().getReference("prestamos");

        cargarDevoluciones();

        // Opcional: si quieres poner fondo como en tu Activity
        getDialog().getWindow().setBackgroundDrawableResource(R.drawable.fondo4);

        return v;

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
                        Log.d("FIREBASE", "Devolucion cargada: " + prestamo.toString());
                        listaDevoluciones.add(prestamo);
                    }
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error al cargar devoluciones", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

