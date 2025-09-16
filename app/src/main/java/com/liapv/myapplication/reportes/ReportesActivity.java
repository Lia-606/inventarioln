package com.liapv.myapplication.reportes;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.liapv.myapplication.R;

public class ReportesActivity extends AppCompatActivity {

    private Button btnInventario, btnPrestamos, btnAuditoria;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reportes);

        // Referencias de los botones
        btnInventario = findViewById(R.id.btnInventario);
        btnPrestamos = findViewById(R.id.btnPrestamos);
        btnAuditoria = findViewById(R.id.btnAuditoria);

        // Por defecto mostrar el fragment de Inventario
        cargarFragment(new FragmentInventario());

        // Listener para botón Inventario
        btnInventario.setOnClickListener(v -> {
            cargarFragment(new FragmentInventario());
        });

        // Listener para botón Préstamos
        btnPrestamos.setOnClickListener(v -> {
            cargarFragment(new FragmentPrestamos());
        });

        // Listener para botón Auditoría
        btnAuditoria.setOnClickListener(v -> {
            cargarFragment(new FragmentAuditoria());
        });
    }

    private void cargarFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.contenedorFragments, fragment);
        transaction.commit();
    }
}
