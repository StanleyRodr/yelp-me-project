package com.yelpme.app.ui.vinculos;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.PopupMenu;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.yelpme.app.R;
import com.yelpme.app.api.ClienteApi;
import com.yelpme.app.api.ErrorApi;
import com.yelpme.app.api.Llamada;
import com.yelpme.app.api.Respuesta;
import com.yelpme.app.api.ServicioContactos;
import com.yelpme.app.databinding.ActivityVinculosBinding;
import com.yelpme.app.modelos.Vinculo;
import com.yelpme.app.ui.base.BaseActivity;
import com.yelpme.app.util.Llamadas;
import com.yelpme.app.util.MapaOsm;

import java.util.Collections;
import java.util.List;

import retrofit2.Call;

/**
 * Quién me tiene como contacto (GET /api/vinculos), con el domicilio que cada
 * persona comparte. Llamar, desvincularme o bloquear. Bloqueados en {@link BloqueosActivity}.
 */
public class VinculosActivity extends BaseActivity {

    private ActivityVinculosBinding vista;
    private ServicioContactos api;
    private AdaptadorVinculos adaptador;
    private Llamadas llamadas;
    private Llamada<?> llamada;
    private boolean cargado;

    @Override
    protected int itemMenu() {
        return R.id.menu_vinculos;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vista = ActivityVinculosBinding.inflate(getLayoutInflater());
        ponerContenido(vista);
        api = ClienteApi.servicio(ServicioContactos.class);
        llamadas = new Llamadas(this, texto -> aviso(texto).show());

        adaptador = new AdaptadorVinculos(new AdaptadorVinculos.Acciones() {
            @Override
            public void llamar(Vinculo v) {
                llamadas.llamar(v.telefono);
            }

            @Override
            public void verMapa(Vinculo v) {
                mostrarMapa(v);
            }

            @Override
            public void opciones(Vinculo v, View ancla) {
                mostrarOpciones(v, ancla);
            }
        });
        vista.rvVinculos.setLayoutManager(new LinearLayoutManager(this));
        vista.rvVinculos.setAdapter(adaptador);
        vista.recargaVinculos.setColorSchemeResources(R.color.ym_marca_texto);
        vista.recargaVinculos.setProgressBackgroundColorSchemeResource(R.color.ym_superficie);
        vista.recargaVinculos.setOnRefreshListener(this::cargar);
        vista.btnBloqueados.setOnClickListener(v -> startActivity(new Intent(this, BloqueosActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
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
        if (llamada != null) {
            llamada.cancelar();
        }
        if (!cargado) {
            vista.recargaVinculos.setRefreshing(true);
        }
        llamada = Llamada.de(api.vinculos()).enviar(new Llamada.Respuestas<List<Vinculo>>() {
            @Override
            public void alExito(List<Vinculo> vinculos) {
                cargado = true;
                vista.recargaVinculos.setRefreshing(false);
                List<Vinculo> lista = vinculos != null ? vinculos : Collections.emptyList();
                adaptador.mostrar(lista);
                vista.tvVacio.setVisibility(lista.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void alError(ErrorApi error) {
                vista.recargaVinculos.setRefreshing(false);
                if (error.getCodigo() != 401) {
                    aviso(R.string.vinculos_error_cargar)
                            .setDuration(Snackbar.LENGTH_INDEFINITE)
                            .setAction(R.string.comun_reintentar, v -> cargar())
                            .show();
                }
            }
        });
    }

    /** Diálogo con el domicilio en un mapa de solo lectura; el mapa se libera al cerrar. */
    private void mostrarMapa(Vinculo v) {
        // El diálogo estira su vista: el mapa va dentro de un marco con alto fijo.
        FrameLayout marco = new FrameLayout(this);
        FrameLayout contenedor = new FrameLayout(this);
        marco.addView(contenedor, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                getResources().getDimensionPixelSize(R.dimen.mapa_dialogo)));
        MapaOsm mapa = new MapaOsm(this, contenedor, getLifecycle());
        mapa.soloLectura(true);
        mapa.centrar(v.domicilio.latitud, v.domicilio.longitud, MapaOsm.ZOOM_CALLE);
        mapa.marcador(v.domicilio.latitud, v.domicilio.longitud, R.drawable.ic_place, v.domicilio.texto);
        new MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.vinculos_domicilio_de, v.nombre))
                .setMessage(v.domicilio.texto)
                .setView(marco)
                .setPositiveButton(R.string.comun_aceptar, null)
                .setOnDismissListener(d -> mapa.liberar())
                .show();
    }

    private void mostrarOpciones(Vinculo v, View ancla) {
        PopupMenu menu = new PopupMenu(this, ancla);
        menu.getMenu().add(0, 1, 0, R.string.vinculos_desvincular);
        menu.getMenu().add(0, 2, 1, R.string.vinculos_bloquear);
        menu.setOnMenuItemClickListener(item -> {
            boolean bloquear = item.getItemId() == 2;
            new MaterialAlertDialogBuilder(this)
                    .setTitle(bloquear ? R.string.vinculos_bloquear : R.string.vinculos_desvincular)
                    .setMessage(getString(bloquear ? R.string.vinculos_bloquear_pregunta : R.string.vinculos_desvincular_pregunta, v.nombre))
                    .setNegativeButton(R.string.comun_cancelar, null)
                    .setPositiveButton(bloquear ? R.string.vinculos_bloquear : R.string.vinculos_desvincular,
                            (d, i) -> ejecutar(v, bloquear))
                    .show();
            return true;
        });
        menu.show();
    }

    private void ejecutar(Vinculo v, boolean bloquear) {
        if (bloquear) {
            enviar(api.bloquear(Collections.singletonMap("contacto_id", v.id)));
        } else {
            enviar(api.desvincular(v.id));
        }
    }

    /** Desvincular o bloquear: aviso con el mensaje de la API y recarga la lista. */
    private <T> void enviar(Call<Respuesta<T>> call) {
        llamada = Llamada.de(call).enviar(new Llamada.Respuestas<T>() {
            @Override
            public void alExito(T datos) {
            }

            @Override
            public void alExito(T datos, String mensaje) {
                aviso(mensaje).show();
                cargar();
            }

            @Override
            public void alError(ErrorApi error) {
                if (error.getCodigo() != 401) {
                    aviso(error.getMessage()).show();
                }
                cargar();
            }
        });
    }
}
