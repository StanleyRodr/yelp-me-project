package com.yelpme.app.ui.registro;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.yelpme.app.databinding.ActivityRegistroBinding;

/** Registro de usuario. Se conecta a la API en la tarea 050. */
public class RegistroActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        ActivityRegistroBinding vista = ActivityRegistroBinding.inflate(getLayoutInflater());
        setContentView(vista.getRoot());

        vista.btnYaTengoCuenta.setOnClickListener(v -> finish());
    }
}
