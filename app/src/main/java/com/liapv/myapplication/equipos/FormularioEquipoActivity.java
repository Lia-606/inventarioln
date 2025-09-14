package com.liapv.myapplication.equipos;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.liapv.myapplication.R;
import com.liapv.myapplication.modelos.Equipo;

public class FormularioEquipoActivity extends AppCompatActivity {

    private EditText etNombre, etTipo, etMarca, etModelo, etEstado, etUbicacion, etCodigo, etStock;
    private Button btnGuardar;
    private DatabaseReference equiposRef;
    private String equipoId = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_formulario_equipo);

        // Inicializar vistas
        etNombre = findViewById(R.id.etNombre);
        etTipo = findViewById(R.id.etTipo);
        etMarca = findViewById(R.id.etMarca);
        etModelo = findViewById(R.id.etModelo);
        etEstado = findViewById(R.id.etEstado);
        etUbicacion = findViewById(R.id.etUbicacion);
        etCodigo = findViewById(R.id.etCodigo);
        etStock = findViewById(R.id.etStock);

        btnGuardar = findViewById(R.id.btnGuardarEquipo);
        equiposRef = FirebaseDatabase.getInstance().getReference("equipos");

        equipoId = getIntent().getStringExtra("equipo_id");

        if (equipoId != null) {
            // Aquí cargarías los datos del equipo para editar (omito consulta Firebase para simplificar)
            // Por ejemplo: cargarEquipo(equipoId);
            // Para que puedas ver cómo cargar los datos, dime si quieres que te lo arme.
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
        String stockStr = etStock.getText().toString().trim();

        if (TextUtils.isEmpty(nombre) || TextUtils.isEmpty(tipo) || TextUtils.isEmpty(stockStr)) {
            Toast.makeText(this, "Por favor, complete los campos obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        int stock;
        try {
            stock = Integer.parseInt(stockStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "El stock debe ser un número válido", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(codigo)) {
            codigo = equiposRef.push().getKey();
            etCodigo.setText(codigo);
        }

        Equipo equipo = new Equipo(nombre, tipo, marca, modelo, estado, ubicacion, codigo, stock);

        if (equipoId == null) {
            // Nuevo equipo
            String id = equiposRef.push().getKey();
            if (id != null) {
                equipo.setId(id);
                equiposRef.child(id).setValue(equipo)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Equipo registrado exitosamente", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, "Error al registrar equipo", Toast.LENGTH_SHORT).show());
            }
        } else {
            // Actualizar equipo existente
            equipo.setId(equipoId);
            equiposRef.child(equipoId).setValue(equipo)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Equipo actualizado exitosamente", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Error al actualizar equipo", Toast.LENGTH_SHORT).show());
        }
    }
}
