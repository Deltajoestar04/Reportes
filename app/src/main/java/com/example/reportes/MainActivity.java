package com.example.reportes;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_PERMISSIONS = 100;
    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnCrearReporte = findViewById(R.id.btnCrearReporte);
        Button btnConsultarReporte = findViewById(R.id.btnConsultarReporte);
        Button btnContacto = findViewById(R.id.btnContacto);

        btnCrearReporte.setOnClickListener(v -> startActivity(new Intent(this, CrearReporteActivity.class)));
        btnConsultarReporte.setOnClickListener(v -> startActivity(new Intent(this, ConsultarReporteActivity.class)));
        btnContacto.setOnClickListener(v -> startActivity(new Intent(this, ContactoActivity.class)));

        if (!tienePermisos()) {
            solicitarPermisos();
        }
    }

    private boolean tienePermisos() {
        for (String permiso : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permiso) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void solicitarPermisos() {
        ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_PERMISSIONS);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS) {
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Algunas funciones pueden no estar disponibles", Toast.LENGTH_LONG).show();
                    break;
                }
            }
        }
    }
}