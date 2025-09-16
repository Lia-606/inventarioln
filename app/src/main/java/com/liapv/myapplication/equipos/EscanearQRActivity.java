package com.liapv.myapplication.equipos;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.*;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.liapv.myapplication.modelos.Equipo;

public class EscanearQRActivity extends AppCompatActivity {

    private DatabaseReference equiposRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        equiposRef = FirebaseDatabase.getInstance().getReference("equipos");

        // Iniciar escaneo
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setPrompt("Escanea el código QR del equipo");
        integrator.setBeepEnabled(true);
        integrator.setOrientationLocked(true);
        integrator.setCaptureActivity(CaptureActivityPortrait.class);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                String codigoEscaneado = result.getContents().trim();
                Toast.makeText(this, "Escaneado: " + codigoEscaneado, Toast.LENGTH_LONG).show();
                buscarEquipoPorCodigo(codigoEscaneado);
            } else {
                Toast.makeText(this, "Escaneo cancelado", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void buscarEquipoPorCodigo(String codigoEscaneado) {
        equiposRef.orderByChild("codigo").equalTo(codigoEscaneado)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot equipoSnap : snapshot.getChildren()) {
                                Equipo equipo = equipoSnap.getValue(Equipo.class);
                                if (equipo != null) {
                                    equipo.setId(equipoSnap.getKey());

                                    Intent intent = new Intent(EscanearQRActivity.this, DetalleEquipoActivity.class);
                                    intent.putExtra("equipo", equipo);
                                    startActivity(intent);
                                    finish();
                                    return;
                                }
                            }
                        } else {
                            Toast.makeText(EscanearQRActivity.this, "Equipo no encontrado", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(EscanearQRActivity.this, "Error al buscar equipo", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
