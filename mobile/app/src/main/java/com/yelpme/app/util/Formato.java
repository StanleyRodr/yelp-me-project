package com.yelpme.app.util;

import androidx.annotation.Nullable;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

/** Formatos de pantalla para datos de la API. */
public final class Formato {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.forLanguageTag("es-MX"));
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", Locale.forLanguageTag("es-MX"));

    private Formato() {
    }

    /** ISO 8601 de la API → "26 nov 2026" (zona del teléfono). "" si no hay fecha. */
    public static String fecha(@Nullable String iso) {
        if (iso == null || iso.isEmpty()) {
            return "";
        }
        try {
            return OffsetDateTime.parse(iso).atZoneSameInstant(ZoneId.systemDefault()).format(FECHA).replace(".", "");
        } catch (DateTimeParseException e) {
            return "";
        }
    }

    /** ISO 8601 de la API → "26 nov 2026, 14:20". */
    public static String fechaHora(@Nullable String iso) {
        if (iso == null || iso.isEmpty()) {
            return "";
        }
        try {
            return OffsetDateTime.parse(iso).atZoneSameInstant(ZoneId.systemDefault()).format(FECHA_HORA).replace(".", "");
        } catch (DateTimeParseException e) {
            return "";
        }
    }
}
