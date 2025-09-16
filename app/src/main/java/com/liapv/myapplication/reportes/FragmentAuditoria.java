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
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class FragmentAuditoria extends Fragment {

    private TextView tvFechaAuditoria;
    private Button btnFiltrar, btnLimpiar, btnExportPdf, btnExportExcel;
    private RecyclerView rvAuditoria;

    private AuditoriaAdapter adapter;
    private List<AuditoriaItem> listaAuditoria = new ArrayList<>();

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private DatabaseReference refEquipos, refEntradas, refSalidas, refPrestamos, refUsuarios;

    private static final String TAG = "FragmentAuditoria";
    private static final int REQUEST_WRITE = 2001;

    private byte[] pendingBytes = null;
    private String pendingFilename = null;
    private String pendingMime = null;

    public FragmentAuditoria() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_auditoria, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvFechaAuditoria = view.findViewById(R.id.tvFechaAuditoria);
        btnFiltrar = view.findViewById(R.id.btnFiltrarAuditoria);
        btnLimpiar = view.findViewById(R.id.btnLimpiarAuditoria);
        btnExportPdf = view.findViewById(R.id.btnExportPdfAuditoria);
        btnExportExcel = view.findViewById(R.id.btnExportExcelAuditoria);
        rvAuditoria = view.findViewById(R.id.rvAuditoria);

        rvAuditoria.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AuditoriaAdapter(getContext(), listaAuditoria);
        rvAuditoria.setAdapter(adapter);

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        refEquipos = db.getReference("equipos");
        refEntradas = db.getReference("inventario/entradas");
        refSalidas = db.getReference("inventario/salidas");
        refPrestamos = db.getReference("prestamos");
        refUsuarios = db.getReference("usuarios");

        cargarAuditoria();

        tvFechaAuditoria.setOnClickListener(v -> mostrarDatePicker());

        btnFiltrar.setOnClickListener(v -> {
            String fecha = tvFechaAuditoria.getText().toString();
            adapter.filtrarPorFechas(fecha, fecha);
            Log.i(TAG, "Filtro aplicado para fecha: " + fecha);
        });

        btnLimpiar.setOnClickListener(v -> {
            tvFechaAuditoria.setText("Seleccionar fecha");
            adapter.limpiarFiltros();
            Log.i(TAG, "Filtros limpiados");
        });

        btnExportPdf.setOnClickListener(v -> generarPdf());
        btnExportExcel.setOnClickListener(v -> generarCsv());
    }

    private void cargarAuditoria() {
        listaAuditoria.clear();

        // Entradas
        refEntradas.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshotEntradas) {
                for (DataSnapshot ds : snapshotEntradas.getChildren()) {
                    String id = ds.getKey();
                    String equipoId = ds.child("equipoId").getValue(String.class);
                    Object fechaObj = ds.child("fecha").getValue();
                    String fecha = fechaObj != null ? fechaObj.toString() : "";
                    Object cantidadObj = ds.child("cantidad").getValue();
                    String cantidad = cantidadObj != null ? cantidadObj.toString() : "";
                    String responsable = ds.child("responsable").getValue(String.class);
                    String proveedor = ds.child("proveedor").getValue(String.class);

                    refEquipos.child(equipoId).get().addOnSuccessListener(equipoSnap -> {
                        String nombreEquipo = equipoSnap.child("nombre").getValue(String.class);
                        listaAuditoria.add(new AuditoriaItem(
                                id,
                                "Entrada",
                                responsable,
                                fecha,
                                cantidad + " x " + nombreEquipo + " (Proveedor: " + proveedor + ")"
                        ));
                        adapter.setData(listaAuditoria);
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { Log.e(TAG, "Error Entradas", error.toException()); }
        });

        // Salidas
        refSalidas.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshotSalidas) {
                for (DataSnapshot ds : snapshotSalidas.getChildren()) {
                    String id = ds.getKey();
                    String equipoId = ds.child("equipoId").getValue(String.class);
                    Object fechaObj = ds.child("fecha").getValue();
                    String fecha = fechaObj != null ? fechaObj.toString() : "";
                    Object cantidadObj = ds.child("cantidad").getValue();
                    String cantidad = cantidadObj != null ? cantidadObj.toString() : "";
                    String responsable = ds.child("responsable").getValue(String.class);
                    String motivo = ds.child("motivo").getValue(String.class);

                    refEquipos.child(equipoId).get().addOnSuccessListener(equipoSnap -> {
                        String nombreEquipo = equipoSnap.child("nombre").getValue(String.class);
                        listaAuditoria.add(new AuditoriaItem(
                                id,
                                "Salida",
                                responsable,
                                fecha,
                                cantidad + " x " + nombreEquipo + " (Motivo: " + motivo + ")"
                        ));
                        adapter.setData(listaAuditoria);
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { Log.e(TAG, "Error Salidas", error.toException()); }
        });

        // Prestamos
        refPrestamos.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshotPrestamos) {
                for (DataSnapshot ds : snapshotPrestamos.getChildren()) {
                    String id = ds.getKey();
                    String equipoId = ds.child("equipoId").getValue(String.class);
                    Object fechaObj = ds.child("fechaSolicitud").getValue();
                    String fecha = fechaObj != null ? fechaObj.toString() : "";
                    String estado = ds.child("estado").getValue(String.class);
                    String nombreSolicitante = ds.child("nombreSolicitante").getValue(String.class);

                    String detalle = (equipoId != null) ? "Prestamo equipo ID: " + equipoId : "Solicitud general";

                    if (equipoId != null) {
                        refEquipos.child(equipoId).get().addOnSuccessListener(equipoSnap -> {
                            String nombreEquipo = equipoSnap.child("nombre").getValue(String.class);
                            listaAuditoria.add(new AuditoriaItem(
                                    id,
                                    "Préstamo",
                                    nombreSolicitante,
                                    fecha,
                                    detalle + " (" + nombreEquipo + ", Estado: " + estado + ")"
                            ));
                            adapter.setData(listaAuditoria);
                        });
                    } else {
                        listaAuditoria.add(new AuditoriaItem(
                                id,
                                "Préstamo",
                                nombreSolicitante,
                                fecha,
                                detalle + " (Estado: " + estado + ")"
                        ));
                        adapter.setData(listaAuditoria);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { Log.e(TAG, "Error Prestamos", error.toException()); }
        });
    }

    private void mostrarDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(requireContext(),
                (DatePicker view, int year, int month, int dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    tvFechaAuditoria.setText(dateFormat.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    // ===================== PDF =====================
    private void generarPdf() {
        if (listaAuditoria.isEmpty()) {
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
        paint.setTextSize(16);
        paint.setFakeBoldText(true);
        canvas.drawText("Reporte de Auditoría", 200, y, paint);

        paint.setTextSize(12);
        y += 30;
        canvas.drawText("Acción", 20, y, paint);
        canvas.drawText("Usuario", 150, y, paint);
        canvas.drawText("Fecha", 300, y, paint);
        canvas.drawText("Detalle", 400, y, paint);
        y += 20;
        paint.setFakeBoldText(false);

        for (AuditoriaItem item : listaAuditoria) {
            canvas.drawText(item.getAccion() != null ? item.getAccion() : "", 20, y, paint);
            canvas.drawText(item.getResponsable() != null ? item.getResponsable() : "", 150, y, paint);
            canvas.drawText(item.getFecha() != null ? item.getFecha() : "", 300, y, paint);
            canvas.drawText(item.getSolicitanteProveedor() != null ? item.getSolicitanteProveedor() : "", 400, y, paint);
            y += 20;
        }
        pdf.finishPage(page);

        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            pdf.writeTo(bos);
            pdf.close();
            byte[] data = bos.toByteArray();
            boolean ok = saveBytesToDownloads(data, "reporte_auditoria.pdf", "application/pdf");
            if (ok) {
                Toast.makeText(getContext(), "PDF guardado en Descargas", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error al generar PDF", Toast.LENGTH_SHORT).show();
        }
    }

    // ===================== CSV =====================
    private void generarCsv() {
        if (listaAuditoria.isEmpty()) {
            Toast.makeText(getContext(), "No hay datos para generar CSV", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Accion,Usuario,Fecha,Detalle\n");
        for (AuditoriaItem item : listaAuditoria) {
            sb.append("\"").append(item.getAccion() != null ? item.getAccion() : "").append("\",")
                    .append("\"").append(item.getResponsable() != null ? item.getResponsable() : "").append("\",")
                    .append("\"").append(item.getFecha() != null ? item.getFecha() : "").append("\",")
                    .append("\"").append(item.getSolicitanteProveedor() != null ? item.getSolicitanteProveedor() : "").append("\"\n");
        }

        byte[] data = sb.toString().getBytes(StandardCharsets.UTF_8);
        boolean ok = saveBytesToDownloads(data, "reporte_auditoria.csv", "text/csv");
        if (ok) {
            Toast.makeText(getContext(), "CSV guardado en Descargas", Toast.LENGTH_LONG).show();
        }
    }

    // ===================== Guardar en Descargas =====================
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
                }
                return true;
            } catch (Exception e) {
                Log.e(TAG, "Error guardando en MediaStore", e);
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
            }
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error escribiendo archivo", e);
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
                    boolean ok = writeBytesToDownloadsLegacy(pendingBytes, pendingFilename);
                    if (ok) {
                        Toast.makeText(getContext(), "Archivo guardado en Descargas", Toast.LENGTH_LONG).show();
                    }
                    pendingBytes = null;
                    pendingFilename = null;
                    pendingMime = null;
                }
            } else {
                Toast.makeText(getContext(), "Permiso denegado", Toast.LENGTH_LONG).show();
            }
        }
    }
}
