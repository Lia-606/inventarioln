package com.liapv.myapplication.inventario;

import android.os.Bundle;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.*;
import com.liapv.myapplication.R;

import java.text.SimpleDateFormat;
import java.util.*;

public class RegistrarEntradaActivity extends AppCompatActivity {

    private Spinner spinnerEquiposEntrada;
    private EditText edtCantidad, edtProveedor, edtResponsable;
    private Button btnRegistrar;

    private DatabaseReference dbInventario, dbEquipos;

    private Map<String, Equipo> mapEquiposEntrada = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar_entrada);

        spinnerEquiposEntrada = findViewById(R.id.spinnerEquiposEntrada);
        edtCantidad = findViewById(R.id.edtCantidad);
        edtProveedor = findViewById(R.id.edtProveedor);
        edtResponsable = findViewById(R.id.edtResponsable);
        btnRegistrar = findViewById(R.id.btnRegistrarEntrada);

        dbInventario = FirebaseDatabase.getInstance().getReference("inventario").child("entradas");
        dbEquipos = FirebaseDatabase.getInstance().getReference("equipos");

        cargarEquipos();

        btnRegistrar.setOnClickListener(v -> registrarEntrada());
    }

    private void cargarEquipos() {
        dbEquipos.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> nombresEquipos = new ArrayList<>();
                nombresEquipos.add("Seleccione un equipo");

                mapEquiposEntrada.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    Equipo equipo = ds.getValue(Equipo.class);
                    if (equipo != null) {
                        equipo.setId(ds.getKey());

                        String key = (equipo.getNombre() != null ? equipo.getNombre() : "Sin nombre")
                                + " - " + (equipo.getCodigo() != null ? equipo.getCodigo() : "Sin código");

                        nombresEquipos.add(key);
                        mapEquiposEntrada.put(key, equipo);
                    }
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(RegistrarEntradaActivity.this,
                        android.R.layout.simple_spinner_item, nombresEquipos);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerEquiposEntrada.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RegistrarEntradaActivity.this, "Error al cargar equipos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void registrarEntrada() {
        String seleccion = (String) spinnerEquiposEntrada.getSelectedItem();
        if (seleccion == null || seleccion.equals("Seleccione un equipo")) {
            Toast.makeText(this, "Seleccione un equipo válido", Toast.LENGTH_SHORT).show();
            return;
        }

        Equipo equipoSeleccionado = mapEquiposEntrada.get(seleccion);
        if (equipoSeleccionado == null) {
            Toast.makeText(this, "Equipo no válido", Toast.LENGTH_SHORT).show();
            return;
        }

        String equipoId = equipoSeleccionado.getId();
        String cantidadStr = edtCantidad.getText().toString().trim();
        String proveedor = edtProveedor.getText().toString().trim();
        String responsable = edtResponsable.getText().toString().trim();

        if (cantidadStr.isEmpty() || proveedor.isEmpty() || responsable.isEmpty()) {
            Toast.makeText(this, getString(R.string.entrada_msg_complete_campos), Toast.LENGTH_SHORT).show();
            return;
        }

        int cantidad;
        try {
            cantidad = Integer.parseInt(cantidadStr);
            if (cantidad <= 0) {
                Toast.makeText(this, getString(R.string.entrada_msg_ingrese_cantidad_valida), Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, getString(R.string.entrada_msg_cantidad_invalida), Toast.LENGTH_SHORT).show();
            return;
        }

        String fecha = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String hora = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

        String id = dbInventario.push().getKey();
        Entrada entrada = new Entrada(equipoId, cantidad, fecha, hora, proveedor, responsable);

        if (id == null) {
            Toast.makeText(this, getString(R.string.entrada_msg_error_registrar), Toast.LENGTH_SHORT).show();
            return;
        }

        dbInventario.child(id).setValue(entrada).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Actualizar stock del equipo
                dbEquipos.child(equipoId).child("stock").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int stockActual = 0;
                        if (snapshot.exists()) {
                            Integer value = snapshot.getValue(Integer.class);
                            if(value != null) stockActual = value;
                        }
                        dbEquipos.child(equipoId).child("stock").setValue(stockActual + cantidad);
                        Toast.makeText(RegistrarEntradaActivity.this, getString(R.string.entrada_msg_registro_exitoso), Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(RegistrarEntradaActivity.this, getString(R.string.entrada_msg_error_actualizar_stock), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(RegistrarEntradaActivity.this, getString(R.string.entrada_msg_error_registrar), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Modelo de entrada para Firebase
    public static class Entrada {
        public String equipoId, fecha, hora, proveedor, responsable;
        public int cantidad;

        public Entrada() {
            // Constructor vacío necesario para Firebase
        }

        public Entrada(String equipoId, int cantidad, String fecha, String hora, String proveedor, String responsable) {
            this.equipoId = equipoId;
            this.cantidad = cantidad;
            this.fecha = fecha;
            this.hora = hora;
            this.proveedor = proveedor;
            this.responsable = responsable;
        }
    }

    // Clase Equipo para mapear datos (puedes extraer esta clase en otro archivo)
    public static class Equipo {
        private String id;
        private String nombre;
        private String codigo;

        public Equipo() {}

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }

        public String getCodigo() { return codigo; }
        public void setCodigo(String codigo) { this.codigo = codigo; }
    }
}
