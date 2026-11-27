package com.yelpme.app.ui.historial;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.util.Consumer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.yelpme.app.R;
import com.yelpme.app.databinding.ItemSolicitudBinding;
import com.yelpme.app.modelos.Servicio;
import com.yelpme.app.modelos.Solicitud;
import com.yelpme.app.ui.comunes.BadgeEstado;
import com.yelpme.app.util.Formato;
import com.yelpme.app.util.IconosServicio;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Historial (item_solicitud): badge, fecha, título, lugar e íconos de servicios. */
public class AdaptadorHistorial extends ListAdapter<Solicitud, AdaptadorHistorial.Fila> {

    private final Consumer<Solicitud> alTocar;
    private List<Servicio> catalogo = Collections.emptyList();

    public AdaptadorHistorial(Consumer<Solicitud> alTocar) {
        super(new DiffUtil.ItemCallback<Solicitud>() {
            @Override
            public boolean areItemsTheSame(@NonNull Solicitud a, @NonNull Solicitud b) {
                return a.id == b.id;
            }

            @Override
            public boolean areContentsTheSame(@NonNull Solicitud a, @NonNull Solicitud b) {
                return Objects.equals(a.estado, b.estado) && Objects.equals(a.titulo, b.titulo)
                        && Objects.equals(a.ubicacionNombre, b.ubicacionNombre) && Objects.equals(a.servicios, b.servicios);
            }
        });
        this.alTocar = alTocar;
    }

    /** Para mostrar los íconos; se pide una vez (caché de Catalogo). */
    public void ponerCatalogo(List<Servicio> servicios) {
        catalogo = servicios;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Fila onCreateViewHolder(@NonNull ViewGroup padre, int tipo) {
        return new Fila(ItemSolicitudBinding.inflate(LayoutInflater.from(padre.getContext()), padre, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Fila fila, int posicion) {
        Solicitud s = getItem(posicion);
        ItemSolicitudBinding v = fila.vista;
        Context ctx = v.getRoot().getContext();
        BadgeEstado.aplicar(v.tvBadge, s.estado);
        v.tvFecha.setText(Formato.fecha(s.createdAt));
        v.tvTituloFila.setText(s.titulo);
        v.tvLugar.setVisibility(s.ubicacionNombre != null ? View.VISIBLE : View.GONE);
        v.tvLugar.setText(s.ubicacionNombre);

        v.llServicios.removeAllViews();
        StringBuilder nombres = new StringBuilder();
        int tam = ctx.getResources().getDimensionPixelSize(R.dimen.icono);
        for (Servicio servicio : catalogo) {
            if (s.servicios != null && s.servicios.contains(servicio.id)) {
                ImageView icono = new ImageView(ctx);
                icono.setImageResource(IconosServicio.de(servicio.icono));
                icono.setColorFilter(ContextCompat.getColor(ctx, R.color.ym_marca_texto));
                icono.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(tam, tam);
                lp.setMarginEnd(ctx.getResources().getDimensionPixelSize(R.dimen.espacio_2));
                v.llServicios.addView(icono, lp);
                nombres.append(nombres.length() > 0 ? ", " : "").append(servicio.nombre);
            }
        }
        if (v.llServicios.getChildCount() == 0) {
            TextView solo = new TextView(ctx);
            solo.setText(R.string.inicio_solo_contactos);
            solo.setTextAppearance(R.style.TextAppearance_YelpMe_Pequeno);
            v.llServicios.addView(solo);
        }

        // Lectores de pantalla: "Pendiente, Choque en la avenida, 27 nov 2026, Policía, Ambulancia".
        v.getRoot().setContentDescription(String.join(", ", v.tvBadge.getText(), s.titulo, v.tvFecha.getText(),
                nombres.length() > 0 ? nombres : ctx.getString(R.string.inicio_solo_contactos)));
        v.getRoot().setOnClickListener(x -> alTocar.accept(s));
    }

    static class Fila extends RecyclerView.ViewHolder {
        final ItemSolicitudBinding vista;

        Fila(ItemSolicitudBinding vista) {
            super(vista.getRoot());
            this.vista = vista;
        }
    }
}
