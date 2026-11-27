package com.yelpme.app.ui.historial;

import android.os.Bundle;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.Snackbar;
import com.yelpme.app.R;
import com.yelpme.app.api.Catalogo;
import com.yelpme.app.api.ClienteApi;
import com.yelpme.app.api.ErrorApi;
import com.yelpme.app.api.Llamada;
import com.yelpme.app.api.ServicioSolicitudes;
import com.yelpme.app.databinding.ActivityHistorialBinding;
import com.yelpme.app.modelos.Servicio;
import com.yelpme.app.modelos.Solicitud;
import com.yelpme.app.ui.base.BaseActivity;
import com.yelpme.app.ui.solicitudes.DetalleSolicitudActivity;
import com.yelpme.app.util.TransicionesSolicitud;

import java.util.Collections;
import java.util.List;

/**
 * Historial de solicitudes (GET /api/solicitudes?estado=…), más recientes
 * primero, con filtro por los 5 estados. Sin paginación (volumen del MVP).
 */
public class HistorialActivity extends BaseActivity {

    private ActivityHistorialBinding vista;
    private AdaptadorHistorial adaptador;
    private Llamada<List<Solicitud>> llamada;

    @Override
    protected int itemMenu() {
        return R.id.menu_historial;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vista = ActivityHistorialBinding.inflate(getLayoutInflater());
        ponerContenido(vista);

        adaptador = new AdaptadorHistorial(s -> startActivity(DetalleSolicitudActivity.intent(this, s.id)));
        vista.rvHistorial.setLayoutManager(new LinearLayoutManager(this));
        vista.rvHistorial.setAdapter(adaptador);
        vista.recargaHistorial.setColorSchemeResources(R.color.ym_marca_texto);
        vista.recargaHistorial.setProgressBackgroundColorSchemeResource(R.color.ym_superficie);
        vista.recargaHistorial.setOnRefreshListener(this::cargar);
        // El ChipGroup guarda solo el chip elegido al rotar (estado de la vista).
        vista.cgFiltro.setOnCheckedStateChangeListener((grupo, ids) -> cargar());

        Catalogo.servicios(new Llamada.Respuestas<List<Servicio>>() {
            @Override
            public void alExito(List<Servicio> servicios) {
                adaptador.ponerCatalogo(servicios);
            }

            @Override
            public void alError(ErrorApi error) {
            }
        });
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

    /** Estado del chip elegido; null = todas. */
    private String estadoElegido() {
        int id = vista.cgFiltro.getCheckedChipId();
        if (id == R.id.chipPendiente) return TransicionesSolicitud.PENDIENTE;
        if (id == R.id.chipEnviado) return TransicionesSolicitud.ENVIADO;
        if (id == R.id.chipTerminado) return TransicionesSolicitud.TERMINADO;
        if (id == R.id.chipCancelado) return TransicionesSolicitud.CANCELADO;
        if (id == R.id.chipRechazado) return TransicionesSolicitud.RECHAZADO;
        return null;
    }

    private void cargar() {
        if (llamada != null) {
            llamada.cancelar();
        }
        String estado = estadoElegido();
        vista.recargaHistorial.setRefreshing(true);
        llamada = Llamada.de(ClienteApi.servicio(ServicioSolicitudes.class).listar(estado)).enviar(new Llamada.Respuestas<List<Solicitud>>() {
            @Override
            public void alExito(List<Solicitud> solicitudes) {
                vista.recargaHistorial.setRefreshing(false);
                List<Solicitud> lista = solicitudes != null ? solicitudes : Collections.emptyList();
                adaptador.submitList(lista);
                vista.tvVacio.setVisibility(lista.isEmpty() ? View.VISIBLE : View.GONE);
                vista.tvVacio.setText(estado == null ? R.string.historial_vacio : R.string.historial_vacio_filtro);
            }

            @Override
            public void alError(ErrorApi error) {
                vista.recargaHistorial.setRefreshing(false);
                if (error.getCodigo() != 401) {
                    aviso(error.getMessage())
                            .setDuration(Snackbar.LENGTH_INDEFINITE)
                            .setAction(R.string.comun_reintentar, v -> cargar())
                            .show();
                }
            }
        });
    }
}
