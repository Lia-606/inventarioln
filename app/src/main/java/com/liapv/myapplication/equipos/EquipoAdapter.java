package com.liapv.myapplication.equipos;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.liapv.myapplication.R;
import com.liapv.myapplication.modelos.Equipo;

import java.util.ArrayList;
import java.util.List;

public class EquipoAdapter extends RecyclerView.Adapter<EquipoAdapter.ViewHolder> implements Filterable {

    private Context context;
    private List<Equipo> listaEquipos;
    private List<Equipo> listaEquiposFull;

    public EquipoAdapter(Context context, List<Equipo> listaEquipos) {
        this.context = context;
        this.listaEquipos = listaEquipos;
        this.listaEquiposFull = new ArrayList<>(listaEquipos);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_equipo, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Equipo equipo = listaEquipos.get(position);
        holder.tvNombre.setText(equipo.getNombre());
        holder.tvTipo.setText(equipo.getTipo());
        holder.tvCodigo.setText(equipo.getCodigo()); // ✅ aquí se usa getCodigo()
    }

    @Override
    public int getItemCount() {
        return listaEquipos.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvTipo, tvCodigo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvTipo = itemView.findViewById(R.id.tvTipo);
            tvCodigo = itemView.findViewById(R.id.tvCodigo);
        }
    }

    @Override
    public Filter getFilter() {
        return filtroEquipos;
    }

    private Filter filtroEquipos = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Equipo> filtrada = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filtrada.addAll(listaEquiposFull);
            } else {
                String filtro = constraint.toString().toLowerCase().trim();
                for (Equipo item : listaEquiposFull) {
                    if (item.getNombre().toLowerCase().contains(filtro) ||
                            item.getTipo().toLowerCase().contains(filtro) ||
                            item.getCodigo().toLowerCase().contains(filtro)) { // ✅ aquí también
                        filtrada.add(item);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filtrada;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            listaEquipos.clear();
            listaEquipos.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };
}
