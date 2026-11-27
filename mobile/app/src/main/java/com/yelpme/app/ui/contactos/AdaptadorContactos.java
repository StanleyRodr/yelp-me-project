package com.yelpme.app.ui.contactos;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.yelpme.app.R;
import com.yelpme.app.databinding.ItemContactoBinding;
import com.yelpme.app.modelos.Contacto;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Lista de la agenda (item_contacto). Toda la fila abre el detalle. */
public class AdaptadorContactos extends ListAdapter<Contacto, AdaptadorContactos.Fila> {

    private final Consumer<Contacto> alTocar;

    public AdaptadorContactos(Consumer<Contacto> alTocar) {
        super(new DiffUtil.ItemCallback<Contacto>() {
            @Override
            public boolean areItemsTheSame(@NonNull Contacto a, @NonNull Contacto b) {
                return a.id == b.id;
            }

            @Override
            public boolean areContentsTheSame(@NonNull Contacto a, @NonNull Contacto b) {
                return a.mismoContenido(b);
            }
        });
        this.alTocar = alTocar;
    }

    @NonNull
    @Override
    public Fila onCreateViewHolder(@NonNull ViewGroup padre, int tipo) {
        return new Fila(ItemContactoBinding.inflate(LayoutInflater.from(padre.getContext()), padre, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Fila fila, int posicion) {
        Contacto c = getItem(posicion);
        ItemContactoBinding v = fila.vista;
        v.tvInicial.setText(c.nombre == null || c.nombre.isEmpty()
                ? "" : new String(Character.toChars(c.nombre.codePointAt(0))).toUpperCase(Locale.ROOT));
        v.tvNombre.setText(c.nombre);
        v.tvDetalle.setText(v.getRoot().getContext().getString(R.string.cd_fila_contacto, c.parentesco, c.telefono));
        v.tvVinculado.setVisibility(c.vinculado ? View.VISIBLE : View.GONE);
        v.ivFavorito.setVisibility(c.favorito ? View.VISIBLE : View.GONE);

        // Lectores de pantalla: "María López, Pareja, 2351000002, vinculado, favorito".
        List<String> partes = new ArrayList<>();
        partes.add(c.nombre);
        partes.add(c.parentesco);
        partes.add(c.telefono);
        if (c.vinculado) {
            partes.add(v.getRoot().getContext().getString(R.string.cd_fila_vinculado));
        }
        if (c.favorito) {
            partes.add(v.getRoot().getContext().getString(R.string.cd_fila_favorito));
        }
        v.getRoot().setContentDescription(String.join(", ", partes));
        v.getRoot().setOnClickListener(x -> alTocar.accept(c));
    }

    static class Fila extends RecyclerView.ViewHolder {
        final ItemContactoBinding vista;

        Fila(ItemContactoBinding vista) {
            super(vista.getRoot());
            this.vista = vista;
        }
    }
}
