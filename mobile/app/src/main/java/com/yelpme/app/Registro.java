package com.yelpme.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class Registro extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registro);

        EditText etNombre = findViewById(R.id.etNombre);
        EditText etApellidoPaterno = findViewById(R.id.etApellidoPaterno);
        EditText etApellidoMaterno = findViewById(R.id.etApellidoMaterno);
        EditText etEmail = findViewById(R.id.etEmailRegister);
        EditText etPassword = findViewById(R.id.etPasswordRegister);
        EditText etDireccion = findViewById(R.id.etDireccion);
        Button btnSubmitRegister = findViewById(R.id.btnSubmitRegister);

        SesionManager sesion = new SesionManager(this);

        btnSubmitRegister.setOnClickListener(v -> {
            String nombre = etNombre.getText().toString().trim();
            String apPaterno = etApellidoPaterno.getText().toString().trim();
            String apMaterno = etApellidoMaterno.getText().toString().trim();
            String correo = etEmail.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();
            String direccion = etDireccion.getText().toString().trim();

            if (nombre.isEmpty() || correo.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Completa al menos nombre, correo y contraseña", Toast.LENGTH_SHORT).show();
                return;
            }

            // Guarda los datos en la sesión para usarlos en toda la app
            sesion.guardarUsuario(nombre, apPaterno, apMaterno, correo, direccion);

            Toast.makeText(this, "¡Bienvenido(a), " + nombre + "!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Registro.this, SolicitudesActivity.class);
            startActivity(intent);
            finishAffinity();
        });
    }
}