package com.liapv.myapplication.equipos;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.liapv.myapplication.modelos.Equipo;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.liapv.myapplication.R;

public class FormularioEquipoActivity extends AppCompatActivity {

    private EditText etNombre, etTipo, etMarca, etModelo, etEstado, etUbicacion, etCodigo;
    private Button btnGuardar;
    private DatabaseReference equiposRef;
    private String equipoId = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_formulario_equipo);

        etNombre = findViewById(R.id.etNombre);
        etTipo = findViewById(R.id.etTipo);
        etMarca = findViewById(R.id.etMarca);
        etModelo = findViewById(R.id.etModelo);
        etEstado = findViewById(R.id.etEstado);
        etUbicacion = findViewById(R.id.etUbicacion);
        etCodigo = findViewById(R.id.etCodigo);

        btnGuardar = findViewById(R.id.btnGuardarEquipo);

        equiposRef = FirebaseDatabase.getInstance().getReference("equipos");

        equipoId = getIntent().getStringExtra("equipo_id");

        if (equipoId != null) {
            // Aquí podrías cargar los datos actuales del equipo si estás editando
            // Por simplicidad se omite en esta primera versión
        }

        btnGuardar.setOnClickListener(view -> guardarEquipo());


    }

    private void guardarEquipo() {
        String nombre = etNombre.getText().toString().trim();
        String tipo = etTipo.getText().toString().trim();
        String marca = etMarca.getText().toString().trim();
        String modelo = etModelo.getText().toString().trim();
        String estado = etEstado.getText().toString().trim();
        String ubicacion = etUbicacion.getText().toString().trim();
        String codigo = etCodigo.getText().toString().trim();

        if (TextUtils.isEmpty(nombre) || TextUtils.isEmpty(tipo) || TextUtils.isEmpty(codigo)) {
            Toast.makeText(this, R.string.equipos_msg_campos_vacios, Toast.LENGTH_SHORT).show();
            return;
        }

        Equipo equipo = new Equipo(nombre, tipo, marca, modelo, estado, ubicacion, codigo);

        if (equipoId == null) {
            String id = equiposRef.push().getKey();
            if (id != null) {
                equipo.setId(id);
                equiposRef.child(id).setValue(equipo)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, R.string.equipos_msg_equipo_registrado, Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, R.string.equipos_msg_error_registrar, Toast.LENGTH_SHORT).show());
            }
        } else {
            equipo.setId(equipoId);
            equiposRef.child(equipoId).setValue(equipo)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, R.string.equipos_msg_equipo_actualizado, Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, R.string.equipos_msg_error_actualizar, Toast.LENGTH_SHORT).show());
        }

    }

}
