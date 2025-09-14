package com.liapv.myapplication.equipos;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.liapv.myapplication.R;
import com.liapv.myapplication.modelos.Equipo;

import java.util.List;

public class EquipoAdapter extends RecyclerView.Adapter<EquipoAdapter.ViewHolder> {
    private Context context;
    private List<Equipo> listaEquipos;

    public EquipoAdapter(Context context, List<Equipo> listaEquipos) {
        this.context = context;
        this.listaEquipos = listaEquipos;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_equipo, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Equipo equipo = listaEquipos.get(position);
        holder.tvNombre.setText(equipo.getNombre());
        holder.tvTipo.setText(equipo.getTipo());
        holder.tvCodigo.setText(equipo.getCodigo());

        // Clic en todo el item lleva al detalle
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetalleEquipoActivity.class);
            intent.putExtra("equipo", equipo);
            context.startActivity(intent);
        });

        // Clic en botón editar también lleva al detalle (puede cambiarse para formulario de edición)
        holder.btnEditar.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetalleEquipoActivity.class);
            intent.putExtra("equipo", equipo);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return listaEquipos.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvTipo, tvCodigo;
        ImageButton btnEditar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvTipo = itemView.findViewById(R.id.tvTipo);
            tvCodigo = itemView.findViewById(R.id.tvCodigo);
            btnEditar = itemView.findViewById(R.id.btnEditar);
        }
    }
}
