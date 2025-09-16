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

public class InventarioAdapter extends RecyclerView.Adapter<InventarioAdapter.Holder> implements Filterable {

    private static final String TAG = "InventarioAdapter";

    private final Context context;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private List<InventarioItem> originalList; // datos completos
    private List<InventarioItem> filteredList; // datos visibles actualmente

    public InventarioAdapter(Context context, List<InventarioItem> initialData) {
        this.context = context;
        this.originalList = initialData != null ? new ArrayList<>(initialData) : new ArrayList<>();
        this.filteredList = new ArrayList<>(this.originalList);
        Log.i(TAG, "Adapter inicializado con " + originalList.size() + " items");
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.tabla__inventario, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        InventarioItem item = filteredList.get(position);
        holder.tvNombre.setText(item.getNombreEquipo() != null ? item.getNombreEquipo() : "-");
        holder.tvStock.setText(String.valueOf(item.getStock()));
        holder.tvFecha.setText(item.getFecha() != null ? item.getFecha() : "-");
        holder.tvTipo.setText(item.getTipoMovimiento() != null ? item.getTipoMovimiento() : "-");
        holder.tvResponsable.setText(item.getResponsable() != null ? item.getResponsable() : "-");
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public void setData(List<InventarioItem> newData) {
        if (newData == null) newData = new ArrayList<>();
        this.originalList = new ArrayList<>(newData);
        this.filteredList = new ArrayList<>(newData);
        notifyDataSetChanged();
        Log.i(TAG, "Datos actualizados, total items: " + filteredList.size());
    }

    // Filtrado por texto
    public void filtrar(String query) {
        if (query == null || query.trim().isEmpty()) {
            filteredList = new ArrayList<>(originalList);
            Log.i(TAG, "Filtrado por texto: vacío, mostrando todos los items");
        } else {
            String q = query.trim().toLowerCase();
            List<InventarioItem> temp = new ArrayList<>();
            for (InventarioItem it : originalList) {
                if ((it.getNombreEquipo() != null && it.getNombreEquipo().toLowerCase().contains(q)) ||
                        (it.getResponsable() != null && it.getResponsable().toLowerCase().contains(q)) ||
                        (it.getTipoMovimiento() != null && it.getTipoMovimiento().toLowerCase().contains(q))) {
                    temp.add(it);
                }
            }
            filteredList = temp;
            Log.i(TAG, "Filtrado por texto: '" + query + "', items encontrados: " + filteredList.size());
        }
        notifyDataSetChanged();
    }

    // Filtrado por rango de fechas
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

        List<InventarioItem> temp = new ArrayList<>();
        for (InventarioItem it : filteredList) { // mantenemos otros filtros aplicados
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
        Log.i(TAG, "Filtrado por fechas: " + fechaInicio + " a " + fechaFin + ", items encontrados: " + filteredList.size());
    }

    // Limpiar filtros
    public void limpiarFiltros() {
        filteredList = new ArrayList<>(originalList);
        notifyDataSetChanged();
        Log.i(TAG, "Filtros limpiados, mostrando todos los items: " + filteredList.size());
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String q = constraint == null ? "" : constraint.toString();
                filtrar(q);
                FilterResults results = new FilterResults();
                results.values = filteredList;
                results.count = filteredList.size();
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                // ya se notifica dentro de filtrar()
            }
        };
    }

    static class Holder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvStock, tvFecha, tvTipo, tvResponsable;

        Holder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvInvNombreEquipo);
            tvStock = itemView.findViewById(R.id.tvInvStock);
            tvFecha = itemView.findViewById(R.id.tvInvFecha);
            tvTipo = itemView.findViewById(R.id.tvInvTipoMovimiento);
            tvResponsable = itemView.findViewById(R.id.tvInvResponsable);
        }
    }
}
