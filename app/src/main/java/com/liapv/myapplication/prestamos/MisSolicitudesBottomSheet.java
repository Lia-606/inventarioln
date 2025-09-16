package com.liapv.myapplication.prestamos;

import android.os.Bundle;
import android.view.*;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;


import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import androidx.recyclerview.widget.*;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.liapv.myapplication.R;
import com.liapv.myapplication.modelos.Prestamo;

import java.util.*;

public class MisSolicitudesBottomSheet extends BottomSheetDialogFragment {

    private RecyclerView recyclerView;
    private MisPrestamosAdapter adapter;
    private List<Prestamo> lista = new ArrayList<>();
    private DatabaseReference dbRef;
    private String uidUsuario;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_mis_solicitudes, container, false);

        recyclerView = v.findViewById(R.id.rvMisPrestamos);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MisPrestamosAdapter(lista);
        recyclerView.setAdapter(adapter);

        uidUsuario = FirebaseAuth.getInstance().getCurrentUser().getUid();
        dbRef = FirebaseDatabase.getInstance().getReference("prestamos");

        cargarMisSolicitudes();

        return v;
    }

    private void cargarMisSolicitudes() {
        dbRef.orderByChild("usuarioIdSolicitante").equalTo(uidUsuario)

                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        lista.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Prestamo p = ds.getValue(Prestamo.class);
                            if (p != null) {
                                Log.d("FIREBASE", "Prestamo cargado: " + p.toString());
                                lista.add(p);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }


                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Error al cargar solicitudes", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
