package com.liapv.myapplication.prestamos;

import com.liapv.myapplication.modelos.Equipo;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.*;
import com.liapv.myapplication.R;
import com.liapv.myapplication.modelos.Prestamo;
import java.text.SimpleDateFormat;
import java.util.*;

public class RegistrarDevolucionActivity extends AppCompatActivity {

    private Spinner spinnerPrestamosActivos;
    private Button btnRegistrarDevolucion;
    private EditText etCantidadDevolver;

    private DatabaseReference dbPrestamos;
    private DatabaseReference dbEquipos;

    private List<Prestamo> listaPrestamosPendientes = new ArrayList<>();
    private Map<String, Prestamo> mapPrestamos = new HashMap<>();

    private ArrayAdapter<String> adapterSpinner;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar_devolucion);

        spinnerPrestamosActivos = findViewById(R.id.spinnerPrestamosActivos);
        btnRegistrarDevolucion = findViewById(R.id.btnRegistrarDevolucion);
        etCantidadDevolver = findViewById(R.id.etCantidadDevolver);

        dbPrestamos = FirebaseDatabase.getInstance().getReference("prestamos");
        dbEquipos = FirebaseDatabase.getInstance().getReference("equipos");

        cargarPrestamosPendientes();

        btnRegistrarDevolucion.setOnClickListener(v -> registrarDevolucion());
        getWindow().setBackgroundDrawableResource(R.drawable.fondo4);
    }

    private void cargarPrestamosPendientes() {
        dbPrestamos.orderByChild("estado").equalTo(getString(R.string.prestamo_estado_aprobado))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        listaPrestamosPendientes.clear();
                        List<String> listaNombres = new ArrayList<>();
                        mapPrestamos.clear();

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Prestamo p = ds.getValue(Prestamo.class);
                            if (p != null && (p.getFechaDevolucion() == null || p.getFechaDevolucion().isEmpty())) {
                                String key = p.getEquipoNombre() + " - " + p.getNombreSolicitante();
                                listaPrestamosPendientes.add(p);
                                listaNombres.add(key);
                                mapPrestamos.put(key, p);
                            }
                        }

                        adapterSpinner = new ArrayAdapter<>(RegistrarDevolucionActivity.this,
                                android.R.layout.simple_spinner_item, listaNombres);
                        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerPrestamosActivos.setAdapter(adapterSpinner);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(RegistrarDevolucionActivity.this,
                                getString(R.string.prestamo_msg_error_cargar),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void registrarDevolucion() {
        if (spinnerPrestamosActivos.getSelectedItem() == null) {
            Toast.makeText(this,
                    getString(R.string.prestamo_msg_seleccione_prestamo),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String cantidadStr = etCantidadDevolver.getText().toString().trim();
        if (cantidadStr.isEmpty()) {
            Toast.makeText(this, "Ingrese la cantidad a devolver", Toast.LENGTH_SHORT).show();
            return;
        }

        int cantidadDevolver = Integer.parseInt(cantidadStr);

        String keySeleccionado = spinnerPrestamosActivos.getSelectedItem().toString();
        Prestamo prestamo = mapPrestamos.get(keySeleccionado);

        if (prestamo == null) {
            Toast.makeText(this,
                    getString(R.string.prestamo_msg_prestamo_no_valido),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        int cantidadPrestada = prestamo.getCantidad();
        int cantidadDevuelta = prestamo.getCantidadDevuelta(); // Debes tener este campo en tu modelo
        int cantidadPendiente = cantidadPrestada - cantidadDevuelta;

        if (cantidadDevolver <= 0 || cantidadDevolver > cantidadPendiente) {
            Toast.makeText(this,
                    "La cantidad a devolver es inválida. Queda pendiente devolver: " + cantidadPendiente,
                    Toast.LENGTH_LONG).show();
            return;
        }

        int nuevaCantidadDevuelta = cantidadDevuelta + cantidadDevolver;
        String fechaDevolucion = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        // Actualizar devolución
        DatabaseReference prestamoRef = dbPrestamos.child(prestamo.getId());
        prestamo.setCantidadDevuelta(nuevaCantidadDevuelta);

        if (nuevaCantidadDevuelta == cantidadPrestada) {
            prestamo.setEstado(getString(R.string.prestamo_estado_devuelto));
            prestamo.setFechaDevolucion(fechaDevolucion);
        }

        prestamoRef.setValue(prestamo).addOnSuccessListener(aVoid -> {
            // Actualizar stock del equipo
            DatabaseReference equipoRef = dbEquipos.child(prestamo.getEquipoId());
            equipoRef.runTransaction(new Transaction.Handler() {
                @Override
                public Transaction.Result doTransaction(MutableData currentData) {
                    Equipo equipo = currentData.getValue(Equipo.class);
                    if (equipo == null) {
                        return Transaction.abort();
                    }
                    equipo.setStock(equipo.getStock() + cantidadDevolver);
                    currentData.setValue(equipo);
                    return Transaction.success(currentData);
                }

                @Override
                public void onComplete(DatabaseError error, boolean committed, DataSnapshot currentData) {
                    if (committed) {
                        Toast.makeText(RegistrarDevolucionActivity.this,
                                getString(R.string.prestamo_msg_devolucion_registrada),
                                Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(RegistrarDevolucionActivity.this,
                                getString(R.string.prestamo_msg_error_actualizar_stock),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(RegistrarDevolucionActivity.this,
                    getString(R.string.prestamo_msg_error_actualizar_prestamo),
                    Toast.LENGTH_SHORT).show();
        });
    }
}
