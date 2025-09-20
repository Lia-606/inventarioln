package com.liapv.myapplication.reportes;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.liapv.myapplication.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class FragmentPrestamos extends Fragment {

    private TextView tvFechaInicio, tvFechaFin;
    private Button btnFiltrar, btnLimpiar, btnExportPdf, btnExportExcel;
    private RecyclerView rvPrestamos;

    private PrestamoAdapter adapter;
    private List<PrestamoItem> listaPrestamos = new ArrayList<>();

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private DatabaseReference refPrestamos, refEquipos;

    private static final String TAG = "FragmentPrestamos";
    private static final int REQUEST_WRITE = 2001;

    private byte[] pendingBytes = null;
    private String pendingFilename = null;
    private String pendingMime = null;

    public FragmentPrestamos() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_prestamos, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvFechaInicio = view.findViewById(R.id.tvFechaInicioPrestamo);
        tvFechaFin = view.findViewById(R.id.tvFechaFinPrestamo);
        btnFiltrar = view.findViewById(R.id.btnFiltrarPrestamos);
        btnLimpiar = view.findViewById(R.id.btnLimpiarFiltrosPrestamos);
        btnExportPdf = view.findViewById(R.id.btnExportPdfPrestamos);
        btnExportExcel = view.findViewById(R.id.btnExportExcelPrestamos);
        rvPrestamos = view.findViewById(R.id.rvPrestamos);

        rvPrestamos.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PrestamoAdapter(getContext(), listaPrestamos);
        rvPrestamos.setAdapter(adapter);

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        refEquipos = db.getReference("equipos");
        refPrestamos = db.getReference("prestamos");

        cargarPrestamos();

        tvFechaInicio.setOnClickListener(v -> mostrarDatePicker(tvFechaInicio));
        tvFechaFin.setOnClickListener(v -> mostrarDatePicker(tvFechaFin));

        btnFiltrar.setOnClickListener(v -> {
            String inicio = tvFechaInicio.getText().toString();
            String fin = tvFechaFin.getText().toString();
            adapter.filtrarPorFechas(inicio, fin);
            Log.i(TAG, "Filtro aplicado de " + inicio + " a " + fin);
        });

        btnLimpiar.setOnClickListener(v -> {
            tvFechaInicio.setText("Fecha inicio");
            tvFechaFin.setText("Fecha fin");
            adapter.limpiarFiltros();
            Log.i(TAG, "Filtros limpiados");
        });

        btnExportPdf.setOnClickListener(v -> generarPdf());
        btnExportExcel.setOnClickListener(v -> generarCsv());
    }

    private void cargarPrestamos() {
        listaPrestamos.clear();

        refPrestamos.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String id = ds.getKey();
                    String equipoId = ds.child("equipoId").getValue(String.class);
                    String nombreEquipo = ds.child("equipoNombre").getValue(String.class);
                    String solicitante = ds.child("nombreSolicitante").getValue(String.class);
                    String estado = ds.child("estado").getValue(String.class);
                    String fechaSolicitud = ds.child("fechaSolicitud").getValue(String.class);
                    String fechaDevolucion = ds.child("fechaDevolucion").getValue(String.class);

                    if (nombreEquipo == null && equipoId != null) {
                        // buscar el equipo en la rama "equipos"
                        refEquipos.child(equipoId).child("nombre")
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snap) {
                                        String nombreEquipoDb = snap.getValue(String.class);
                                        if (nombreEquipoDb == null) nombreEquipoDb = equipoId;

                                        listaPrestamos.add(new PrestamoItem(
                                                id,
                                                equipoId,
                                                nombreEquipoDb,
                                                solicitante,
                                                null,
                                                estado,
                                                fechaSolicitud,
                                                fechaDevolucion
                                        ));
                                        adapter.setData(listaPrestamos);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        listaPrestamos.add(new PrestamoItem(
                                                id,
                                                equipoId,
                                                equipoId,
                                                solicitante,
                                                null,
                                                estado,
                                                fechaSolicitud,
                                                fechaDevolucion // ✅ corregido
                                        ));
                                        adapter.setData(listaPrestamos);
                                    }
                                });
                    } else {
                        // ya viene con equipoNombre o no tiene equipoId
                        listaPrestamos.add(new PrestamoItem(
                                id,
                                equipoId,
                                nombreEquipo != null ? nombreEquipo : "",
                                solicitante,
                                null,
                                estado,
                                fechaSolicitud,
                                fechaDevolucion // ✅ corregido
                        ));
                        adapter.setData(listaPrestamos);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error en refPrestamos", error.toException());
            }
        });
    }


    private void mostrarDatePicker(TextView textView) {
        final Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(requireContext(),
                (DatePicker view, int year, int month, int dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    textView.setText(dateFormat.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    // ===================== PDF =====================
    private void generarPdf() {
        if (listaPrestamos.isEmpty()) {
            Toast.makeText(getContext(), "No hay datos para generar PDF", Toast.LENGTH_SHORT).show();
            return;
        }

        PdfDocument pdf = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = pdf.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(12);

        int y = 40;
        // Título
        paint.setFakeBoldText(true);
        canvas.drawText("Reporte de Préstamos", 200, y, paint);
        paint.setFakeBoldText(false);
        y += 30;

        // Definir columnas (x-coordinates)
        int xEstado = 20;
        int xEquipo = 120;
        int xSolicitante = 270;
        int xFechaSolicitud = 400;
        int xFechaDevolucion = 500;

        // Dibujar encabezados
        paint.setFakeBoldText(true);
        canvas.drawText("Estado", xEstado, y, paint);
        canvas.drawText("Equipo", xEquipo, y, paint);
        canvas.drawText("Solicitante", xSolicitante, y, paint);
        canvas.drawText("F. Solicitud", xFechaSolicitud, y, paint);
        canvas.drawText("F. Devolución", xFechaDevolucion, y, paint);
        paint.setFakeBoldText(false);

        y += 20;

        // Dibujar filas
        for (PrestamoItem item : listaPrestamos) {
            canvas.drawText(item.getEstado() != null ? item.getEstado() : "-", xEstado, y, paint);
            canvas.drawText(item.getNombreEquipo() != null ? item.getNombreEquipo() : "-", xEquipo, y, paint);
            canvas.drawText(item.getSolicitante() != null ? item.getSolicitante() : "-", xSolicitante, y, paint);
            canvas.drawText(item.getFechaSolicitud() != null ? item.getFechaSolicitud() : "-", xFechaSolicitud, y, paint);
            canvas.drawText(item.getFechaDevolucion() != null ? item.getFechaDevolucion() : "-", xFechaDevolucion, y, paint);

            y += 20;

            // Saltar a nueva página si se llena
            if (y > 800) {
                pdf.finishPage(page);
                page = pdf.startPage(new PdfDocument.PageInfo.Builder(595, 842, 1).create());
                canvas = page.getCanvas();
                y = 40;
            }
        }

        pdf.finishPage(page);

        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            pdf.writeTo(bos);
            pdf.close();
            byte[] data = bos.toByteArray();
            boolean ok = saveBytesToDownloads(data, "reporte_prestamos.pdf", "application/pdf");
            if (ok) {
                Toast.makeText(getContext(), "PDF guardado en Descargas", Toast.LENGTH_LONG).show();
            }
        } catch (IOException e) {
            Toast.makeText(getContext(), "Error al generar PDF", Toast.LENGTH_SHORT).show();
        }
    }


    // ===================== CSV =====================
    private void generarCsv() {
        if (listaPrestamos.isEmpty()) {
            Toast.makeText(getContext(), "No hay datos para generar CSV", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder sb = new StringBuilder();
        // Encabezados de las columnas
        sb.append("Estado,Equipo,Solicitante,FechaSolicitud,FechaDevolucion\n");

        for (PrestamoItem item : listaPrestamos) {
            sb.append("\"").append(escapeCsv(item.getEstado())).append("\",")
                    .append("\"").append(escapeCsv(item.getNombreEquipo())).append("\",")
                    .append("\"").append(escapeCsv(item.getSolicitante())).append("\",")
                    .append("\"").append(escapeCsv(item.getFechaSolicitud())).append("\",")
                    .append("\"").append(escapeCsv(item.getFechaDevolucion())).append("\"\n");
        }

        byte[] data = sb.toString().getBytes(StandardCharsets.UTF_8);
        boolean ok = saveBytesToDownloads(data, "reporte_prestamos.csv", "text/csv");
        if (ok) {
            Toast.makeText(getContext(), "CSV guardado en Descargas", Toast.LENGTH_LONG).show();
        }
    }


    private String escapeCsv(String s) {
        if (s == null) return "";
        return s.replace("\"", "\"\"");
    }

    // ===================== Guardar =====================
    private boolean saveBytesToDownloads(byte[] data, String filename, String mime) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, filename);
                values.put(MediaStore.MediaColumns.MIME_TYPE, mime);
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

                Uri collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
                Uri uri = requireContext().getContentResolver().insert(collection, values);
                if (uri == null) return false;

                try (OutputStream out = requireContext().getContentResolver().openOutputStream(uri)) {
                    out.write(data);
                    out.flush();
                }
                return true;
            } catch (Exception e) {
                return false;
            }
        } else {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                pendingBytes = data;
                pendingFilename = filename;
                pendingMime = mime;
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE);
                return false;
            } else {
                return writeBytesToDownloadsLegacy(data, filename);
            }
        }
    }

    private boolean writeBytesToDownloadsLegacy(byte[] data, String filename) {
        try {
            File downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (!downloads.exists()) downloads.mkdirs();
            File file = new File(downloads, filename);
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(data);
                fos.flush();
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (pendingBytes != null && pendingFilename != null) {
                    writeBytesToDownloadsLegacy(pendingBytes, pendingFilename);
                    pendingBytes = null;
                    pendingFilename = null;
                    pendingMime = null;
                }
            } else {
                Toast.makeText(getContext(), "Permiso denegado: no se puede guardar en Descargas", Toast.LENGTH_LONG).show();
            }
        }
    }
}
