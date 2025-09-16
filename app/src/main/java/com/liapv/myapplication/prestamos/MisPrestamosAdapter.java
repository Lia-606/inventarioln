package com.liapv.myapplication.prestamos;

import android.content.Context;
import android.graphics.Color;
import android.view.*;
import android.widget.TextView;
import android.widget.LinearLayout;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.liapv.myapplication.R;
import com.liapv.myapplication.modelos.Prestamo;

import java.text.SimpleDateFormat;
import java.util.*;

public class MisPrestamosAdapter extends RecyclerView.Adapter<MisPrestamosAdapter.MiPrestamoViewHolder> {

    private final List<Prestamo> lista;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public MisPrestamosAdapter(List<Prestamo> lista) {
        this.lista = lista;
    }

    @NonNull
    @Override
    public MiPrestamoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_solicitud, parent, false);
        return new MiPrestamoViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MiPrestamoViewHolder holder, int position) {
        Prestamo p = lista.get(position);
        Context context = holder.itemView.getContext();

        String equipo = p.getEquipoNombre() != null ? p.getEquipoNombre() : "N/D";
        String fecha = p.getFechaSolicitud() != null ? p.getFechaSolicitud() : "N/D";
        String estado = p.getEstado() != null ? p.getEstado() : "Desconocido";
        String solicitante = p.getNombreSolicitante() != null ? p.getNombreSolicitante() : "N/D";
        String fechaDevolucion = p.getFechaDevolucion() != null ? p.getFechaDevolucion() : "";

        holder.tvEquipo.setText(String.format("%s: %s", context.getString(R.string.label_equipo), equipo));
        holder.tvFecha.setText(String.format("%s: %s", context.getString(R.string.label_fecha), fecha));
        holder.tvEstado.setText(String.format("%s: %s", context.getString(R.string.label_estado), estado));
        holder.tvSolicitante.setText(String.format("%s: %s", context.getString(R.string.label_solicitante), solicitante));

        // Cambiar color según estado
        switch (estado.toLowerCase()) {
            case "aprobado":
                holder.tvEstado.setTextColor(Color.parseColor("#27ae60")); // verde
                break;
            case "rechazado":
                holder.tvEstado.setTextColor(Color.parseColor("#c0392b")); // rojo
                break;
            case "pendiente":
                holder.tvEstado.setTextColor(Color.parseColor("#f39c12")); // naranja
                break;
            default:
                holder.tvEstado.setTextColor(Color.DKGRAY);
        }

        // Verificar vencimiento
        try {
            if (!fechaDevolucion.isEmpty()) {
                Date fechaDev = sdf.parse(fechaDevolucion);
                Date hoy = new Date();
                if (fechaDev != null && hoy.after(fechaDev) && !"Devuelto".equalsIgnoreCase(estado)) {
                    holder.tvEstado.setText("⚠️ VENCIDO");
                    holder.tvEstado.setTextColor(Color.RED);
                }
            }
        } catch (Exception ignored) {}

        // Ocultar botones
        holder.layoutBotones.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return lista != null ? lista.size() : 0;
    }

    public static class MiPrestamoViewHolder extends RecyclerView.ViewHolder {
        TextView tvEquipo, tvFecha, tvEstado, tvSolicitante;
        LinearLayout layoutBotones;

        public MiPrestamoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEquipo = itemView.findViewById(R.id.tvEquipo);
            tvFecha = itemView.findViewById(R.id.tvFechaSolicitud);
            tvEstado = itemView.findViewById(R.id.tvEstado);
            tvSolicitante = itemView.findViewById(R.id.tvSolicitante);
            layoutBotones = itemView.findViewById(R.id.layoutBotones);
        }
    }
}
