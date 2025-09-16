package com.liapv.myapplication.equipos;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.*;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.liapv.myapplication.R;
import com.liapv.myapplication.modelos.Equipo;

import java.util.List;

public class EquipoAdapter extends RecyclerView.Adapter<EquipoAdapter.ViewHolder> {

    private Context context;
    private List<Equipo> listaEquipos;
    private boolean modoQR = false;
    private String userRol = "";

    // ✅ Constructor para modo normal con rol
    public EquipoAdapter(Context context, List<Equipo> listaEquipos, String userRol) {
        this.context = context;
        this.listaEquipos = listaEquipos;
        this.userRol = userRol;
        this.modoQR = false;
    }

    // ✅ Constructor para modo QR (no usa rol)
    public EquipoAdapter(Context context, List<Equipo> listaEquipos, boolean modoQR) {
        this.context = context;
        this.listaEquipos = listaEquipos;
        this.modoQR = modoQR;
        this.userRol = ""; // No necesario en modo QR
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

        if (modoQR) {
            // 👉 Modo QR
            holder.itemView.setOnClickListener(v -> mostrarDialogoQR(equipo));
            holder.btnEditar.setVisibility(View.GONE);
        } else {
            // 👉 Modo normal: abrir detalle
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, DetalleEquipoActivity.class);
                intent.putExtra("equipo", equipo);
                intent.putExtra("rol", userRol);
                context.startActivity(intent);
            });

            holder.btnEditar.setOnClickListener(v -> {
                Intent intent = new Intent(context, DetalleEquipoActivity.class);
                intent.putExtra("equipo", equipo);
                intent.putExtra("rol", userRol);
                context.startActivity(intent);
            });

            // 👮 Solo Admin puede editar
            if ("Admin".equalsIgnoreCase(userRol)) {
                holder.btnEditar.setVisibility(View.VISIBLE);
            } else {
                holder.btnEditar.setVisibility(View.GONE);
            }
        }
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

    private void mostrarDialogoQR(Equipo equipo) {
        try {
            Bitmap qrBitmap = QRGenerator.generarQR(equipo.getCodigo());

            View qrView = LayoutInflater.from(context).inflate(R.layout.dialog_qr, null);
            ImageView ivQR = qrView.findViewById(R.id.ivDialogQR);
            ivQR.setImageBitmap(qrBitmap);

            new AlertDialog.Builder(context)
                    .setTitle("Código QR: " + equipo.getNombre())
                    .setView(qrView)
                    .setPositiveButton("Cerrar", null)
                    .show();

        } catch (Exception e) {
            Toast.makeText(context, "Error al generar QR", Toast.LENGTH_SHORT).show();
        }
    }
}
