package com.yelpme.app.ui.inicio;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.yelpme.app.R;
import com.yelpme.app.api.Catalogo;
import com.yelpme.app.api.ClienteApi;
import com.yelpme.app.api.ErrorApi;
import com.yelpme.app.api.Llamada;
import com.yelpme.app.api.ServicioSolicitudes;
import com.yelpme.app.databinding.ActivityInicioBinding;
import com.yelpme.app.databinding.DialogResumenBinding;
import com.yelpme.app.databinding.ItemChipServicioBinding;
import com.yelpme.app.modelos.NuevaSolicitud;
import com.yelpme.app.modelos.Servicio;
import com.yelpme.app.modelos.Solicitud;
import com.yelpme.app.ui.base.BaseActivity;
import com.yelpme.app.ui.comunes.BadgeEstado;
import com.yelpme.app.ui.solicitudes.DetalleSolicitudActivity;
import com.yelpme.app.util.Formato;
import com.yelpme.app.util.Formularios;
import com.yelpme.app.util.IconosServicio;
import com.yelpme.app.util.MapaOsm;
import com.yelpme.app.util.TransicionesSolicitud;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Pantalla principal: la solicitud activa arriba (Pendiente o Enviado, se
 * recarga cada 15 s) y el formulario para pedir ayuda (POST /api/solicitudes).
 */
public class InicioActivity extends BaseActivity {

    /** Extra: se entró sin poder confirmar la sesión (sin conexión). */
    public static final String EXTRA_SIN_CONEXION = "sin_conexion";
    /** Extra: contactos vinculados al registrarse. */
    public static final String EXTRA_VINCULADOS = "vinculados";

    private static final long RECARGA_MS = 15_000;
    private static final String PERMISO_NOTIFICACIONES_PEDIDO = "permiso_notificaciones_pedido";

    private ActivityInicioBinding vista;
    private FormularioSolicitud formulario;
    private Llamada<?> llamada;
    private Llamada<List<Solicitud>> llamadaActiva;
    @Nullable
    private Solicitud activa;
    private final Handler recarga = new Handler(Looper.getMainLooper());
    private final Runnable recargar = new Runnable() {
        @Override
        public void run() {
            cargarActiva();
            recarga.postDelayed(this, RECARGA_MS);
        }
    };

    /** Android 13+: permiso para mostrar notificaciones (alertas de contactos y estados). */
    private final ActivityResultLauncher<String> permisoNotificaciones = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), concedido -> {
            });

    @Override
    protected int itemMenu() {
        return R.id.menu_inicio;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vista = ActivityInicioBinding.inflate(getLayoutInflater());
        ponerContenido(vista);
        formulario = new FormularioSolicitud(this, vista.formulario, sesion);

        vista.btnLimpiar.setOnClickListener(v -> formulario.limpiar());
        vista.btnEnviarSolicitud.setOnClickListener(v -> pedirAyuda());
        vista.cardTuSolicitud.setOnClickListener(v -> {
            if (activa != null) {
                startActivity(DetalleSolicitudActivity.intent(this, activa.id));
            }
        });

        if (!formulario.restaurar(savedInstanceState)) {
            formulario.buscarUbicacion(false);
        }
        if (savedInstanceState == null) {
            avisosDeEntrada();
            pedirPermisoNotificaciones();
        }
    }

    /** Una sola vez, con explicación antes del diálogo del sistema. Si lo niega, Perfil lo recuerda. */
    private void pedirPermisoNotificaciones() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            return;
        }
        SharedPreferences ajustes = getSharedPreferences("ym_ajustes", MODE_PRIVATE);
        if (ajustes.getBoolean(PERMISO_NOTIFICACIONES_PEDIDO, false)) {
            return;
        }
        ajustes.edit().putBoolean(PERMISO_NOTIFICACIONES_PEDIDO, true).apply();
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.notificaciones_permiso_titulo)
                .setMessage(R.string.notificaciones_permiso_explicacion)
                .setNegativeButton(R.string.comun_cancelar, null)
                .setPositiveButton(R.string.comun_continuar, (d, i) -> permisoNotificaciones.launch(Manifest.permission.POST_NOTIFICATIONS))
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        formulario.aplicarSuspension(sesion.usuario());
        // Suspensión o rehabilitación hechas por el admin mientras la app estaba en segundo plano.
        formulario.refrescarUsuario();
        recarga.post(recargar);
    }

    @Override
    protected void onPause() {
        recarga.removeCallbacks(recargar);
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle estado) {
        super.onSaveInstanceState(estado);
        formulario.guardar(estado);
    }

    @Override
    protected void onDestroy() {
        if (llamada != null) {
            llamada.cancelar();
        }
        if (llamadaActiva != null) {
            llamadaActiva.cancelar();
        }
        super.onDestroy();
    }

    // ---------- Solicitud activa ----------

    /** La más reciente en Pendiente o Enviado (la API las trae de la más nueva a la más vieja). */
    private void cargarActiva() {
        if (llamadaActiva != null) {
            llamadaActiva.cancelar();
        }
        llamadaActiva = Llamada.de(ClienteApi.servicio(ServicioSolicitudes.class).listar(null)).enviar(new Llamada.Respuestas<List<Solicitud>>() {
            @Override
            public void alExito(List<Solicitud> solicitudes) {
                Solicitud encontrada = null;
                if (solicitudes != null) {
                    for (Solicitud s : solicitudes) {
                        if (TransicionesSolicitud.esActiva(s)) {
                            encontrada = s;
                            break;
                        }
                    }
                }
                mostrarActiva(encontrada);
            }

            @Override
            public void alError(ErrorApi error) {
                // Recarga silenciosa: se conserva lo que ya se veía.
            }
        });
    }

    private void mostrarActiva(@Nullable Solicitud s) {
        activa = s;
        if (s == null) {
            vista.cardTuSolicitud.setVisibility(View.GONE);
            return;
        }
        vista.cardTuSolicitud.setVisibility(View.VISIBLE);
        BadgeEstado.aplicar(vista.tvBadgeActiva, s.estado);
        vista.tvActivaTitulo.setText(s.titulo);
        vista.tvActivaMensaje.setText(TransicionesSolicitud.ENVIADO.equals(s.estado)
                ? R.string.inicio_activa_enviado : R.string.inicio_activa_pendiente);
        vista.tvActivaHace.setText(getString(R.string.inicio_activa_hace, hace(s.createdAt)));
        iconosServicios(vista.llActivaServicios, s.servicios);
    }

    /** "Hace 5 min" / "Hace 2 h"; más de un día, la fecha (siempre en español). */
    private CharSequence hace(String iso) {
        try {
            long minutos = (System.currentTimeMillis() - OffsetDateTime.parse(iso).toInstant().toEpochMilli()) / 60_000;
            if (minutos < 1) {
                return getString(R.string.hace_momento);
            }
            if (minutos < 60) {
                return getString(R.string.hace_minutos, minutos);
            }
            if (minutos < 24 * 60) {
                return getString(R.string.hace_horas, minutos / 60);
            }
            return Formato.fechaHora(iso);
        } catch (RuntimeException e) {
            return "";
        }
    }

    /** Íconos de los servicios (o "Solo tus contactos"). */
    private void iconosServicios(LinearLayout fila, List<Long> ids) {
        Catalogo.servicios(new Llamada.Respuestas<List<Servicio>>() {
            @Override
            public void alExito(List<Servicio> catalogo) {
                fila.removeAllViews();
                StringBuilder nombres = new StringBuilder();
                int tam = getResources().getDimensionPixelSize(R.dimen.icono);
                for (Servicio s : catalogo) {
                    if (ids != null && ids.contains(s.id)) {
                        ImageView icono = new ImageView(InicioActivity.this);
                        icono.setImageResource(IconosServicio.de(s.icono));
                        icono.setColorFilter(ContextCompat.getColor(InicioActivity.this, R.color.ym_marca_texto));
                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(tam, tam);
                        lp.setMarginEnd(getResources().getDimensionPixelSize(R.dimen.espacio_2));
                        fila.addView(icono, lp);
                        nombres.append(nombres.length() > 0 ? ", " : "").append(s.nombre);
                    }
                }
                fila.setContentDescription(nombres.length() > 0 ? nombres : getString(R.string.inicio_solo_contactos));
                fila.setVisibility(fila.getChildCount() > 0 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void alError(ErrorApi error) {
                fila.setVisibility(View.GONE);
            }
        });
    }

    // ---------- Pedir ayuda ----------

    private void pedirAyuda() {
        if (!vista.btnEnviarSolicitud.isEnabled() || !formulario.validar()) {
            return;
        }
        formulario.conUbicacion(this::mostrarResumen);
    }

    private void mostrarResumen() {
        DialogResumenBinding r = DialogResumenBinding.inflate(getLayoutInflater());
        r.tvResumenEmergencia.setText(getString(R.string.resumen_emergencia, Formularios.texto(vista.formulario.etEmergencia)));
        r.tvResumenLugar.setText(formulario.lugarVisible());
        List<Servicio> elegidos = formulario.serviciosElegidosCatalogo();
        for (Servicio s : elegidos) {
            Chip chip = ItemChipServicioBinding.inflate(getLayoutInflater(), r.cgResumenServicios, false).getRoot();
            chip.setText(s.nombre);
            chip.setChipIconResource(IconosServicio.de(s.icono));
            chip.setCheckable(false);
            chip.setClickable(false);
            r.cgResumenServicios.addView(chip);
        }
        r.tvResumenSoloContactos.setVisibility(elegidos.isEmpty() ? View.VISIBLE : View.GONE);

        MapaOsm mapaResumen = new MapaOsm(this, r.mapResumenContainer, getLifecycle());
        mapaResumen.soloLectura(true);
        mapaResumen.centrar(formulario.latitud(), formulario.longitud(), MapaOsm.ZOOM_CALLE);
        mapaResumen.marcador(formulario.latitud(), formulario.longitud(), R.drawable.ic_place, String.valueOf(r.tvResumenLugar.getText()));

        AlertDialog dialogo = new MaterialAlertDialogBuilder(this)
                .setView(r.getRoot())
                .setOnDismissListener(d -> mapaResumen.liberar())
                .show();
        r.btnCerrarModal.setOnClickListener(v -> dialogo.dismiss());
        r.btnModalCancelar.setOnClickListener(v -> dialogo.dismiss());
        r.btnModalAceptar.setOnClickListener(v -> {
            dialogo.dismiss();
            enviar(formulario.datos(false));
        });
    }

    private void enviar(NuevaSolicitud s) {
        Formularios.cargando(vista.btnEnviarSolicitud, true, R.string.inicio_enviar, R.string.inicio_enviando);
        llamada = Llamada.de(ClienteApi.servicio(ServicioSolicitudes.class).crear(s)).enviar(new Llamada.Respuestas<Solicitud>() {
            @Override
            public void alExito(Solicitud creada) {
                Formularios.cargando(vista.btnEnviarSolicitud, false, R.string.inicio_enviar, R.string.inicio_enviando);
                formulario.limpiar();
                mostrarActiva(creada);
                vista.getRoot().smoothScrollTo(0, 0);
                aviso(R.string.inicio_enviada).show();
            }

            @Override
            public void alError(ErrorApi error) {
                Formularios.cargando(vista.btnEnviarSolicitud, false, R.string.inicio_enviar, R.string.inicio_enviando);
                if (error.getCodigo() == 401) {
                    return;
                }
                if (error.getCodigo() == 403) {
                    formulario.refrescarUsuario();
                }
                String titulo = formulario.errorTitulo(error);
                aviso(titulo != null ? titulo : error.getMessage()).show();
            }
        });
    }

    private void avisosDeEntrada() {
        int vinculados = getIntent().getIntExtra(EXTRA_VINCULADOS, 0);
        if (getIntent().getBooleanExtra(EXTRA_SIN_CONEXION, false)) {
            aviso(R.string.sesion_sin_conexion).show();
        } else if (vinculados > 0) {
            aviso(getResources().getQuantityString(R.plurals.registro_vinculados, vinculados, vinculados)).show();
        }
    }
}
