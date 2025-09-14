package com.liapv.myapplication.prestamos;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.liapv.myapplication.R;
import com.liapv.myapplication.modelos.Prestamo;

import java.util.List;

public class SolicitudesAdapter extends RecyclerView.Adapter<SolicitudesAdapter.SolicitudViewHolder> {

    public interface OnSolicitudActionListener {
        void onAprobar(Prestamo prestamo);
        void onRechazar(Prestamo prestamo);
    }

    private List<Prestamo> listaSolicitudes;
    private final OnSolicitudActionListener listener;

    public SolicitudesAdapter(List<Prestamo> listaSolicitudes, OnSolicitudActionListener listener) {
        this.listaSolicitudes = listaSolicitudes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SolicitudViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_solicitud, parent, false);
        return new SolicitudViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull SolicitudViewHolder holder, int position) {
        Prestamo prestamo = listaSolicitudes.get(position);
        Context context = holder.itemView.getContext();

        // Prevención de nulos
        String equipo = prestamo.getEquipoNombre() != null ? prestamo.getEquipoNombre() : "N/D";
        String solicitante = prestamo.getNombreSolicitante() != null ? prestamo.getNombreSolicitante() : "N/D";
        String fecha = prestamo.getFechaSolicitud() != null ? prestamo.getFechaSolicitud() : "N/D";
        String estado = prestamo.getEstado() != null ? prestamo.getEstado() : "Desconocido";

        // Set de texto usando String.format
        holder.tvEquipo.setText(String.format("%s: %s", context.getString(R.string.label_equipo), equipo));
        holder.tvSolicitante.setText(String.format("%s: %s", context.getString(R.string.label_solicitante), solicitante));
        holder.tvFechaSolicitud.setText(String.format("%s: %s", context.getString(R.string.label_fecha), fecha));
        holder.tvEstado.setText(String.format("%s: %s", context.getString(R.string.label_estado), estado));

        // Mostrar u ocultar botones según estado
        holder.layoutBotones.setVisibility("Pendiente".equals(estado) ? View.VISIBLE : View.GONE);

        // Acciones
        holder.btnAprobar.setOnClickListener(v -> listener.onAprobar(prestamo));
        holder.btnRechazar.setOnClickListener(v -> listener.onRechazar(prestamo));
    }

    @Override
    public int getItemCount() {
        return listaSolicitudes != null ? listaSolicitudes.size() : 0;
    }

    public void actualizarLista(List<Prestamo> nuevaLista) {
        this.listaSolicitudes = nuevaLista;
        notifyDataSetChanged();
    }

    static class SolicitudViewHolder extends RecyclerView.ViewHolder {
        TextView tvEquipo, tvSolicitante, tvFechaSolicitud, tvEstado;
        Button btnAprobar, btnRechazar;
        LinearLayout layoutBotones;

        public SolicitudViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEquipo = itemView.findViewById(R.id.tvEquipo);
            tvSolicitante = itemView.findViewById(R.id.tvSolicitante);
            tvFechaSolicitud = itemView.findViewById(R.id.tvFechaSolicitud);
            tvEstado = itemView.findViewById(R.id.tvEstado);
            btnAprobar = itemView.findViewById(R.id.btnAprobar);
            btnRechazar = itemView.findViewById(R.id.btnRechazar);
            layoutBotones = itemView.findViewById(R.id.layoutBotones);
        }
    }
}
