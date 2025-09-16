package com.liapv.myapplication.equipos;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.WriterException;
import com.liapv.myapplication.R;

public class GenerarQRActivity extends AppCompatActivity {

    private ImageView ivQR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generar_qr);

        ivQR = findViewById(R.id.ivQRGenerado);

        // Recibe el código único del equipo para generar el QR
        String contenidoQR = getIntent().getStringExtra("contenidoQR");

        if (contenidoQR == null || contenidoQR.isEmpty()) {
            Toast.makeText(this, "No se proporcionó información del equipo", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        try {
            Bitmap bitmapQR = QRGenerator.generarQR(contenidoQR.trim());
            ivQR.setImageBitmap(bitmapQR);
        } catch (WriterException e) {
            Toast.makeText(this, "Error al generar QR", Toast.LENGTH_SHORT).show();
        }
    }
}
