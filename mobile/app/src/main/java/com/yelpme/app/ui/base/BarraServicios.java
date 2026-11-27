package com.yelpme.app.ui.base;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yelpme.app.R;
import com.yelpme.app.databinding.ItemServicioBarraBinding;
import com.yelpme.app.modelos.Servicio;
import com.yelpme.app.util.IconosServicio;

import java.util.ArrayList;
import java.util.List;

/** Adapter de la barra inferior: un botón por servicio del catálogo. */
public class BarraServicios extends RecyclerView.Adapter<BarraServicios.Fila> {

    /** Qué hacer al tocar un servicio. */
    public interface AlTocar {
        void servicio(Servicio servicio);
    }

    static class Fila extends RecyclerView.ViewHolder {
        final ItemServicioBarraBinding vista;

        Fila(ItemServicioBarraBinding vista) {
            super(vista.getRoot());
            this.vista = vista;
        }
    }

    private final List<Servicio> servicios = new ArrayList<>();
    private final AlTocar alTocar;

    public BarraServicios(AlTocar alTocar) {
        this.alTocar = alTocar;
    }

    public void mostrar(List<Servicio> lista) {
        servicios.clear();
        servicios.addAll(lista);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Fila onCreateViewHolder(@NonNull ViewGroup padre, int tipo) {
        return new Fila(ItemServicioBarraBinding.inflate(LayoutInflater.from(padre.getContext()), padre, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Fila fila, int posicion) {
        Servicio s = servicios.get(posicion);
        fila.vista.ivIcono.setImageResource(IconosServicio.de(s.icono));
        fila.vista.tvNombre.setText(s.nombre);
        fila.vista.getRoot().setContentDescription(fila.itemView.getContext().getString(R.string.cd_servicio, s.nombre));
        fila.vista.getRoot().setOnClickListener(v -> alTocar.servicio(s));
    }

    @Override
    public int getItemCount() {
        return servicios.size();
    }
}
