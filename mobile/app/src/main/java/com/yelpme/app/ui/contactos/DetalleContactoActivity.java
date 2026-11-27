package com.yelpme.app.ui.contactos;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

/** Detalle de un contacto. Se completa en la tarea 064. */
public class DetalleContactoActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "id";

    public static Intent intent(Context contexto, long id) {
        return new Intent(contexto, DetalleContactoActivity.class).putExtra(EXTRA_ID, id);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        finish();
    }
}
