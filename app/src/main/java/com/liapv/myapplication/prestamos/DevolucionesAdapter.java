package com.liapv.myapplication.prestamos;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.liapv.myapplication.R;
import com.liapv.myapplication.modelos.Equipo;
import com.liapv.myapplication.modelos.Prestamo;

import java.util.List;

public class DevolucionesAdapter extends RecyclerView.Adapter<DevolucionesAdapter.ViewHolder> {

    private List<Prestamo> devoluciones;
    private Context context;

    public DevolucionesAdapter(Context context, List<Prestamo> devoluciones) {
        this.context = context;
        this.devoluciones = devoluciones;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_prestamo, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Prestamo prestamo = devoluciones.get(position);

        holder.tvEquipo.setText("Equipo: " + prestamo.getEquipoNombre());
        holder.tvSolicitante.setText("Solicitante: " + prestamo.getNombreSolicitante());
        holder.tvFechaSolicitud.setText("Fecha Solicitud: " + prestamo.getFechaSolicitud());
        holder.tvEstado.setText("Estado: " + prestamo.getEstado());

        // En esta lista de devoluciones ocultamos los botones (solo visualización)
        holder.btnAprobar.setVisibility(View.GONE);
        holder.btnRechazar.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return devoluciones.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvEquipo, tvSolicitante, tvFechaSolicitud, tvEstado;
        Button btnAprobar, btnRechazar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEquipo = itemView.findViewById(R.id.tvEquipo);
            tvSolicitante = itemView.findViewById(R.id.tvSolicitante);
            tvFechaSolicitud = itemView.findViewById(R.id.tvFechaSolicitud);
            tvEstado = itemView.findViewById(R.id.tvEstado);
            btnAprobar = itemView.findViewById(R.id.btnAprobar);
            btnRechazar = itemView.findViewById(R.id.btnRechazar);
        }
    }
}

