package com.yelpme.app.ui.login;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.yelpme.app.databinding.ActivityLoginBinding;
import com.yelpme.app.ui.recuperar.RecuperarContrasenaActivity;
import com.yelpme.app.ui.registro.RegistroActivity;

/** Inicio de sesión. Se conecta a la API en la tarea 050. */
public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding vista;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        vista = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(vista.getRoot());

        vista.btnRegistrate.setOnClickListener(v -> startActivity(new Intent(this, RegistroActivity.class)));
        vista.btnOlvidaste.setOnClickListener(v -> startActivity(new Intent(this, RecuperarContrasenaActivity.class)));
    }
}
