package com.liapv.myapplication.prestamos;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.liapv.myapplication.R;

public class PrestamoBottomSheet extends BottomSheetDialogFragment {

    private String userRol;

    public PrestamoBottomSheet(String rol) {
        this.userRol = rol;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_prestamo, container, false);

        TextView btnSolicitar = view.findViewById(R.id.btnSolicitarPrestamo);
        TextView btnListaSolicitudes = view.findViewById(R.id.btnListaSolicitudes);
        TextView btnRegistrarDevolucion = view.findViewById(R.id.btnRegistrarDevolucion);
        TextView btnListaDevoluciones = view.findViewById(R.id.btnListaDevoluciones);
        TextView btnVerMisSolicitudes = view.findViewById(R.id.btnVerMisSolicitudes);

        btnSolicitar.setOnClickListener(v -> {
            if (rolPermitido("Instructor")) {
                startActivity(new Intent(getContext(), SolicitarPrestamoActivity.class));
                dismiss();
            } else {
                showError("Solo los instructores pueden solicitar préstamos");
            }
        });

        btnListaSolicitudes.setOnClickListener(v -> {
            if (rolPermitido("Supervisor", "Admin")) {
                startActivity(new Intent(getContext(), ListaSolicitudesActivity.class));
                dismiss();
            } else {
                showError("No tienes permiso para ver solicitudes");
            }
        });

        btnRegistrarDevolucion.setOnClickListener(v -> {
            if (rolPermitido("Instructor", "Supervisor", "Admin")) {
                startActivity(new Intent(getContext(), RegistrarDevolucionActivity.class));
                dismiss();
            } else {
                showError("No tienes permiso para registrar devoluciones");
            }
        });

        btnListaDevoluciones.setOnClickListener(v -> {
            if (rolPermitido("Instructor", "Supervisor", "Admin")) {
                new DevolucionesBottomSheet().show(getParentFragmentManager(), "Devoluciones");
                dismiss();
            } else {
                showError("No tienes permiso para ver devoluciones");
            }
        });

        btnVerMisSolicitudes.setOnClickListener(v -> {
            new MisSolicitudesBottomSheet().show(getParentFragmentManager(), "MisSolicitudes");
            dismiss();
        });

        // Ocultar botones según rol
        if (!rolPermitido("Instructor")) {
            btnSolicitar.setVisibility(View.GONE);
            btnVerMisSolicitudes.setVisibility(View.GONE);
        }

        if (!rolPermitido("Supervisor", "Admin")) {
            btnListaSolicitudes.setVisibility(View.GONE);
        }

        if (!rolPermitido("Instructor", "Supervisor", "Admin")) {
            btnRegistrarDevolucion.setVisibility(View.GONE);
            btnListaDevoluciones.setVisibility(View.GONE);
        }

        return view;
    }

    private boolean rolPermitido(String... roles) {
        for (String rol : roles) {
            if (userRol != null && userRol.equalsIgnoreCase(rol)) {
                return true;
            }
        }
        return false;
    }

    private void showError(String mensaje) {
        Toast.makeText(getContext(), mensaje, Toast.LENGTH_SHORT).show();
    }
}
