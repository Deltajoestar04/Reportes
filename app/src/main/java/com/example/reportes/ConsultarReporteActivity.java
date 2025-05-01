package com.example.reportes;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ConsultarReporteActivity extends AppCompatActivity {
    EditText etReporteId;
    TextView tvDatosReporte;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consultar_reporte);

        etReporteId = findViewById(R.id.etReporteId);
        tvDatosReporte = findViewById(R.id.tvDatosReporte);
        Button btnBuscar = findViewById(R.id.btnBuscarReporte);

        btnBuscar.setOnClickListener(v -> buscarReporte());
    }

    private void buscarReporte() {
        // Aquí debes hacer un consumo a un Web Service para traer información del reporte
        // Te puedo pasar ejemplo usando Volley o Retrofit
    }
}
