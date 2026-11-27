package com.yelpme.app.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.yelpme.app.R;
import com.yelpme.app.ui.inicio.InicioActivity;
import com.yelpme.app.ui.recuperar.RecuperarContrasenaActivity;
import com.yelpme.app.ui.registro.RegistroActivity;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        EditText etEmail = findViewById(R.id.editTextTextEmailAddress);
        EditText etPassword = findViewById(R.id.editTextTextPassword);
        Button btnIniciarSesion = findViewById(R.id.btnIniciarSesion);
        Button btnRegistrate = findViewById(R.id.btnRegistrate);
        TextView tvOlvidaste = findViewById(R.id.tvOlvidasteContrasena);

        // 1. Iniciar sesión -> Valida campos y entra a SolicitudesActivity
        btnIniciarSesion.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Ingresa tu correo y contraseña", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(LoginActivity.this, InicioActivity.class);
            startActivity(intent);
            finish();
        });

        // 2. Ir a Registro
        btnRegistrate.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegistroActivity.class);
            startActivity(intent);
        });

        // 3. Ir a Recuperar Contraseña
        tvOlvidaste.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RecuperarContrasenaActivity.class);
            startActivity(intent);
        });
    }
}