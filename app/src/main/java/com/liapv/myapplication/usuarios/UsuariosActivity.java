package com.liapv.myapplication.usuarios;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.liapv.myapplication.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;



public class UsuariosActivity extends AppCompatActivity {

    private RecyclerView rvUsuarios;
    private UsuariosAdapter adapter;
    private List<Usuario> listaUsuarios = new ArrayList<>();
    private List<Usuario> listaFiltrada = new ArrayList<>();
    private DatabaseReference usuariosRef;
    private SearchView searchView;
    private FloatingActionButton fabAddUsuario;
    private FirebaseAuth mAuth; // sesión principal (admin)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuarios);

        // Firebase
        usuariosRef = FirebaseDatabase.getInstance().getReference("usuarios");
        mAuth = FirebaseAuth.getInstance();

        // UI
        rvUsuarios = findViewById(R.id.rvUsuarios);
        searchView = findViewById(R.id.searchUsuarios);
        fabAddUsuario = findViewById(R.id.fabAddUsuario);

        // 🔎 Configuración extra para que funcione el teclado
        searchView.setIconifiedByDefault(false); // hace que esté expandido por defecto
        searchView.setFocusable(true);
        searchView.setFocusableInTouchMode(true);
        searchView.requestFocus(); // asegura que pueda escribir

        // Configuración RecyclerView
        rvUsuarios.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UsuariosAdapter(this, listaFiltrada, new UsuariosAdapter.OnItemClickListener() {
            @Override
            public void onEditarClick(Usuario usuario) {
                mostrarDialogoEditarUsuario(usuario); // ✅ llamamos al diálogo de editar
            }

            @Override
            public void onVerMasClick(Usuario usuario) {
                mostrarDialogoVerMasUsuario(usuario); // ✅ llamamos al diálogo de solo lectura
            }
        });
        rvUsuarios.setAdapter(adapter);


        // Cargar usuarios existentes
        cargarUsuarios();

        // Buscar usuarios
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filtrarUsuarios(newText);
                return true;
            }
        });

        // Abrir diálogo de registro
        fabAddUsuario.setOnClickListener(v -> mostrarDialogoRegistro());
    }

    private void cargarUsuarios() {
        usuariosRef.addValueEventListener(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaUsuarios.clear();
                if (!snapshot.exists()) {
                    Toast.makeText(UsuariosActivity.this, "No hay usuarios en Firebase", Toast.LENGTH_SHORT).show();
                    return;
                }

                String uidAdmin = mAuth.getCurrentUser().getUid(); // 🔹 UID del admin logueado

                for (DataSnapshot ds : snapshot.getChildren()) {
                    Usuario usuario = ds.getValue(Usuario.class);
                    if (usuario != null) {
                        usuario.setUid(ds.getKey());

                        // 🔹 Filtrar: si es el admin actual, NO agregar
                        if (!usuario.getUid().equals(uidAdmin)) {
                            listaUsuarios.add(usuario);
                        }
                    }
                }

                listaFiltrada.clear();
                listaFiltrada.addAll(listaUsuarios);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UsuariosActivity.this, "Error al cargar usuarios", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void filtrarUsuarios(String texto) {
        listaFiltrada.clear();
        if (TextUtils.isEmpty(texto)) {
            listaFiltrada.addAll(listaUsuarios);
        } else {
            for (Usuario u : listaUsuarios) {
                String nombreCompleto = (u.getNombre() + " " + u.getApellido()).toLowerCase();
                if (nombreCompleto.contains(texto.toLowerCase())
                        || u.getRol().toLowerCase().contains(texto.toLowerCase())
                        || u.getSede().toLowerCase().contains(texto.toLowerCase())) {
                    listaFiltrada.add(u);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    public void mostrarDialogoRegistro() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_registrar_usuario, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        EditText etNombre = dialogView.findViewById(R.id.etNombre);
        EditText etApellido = dialogView.findViewById(R.id.etApellido);
        EditText etCorreo = dialogView.findViewById(R.id.etCorreo);
        EditText etContrasena = dialogView.findViewById(R.id.etContrasena);
        EditText etCelular = dialogView.findViewById(R.id.etCelular);
        EditText etDireccion = dialogView.findViewById(R.id.etDireccion);
        Spinner spRol = dialogView.findViewById(R.id.spRol);   // ✅ Spinner
        Spinner spSede = dialogView.findViewById(R.id.spSede); // ✅ Spinner

        Button btnGuardar = dialogView.findViewById(R.id.btnGuardar);
        Button btnCancelar = dialogView.findViewById(R.id.btnCancelar);

        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        btnGuardar.setOnClickListener(v -> {
            String nombre = etNombre.getText().toString().trim();
            String apellido = etApellido.getText().toString().trim();
            String correo = etCorreo.getText().toString().trim();
            String contrasena = etContrasena.getText().toString().trim();
            String celular = etCelular.getText().toString().trim();
            String direccion = etDireccion.getText().toString().trim();
            String rol = spRol.getSelectedItem().toString();   // ✅ Spinner obtiene selección
            String sede = spSede.getSelectedItem().toString(); // ✅ Spinner obtiene selección

            if (TextUtils.isEmpty(nombre) || TextUtils.isEmpty(apellido) ||
                    TextUtils.isEmpty(correo) || TextUtils.isEmpty(contrasena)) {
                Toast.makeText(this, "Completa todos los campos obligatorios", Toast.LENGTH_SHORT).show();
                return;
            }

            // Crear un FirebaseApp secundario para no cerrar la sesión del admin
            FirebaseApp secondaryApp;
            try {
                secondaryApp = FirebaseApp.getInstance("secondary");
            } catch (IllegalStateException e) {
                FirebaseOptions options = FirebaseApp.getInstance().getOptions();
                secondaryApp = FirebaseApp.initializeApp(getApplicationContext(), options, "secondary");
            }

            FirebaseAuth tempAuth = FirebaseAuth.getInstance(secondaryApp);

            tempAuth.createUserWithEmailAndPassword(correo, contrasena)
                    .addOnSuccessListener(authResult -> {
                        String uid = authResult.getUser().getUid();

                        DatabaseReference nuevoUsuario = usuariosRef.child(uid);
                        nuevoUsuario.child("nombre").setValue(nombre);
                        nuevoUsuario.child("apellido").setValue(apellido);
                        nuevoUsuario.child("correo").setValue(correo);
                        nuevoUsuario.child("celular").setValue(celular);
                        nuevoUsuario.child("direccion").setValue(direccion);
                        nuevoUsuario.child("rol").setValue(rol);
                        nuevoUsuario.child("sede").setValue(sede);
                        nuevoUsuario.child("estado").setValue("Activo");
                        nuevoUsuario.child("fecha_registro").setValue(obtenerFechaActual());
                        nuevoUsuario.child("ultimo_login").setValue("");

                        tempAuth.signOut();

                        Toast.makeText(this, "Usuario registrado correctamente", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> {
                        if (e instanceof FirebaseAuthUserCollisionException) {
                            Toast.makeText(this, "El correo ya está registrado", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Error al crear usuario: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        dialog.show();
    }

    // 🔹 Mostrar diálogo para EDITAR usuario
    private void mostrarDialogoEditarUsuario(Usuario usuario) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_editar_usuario, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        EditText etNombre = dialogView.findViewById(R.id.etNombre);
        EditText etApellido = dialogView.findViewById(R.id.etApellido);
        EditText etCorreo = dialogView.findViewById(R.id.etCorreo);
        EditText etCelular = dialogView.findViewById(R.id.etCelular);
        EditText etDireccion = dialogView.findViewById(R.id.etDireccion);
        Spinner spRol = dialogView.findViewById(R.id.spRol);
        Spinner spSede = dialogView.findViewById(R.id.spSede);
        androidx.appcompat.widget.SwitchCompat switchEstado = dialogView.findViewById(R.id.switchEstado);

        Button btnGuardar = dialogView.findViewById(R.id.btnGuardar);
        Button btnCancelar = dialogView.findViewById(R.id.btnCancelar);

        // Cargar datos existentes
        etNombre.setText(usuario.getNombre());
        etApellido.setText(usuario.getApellido());
        etCorreo.setText(usuario.getCorreo());
        etCelular.setText(usuario.getCelular());
        etDireccion.setText(usuario.getDireccion());
        // Rol y sede, si es Spinner, deberás ponerlo en la posición correspondiente
        switchEstado.setChecked(usuario.getEstado().equalsIgnoreCase("Activo"));

        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        btnGuardar.setOnClickListener(v -> {
            String nombre = etNombre.getText().toString().trim();
            String apellido = etApellido.getText().toString().trim();
            String correo = etCorreo.getText().toString().trim();
            String celular = etCelular.getText().toString().trim();
            String direccion = etDireccion.getText().toString().trim();
            String rol = spRol.getSelectedItem().toString();
            String sede = spSede.getSelectedItem().toString();
            String estado = switchEstado.isChecked() ? "Activo" : "Inactivo";

            if (TextUtils.isEmpty(nombre) || TextUtils.isEmpty(apellido) || TextUtils.isEmpty(correo)) {
                Toast.makeText(this, "Completa los campos obligatorios", Toast.LENGTH_SHORT).show();
                return;
            }

            // Actualizar usuario en Firebase
            DatabaseReference usuarioRef = usuariosRef.child(usuario.getUid());
            usuarioRef.child("nombre").setValue(nombre);
            usuarioRef.child("apellido").setValue(apellido);
            usuarioRef.child("correo").setValue(correo);
            usuarioRef.child("celular").setValue(celular);
            usuarioRef.child("direccion").setValue(direccion);
            usuarioRef.child("rol").setValue(rol);
            usuarioRef.child("sede").setValue(sede);
            usuarioRef.child("estado").setValue(estado);

            Toast.makeText(this, "Usuario actualizado", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    // 🔹 Mostrar diálogo solo LECTURA
    private void mostrarDialogoVerMasUsuario(Usuario usuario) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_ver_mas_usuario, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        TextView tvNombre = dialogView.findViewById(R.id.tvNombre);
        TextView tvApellido = dialogView.findViewById(R.id.tvApellido);
        TextView tvCorreo = dialogView.findViewById(R.id.tvCorreo);
        TextView tvCelular = dialogView.findViewById(R.id.tvCelular);
        TextView tvDireccion = dialogView.findViewById(R.id.tvDireccion);
        TextView tvRol = dialogView.findViewById(R.id.tvRol);
        TextView tvSede = dialogView.findViewById(R.id.tvSede);
        TextView tvEstado = dialogView.findViewById(R.id.tvEstado);
        TextView tvFechaRegistro = dialogView.findViewById(R.id.tvFechaRegistro);
        TextView tvUltimoLogin = dialogView.findViewById(R.id.tvUltimoLogin);

        tvNombre.setText(usuario.getNombre());
        tvApellido.setText(usuario.getApellido());
        tvCorreo.setText(usuario.getCorreo());
        tvCelular.setText(usuario.getCelular());
        tvDireccion.setText(usuario.getDireccion());
        tvRol.setText(usuario.getRol());
        tvSede.setText(usuario.getSede());
        tvEstado.setText(usuario.getEstado());
        tvFechaRegistro.setText(usuario.getFecha_registro());
        tvUltimoLogin.setText(usuario.getUltimo_login());

        Button btnCerrar = dialogView.findViewById(R.id.btnCerrarPerfil);
        btnCerrar.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }


    private boolean validarCampos(String nombre, String apellido, String correo, String contrasena, String repetir, String celular) {
        if (TextUtils.isEmpty(nombre) || !nombre.matches("[a-zA-Z ]+")) {
            Toast.makeText(this, "Nombre inválido", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(apellido) || !apellido.matches("[a-zA-Z ]+")) {
            Toast.makeText(this, "Apellido inválido", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(correo) || !Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            Toast.makeText(this, "Correo inválido", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (contrasena.length() < 6) {
            Toast.makeText(this, "Contraseña mínimo 6 caracteres", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!contrasena.equals(repetir)) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!celular.matches("\\d{9}")) {
            Toast.makeText(this, "Celular inválido (9 dígitos)", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private String obtenerFechaActual() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        return sdf.format(calendar.getTime());
    }
}
