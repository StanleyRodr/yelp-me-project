package com.yelpme.app.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ErrorApiTest {

    @Test
    public void parseaErroresPorCampo() {
        String cuerpo = "{\"ok\":false,\"mensaje\":\"Los datos no son válidos.\","
                + "\"errores\":{\"telefono\":[\"El teléfono ya está registrado.\"]}}";

        ErrorApi error = ErrorApi.desdeCuerpo(422, cuerpo, "genérico");

        assertEquals(422, error.getCodigo());
        assertTrue(error.esValidacion());
        assertEquals("Los datos no son válidos.", error.getMessage());
        assertEquals("El teléfono ya está registrado.", error.campo("telefono"));
        assertNull(error.campo("correo"));
    }

    @Test
    public void cuerpoQueNoEsJsonUsaElMensajePorDefecto() {
        ErrorApi error = ErrorApi.desdeCuerpo(502, "<html>Bad gateway</html>", "genérico");

        assertEquals("genérico", error.getMessage());
        assertNull(error.campo("telefono"));
    }

    @Test
    public void sinErroresDeCampo() {
        ErrorApi error = ErrorApi.desdeCuerpo(401, "{\"ok\":false,\"mensaje\":\"No autenticado.\"}", "genérico");

        assertEquals("No autenticado.", error.getMessage());
        assertTrue(error.getErrores().isEmpty());
    }
}
