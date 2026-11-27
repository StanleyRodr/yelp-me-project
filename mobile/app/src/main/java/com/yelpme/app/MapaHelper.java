package com.yelpme.app;

import android.content.Context;
import android.preference.PreferenceManager;
import android.widget.FrameLayout;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class MapaHelper {

    // Coordenadas de Misantla, Veracruz
    public static final double LAT_DEFAULT = 19.9311;
    public static final double LON_DEFAULT = -96.8519;

    public static MapView cargarMapaEnContenedor(Context context, FrameLayout contenedor, double latitud, double longitud, String tituloPin) {
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));
        Configuration.getInstance().setUserAgentValue(context.getPackageName());

        contenedor.removeAllViews();

        MapView mapView = new MapView(context);
        mapView.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));

        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);

        GeoPoint punto = new GeoPoint(latitud, longitud);
        mapView.getController().setZoom(16.0);
        mapView.getController().setCenter(punto);

        Marker marcador = new Marker(mapView);
        marcador.setPosition(punto);
        marcador.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marcador.setTitle(tituloPin);
        mapView.getOverlays().add(marcador);

        contenedor.addView(mapView);
        return mapView;
    }
}