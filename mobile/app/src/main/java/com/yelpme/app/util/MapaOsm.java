package com.yelpme.app.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.util.Consumer;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;

import com.yelpme.app.BuildConfig;
import com.yelpme.app.R;

import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.CopyrightOverlay;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;

/**
 * Mapa de OpenStreetMap (osmdroid) dentro de un FrameLayout, atado al ciclo de
 * vida de la pantalla: onResume/onPause del mapa y onDetach al destruirse (sin
 * fugas). Se puede usar dentro de un ScrollView: al arrastrar, el scroll no
 * se lleva el gesto.
 */
public class MapaOsm implements DefaultLifecycleObserver {

    /** Misantla, Ver.: centro por defecto. */
    public static final double LAT_INICIAL = 19.9311;
    public static final double LNG_INICIAL = -96.8519;
    public static final double ZOOM_CALLE = 17;

    private final Context contexto;
    private MapView mapa;
    private Marker marcador;
    private MapEventsOverlay eventos;

    @SuppressLint("ClickableViewAccessibility")
    public MapaOsm(Context contexto, FrameLayout contenedor, Lifecycle lifecycle) {
        this.contexto = contexto;
        Configuration.getInstance().load(contexto, contexto.getSharedPreferences("osmdroid", Context.MODE_PRIVATE));
        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);

        mapa = new MapView(contexto);
        mapa.setTileSource(TileSourceFactory.MAPNIK);
        mapa.setMultiTouchControls(true);
        mapa.setTilesScaledToDpi(true);
        mapa.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
        mapa.setMinZoomLevel(4.0);
        mapa.getOverlays().add(new CopyrightOverlay(contexto));
        if (Tema.esOscuro(contexto)) {
            mapa.getOverlayManager().getTilesOverlay().setColorFilter(filtroOscuro());
        }

        // Dentro de un ScrollView: mientras el dedo está en el mapa, el padre no intercepta.
        mapa.setOnTouchListener((v, evento) -> {
            int accion = evento.getActionMasked();
            if (accion == MotionEvent.ACTION_DOWN) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
            } else if (accion == MotionEvent.ACTION_UP || accion == MotionEvent.ACTION_CANCEL) {
                v.getParent().requestDisallowInterceptTouchEvent(false);
            }
            return false;
        });

        contenedor.removeAllViews();
        contenedor.addView(mapa, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        centrar(LAT_INICIAL, LNG_INICIAL, 15);
        lifecycle.addObserver(this);
    }

    /** Igual que la web (tarea 046): brightness(0.85) contrast(1.1). Las teselas OSM no tienen versión oscura. */
    private static ColorMatrixColorFilter filtroOscuro() {
        float escala = 0.85f * 1.1f;
        float desplazamiento = 127.5f * (1 - 1.1f);
        return new ColorMatrixColorFilter(new ColorMatrix(new float[]{
                escala, 0, 0, 0, desplazamiento,
                0, escala, 0, 0, desplazamiento,
                0, 0, escala, 0, desplazamiento,
                0, 0, 0, 1, 0,
        }));
    }

    public void centrar(double lat, double lng, double zoom) {
        mapa.getController().setZoom(zoom);
        mapa.getController().setCenter(new GeoPoint(lat, lng));
    }

    /** Centro actual del mapa (selector de punto). */
    public GeoPoint centro() {
        return (GeoPoint) mapa.getMapCenter();
    }

    /** Un solo marcador: llamar otra vez lo mueve. */
    public void marcador(double lat, double lng, @DrawableRes int icono, String titulo) {
        if (marcador == null) {
            marcador = new Marker(mapa);
            marcador.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            marcador.setInfoWindow(null);
            mapa.getOverlays().add(marcador);
        }
        // Pin de 40dp (los vectores son de 24dp: se ven chicos sobre el mapa).
        int tamano = Math.round(40 * contexto.getResources().getDisplayMetrics().density);
        Drawable vector = ContextCompat.getDrawable(contexto, icono).mutate();
        DrawableCompat.setTint(vector, ContextCompat.getColor(contexto, R.color.ym_marca_texto));
        Bitmap mapaBits = Bitmap.createBitmap(tamano, tamano, Bitmap.Config.ARGB_8888);
        vector.setBounds(0, 0, tamano, tamano);
        vector.draw(new Canvas(mapaBits));
        Drawable dibujo = new BitmapDrawable(contexto.getResources(), mapaBits);
        marcador.setIcon(dibujo);
        marcador.setTitle(titulo);
        marcador.setPosition(new GeoPoint(lat, lng));
        mapa.invalidate();
    }

    /** Vista previa: sin gestos (el toque pasa al contenedor). */
    public void soloLectura(boolean soloLectura) {
        mapa.setMultiTouchControls(!soloLectura);
        mapa.setClickable(!soloLectura);
        mapa.setEnabled(!soloLectura);
        if (soloLectura) {
            mapa.setOnTouchListener((v, evento) -> true);
        }
    }

    /** Toque simple en el mapa. */
    public void alTocar(Consumer<GeoPoint> accion) {
        if (eventos != null) {
            mapa.getOverlays().remove(eventos);
        }
        eventos = new MapEventsOverlay(new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint punto) {
                accion.accept(punto);
                return true;
            }

            @Override
            public boolean longPressHelper(GeoPoint punto) {
                return false;
            }
        });
        mapa.getOverlays().add(0, eventos);
    }

    @Override
    public void onResume(@NonNull LifecycleOwner owner) {
        if (mapa != null) {
            mapa.onResume();
        }
    }

    @Override
    public void onPause(@NonNull LifecycleOwner owner) {
        if (mapa != null) {
            mapa.onPause();
        }
    }

    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {
        owner.getLifecycle().removeObserver(this);
        if (mapa != null) {
            mapa.onDetach();
            mapa = null;
        }
        marcador = null;
        eventos = null;
    }
}
