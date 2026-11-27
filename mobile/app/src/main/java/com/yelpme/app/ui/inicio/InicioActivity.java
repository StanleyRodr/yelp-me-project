package com.yelpme.app.ui.inicio;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.yelpme.app.R;
import com.yelpme.app.api.Catalogo;
import com.yelpme.app.api.ClienteApi;
import com.yelpme.app.api.ErrorApi;
import com.yelpme.app.api.Llamada;
import com.yelpme.app.api.ServicioPerfil;
import com.yelpme.app.api.ServicioSolicitudes;
import com.yelpme.app.databinding.ActivityInicioBinding;
import com.yelpme.app.databinding.DialogResumenBinding;
import com.yelpme.app.databinding.ItemChipServicioBinding;
import com.yelpme.app.modelos.Domicilio;
import com.yelpme.app.modelos.NuevaSolicitud;
import com.yelpme.app.modelos.Servicio;
import com.yelpme.app.modelos.Solicitud;
import com.yelpme.app.modelos.Usuario;
import com.yelpme.app.ui.base.BaseActivity;
import com.yelpme.app.ui.mapa.ElegirUbicacionActivity;
import com.yelpme.app.util.Formularios;
import com.yelpme.app.util.IconosServicio;
import com.yelpme.app.util.MapaOsm;
import com.yelpme.app.util.Ubicacion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Pantalla principal: pedir ayuda (POST /api/solicitudes) con la ubicación
 * actual, el domicilio o un punto en el mapa, y los servicios del catálogo.
 * Sin servicios la solicitud solo avisa a los contactos.
 */
public class InicioActivity extends BaseActivity {

    /** Extra: se entró sin poder confirmar la sesión (sin conexión). */
    public static final String EXTRA_SIN_CONEXION = "sin_conexion";
    /** Extra: contactos vinculados al registrarse. */
    public static final String EXTRA_VINCULADOS = "vinculados";

    private static final String ESTADO_LAT = "lat";
    private static final String ESTADO_LNG = "lng";
    private static final String ESTADO_ORIGEN = "origen";

    protected ActivityInicioBinding vista;
    private Ubicacion ubicacion;
    private MapaOsm mapa;
    private List<Servicio> catalogo = Collections.emptyList();
    @Nullable
    private Domicilio domicilio;
    private Double lat;
    private Double lng;
    private Ubicacion.Origen origen;
    /** Al tocar "Pedir ayuda" sin ubicación: enviar en cuanto llegue. */
    private boolean enviarAlUbicar;
    private Llamada<?> llamada;

    private final ActivityResultLauncher<Intent> elegirPunto = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), r -> {
                Intent datos = r.getData();
                if (r.getResultCode() == RESULT_OK && datos != null) {
                    ponerUbicacion(datos.getDoubleExtra(ElegirUbicacionActivity.EXTRA_LAT, 0),
                            datos.getDoubleExtra(ElegirUbicacionActivity.EXTRA_LNG, 0), Ubicacion.Origen.MAPA);
                    if (enviarAlUbicar) {
                        enviarAlUbicar = false;
                        mostrarResumen();
                    }
                } else {
                    enviarAlUbicar = false;
                }
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
        ubicacion = new Ubicacion(this);
        mapa = new MapaOsm(this, vista.mapContainer, getLifecycle());
        mapa.soloLectura(true);

        vista.btnLimpiar.setOnClickListener(v -> limpiar());
        vista.btnEnviarSolicitud.setOnClickListener(v -> pedirAyuda());
        vista.btnUsarDomicilio.setOnClickListener(v -> {
            if (domicilio != null) {
                ponerUbicacion(domicilio.latitud, domicilio.longitud, Ubicacion.Origen.DOMICILIO);
            }
        });
        vista.btnCambiarMapa.setOnClickListener(v -> elegirPunto.launch(
                ElegirUbicacionActivity.intent(this, lat, lng, getString(R.string.inicio_cambiar_mapa))));

        mostrarServicios();
        cargarDomicilio();
        if (savedInstanceState != null && savedInstanceState.containsKey(ESTADO_LAT)) {
            ponerUbicacion(savedInstanceState.getDouble(ESTADO_LAT), savedInstanceState.getDouble(ESTADO_LNG),
                    Ubicacion.Origen.valueOf(savedInstanceState.getString(ESTADO_ORIGEN)));
        } else {
            buscarUbicacion(false);
        }
        if (savedInstanceState == null) {
            avisosDeEntrada();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        aplicarSuspension(sesion.usuario());
    }

    @Override
    protected void onSaveInstanceState(Bundle estado) {
        super.onSaveInstanceState(estado);
        if (lat != null && lng != null && origen != null) {
            estado.putDouble(ESTADO_LAT, lat);
            estado.putDouble(ESTADO_LNG, lng);
            estado.putString(ESTADO_ORIGEN, origen.name());
        }
    }

    @Override
    protected void onDestroy() {
        if (llamada != null) {
            llamada.cancelar();
        }
        super.onDestroy();
    }

    // ---------- Ubicación ----------

    /** `alFallarPreguntar`: si falla, muestra el diálogo de respaldo (al enviar). */
    private void buscarUbicacion(boolean alFallarPreguntar) {
        vista.tvOrigen.setText(R.string.inicio_origen_buscando);
        ubicacion.obtener(new Ubicacion.Resultado() {
            @Override
            public void alObtener(double latitud, double longitud) {
                // Si ya eligió domicilio o mapa mientras se buscaba, no se pisa.
                if (origen == null || origen == Ubicacion.Origen.GPS) {
                    ponerUbicacion(latitud, longitud, Ubicacion.Origen.GPS);
                }
                if (enviarAlUbicar) {
                    enviarAlUbicar = false;
                    mostrarResumen();
                }
            }

            @Override
            public void alFallar(Ubicacion.Motivo motivo) {
                if (origen == null) {
                    vista.tvOrigen.setText(R.string.inicio_origen_falta);
                }
                if (alFallarPreguntar) {
                    preguntarRespaldo(motivo);
                }
            }
        });
    }

    private void preguntarRespaldo(Ubicacion.Motivo motivo) {
        DialogoSinUbicacion.mostrar(this, motivo, domicilio, new DialogoSinUbicacion.Acciones() {
            @Override
            public void usarDomicilio(Domicilio d) {
                ponerUbicacion(d.latitud, d.longitud, Ubicacion.Origen.DOMICILIO);
                enviarAlUbicar = false;
                mostrarResumen();
            }

            @Override
            public void reintentar() {
                buscarUbicacion(true);
            }

            @Override
            public void marcarEnMapa() {
                elegirPunto.launch(ElegirUbicacionActivity.intent(InicioActivity.this, lat, lng,
                        getString(R.string.inicio_cambiar_mapa)));
            }
        });
    }

    private void ponerUbicacion(double latitud, double longitud, Ubicacion.Origen de) {
        lat = latitud;
        lng = longitud;
        origen = de;
        int texto;
        int icono;
        switch (de) {
            case DOMICILIO:
                texto = R.string.inicio_origen_domicilio;
                icono = R.drawable.ic_home;
                break;
            case MAPA:
                texto = R.string.inicio_origen_mapa;
                icono = R.drawable.ic_place;
                break;
            default:
                texto = R.string.inicio_origen_gps;
                icono = R.drawable.ic_my_location;
        }
        vista.tvOrigen.setText(texto);
        vista.tvOrigen.setCompoundDrawablesRelativeWithIntrinsicBounds(icono, 0, 0, 0);
        vista.mapContainer.setVisibility(View.VISIBLE);
        mapa.centrar(latitud, longitud, MapaOsm.ZOOM_CALLE);
        mapa.marcador(latitud, longitud, R.drawable.ic_place, getString(texto));
    }

    private void cargarDomicilio() {
        Llamada.de(ClienteApi.servicio(ServicioPerfil.class).domicilio()).enviar(new Llamada.Respuestas<Domicilio>() {
            @Override
            public void alExito(Domicilio d) {
                domicilio = d;
                vista.btnUsarDomicilio.setVisibility(d != null ? View.VISIBLE : View.GONE);
            }

            @Override
            public void alError(ErrorApi error) {
                // Sin domicilio disponible: el respaldo ofrece reintentar y mapa.
            }
        });
    }

    // ---------- Servicios ----------

    /** Un chip por servicio del catálogo (con check visible). */
    private void mostrarServicios() {
        Catalogo.servicios(new Llamada.Respuestas<List<Servicio>>() {
            @Override
            public void alExito(List<Servicio> servicios) {
                catalogo = servicios;
                vista.grupoServicios.removeAllViews();
                for (Servicio s : servicios) {
                    Chip chip = ItemChipServicioBinding.inflate(getLayoutInflater(), vista.grupoServicios, false).getRoot();
                    chip.setText(s.nombre);
                    chip.setChipIconResource(IconosServicio.de(s.icono));
                    chip.setTag(s.id);
                    vista.grupoServicios.addView(chip);
                }
                aplicarSuspension(sesion.usuario());
            }

            @Override
            public void alError(ErrorApi error) {
                // Sin catálogo: la solicitud puede ir solo a contactos.
            }
        });
    }

    /** Suspendido: no puede pedir servicios, sí avisar a sus contactos. */
    private void aplicarSuspension(@Nullable Usuario u) {
        boolean suspendido = u != null && u.suspendido;
        vista.tvAvisoSuspendido.setVisibility(suspendido ? View.VISIBLE : View.GONE);
        for (int i = 0; i < vista.grupoServicios.getChildCount(); i++) {
            Chip chip = (Chip) vista.grupoServicios.getChildAt(i);
            chip.setEnabled(!suspendido);
            if (suspendido) {
                chip.setChecked(false);
            }
        }
    }

    private List<Long> serviciosElegidos() {
        List<Long> ids = new ArrayList<>();
        for (int i = 0; i < vista.grupoServicios.getChildCount(); i++) {
            Chip chip = (Chip) vista.grupoServicios.getChildAt(i);
            if (chip.isChecked()) {
                ids.add((Long) chip.getTag());
            }
        }
        return ids;
    }

    // ---------- Enviar ----------

    private void pedirAyuda() {
        if (!vista.btnEnviarSolicitud.isEnabled()) {
            return;
        }
        vista.tilEmergencia.setError(null);
        if (Formularios.texto(vista.etEmergencia).isEmpty()) {
            Formularios.error(vista.tilEmergencia, R.string.inicio_falta_titulo);
            vista.etEmergencia.requestFocus();
            return;
        }
        if (lat == null || lng == null) {
            enviarAlUbicar = true;
            buscarUbicacion(true);
            return;
        }
        mostrarResumen();
    }

    /** Lugar escrito por la persona; si no escribió y usa su domicilio, "Domicilio: …". */
    @Nullable
    private String ubicacionNombre() {
        String lugar = Formularios.texto(vista.etLugar);
        if (!lugar.isEmpty()) {
            return lugar;
        }
        if (origen == Ubicacion.Origen.DOMICILIO && domicilio != null) {
            return getString(R.string.ubicacion_prefijo_domicilio, domicilio.texto);
        }
        return null;
    }

    private void mostrarResumen() {
        DialogResumenBinding r = DialogResumenBinding.inflate(getLayoutInflater());
        String lugar = ubicacionNombre();
        r.tvResumenEmergencia.setText(getString(R.string.resumen_emergencia, Formularios.texto(vista.etEmergencia)));
        r.tvResumenLugar.setText(lugar != null ? lugar : vista.tvOrigen.getText());
        List<Long> elegidos = serviciosElegidos();
        for (Servicio s : catalogo) {
            if (elegidos.contains(s.id)) {
                Chip chip = ItemChipServicioBinding.inflate(getLayoutInflater(), r.cgResumenServicios, false).getRoot();
                chip.setText(s.nombre);
                chip.setChipIconResource(IconosServicio.de(s.icono));
                chip.setCheckable(false);
                chip.setClickable(false);
                r.cgResumenServicios.addView(chip);
            }
        }
        r.tvResumenSoloContactos.setVisibility(elegidos.isEmpty() ? View.VISIBLE : View.GONE);

        MapaOsm mapaResumen = new MapaOsm(this, r.mapResumenContainer, getLifecycle());
        mapaResumen.soloLectura(true);
        mapaResumen.centrar(lat, lng, MapaOsm.ZOOM_CALLE);
        mapaResumen.marcador(lat, lng, R.drawable.ic_place, String.valueOf(r.tvResumenLugar.getText()));

        AlertDialog dialogo = new MaterialAlertDialogBuilder(this)
                .setView(r.getRoot())
                .setOnDismissListener(d -> mapaResumen.liberar())
                .show();
        r.btnCerrarModal.setOnClickListener(v -> dialogo.dismiss());
        r.btnModalCancelar.setOnClickListener(v -> dialogo.dismiss());
        r.btnModalAceptar.setOnClickListener(v -> {
            dialogo.dismiss();
            enviar(elegidos, lugar);
        });
    }

    private void enviar(List<Long> servicios, @Nullable String lugar) {
        NuevaSolicitud s = new NuevaSolicitud();
        s.titulo = Formularios.texto(vista.etEmergencia);
        String comentarios = Formularios.texto(vista.etComentarios);
        s.descripcion = comentarios.isEmpty() ? null : comentarios;
        s.latitud = lat;
        s.longitud = lng;
        s.ubicacionNombre = lugar;
        s.servicios = servicios;

        Formularios.cargando(vista.btnEnviarSolicitud, true, R.string.inicio_enviar, R.string.inicio_enviando);
        llamada = Llamada.de(ClienteApi.servicio(ServicioSolicitudes.class).crear(s)).enviar(new Llamada.Respuestas<Solicitud>() {
            @Override
            public void alExito(Solicitud creada) {
                Formularios.cargando(vista.btnEnviarSolicitud, false, R.string.inicio_enviar, R.string.inicio_enviando);
                limpiar();
                aviso(R.string.inicio_enviada).show();
                alCrear(creada);
            }

            @Override
            public void alError(ErrorApi error) {
                Formularios.cargando(vista.btnEnviarSolicitud, false, R.string.inicio_enviar, R.string.inicio_enviando);
                if (error.getCodigo() == 401) {
                    return;
                }
                if (error.getCodigo() == 403) {
                    refrescarUsuario(); // p. ej. lo suspendieron mientras llenaba el formulario
                }
                String titulo = error.campo("titulo");
                if (titulo != null) {
                    vista.tilEmergencia.setError(titulo);
                }
                aviso(titulo != null ? titulo : error.getMessage()).show();
            }
        });
    }

    /** La tarjeta de la solicitud activa (tarea 069) la muestra al crearse. */
    protected void alCrear(Solicitud creada) {
    }

    private void refrescarUsuario() {
        Llamada.de(ClienteApi.servicio(ServicioPerfil.class).sesion()).enviar(new Llamada.Respuestas<Usuario>() {
            @Override
            public void alExito(Usuario u) {
                sesion.actualizarUsuario(u);
                aplicarSuspension(u);
            }

            @Override
            public void alError(ErrorApi error) {
            }
        });
    }

    private void limpiar() {
        vista.etEmergencia.setText(null);
        vista.etLugar.setText(null);
        vista.etComentarios.setText(null);
        vista.tilEmergencia.setError(null);
        vista.grupoServicios.clearCheck();
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
