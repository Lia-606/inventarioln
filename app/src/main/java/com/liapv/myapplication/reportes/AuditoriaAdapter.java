package com.liapv.myapplication.reportes;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.liapv.myapplication.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AuditoriaAdapter extends RecyclerView.Adapter<AuditoriaAdapter.Holder> implements Filterable {

    private static final String TAG = "AuditoriaAdapter";

    private final Context context;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private List<AuditoriaItem> originalList;
    private List<AuditoriaItem> filteredList;

    public AuditoriaAdapter(Context context, List<AuditoriaItem> initialData) {
        this.context = context;
        this.originalList = initialData != null ? new ArrayList<>(initialData) : new ArrayList<>();
        this.filteredList = new ArrayList<>(this.originalList);
        Log.i(TAG, "Adapter inicializado con " + originalList.size() + " items");
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.tabla_auditoria, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        AuditoriaItem item = filteredList.get(position);
        holder.tvFecha.setText(item.getFecha() != null ? item.getFecha() : "-");
        holder.tvAccion.setText(item.getAccion() != null ? item.getAccion() : "-");
        holder.tvEquipo.setText(item.getEquipo() != null ? item.getEquipo() : "-");
        holder.tvCantidad.setText(item.getCantidad() != null ? String.valueOf(item.getCantidad()) : "-");
        holder.tvResponsable.setText(item.getResponsable() != null ? item.getResponsable() : "-");
        holder.tvSolicitanteProveedor.setText(item.getSolicitanteProveedor() != null ? item.getSolicitanteProveedor() : "-");
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public void setData(List<AuditoriaItem> newData) {
        if (newData == null) newData = new ArrayList<>();
        this.originalList = new ArrayList<>(newData);
        this.filteredList = new ArrayList<>(newData);
        notifyDataSetChanged();
        Log.i(TAG, "Datos actualizados, total items: " + filteredList.size());
    }

    public void filtrar(String query) {
        if (query == null || query.trim().isEmpty()) {
            filteredList = new ArrayList<>(originalList);
        } else {
            String q = query.trim().toLowerCase();
            List<AuditoriaItem> temp = new ArrayList<>();
            for (AuditoriaItem it : originalList) {
                if ((it.getAccion() != null && it.getAccion().toLowerCase().contains(q)) ||
                        (it.getEquipo() != null && it.getEquipo().toLowerCase().contains(q)) ||
                        (it.getResponsable() != null && it.getResponsable().toLowerCase().contains(q)) ||
                        (it.getSolicitanteProveedor() != null && it.getSolicitanteProveedor().toLowerCase().contains(q))) {
                    temp.add(it);
                }
            }
            filteredList = temp;
        }
        notifyDataSetChanged();
    }

    public void filtrarPorFechas(String fechaInicio, String fechaFin) {
        Date inicio = null, fin = null;
        try {
            if (fechaInicio != null && !fechaInicio.equalsIgnoreCase("Fecha inicio") && !fechaInicio.isEmpty()) {
                inicio = sdf.parse(fechaInicio);
            }
            if (fechaFin != null && !fechaFin.equalsIgnoreCase("Fecha fin") && !fechaFin.isEmpty()) {
                fin = sdf.parse(fechaFin);
            }
        } catch (ParseException e) {
            Log.e(TAG, "Error al parsear fechas: inicio=" + fechaInicio + " fin=" + fechaFin, e);
        }

        List<AuditoriaItem> temp = new ArrayList<>();
        for (AuditoriaItem it : originalList) {
            try {
                if (it.getFecha() == null || it.getFecha().isEmpty()) continue;
                Date d = sdf.parse(it.getFecha());
                boolean afterInicio = (inicio == null) || !d.before(inicio);
                boolean beforeFin = (fin == null) || !d.after(fin);
                if (afterInicio && beforeFin) temp.add(it);
            } catch (ParseException e) {
                Log.w(TAG, "Omitiendo fecha inválida: " + it.getFecha());
            }
        }
        filteredList = temp;
        notifyDataSetChanged();
    }

    public void limpiarFiltros() {
        filteredList = new ArrayList<>(originalList);
        notifyDataSetChanged();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String q = constraint == null ? "" : constraint.toString();
                List<AuditoriaItem> tempList = new ArrayList<>();
                if (q.isEmpty()) {
                    tempList.addAll(originalList);
                } else {
                    for (AuditoriaItem it : originalList) {
                        if ((it.getAccion() != null && it.getAccion().toLowerCase().contains(q)) ||
                                (it.getEquipo() != null && it.getEquipo().toLowerCase().contains(q)) ||
                                (it.getResponsable() != null && it.getResponsable().toLowerCase().contains(q)) ||
                                (it.getSolicitanteProveedor() != null && it.getSolicitanteProveedor().toLowerCase().contains(q))) {
                            tempList.add(it);
                        }
                    }
                }
                FilterResults results = new FilterResults();
                results.values = tempList;
                results.count = tempList.size();
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredList = (List<AuditoriaItem>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    static class Holder extends RecyclerView.ViewHolder {
        TextView tvFecha, tvAccion, tvEquipo, tvCantidad, tvResponsable, tvSolicitanteProveedor;

        Holder(@NonNull View itemView) {
            super(itemView);
            tvFecha = itemView.findViewById(R.id.tvAudFecha);
            tvAccion = itemView.findViewById(R.id.tvAudAccion);
            tvEquipo = itemView.findViewById(R.id.tvAudEquipo);
            tvCantidad = itemView.findViewById(R.id.tvAudCantidad);
            tvResponsable = itemView.findViewById(R.id.tvAudResponsable);
            tvSolicitanteProveedor = itemView.findViewById(R.id.tvAudSolicitanteProveedor);
        }
    }
}
