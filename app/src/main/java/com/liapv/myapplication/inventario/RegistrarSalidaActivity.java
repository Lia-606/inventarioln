package com.liapv.myapplication.inventario;

import android.os.Bundle;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.*;
import com.liapv.myapplication.R;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RegistrarSalidaActivity extends AppCompatActivity {

    private EditText edtEquipoIdSalida, edtCantidadSalida, edtResponsableSalida;
    private Spinner spnMotivo;
    private Button btnRegistrarSalida;
    private DatabaseReference dbInventario, dbEquipos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar_salida);

        edtEquipoIdSalida = findViewById(R.id.edtEquipoIdSalida);
        edtCantidadSalida = findViewById(R.id.edtCantidadSalida);
        edtResponsableSalida = findViewById(R.id.edtResponsableSalida);
        spnMotivo = findViewById(R.id.spnMotivo);
        btnRegistrarSalida = findViewById(R.id.btnRegistrarSalida);

        dbInventario = FirebaseDatabase.getInstance().getReference("inventario").child("salidas");
        dbEquipos = FirebaseDatabase.getInstance().getReference("equipos");

        // Adaptador para el spinner con el arreglo definido en strings.xml
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.salida_motivos, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnMotivo.setAdapter(adapter);

        btnRegistrarSalida.setOnClickListener(v -> registrarSalida());
    }

    private void registrarSalida() {
        String equipoId = edtEquipoIdSalida.getText().toString().trim();
        String cantidadStr = edtCantidadSalida.getText().toString().trim();
        String responsable = edtResponsableSalida.getText().toString().trim();
        String motivo = spnMotivo.getSelectedItem().toString();

        if (equipoId.isEmpty() || cantidadStr.isEmpty() || responsable.isEmpty()) {
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
}
