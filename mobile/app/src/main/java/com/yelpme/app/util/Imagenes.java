package com.yelpme.app.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/** Fotos de perfil: la API acepta hasta 1 MB, así que se reducen en el teléfono. */
public final class Imagenes {

    private static final int LADO_MAX = 1024;
    private static final int LIMITE_BYTES = 1024 * 1024;

    private Imagenes() {
    }

    /**
     * Lee la imagen (respeta la rotación EXIF), la reduce a 1024 px por lado y la
     * guarda como JPEG en cacheDir: calidad 80 y baja de 10 en 10 si pasa de 1 MB.
     * Llamar fuera del hilo principal.
     */
    public static File comprimir(Context contexto, Uri uri) throws IOException {
        ImageDecoder.Source origen = ImageDecoder.createSource(contexto.getContentResolver(), uri);
        Bitmap imagen = ImageDecoder.decodeBitmap(origen, (decoder, info, fuente) -> {
            int ancho = info.getSize().getWidth();
            int alto = info.getSize().getHeight();
            float escala = Math.min(1f, (float) LADO_MAX / Math.max(ancho, alto));
            decoder.setTargetSize(Math.max(1, Math.round(ancho * escala)), Math.max(1, Math.round(alto * escala)));
            decoder.setAllocator(ImageDecoder.ALLOCATOR_SOFTWARE);
        });

        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        int calidad = 80;
        do {
            salida.reset();
            imagen.compress(Bitmap.CompressFormat.JPEG, calidad, salida);
            calidad -= 10;
        } while (salida.size() > LIMITE_BYTES && calidad >= 30);
        imagen.recycle();

        File archivo = new File(contexto.getCacheDir(), "perfil.jpg");
        try (FileOutputStream f = new FileOutputStream(archivo)) {
            salida.writeTo(f);
        }
        return archivo;
    }
}
