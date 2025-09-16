package com.liapv.myapplication.equipos;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.*;
import android.view.View;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.liapv.myapplication.modelos.Equipo;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.liapv.myapplication.R;

public class DetalleEquipoActivity extends AppCompatActivity {

    private TextView tvNombre, tvTipo, tvMarca, tvModelo, tvEstado, tvUbicacion, tvCodigo;
    private ImageView ivQR;
    private Button btnEditar, btnEliminar, btnVolver;
    private DatabaseReference equiposRef;
    private Equipo equipo;
    private String userRol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_equipo);

        tvNombre = findViewById(R.id.tvNombre);
        tvTipo = findViewById(R.id.tvTipo);
        tvMarca = findViewById(R.id.tvMarca);
        tvModelo = findViewById(R.id.tvModelo);
        tvEstado = findViewById(R.id.tvEstado);
        tvUbicacion = findViewById(R.id.tvUbicacion);
        tvCodigo = findViewById(R.id.tvCodigo);
        ivQR = findViewById(R.id.ivQR);

        btnEditar = findViewById(R.id.btnEditar);
        btnEliminar = findViewById(R.id.btnEliminar);
        btnVolver = findViewById(R.id.btnVolver);

        equiposRef = FirebaseDatabase.getInstance().getReference("equipos");

        equipo = (Equipo) getIntent().getSerializableExtra("equipo");
        userRol = getIntent().getStringExtra("rol");

        if (equipo != null) {
            tvNombre.setText(equipo.getNombre());
            tvTipo.setText(equipo.getTipo());
            tvMarca.setText(equipo.getMarca());
            tvModelo.setText(equipo.getModelo());
            tvEstado.setText(equipo.getEstado());
            tvUbicacion.setText(equipo.getUbicacion());
            tvCodigo.setText(equipo.getCodigo());

            generarQR(equipo.getCodigo());

            Toast.makeText(this, "Mostrando equipo: " + equipo.getNombre(), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "No se recibió equipo", Toast.LENGTH_SHORT).show();
            finish();  // Opcional: cerrar la actividad si no hay equipo
            return;
        }

        if (userRol != null && userRol.equalsIgnoreCase("Admin")) {
            btnEditar.setVisibility(View.VISIBLE);
            btnEliminar.setVisibility(View.VISIBLE);
        } else {
            btnEditar.setVisibility(View.GONE);
            btnEliminar.setVisibility(View.GONE);
        }

        btnEditar.setOnClickListener(v -> {
            Intent intent = new Intent(this, FormularioEquipoActivity.class);
            intent.putExtra("equipo_id", equipo.getId());
            startActivity(intent);
        });

        btnEliminar.setOnClickListener(v -> confirmarEliminacion());

        btnVolver.setOnClickListener(v -> finish());
    }

    private void generarQR(String data) {
        if (data == null || data.isEmpty()) {
            Toast.makeText(this, R.string.equipos_msg_error_codigo_vacio, Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Bitmap qrBitmap = QRGenerator.generarQR(data);
            ivQR.setImageBitmap(qrBitmap);
        } catch (Exception e) {
            Toast.makeText(this, R.string.equipos_msg_error_generar_qr, Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmarEliminacion() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.equipos_btn_eliminar)
                .setMessage(R.string.equipos_msg_confirmar_eliminacion)
                .setPositiveButton(android.R.string.yes, (dialog, which) -> eliminarEquipo())
                .setNegativeButton(android.R.string.no, (dialog, which) ->
                        Toast.makeText(this, R.string.equipos_msg_eliminacion_cancelada, Toast.LENGTH_SHORT).show())
                .show();
    }

    private void eliminarEquipo() {
        if (equipo != null && equipo.getId() != null) {
            equiposRef.child(equipo.getId()).removeValue()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, R.string.equipos_msg_equipo_eliminado, Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, R.string.equipos_msg_error_eliminar, Toast.LENGTH_SHORT).show());
        }
    }
}
