package com.yelpme.app.ui.solicitudes;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.yelpme.app.R;
import com.yelpme.app.api.Catalogo;
import com.yelpme.app.api.ClienteApi;
import com.yelpme.app.api.ErrorApi;
import com.yelpme.app.api.Llamada;
import com.yelpme.app.api.ServicioSolicitudes;
import com.yelpme.app.databinding.ActivityDetalleSolicitudBinding;
import com.yelpme.app.databinding.DialogMotivoBinding;
import com.yelpme.app.databinding.ItemChipServicioBinding;
import com.yelpme.app.modelos.Servicio;
import com.yelpme.app.modelos.Solicitud;
import com.yelpme.app.ui.comunes.BadgeEstado;
import com.yelpme.app.util.Formato;
import com.yelpme.app.util.Formularios;
import com.yelpme.app.util.MapaOsm;
import com.yelpme.app.util.Margenes;
import com.yelpme.app.util.TransicionesSolicitud;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Detalle de una solicitud propia: estado, datos, mapa y acciones del usuario
 * (editar en Pendiente, cancelar con motivo, terminar) según
 * {@link TransicionesSolicitud}. Se recarga cada 15 s mientras está visible.
 */
public class DetalleSolicitudActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "id";
    private static final long RECARGA_MS = 15_000;

    private ActivityDetalleSolicitudBinding vista;
    private ServicioSolicitudes api;
    private MapaOsm mapa;
    private long id;
    @Nullable
    private Solicitud solicitud;
    private Llamada<?> llamada;
    private final Handler recarga = new Handler(Looper.getMainLooper());
    private final Runnable recargar = new Runnable() {
        @Override
        public void run() {
            cargar();
            recarga.postDelayed(this, RECARGA_MS);
        }
    };

    private final ActivityResultLauncher<Intent> editar = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), r -> {
                if (r.getResultCode() == RESULT_OK) {
                    aviso(getString(R.string.solicitud_actualizada));
                }
            });

    public static Intent intent(Context contexto, long id) {
        return new Intent(contexto, DetalleSolicitudActivity.class).putExtra(EXTRA_ID, id);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Margenes.bordeABorde(this, false);
        super.onCreate(savedInstanceState);
        vista = ActivityDetalleSolicitudBinding.inflate(getLayoutInflater());
        setContentView(vista.getRoot());
        Margenes.aplicar(vista.barra.getRoot(), vista.desplazable);
        api = ClienteApi.servicio(ServicioSolicitudes.class);
        id = getIntent().getLongExtra(EXTRA_ID, 0);

        vista.barra.tvTitulo.setText(R.string.solicitud_titulo);
        vista.barra.btnAtras.setOnClickListener(v -> finish());
        vista.recarga.setColorSchemeResources(R.color.ym_marca_texto);
        vista.recarga.setProgressBackgroundColorSchemeResource(R.color.ym_superficie);
        vista.recarga.setOnRefreshListener(this::cargar);
        mapa = new MapaOsm(this, vista.mapDetalleContainer, getLifecycle());
        mapa.soloLectura(true);

        vista.btnEditar.setOnClickListener(v -> editar.launch(FormSolicitudActivity.intent(this, id)));
        vista.btnCancelar.setOnClickListener(v -> pedirMotivo());
        vista.btnTerminar.setOnClickListener(v -> new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.solicitud_terminar_pregunta)
                .setMessage(R.string.solicitud_terminar_explicacion)
                .setNegativeButton(R.string.comun_volver, null)
                .setPositiveButton(R.string.solicitud_terminar, (d, i) -> cambiarEstado(TransicionesSolicitud.TERMINADO, null))
                .show());
        vista.recarga.setRefreshing(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        recarga.post(recargar);
    }

    @Override
    protected void onPause() {
        recarga.removeCallbacks(recargar);
        super.onPause();
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
        llamada = Llamada.de(api.obtener(id)).enviar(new Llamada.Respuestas<Solicitud>() {
            @Override
            public void alExito(Solicitud s) {
                vista.recarga.setRefreshing(false);
                mostrar(s);
            }

            @Override
            public void alError(ErrorApi error) {
                vista.recarga.setRefreshing(false);
                if (error.getCodigo() == 404) {
                    finish();
                } else if (error.getCodigo() != 401 && solicitud == null) {
                    Snackbar.make(vista.raiz, error.getMessage(), Snackbar.LENGTH_INDEFINITE)
                            .setAction(R.string.comun_reintentar, v -> cargar())
                            .show();
                }
            }
        });
    }

    private void mostrar(Solicitud s) {
        boolean primeraVez = solicitud == null;
        solicitud = s;
        vista.contenido.setVisibility(View.VISIBLE);
        BadgeEstado.aplicar(vista.tvBadge, s.estado);
        vista.tvMensajeEstado.setText(mensajeEstado(s));
        boolean conMotivo = s.motivo != null && !s.motivo.isEmpty();
        vista.tvMotivo.setVisibility(conMotivo ? View.VISIBLE : View.GONE);
        if (conMotivo) {
            vista.tvMotivo.setText(getString(R.string.solicitud_motivo_valor, s.motivo));
        }
        vista.tvTituloSolicitud.setText(s.titulo);
        vista.tvDescripcion.setVisibility(s.descripcion != null ? View.VISIBLE : View.GONE);
        vista.tvDescripcion.setText(s.descripcion);
        vista.tvLugar.setText(s.ubicacionNombre != null ? s.ubicacionNombre : getString(R.string.solicitud_sin_lugar));
        if (primeraVez) {
            mapa.centrar(s.latitud, s.longitud, MapaOsm.ZOOM_CALLE);
        }
        mapa.marcador(s.latitud, s.longitud, R.drawable.ic_place, s.titulo);
        vista.tvFechas.setText(fechas(s));
        mostrarServicios(s.servicios);

        List<TransicionesSolicitud.Accion> acciones = TransicionesSolicitud.delUsuario(s);
        vista.btnEditar.setVisibility(acciones.contains(TransicionesSolicitud.Accion.EDITAR) ? View.VISIBLE : View.GONE);
        vista.btnTerminar.setVisibility(acciones.contains(TransicionesSolicitud.Accion.TERMINAR) ? View.VISIBLE : View.GONE);
        vista.btnCancelar.setVisibility(acciones.contains(TransicionesSolicitud.Accion.CANCELAR) ? View.VISIBLE : View.GONE);
    }

    private String mensajeEstado(Solicitud s) {
        switch (s.estado) {
            case TransicionesSolicitud.ENVIADO:
                return getString(R.string.inicio_activa_enviado);
            case TransicionesSolicitud.RECHAZADO:
                return getString(R.string.solicitud_msg_rechazado);
            case TransicionesSolicitud.CANCELADO:
                return getString(R.string.solicitud_msg_cancelado);
            case TransicionesSolicitud.TERMINADO:
                return getString(R.string.solicitud_msg_terminado);
            default:
                return getString(s.servicios == null || s.servicios.isEmpty()
                        ? R.string.solicitud_msg_solo_contactos : R.string.inicio_activa_pendiente);
        }
    }

    /** Creada, enviada, cerrada y quién atendió. */
    private String fechas(Solicitud s) {
        StringBuilder t = new StringBuilder(getString(R.string.solicitud_creada, Formato.fechaHora(s.createdAt)));
        if (s.enviadoEn != null) {
            t.append('\n').append(getString(R.string.solicitud_enviada_en, Formato.fechaHora(s.enviadoEn)));
        }
        if (s.cerradoEn != null) {
            t.append('\n').append(getString(R.string.solicitud_cerrada_en, Formato.fechaHora(s.cerradoEn)));
        }
        if (s.admin != null) {
            t.append('\n').append(getString(R.string.solicitud_atendida_por, s.admin.nombreCompleto()));
        }
        return t.toString();
    }

    private void mostrarServicios(List<Long> ids) {
        Catalogo.servicios(new Llamada.Respuestas<List<Servicio>>() {
            @Override
            public void alExito(List<Servicio> catalogo) {
                vista.cgServicios.removeAllViews();
                for (Servicio s : catalogo) {
                    if (ids != null && ids.contains(s.id)) {
                        Chip chip = ItemChipServicioBinding.inflate(getLayoutInflater(), vista.cgServicios, false).getRoot();
                        chip.setText(s.nombre);
                        chip.setChipIconResource(com.yelpme.app.util.IconosServicio.de(s.icono));
                        chip.setCheckable(false);
                        chip.setClickable(false);
                        vista.cgServicios.addView(chip);
                    }
                }
                vista.tvSoloContactos.setVisibility(vista.cgServicios.getChildCount() == 0 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void alError(ErrorApi error) {
            }
        });
    }

    // ---------- Acciones ----------

    /** Cancelar exige motivo: el diálogo no se cierra si está vacío. */
    private void pedirMotivo() {
        DialogMotivoBinding m = DialogMotivoBinding.inflate(getLayoutInflater());
        AlertDialog dialogo = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.solicitud_cancelar_pregunta)
                .setView(m.getRoot())
                .setNegativeButton(R.string.comun_volver, null)
                .setPositiveButton(R.string.inicio_cancelar, null)
                .show();
        dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String motivo = Formularios.texto(m.etMotivo);
            if (motivo.isEmpty()) {
                Formularios.error(m.tilMotivo, R.string.solicitud_falta_motivo);
                return;
            }
            dialogo.dismiss();
            cambiarEstado(TransicionesSolicitud.CANCELADO, motivo);
        });
    }

    private void cambiarEstado(String estado, @Nullable String motivo) {
        Map<String, String> cuerpo = new HashMap<>();
        cuerpo.put("estado", estado);
        if (motivo != null) {
            cuerpo.put("motivo", motivo);
        }
        habilitarAcciones(false);
        llamada = Llamada.de(api.cambiarEstado(id, cuerpo)).enviar(new Llamada.Respuestas<Solicitud>() {
            @Override
            public void alExito(Solicitud s) {
            }

            @Override
            public void alExito(Solicitud s, String mensaje) {
                habilitarAcciones(true);
                mostrar(s);
                aviso(mensaje);
            }

            @Override
            public void alError(ErrorApi error) {
                habilitarAcciones(true);
                if (error.getCodigo() == 401) {
                    return;
                }
                // 409: el admin la cambió antes; se muestra su estado actual.
                aviso(error.getMessage());
                cargar();
            }
        });
    }

    private void habilitarAcciones(boolean habilitado) {
        vista.btnEditar.setEnabled(habilitado);
        vista.btnTerminar.setEnabled(habilitado);
        vista.btnCancelar.setEnabled(habilitado);
    }

    private void aviso(String texto) {
        Snackbar.make(vista.raiz, texto, Snackbar.LENGTH_LONG).show();
    }
}
