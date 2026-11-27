package com.yelpme.app.util;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.core.util.Consumer;

/**
 * Llamada directa a un contacto: ACTION_CALL con permiso CALL_PHONE; si se
 * niega, ACTION_DIAL (marcador con el número). Crear en onCreate (registra el
 * pedido de permiso). Los servicios de emergencia nunca llaman: son simulados.
 */
public class Llamadas {

    private final ComponentActivity activity;
    private final Consumer<Integer> aviso;
    private final ActivityResultLauncher<String> permiso;
    private String pendiente;

    /** `aviso` muestra un texto (R.string) si el dispositivo no puede llamar. */
    public Llamadas(ComponentActivity activity, Consumer<Integer> aviso) {
        this.activity = activity;
        this.aviso = aviso;
        this.permiso = activity.registerForActivityResult(new ActivityResultContracts.RequestPermission(), concedido -> {
            if (pendiente != null) {
                abrir(concedido ? Intent.ACTION_CALL : Intent.ACTION_DIAL, pendiente);
                pendiente = null;
            }
        });
    }

    public void llamar(String telefono) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            abrir(Intent.ACTION_CALL, telefono);
        } else {
            pendiente = telefono;
            permiso.launch(Manifest.permission.CALL_PHONE);
        }
    }

    private void abrir(String accion, String telefono) {
        try {
            activity.startActivity(new Intent(accion, Uri.parse("tel:" + telefono)));
        } catch (ActivityNotFoundException | SecurityException e) {
            aviso.accept(com.yelpme.app.R.string.llamada_no_disponible);
        }
    }
}
