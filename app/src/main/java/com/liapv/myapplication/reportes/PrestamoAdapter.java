package com.liapv.myapplication.reportes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.liapv.myapplication.R;

import java.util.ArrayList;
import java.util.List;

public class PrestamoAdapter extends RecyclerView.Adapter<PrestamoAdapter.ViewHolder> {

    private final Context context;
    private List<PrestamoItem> listaOriginal;
    private List<PrestamoItem> listaFiltrada;

    public PrestamoAdapter(Context context, List<PrestamoItem> lista) {
        this.context = context;
        this.listaOriginal = lista;
        this.listaFiltrada = new ArrayList<>(lista);
    }

    public void setData(List<PrestamoItem> nuevaLista) {
        this.listaOriginal = nuevaLista;
        this.listaFiltrada = new ArrayList<>(nuevaLista);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflamos tabla_prestamos.xml
        View view = LayoutInflater.from(context).inflate(R.layout.tabla_prestamos, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PrestamoItem item = listaFiltrada.get(position);

        holder.tvEquipo.setText(item.getNombreEquipo());
        holder.tvSolicitante.setText(item.getSolicitante());
        holder.tvEstado.setText(item.getEstado());
        holder.tvFechaSolicitud.setText(item.getFechaSolicitud());
        holder.tvFechaDevolucion.setText(
                item.getFechaDevolucion() != null ? item.getFechaDevolucion() : "-"
        );
    }

    @Override
    public int getItemCount() {
        return listaFiltrada.size();
    }

    public void filtrarPorFechas(String inicio, String fin) {
        if (inicio.equals("Fecha inicio") || fin.equals("Fecha fin")) {
            listaFiltrada = new ArrayList<>(listaOriginal);
        } else {
            listaFiltrada = new ArrayList<>();
            for (PrestamoItem item : listaOriginal) {
                if (item.getFechaSolicitud() != null &&
                        item.getFechaSolicitud().compareTo(inicio) >= 0 &&
                        item.getFechaSolicitud().compareTo(fin) <= 0) {
                    listaFiltrada.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void limpiarFiltros() {
        listaFiltrada = new ArrayList<>(listaOriginal);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvEquipo, tvSolicitante, tvEstado, tvFechaSolicitud, tvFechaDevolucion;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEquipo = itemView.findViewById(R.id.tvPrestamoEquipo);
            tvSolicitante = itemView.findViewById(R.id.tvPrestamoSolicitante);
            tvEstado = itemView.findViewById(R.id.tvPrestamoEstado);
            tvFechaSolicitud = itemView.findViewById(R.id.tvPrestamoFechaSolicitud);
            tvFechaDevolucion = itemView.findViewById(R.id.tvPrestamoFechaDevolucion);
        }
    }
}
