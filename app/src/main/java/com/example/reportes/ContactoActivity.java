package com.example.reportes;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class ContactoActivity extends AppCompatActivity {

    private Button btnLlamar;
    private Button btnEnviarCorreo;
    private Button btnMostrarUbicacion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacto);

        btnLlamar = findViewById(R.id.btnLlamar);
        btnEnviarCorreo = findViewById(R.id.btnEnviarCorreo);
        btnMostrarUbicacion = findViewById(R.id.btnUbicacion);

        btnLlamar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hacerLlamada();
            }
        });

        btnEnviarCorreo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enviarCorreo();
            }
        });

        btnMostrarUbicacion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarUbicacion();
            }
        });
    }

    private void hacerLlamada() {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:1234567890"));// contacoto telefonico
        startActivity(intent);
    }

    private void enviarCorreo() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("msoporte@ciudadanoapp.com")); // contacto correo
        intent.putExtra(Intent.EXTRA_SUBJECT, "Consulta Ciudadana");
        startActivity(intent);
    }

    private void mostrarUbicacion() {
        Uri location = Uri.parse("geo:19.4326,-99.1332"); // contacto uvicacion
        Intent intent = new Intent(Intent.ACTION_VIEW, location);
        startActivity(intent);
    }
}
