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

public class FragmentInventario extends Fragment {

    private TextView tvFechaInicio, tvFechaFin;
    private Button btnFiltrar, btnLimpiar, btnExportPdf, btnExportExcel;
    private RecyclerView rvInventario;

    private InventarioAdapter adapter;
    private List<InventarioItem> listaInventario = new ArrayList<>();

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private DatabaseReference refEntradas, refSalidas, refEquipos;

    private static final String TAG = "FragmentInventario";
    private static final int REQUEST_WRITE = 1001;

    // para reintentar guardado si pedimos permiso
    private byte[] pendingBytes = null;
    private String pendingFilename = null;
    private String pendingMime = null;

    public FragmentInventario() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_inventario, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvFechaInicio = view.findViewById(R.id.tvFechaInicio);
        tvFechaFin = view.findViewById(R.id.tvFechaFin);
        btnFiltrar = view.findViewById(R.id.btnFiltrar);
        btnLimpiar = view.findViewById(R.id.btnLimpiarFiltros);
        btnExportPdf = view.findViewById(R.id.btnExportPdfInventario);
        btnExportExcel = view.findViewById(R.id.btnExportExcelInventario);
        rvInventario = view.findViewById(R.id.rvInventario);

        rvInventario.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new InventarioAdapter(getContext(), listaInventario);
        rvInventario.setAdapter(adapter);

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        refEquipos = db.getReference("equipos");
        refEntradas = db.getReference("inventario").child("entradas");
        refSalidas = db.getReference("inventario").child("salidas");

        cargarInventario();

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
        btnExportExcel.setOnClickListener(v -> generarCsv()); // exportamos CSV en vez de XLSX
    }

    private void cargarInventario() {
        listaInventario.clear();

        // Entradas
        refEntradas.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String id = ds.getKey();
                    String equipoId = ds.child("equipoId").getValue(String.class);
                    int cantidad = ds.child("cantidad").getValue(Integer.class) != null ?
                            ds.child("cantidad").getValue(Integer.class) : 0;
                    String fecha = ds.child("fecha").getValue(String.class);
                    String responsable = ds.child("responsable").getValue(String.class);

                    refEquipos.child(equipoId).child("nombre").addListenerForSingleValueEvent(
                            new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snap) {
                                    String nombreEquipo = snap.getValue(String.class);
                                    listaInventario.add(new InventarioItem(
                                            id, nombreEquipo, cantidad, fecha, "Entrada", responsable
                                    ));
                                    adapter.setData(listaInventario);
                                    Log.i(TAG, "Entrada cargada: " + nombreEquipo + " - " + cantidad);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.e(TAG, "Error al cargar entrada", error.toException());
                                }
                            }
                    );
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error en refEntradas", error.toException());
            }
        });

        // Salidas
        refSalidas.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String id = ds.getKey();
                    String equipoId = ds.child("equipoId").getValue(String.class);
                    int cantidad = ds.child("cantidad").getValue(Integer.class) != null ?
                            ds.child("cantidad").getValue(Integer.class) : 0;
                    String fecha = ds.child("fecha").getValue(String.class);
                    String responsable = ds.child("responsable").getValue(String.class);

                    refEquipos.child(equipoId).child("nombre").addListenerForSingleValueEvent(
                            new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snap) {
                                    String nombreEquipo = snap.getValue(String.class);
                                    listaInventario.add(new InventarioItem(
                                            id, nombreEquipo, cantidad, fecha, "Salida", responsable
                                    ));
                                    adapter.setData(listaInventario);
                                    Log.i(TAG, "Salida cargada: " + nombreEquipo + " - " + cantidad);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.e(TAG, "Error al cargar salida", error.toException());
                                }
                            }
                    );
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error en refSalidas", error.toException());
            }
        });
    }

    private void mostrarDatePicker(TextView textView) {
        final Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(requireContext(),
                (DatePicker view, int year, int month, int dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    textView.setText(dateFormat.format(calendar.getTime()));
                    Log.i(TAG, "Fecha seleccionada: " + textView.getText().toString());
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    // ===================== PDF =====================
    private void generarPdf() {
        if (listaInventario.isEmpty()) {
            Toast.makeText(getContext(), "No hay datos para generar PDF", Toast.LENGTH_SHORT).show();
            return;
        }

        // Método auxiliar para evitar null
        java.util.function.Function<String, String> safe = text -> (text == null ? "" : text);

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
        canvas.drawText("Reporte de Inventario", 200, y, paint);

        paint.setTextSize(12);
        paint.setFakeBoldText(true);
        y += 30;

        // Encabezados de tabla
        int xTipo = 20, xEquipo = 100, xCantidad = 300, xFecha = 370, xResp = 470;
        canvas.drawText("Tipo", xTipo, y, paint);
        canvas.drawText("Equipo", xEquipo, y, paint);
        canvas.drawText("Cantidad", xCantidad, y, paint);
        canvas.drawText("Fecha", xFecha, y, paint);
        canvas.drawText("Responsable", xResp, y, paint);
        y += 20;

        paint.setFakeBoldText(false);

        // Filas de tabla
        for (InventarioItem item : listaInventario) {
            canvas.drawText(safe.apply(item.getTipoMovimiento()), xTipo, y, paint);
            canvas.drawText(safe.apply(item.getNombreEquipo()), xEquipo, y, paint);
            canvas.drawText(String.valueOf(item.getStock()), xCantidad, y, paint);
            canvas.drawText(safe.apply(item.getFecha()), xFecha, y, paint);
            canvas.drawText(safe.apply(item.getResponsable()), xResp, y, paint);
            y += 20;

            // Paginación
            if (y > 800) {
                pdf.finishPage(page);
                page = pdf.startPage(new PdfDocument.PageInfo.Builder(595, 842, 1).create());
                canvas = page.getCanvas();
                paint.setTextSize(12);
                y = 40;
            }
        }
        pdf.finishPage(page);

        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            pdf.writeTo(bos);
            pdf.close();
            byte[] data = bos.toByteArray();
            boolean ok = saveBytesToDownloads(data, "reporte_inventario.pdf", "application/pdf");
            if (ok) {
                Toast.makeText(getContext(), "PDF guardado en Descargas", Toast.LENGTH_LONG).show();
            }
        } catch (IOException e) {
            Toast.makeText(getContext(), "Error al generar PDF", Toast.LENGTH_SHORT).show();
        }
    }


    // ===================== CSV =====================
    private void generarCsv() {
        if (listaInventario.isEmpty()) {
            Toast.makeText(getContext(), "No hay datos para generar CSV", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Tipo,Equipo,Cantidad,Fecha,Responsable\n");
        for (InventarioItem item : listaInventario) {
            String tipo = escapeCsv(item.getTipoMovimiento());
            String equipo = escapeCsv(item.getNombreEquipo());
            String cantidad = String.valueOf(item.getStock());
            String fecha = escapeCsv(item.getFecha());
            String responsable = escapeCsv(item.getResponsable());

            sb.append("\"").append(tipo).append("\",")
                    .append("\"").append(equipo).append("\",")
                    .append(cantidad).append(",")
                    .append("\"").append(fecha).append("\",")
                    .append("\"").append(responsable).append("\"\n");
        }

        byte[] data = sb.toString().getBytes(StandardCharsets.UTF_8);
        boolean ok = saveBytesToDownloads(data, "reporte_inventario.csv", "text/csv");
        if (ok) {
            Toast.makeText(getContext(), "CSV guardado en Descargas", Toast.LENGTH_LONG).show();
        }
    }


    private String escapeCsv(String s) {
        if (s == null) return "";
        return s.replace("\"", "\"\""); // doble comilla para CSV
    }

    // ===================== Guardar en Descargas (MediaStore para API29+) =====================
    private boolean saveBytesToDownloads(byte[] data, String filename, String mime) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, filename);
                values.put(MediaStore.MediaColumns.MIME_TYPE, mime);
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

                Uri collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
                Uri uri = requireContext().getContentResolver().insert(collection, values);
                if (uri == null) {
                    Toast.makeText(getContext(), "No se pudo crear archivo en Descargas", Toast.LENGTH_SHORT).show();
                    return false;
                }
                try (OutputStream out = requireContext().getContentResolver().openOutputStream(uri)) {
                    out.write(data);
                    out.flush();
                }
                return true;
            } catch (Exception e) {
                Log.e(TAG, "Error guardando en MediaStore", e);
                Toast.makeText(getContext(), "Error al guardar en Descargas", Toast.LENGTH_SHORT).show();
                return false;
            }
        } else {
            // API < 29 -> escribir directamente en Environment.getExternalStoragePublicDirectory
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                // Guardamos los bytes en variables pendientes y pedimos permiso
                pendingBytes = data;
                pendingFilename = filename;
                pendingMime = mime;
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE);
                Toast.makeText(getContext(), "Solicitando permiso para guardar en Descargas...", Toast.LENGTH_SHORT).show();
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
            Log.e(TAG, "Error escribiendo en Descargas legacy", e);
            Toast.makeText(getContext(), "Error al guardar archivo en Descargas", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    // manejar resultado del permiso
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // intentar guardar lo pendiente
                if (pendingBytes != null && pendingFilename != null) {
                    boolean ok = writeBytesToDownloadsLegacy(pendingBytes, pendingFilename);
                    if (ok) {
                        Toast.makeText(getContext(), "Archivo guardado en Descargas", Toast.LENGTH_LONG).show();
                        Log.i(TAG, "Archivo pendiente guardado: " + pendingFilename);
                    }
                    // limpiar pendientes
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
