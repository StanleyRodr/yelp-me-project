package com.yelpme.app.ui.contactos;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

/** Crear o editar un contacto. Se completa en la tarea 063. */
public class FormContactoActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "id";

    public static Intent nuevo(Context contexto) {
        return new Intent(contexto, FormContactoActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        finish();
    }
}
