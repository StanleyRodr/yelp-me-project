package com.yelpme.app.ui.inicio;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;

import com.google.android.material.chip.Chip;
import com.yelpme.app.R;
import com.yelpme.app.api.Catalogo;
import com.yelpme.app.api.ClienteApi;
import com.yelpme.app.api.ErrorApi;
import com.yelpme.app.api.Llamada;
import com.yelpme.app.api.ServicioPerfil;
import com.yelpme.app.databinding.ItemChipServicioBinding;
import com.yelpme.app.databinding.LayoutFormSolicitudBinding;
import com.yelpme.app.modelos.Domicilio;
import com.yelpme.app.modelos.NuevaSolicitud;
import com.yelpme.app.modelos.Servicio;
import com.yelpme.app.modelos.Solicitud;
import com.yelpme.app.modelos.Usuario;
import com.yelpme.app.ui.mapa.ElegirUbicacionActivity;
import com.yelpme.app.util.Formularios;
import com.yelpme.app.util.IconosServicio;
import com.yelpme.app.util.MapaOsm;
import com.yelpme.app.util.Sesion;
import com.yelpme.app.util.Ubicacion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Lógica de los campos de una solicitud (layout_form_solicitud): ubicación
 * (GPS, domicilio o mapa, con el respaldo del plan principal 7.5), servicios
 * del catálogo y suspensión. Lo usan Inicio (crear) y FormSolicitudActivity
 * (editar). Crear en onCreate: registra launchers.
 */
public class FormularioSolicitud {

    private static final String ESTADO_LAT = "form_lat";
    private static final String ESTADO_LNG = "form_lng";
    private static final String ESTADO_ORIGEN = "form_origen";

    private final ComponentActivity activity;
    private final LayoutFormSolicitudBinding vista;
    private final Sesion sesion;
    private final Ubicacion ubicacion;
    private final MapaOsm mapa;
    private final ActivityResultLauncher<Intent> elegirPunto;

    private List<Servicio> catalogo = Collections.emptyList();
    /** Al editar: servicios que ya tenía (un suspendido puede quitarlos, no agregar). */
    private Set<Long> originales = Collections.emptySet();
    @Nullable
    private Domicilio domicilio;
    private Double lat;
    private Double lng;
    private Ubicacion.Origen origen;
    @Nullable
    private Runnable alUbicar;

    public FormularioSolicitud(ComponentActivity activity, LayoutFormSolicitudBinding vista, Sesion sesion) {
        this.activity = activity;
        this.vista = vista;
        this.sesion = sesion;
        this.ubicacion = new Ubicacion(activity);
        this.mapa = new MapaOsm(activity, vista.mapContainer, activity.getLifecycle());
        mapa.soloLectura(true);
        this.elegirPunto = activity.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), r -> {
            Intent datos = r.getData();
            if (r.getResultCode() == ComponentActivity.RESULT_OK && datos != null) {
                ponerUbicacion(datos.getDoubleExtra(ElegirUbicacionActivity.EXTRA_LAT, 0),
                        datos.getDoubleExtra(ElegirUbicacionActivity.EXTRA_LNG, 0), Ubicacion.Origen.MAPA);
                listo();
            } else {
                alUbicar = null;
            }
        });

        vista.btnUsarDomicilio.setOnClickListener(v -> {
            if (domicilio != null) {
                ponerUbicacion(domicilio.latitud, domicilio.longitud, Ubicacion.Origen.DOMICILIO);
            }
        });
        vista.btnCambiarMapa.setOnClickListener(v -> marcarEnMapa());
        mostrarServicios();
        cargarDomicilio();
    }

    // ---------- Estado ----------

    /** Restaura el punto tras rotar; devuelve false si no había. */
    public boolean restaurar(@Nullable Bundle estado) {
        if (estado == null || !estado.containsKey(ESTADO_LAT)) {
            return false;
        }
        ponerUbicacion(estado.getDouble(ESTADO_LAT), estado.getDouble(ESTADO_LNG),
                Ubicacion.Origen.valueOf(estado.getString(ESTADO_ORIGEN)));
        return true;
    }

    public void guardar(Bundle estado) {
        if (lat != null && lng != null && origen != null) {
            estado.putDouble(ESTADO_LAT, lat);
            estado.putDouble(ESTADO_LNG, lng);
            estado.putString(ESTADO_ORIGEN, origen.name());
        }
    }

    /** Edición: campos y punto de la solicitud (origen "mapa": es el punto guardado). */
    public void precargar(Solicitud s, boolean llenarCampos) {
        originales = new HashSet<>(s.servicios);
        if (llenarCampos) {
            vista.etEmergencia.setText(s.titulo);
            vista.etLugar.setText(s.ubicacionNombre);
            vista.etComentarios.setText(s.descripcion);
            ponerUbicacion(s.latitud, s.longitud, Ubicacion.Origen.MAPA);
        }
        marcarServicios(llenarCampos);
    }

    // ---------- Ubicación ----------

    /** GPS en segundo plano; si `preguntar`, ofrece el respaldo al fallar. */
    public void buscarUbicacion(boolean preguntar) {
        vista.tvOrigen.setText(R.string.inicio_origen_buscando);
        ubicacion.obtener(new Ubicacion.Resultado() {
            @Override
            public void alObtener(double latitud, double longitud) {
                // Si ya eligió domicilio o mapa mientras se buscaba, no se pisa.
                if (origen == null || origen == Ubicacion.Origen.GPS) {
                    ponerUbicacion(latitud, longitud, Ubicacion.Origen.GPS);
                }
                listo();
            }

            @Override
            public void alFallar(Ubicacion.Motivo motivo) {
                if (origen == null) {
                    vista.tvOrigen.setText(R.string.inicio_origen_falta);
                }
                if (preguntar) {
                    preguntarRespaldo(motivo);
                }
            }
        });
    }

    /** Corre `accion` con ubicación lista: de inmediato, o tras obtenerla (o el respaldo). */
    public void conUbicacion(Runnable accion) {
        if (lat != null && lng != null) {
            accion.run();
            return;
        }
        alUbicar = accion;
        buscarUbicacion(true);
    }

    private void listo() {
        Runnable r = alUbicar;
        alUbicar = null;
        if (r != null) {
            r.run();
        }
    }

    private void preguntarRespaldo(Ubicacion.Motivo motivo) {
        DialogoSinUbicacion.mostrar(activity, motivo, domicilio, new DialogoSinUbicacion.Acciones() {
            @Override
            public void usarDomicilio(Domicilio d) {
                ponerUbicacion(d.latitud, d.longitud, Ubicacion.Origen.DOMICILIO);
                listo();
            }

            @Override
            public void reintentar() {
                buscarUbicacion(true);
            }

            @Override
            public void marcarEnMapa() {
                FormularioSolicitud.this.marcarEnMapa();
            }
        });
    }

    private void marcarEnMapa() {
        elegirPunto.launch(ElegirUbicacionActivity.intent(activity, lat, lng, activity.getString(R.string.inicio_cambiar_mapa)));
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
        mapa.marcador(latitud, longitud, R.drawable.ic_place, activity.getString(texto));
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

    private void mostrarServicios() {
        Catalogo.servicios(new Llamada.Respuestas<List<Servicio>>() {
            @Override
            public void alExito(List<Servicio> servicios) {
                catalogo = servicios;
                vista.grupoServicios.removeAllViews();
                for (Servicio s : servicios) {
                    Chip chip = ItemChipServicioBinding.inflate(activity.getLayoutInflater(), vista.grupoServicios, false).getRoot();
                    chip.setText(s.nombre);
                    chip.setChipIconResource(IconosServicio.de(s.icono));
                    chip.setTag(s.id);
                    vista.grupoServicios.addView(chip);
                }
                marcarServicios(true);
            }

            @Override
            public void alError(ErrorApi error) {
                // Sin catálogo: la solicitud puede ir solo a contactos.
            }
        });
    }

    private void marcarServicios(boolean marcarOriginales) {
        for (int i = 0; i < vista.grupoServicios.getChildCount(); i++) {
            Chip chip = (Chip) vista.grupoServicios.getChildAt(i);
            if (marcarOriginales && originales.contains((Long) chip.getTag())) {
                chip.setChecked(true);
            }
        }
        aplicarSuspension(sesion.usuario());
    }

    /** Suspendido: no puede pedir servicios nuevos; al editar puede quitar los que tenía. */
    public void aplicarSuspension(@Nullable Usuario u) {
        boolean suspendido = u != null && u.suspendido;
        vista.tvAvisoSuspendido.setVisibility(suspendido ? View.VISIBLE : View.GONE);
        for (int i = 0; i < vista.grupoServicios.getChildCount(); i++) {
            Chip chip = (Chip) vista.grupoServicios.getChildAt(i);
            boolean original = originales.contains((Long) chip.getTag());
            chip.setEnabled(!suspendido || original);
            if (suspendido && !original) {
                chip.setChecked(false);
            }
        }
    }

    /** Tras un 403 (p. ej. lo suspendieron mientras llenaba el formulario). */
    public void refrescarUsuario() {
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

    public List<Long> serviciosElegidos() {
        List<Long> ids = new ArrayList<>();
        for (int i = 0; i < vista.grupoServicios.getChildCount(); i++) {
            Chip chip = (Chip) vista.grupoServicios.getChildAt(i);
            if (chip.isChecked()) {
                ids.add((Long) chip.getTag());
            }
        }
        return ids;
    }

    /** Servicios elegidos del catálogo (para mostrarlos con ícono). */
    public List<Servicio> serviciosElegidosCatalogo() {
        List<Long> ids = serviciosElegidos();
        List<Servicio> elegidos = new ArrayList<>();
        for (Servicio s : catalogo) {
            if (ids.contains(s.id)) {
                elegidos.add(s);
            }
        }
        return elegidos;
    }

    // ---------- Datos ----------

    /** Valida "¿Qué pasa?" (la API valida el resto). */
    public boolean validar() {
        vista.tilEmergencia.setError(null);
        if (Formularios.texto(vista.etEmergencia).isEmpty()) {
            Formularios.error(vista.tilEmergencia, R.string.inicio_falta_titulo);
            vista.etEmergencia.requestFocus();
            return false;
        }
        return true;
    }

    /** Lugar escrito; si no escribió y usa su domicilio, "Domicilio: …". */
    @Nullable
    public String ubicacionNombre() {
        String lugar = Formularios.texto(vista.etLugar);
        if (!lugar.isEmpty()) {
            return lugar;
        }
        if (origen == Ubicacion.Origen.DOMICILIO && domicilio != null) {
            return activity.getString(R.string.ubicacion_prefijo_domicilio, domicilio.texto);
        }
        return null;
    }

    /** Texto del lugar para mostrar (lo escrito o el origen del punto). */
    public CharSequence lugarVisible() {
        String lugar = ubicacionNombre();
        return lugar != null ? lugar : vista.tvOrigen.getText();
    }

    public double latitud() {
        return lat;
    }

    public double longitud() {
        return lng;
    }

    /**
     * Cuerpo para POST/PUT. `vaciosComoTexto`: al editar, los opcionales vacíos
     * van como "" para borrarlos (Gson omite los null).
     */
    public NuevaSolicitud datos(boolean vaciosComoTexto) {
        NuevaSolicitud s = new NuevaSolicitud();
        s.titulo = Formularios.texto(vista.etEmergencia);
        String comentarios = Formularios.texto(vista.etComentarios);
        String lugar = ubicacionNombre();
        s.descripcion = comentarios.isEmpty() ? (vaciosComoTexto ? "" : null) : comentarios;
        s.ubicacionNombre = lugar == null ? (vaciosComoTexto ? "" : null) : lugar;
        s.latitud = lat;
        s.longitud = lng;
        s.servicios = serviciosElegidos();
        return s;
    }

    /** Error 422 de "titulo" bajo su campo; devuelve el mensaje o null. */
    @Nullable
    public String errorTitulo(ErrorApi error) {
        String titulo = error.campo("titulo");
        if (titulo != null) {
            vista.tilEmergencia.setError(titulo);
        }
        return titulo;
    }

    public void limpiar() {
        vista.etEmergencia.setText(null);
        vista.etLugar.setText(null);
        vista.etComentarios.setText(null);
        vista.tilEmergencia.setError(null);
        vista.grupoServicios.clearCheck();
    }
}
