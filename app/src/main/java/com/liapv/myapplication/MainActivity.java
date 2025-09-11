package com.liapv.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin, btnForgotPassword;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Inicializar FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        // Referencias UI
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnForgotPassword = findViewById(R.id.btnForgotPassword);

        // Acción del botón login
        btnLogin.setOnClickListener(v -> loginUser());

        // Acción del botón "Olvidé mi contraseña"
        btnForgotPassword.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(this, "Ingresa tu correo primero", Toast.LENGTH_SHORT).show();
                return;
            }
            mAuth.sendPasswordResetEmail(email)
                    .addOnSuccessListener(aVoid ->
                            Toast.makeText(this, "Se envió un enlace a tu correo", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // 🔹 Validaciones básicas
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("El correo es obligatorio");
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Correo inválido");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("La contraseña es obligatoria");
            return;
        }
        if (password.length() < 6) {
            etPassword.setError("Mínimo 6 caracteres");
            return;
        }

        // 🔹 Autenticación en Firebase
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    // ✅ Si se loguea correctamente → pasa al Dashboard
                    Toast.makeText(this, "Bienvenido", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(MainActivity.this, Dashboard.class));
                    finish(); // cerrar login
                })
                .addOnFailureListener(e -> {
                    // ❌ Error de login
                    Toast.makeText(this, "Correo o contraseña incorrectos", Toast.LENGTH_SHORT).show();
                });
    }
}
