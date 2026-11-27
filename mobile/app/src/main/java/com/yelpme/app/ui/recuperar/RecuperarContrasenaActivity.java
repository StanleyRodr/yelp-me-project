package com.yelpme.app.ui.recuperar;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.yelpme.app.util.Margenes;
import com.yelpme.app.databinding.ActivityRecuperarContrasenaBinding;

/** Recuperar contraseña en 3 pasos. Se conecta a la API en la tarea 051. */
public class RecuperarContrasenaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Margenes.bordeABorde(this, false);
        ActivityRecuperarContrasenaBinding vista = ActivityRecuperarContrasenaBinding.inflate(getLayoutInflater());
        setContentView(vista.getRoot());
    }
}
