package com.liapv.myapplication.usuarios;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.liapv.myapplication.R;

import java.util.List;

public class UsuariosAdapter extends RecyclerView.Adapter<UsuariosAdapter.UsuarioViewHolder> {

    private Context context;
    private List<Usuario> listaUsuarios;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onEditarClick(Usuario usuario);
        void onVerMasClick(Usuario usuario);
    }

    public UsuariosAdapter(Context context, List<Usuario> listaUsuarios, OnItemClickListener listener) {
        this.context = context;
        this.listaUsuarios = listaUsuarios;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UsuarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_usuario, parent, false);
        return new UsuarioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UsuarioViewHolder holder, int position) {
        Usuario usuario = listaUsuarios.get(position);

        holder.tvNombreApellido.setText(usuario.getNombreCompleto());
        holder.tvRol.setText("Rol: " + usuario.getRol());
        holder.tvSede.setText("Sede: " + usuario.getSede());

        holder.imgUsuario.setImageResource(R.drawable.ic_usuario); // default

        holder.btnEditar.setOnClickListener(v -> listener.onEditarClick(usuario));
        holder.btnVerMas.setOnClickListener(v -> listener.onVerMasClick(usuario));
    }

    @Override
    public int getItemCount() {
        return listaUsuarios.size();
    }

    public static class UsuarioViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombreApellido, tvRol, tvSede;
        ImageView imgUsuario;
        Button btnEditar, btnVerMas;

        public UsuarioViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombreApellido = itemView.findViewById(R.id.tvNombreApellido);
            tvRol = itemView.findViewById(R.id.tvRol);
            tvSede = itemView.findViewById(R.id.tvSede);
            imgUsuario = itemView.findViewById(R.id.imgUsuario);
            btnEditar = itemView.findViewById(R.id.btnEditar);
            btnVerMas = itemView.findViewById(R.id.btnVerMas);
        }
    }
}
