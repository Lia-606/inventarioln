package com.liapv.myapplication.perfil;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.liapv.myapplication.R;
import com.liapv.myapplication.MainActivity;
import com.liapv.myapplication.Dashboard; // 👈 importa tu Dashboard

public class MiPerfilActivity extends AppCompatActivity {

    private TextView tvNombre, tvApellido, tvCorreo, tvCelular;
    private ImageView ivFotoPerfil, btnMenuPerfil;
    private Button btnEditarPerfil, btnCerrarSesion;

    private DatabaseReference usuariosRef;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mi_perfil);

        // Referencias UI
        tvNombre = findViewById(R.id.tvNombrePerfilVal);
        tvApellido = findViewById(R.id.tvApellidoPerfilVal);
        tvCorreo = findViewById(R.id.tvCorreoPerfilVal);
        tvCelular = findViewById(R.id.tvCelularPerfilVal);
        ivFotoPerfil = findViewById(R.id.ivFotoPerfil);

        btnEditarPerfil = findViewById(R.id.btnEditarPerfil);
        btnCerrarSesion = findViewById(R.id.btnCerrarPerfil);
        btnMenuPerfil = findViewById(R.id.btnMenuPerfil); // 👈 nuevo

        // Firebase
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        usuariosRef = FirebaseDatabase.getInstance().getReference("usuarios").child(userId);

        cargarDatosPerfil();

        btnEditarPerfil.setOnClickListener(v -> mostrarDialogEditarPerfil());
        btnCerrarSesion.setOnClickListener(v -> cerrarSesion());

        // 👉 Acción para regresar al Dashboard
        btnMenuPerfil.setOnClickListener(v -> {
            Intent intent = new Intent(MiPerfilActivity.this, Dashboard.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void cargarDatosPerfil() {
        usuariosRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if(snapshot.exists()){
                    tvNombre.setText(snapshot.child("nombre").getValue(String.class));
                    tvApellido.setText(snapshot.child("apellido").getValue(String.class));
                    tvCorreo.setText(snapshot.child("correo").getValue(String.class));
                    tvCelular.setText(snapshot.child("celular").getValue(String.class));
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(MiPerfilActivity.this, "Error al cargar datos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarDialogEditarPerfil() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_editar_perfil, null);
        builder.setView(view);

        EditText etNombre = view.findViewById(R.id.etNombre);
        EditText etApellido = view.findViewById(R.id.etApellido);
        EditText etCelular = view.findViewById(R.id.etCelular);
        EditText etDireccion = view.findViewById(R.id.etDireccion);

        // Prellenar con datos actuales
        usuariosRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if(snapshot.exists()){
                    etNombre.setText(snapshot.child("nombre").getValue(String.class));
                    etApellido.setText(snapshot.child("apellido").getValue(String.class));
                    etCelular.setText(snapshot.child("celular").getValue(String.class));
                    etDireccion.setText(snapshot.child("direccion").getValue(String.class));
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });

        Button btnCancelar = view.findViewById(R.id.btnCancelar);
        Button btnGuardar = view.findViewById(R.id.btnGuardar);

        AlertDialog dialog = builder.create();

        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        btnGuardar.setOnClickListener(v -> {
            String nuevoNombre = etNombre.getText().toString().trim();
            String nuevoApellido = etApellido.getText().toString().trim();
            String nuevoCelular = etCelular.getText().toString().trim();
            String nuevaDireccion = etDireccion.getText().toString().trim();

            if(nuevoNombre.isEmpty() || nuevoApellido.isEmpty()){
                Toast.makeText(this, "Nombre y Apellido son obligatorios", Toast.LENGTH_SHORT).show();
                return;
            }

            // Actualizar Firebase
            usuariosRef.child("nombre").setValue(nuevoNombre);
            usuariosRef.child("apellido").setValue(nuevoApellido);
            usuariosRef.child("celular").setValue(nuevoCelular);
            usuariosRef.child("direccion").setValue(nuevaDireccion);

            Toast.makeText(this, "Perfil actualizado", Toast.LENGTH_SHORT).show();
            dialog.dismiss();

            // Refrescar UI
            cargarDatosPerfil();
        });

        dialog.show();
    }

    private void cerrarSesion() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(MiPerfilActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
