package com.yelpme.app.ui.registro;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.yelpme.app.R;
import com.yelpme.app.ui.inicio.InicioActivity;

/** Registro de usuario. Se conecta a la API en la tarea 050. */
public class RegistroActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registro);

        findViewById(R.id.btnSubmitRegister).setOnClickListener(v -> {
            startActivity(new Intent(this, InicioActivity.class));
            finishAffinity();
        });
    }
}
