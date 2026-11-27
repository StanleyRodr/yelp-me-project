package com.yelpme.app.modelos;

import java.util.Objects;

/** Contacto de la agenda (PresentadorContacto). Al crear/editar solo se envían nombre, teléfono y parentesco. */
public class Contacto {

    public long id;
    public String nombre;
    public String telefono;
    public String parentesco;
    /** Solo lectura: el teléfono es de una cuenta (recibe avisos). */
    public boolean vinculado;
    public boolean favorito;

    public Contacto() {
    }

    /** Cuerpo de POST/PUT. */
    public Contacto(String nombre, String telefono, String parentesco) {
        this.nombre = nombre;
        this.telefono = telefono;
        this.parentesco = parentesco;
    }

    /** Para DiffUtil: mismo contenido visible. */
    public boolean mismoContenido(Contacto otro) {
        return Objects.equals(nombre, otro.nombre)
                && Objects.equals(telefono, otro.telefono)
                && Objects.equals(parentesco, otro.parentesco)
                && vinculado == otro.vinculado
                && favorito == otro.favorito;
    }
}
