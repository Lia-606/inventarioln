package com.liapv.myapplication.inventario;

import android.os.Bundle;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.*;
import com.liapv.myapplication.R;

import java.text.SimpleDateFormat;
import java.util.*;

public class RegistrarSalidaActivity extends AppCompatActivity {

    private Spinner spinnerEquiposSalida, spnMotivo;
    private EditText edtCantidadSalida, edtResponsableSalida;
    private Button btnRegistrarSalida;

    private DatabaseReference dbInventario, dbEquipos;

    private Map<String, Equipo> mapEquiposSalida = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar_salida);

        spinnerEquiposSalida = findViewById(R.id.spinnerEquiposSalida);
        edtCantidadSalida = findViewById(R.id.edtCantidadSalida);
        edtResponsableSalida = findViewById(R.id.edtResponsableSalida);
        spnMotivo = findViewById(R.id.spnMotivo);
        btnRegistrarSalida = findViewById(R.id.btnRegistrarSalida);

        dbInventario = FirebaseDatabase.getInstance().getReference("inventario").child("salidas");
        dbEquipos = FirebaseDatabase.getInstance().getReference("equipos");

        // Motivos de salida desde strings.xml
        ArrayAdapter<CharSequence> adapterMotivo = ArrayAdapter.createFromResource(this,
                R.array.salida_motivos, android.R.layout.simple_spinner_item);
        adapterMotivo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnMotivo.setAdapter(adapterMotivo);

        cargarEquipos();

        btnRegistrarSalida.setOnClickListener(v -> registrarSalida());
    }

    private void cargarEquipos() {
        dbEquipos.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> nombresEquipos = new ArrayList<>();
                nombresEquipos.add("Seleccione un equipo");

                mapEquiposSalida.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    Equipo equipo = ds.getValue(Equipo.class);
                    if (equipo != null) {
                        equipo.setId(ds.getKey());

                        String key = (equipo.getNombre() != null ? equipo.getNombre() : "Sin nombre")
                                + " - " + (equipo.getCodigo() != null ? equipo.getCodigo() : "Sin código");

                        nombresEquipos.add(key);
                        mapEquiposSalida.put(key, equipo);
                    }
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(RegistrarSalidaActivity.this,
                        android.R.layout.simple_spinner_item, nombresEquipos);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerEquiposSalida.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RegistrarSalidaActivity.this, "Error al cargar equipos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void registrarSalida() {
        String seleccion = (String) spinnerEquiposSalida.getSelectedItem();
        if (seleccion == null || seleccion.equals("Seleccione un equipo")) {
            Toast.makeText(this, "Seleccione un equipo válido", Toast.LENGTH_SHORT).show();
            return;
        }

        Equipo equipoSeleccionado = mapEquiposSalida.get(seleccion);
        if (equipoSeleccionado == null) {
            Toast.makeText(this, "Equipo no válido", Toast.LENGTH_SHORT).show();
            return;
        }

        String equipoId = equipoSeleccionado.getId();
        String cantidadStr = edtCantidadSalida.getText().toString().trim();
        String responsable = edtResponsableSalida.getText().toString().trim();
        String motivo = spnMotivo.getSelectedItem().toString();

        if (cantidadStr.isEmpty() || responsable.isEmpty()) {
            Toast.makeText(this, getString(R.string.salida_msg_complete_campos), Toast.LENGTH_SHORT).show();
            return;
        }

        int cantidad;
        try {
            cantidad = Integer.parseInt(cantidadStr);
            if (cantidad <= 0) {
                Toast.makeText(this, getString(R.string.salida_msg_complete_campos), Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, getString(R.string.salida_msg_complete_campos), Toast.LENGTH_SHORT).show();
            return;
        }

        String fecha = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String hora = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

        dbEquipos.child(equipoId).child("stock").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int stockActual = snapshot.exists() ? snapshot.getValue(Integer.class) : 0;

                if (stockActual >= cantidad) {
                    String id = dbInventario.push().getKey();
                    Salida salida = new Salida(equipoId, cantidad, fecha, hora, responsable, motivo);
                    dbInventario.child(id).setValue(salida);

                    dbEquipos.child(equipoId).child("stock").setValue(stockActual - cantidad);

                    Toast.makeText(RegistrarSalidaActivity.this, getString(R.string.salida_msg_salida_registrada), Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(RegistrarSalidaActivity.this, getString(R.string.salida_msg_stock_insuficiente), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RegistrarSalidaActivity.this, getString(R.string.salida_msg_error_db), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static class Salida {
        public String equipoId, fecha, hora, responsable, motivo;
        public int cantidad;

        public Salida() {}

        public Salida(String equipoId, int cantidad, String fecha, String hora, String responsable, String motivo) {
            this.equipoId = equipoId;
            this.cantidad = cantidad;
            this.fecha = fecha;
            this.hora = hora;
            this.responsable = responsable;
            this.motivo = motivo;
        }
    }

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
