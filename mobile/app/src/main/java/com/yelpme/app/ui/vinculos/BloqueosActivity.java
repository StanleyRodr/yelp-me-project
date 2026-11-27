package com.yelpme.app.ui.vinculos;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.yelpme.app.R;
import com.yelpme.app.api.ClienteApi;
import com.yelpme.app.api.ErrorApi;
import com.yelpme.app.api.Llamada;
import com.yelpme.app.api.ServicioContactos;
import com.yelpme.app.databinding.ActivityBloqueosBinding;
import com.yelpme.app.databinding.ItemBloqueoBinding;
import com.yelpme.app.modelos.Vinculo;
import com.yelpme.app.util.Formato;
import com.yelpme.app.util.Margenes;

import java.util.ArrayList;
import java.util.List;

/** Personas que bloqueé (GET /api/bloqueos) y desbloquear (DELETE /api/bloqueos/{id}). */
public class BloqueosActivity extends AppCompatActivity {

    private ActivityBloqueosBinding vista;
    private ServicioContactos api;
    private Llamada<?> llamada;
    private List<Vinculo> bloqueos = new ArrayList<>();

    private final RecyclerView.Adapter<Fila> adaptador = new RecyclerView.Adapter<Fila>() {
        @NonNull
        @Override
        public Fila onCreateViewHolder(@NonNull ViewGroup padre, int tipo) {
            return new Fila(ItemBloqueoBinding.inflate(LayoutInflater.from(padre.getContext()), padre, false));
        }

        @Override
        public void onBindViewHolder(@NonNull Fila fila, int posicion) {
            Vinculo b = bloqueos.get(posicion);
            fila.vista.tvNombre.setText(b.nombreCompleto());
            fila.vista.tvDetalle.setText(getString(R.string.vinculos_desde, b.telefono, Formato.fecha(b.desde)));
            fila.vista.btnDesbloquear.setOnClickListener(v -> new MaterialAlertDialogBuilder(BloqueosActivity.this)
                    .setMessage(getString(R.string.bloqueos_desbloquear_pregunta, b.nombre))
                    .setNegativeButton(R.string.comun_cancelar, null)
                    .setPositiveButton(R.string.bloqueos_desbloquear, (d, i) -> desbloquear(b))
                    .show());
        }

        @Override
        public int getItemCount() {
            return bloqueos.size();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Margenes.bordeABorde(this, false);
        super.onCreate(savedInstanceState);
        vista = ActivityBloqueosBinding.inflate(getLayoutInflater());
        setContentView(vista.getRoot());
        Margenes.aplicar(vista.barra.getRoot(), vista.contenido);
        api = ClienteApi.servicio(ServicioContactos.class);

        vista.barra.tvTitulo.setText(R.string.bloqueos_titulo);
        vista.barra.btnAtras.setOnClickListener(v -> finish());
        vista.rvBloqueos.setLayoutManager(new LinearLayoutManager(this));
        vista.rvBloqueos.setAdapter(adaptador);
        cargar();
    }

    @Override
    protected void onDestroy() {
        if (llamada != null) {
            llamada.cancelar();
        }
        super.onDestroy();
    }

    private void cargar() {
        llamada = Llamada.de(api.bloqueos()).enviar(new Llamada.Respuestas<List<Vinculo>>() {
            @Override
            public void alExito(List<Vinculo> lista) {
                bloqueos = lista != null ? lista : new ArrayList<>();
                adaptador.notifyDataSetChanged();
                vista.tvVacio.setVisibility(bloqueos.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void alError(ErrorApi error) {
                if (error.getCodigo() != 401) {
                    Snackbar.make(vista.raiz, R.string.vinculos_error_cargar, Snackbar.LENGTH_INDEFINITE)
                            .setAction(R.string.comun_reintentar, v -> cargar())
                            .show();
                }
            }
        });
    }

    private void desbloquear(Vinculo b) {
        llamada = Llamada.de(api.desbloquear(b.id)).enviar(new Llamada.Respuestas<Void>() {
            @Override
            public void alExito(Void nada) {
            }

            @Override
            public void alExito(Void nada, String mensaje) {
                Snackbar.make(vista.raiz, mensaje, Snackbar.LENGTH_LONG).show();
                cargar();
            }

            @Override
            public void alError(ErrorApi error) {
                if (error.getCodigo() != 401) {
                    Snackbar.make(vista.raiz, error.getMessage(), Snackbar.LENGTH_LONG).show();
                }
                cargar();
            }
        });
    }

    static class Fila extends RecyclerView.ViewHolder {
        final ItemBloqueoBinding vista;

        Fila(ItemBloqueoBinding vista) {
            super(vista.getRoot());
            this.vista = vista;
        }
    }
}
