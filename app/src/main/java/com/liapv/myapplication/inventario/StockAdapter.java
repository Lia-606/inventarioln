package com.liapv.myapplication.inventario;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.liapv.myapplication.R;
import java.util.List;
import com.liapv.myapplication.modelos.Equipo;

public class StockAdapter extends RecyclerView.Adapter<StockAdapter.StockViewHolder> {

    private List<Equipo> equipoList;

    public StockAdapter(List<Equipo> equipoList) {
        this.equipoList = equipoList;
    }

    @NonNull
    @Override
    public StockViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_equipo_stock, parent, false);
        return new StockViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull StockViewHolder holder, int position) {
        Equipo equipo = equipoList.get(position);
        holder.tvNombre.setText(equipo.getNombre());
        holder.tvId.setText("ID: " + equipo.getId());
        holder.tvStock.setText("Stock: " + equipo.getStock());
    }

    @Override
    public int getItemCount() {
        return equipoList.size();
    }

    public static class StockViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvId, tvStock;

        public StockViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvEquipoNombre);
            tvId = itemView.findViewById(R.id.tvEquipoId);
            tvStock = itemView.findViewById(R.id.tvStock);
        }
    }
}
