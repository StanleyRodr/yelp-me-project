package com.yelpme.app.ui.vinculos;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yelpme.app.R;
import com.yelpme.app.databinding.ItemVinculoBinding;
import com.yelpme.app.modelos.Vinculo;
import com.yelpme.app.util.Formato;

import java.util.ArrayList;
import java.util.List;

/** Quién me tiene (item_vinculo): llamar, ver su domicilio y opciones (desvincular, bloquear). */
public class AdaptadorVinculos extends RecyclerView.Adapter<AdaptadorVinculos.Fila> {

    /** Acciones de una fila. */
    public interface Acciones {
        void llamar(Vinculo v);

        void verMapa(Vinculo v);

        void opciones(Vinculo v, View ancla);
    }

    private final Acciones acciones;
    private List<Vinculo> vinculos = new ArrayList<>();

    public AdaptadorVinculos(Acciones acciones) {
        this.acciones = acciones;
    }

    public void mostrar(List<Vinculo> lista) {
        vinculos = lista != null ? lista : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Fila onCreateViewHolder(@NonNull ViewGroup padre, int tipo) {
        return new Fila(ItemVinculoBinding.inflate(LayoutInflater.from(padre.getContext()), padre, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Fila fila, int posicion) {
        Vinculo v = vinculos.get(posicion);
        ItemVinculoBinding b = fila.vista;
        b.tvNombre.setText(v.nombreCompleto());
        b.tvDetalle.setText(b.getRoot().getContext().getString(R.string.vinculos_desde, v.telefono, Formato.fecha(v.desde)));
        boolean conDomicilio = v.domicilio != null;
        b.tvDomicilio.setVisibility(conDomicilio ? View.VISIBLE : View.GONE);
        b.btnMapa.setVisibility(conDomicilio ? View.VISIBLE : View.GONE);
        if (conDomicilio) {
            b.tvDomicilio.setText(v.domicilio.texto);
        }
        b.btnLlamar.setOnClickListener(x -> acciones.llamar(v));
        b.btnMapa.setOnClickListener(x -> acciones.verMapa(v));
        b.btnMas.setOnClickListener(x -> acciones.opciones(v, x));
    }

    @Override
    public int getItemCount() {
        return vinculos.size();
    }

    static class Fila extends RecyclerView.ViewHolder {
        final ItemVinculoBinding vista;

        Fila(ItemVinculoBinding vista) {
            super(vista.getRoot());
            this.vista = vista;
        }
    }
}
