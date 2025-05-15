package com.example.reportes;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ConsultarReporteActivity extends AppCompatActivity {

    private static final String TAG = "ConsultarReporte";
    private EditText etReporteId;
    private TextView tvDatosReporte;
    private ProgressBar progressBar;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consultar_reporte);

        // Configuración inicial
        setupFirebase();
        initViews();
        setupButtons();

        // Verificar si se pasó un ID desde otra actividad
        checkIntentExtras();
    }

    private void setupFirebase() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("reportes");
    }

    private void initViews() {
        etReporteId = findViewById(R.id.etReporteId);
        tvDatosReporte = findViewById(R.id.tvDatosReporte);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupButtons() {
        Button btnBuscar = findViewById(R.id.btnBuscarReporte);
        Button btnPegar = findViewById(R.id.btnPegar);

        btnBuscar.setOnClickListener(v -> buscarReporte());
        btnPegar.setOnClickListener(v -> pegarDesdePortapapeles());
    }

    private void checkIntentExtras() {
        String idReporte = getIntent().getStringExtra("REPORTE_ID");
        if (idReporte != null && !idReporte.isEmpty()) {
            etReporteId.setText(idReporte);
            buscarReporte();
        }
    }

    private void buscarReporte() {
        String reporteId = etReporteId.getText().toString().trim();

        if (reporteId.isEmpty()) {
            showToast("Por favor ingrese un ID de reporte");
            return;
        }

        showLoading(true);
        Log.d(TAG, "Buscando reporte ID: " + reporteId);

        databaseReference.child(reporteId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                showLoading(false);

                if (!dataSnapshot.exists()) {
                    showError("No se encontró ningún reporte con ese ID");
                    Log.w(TAG, "Reporte no encontrado: " + reporteId);
                    return;
                }

                try {
                    Reporte reporte = dataSnapshot.getValue(Reporte.class);
                    if (reporte == null) {
                        showError("Error al interpretar los datos");
                        Log.e(TAG, "Datos nulos para ID: " + reporteId);
                        return;
                    }

                    mostrarDatosReporte(reporte);
                    Log.d(TAG, "Reporte mostrado: " + reporte.getId());

                } catch (Exception e) {
                    showError("Error al procesar los datos");
                    Log.e(TAG, "Error al parsear reporte", e);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showLoading(false);
                String errorMsg = "Error Firebase: " + databaseError.getMessage();
                showError(errorMsg);
                Log.e(TAG, errorMsg);
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void mostrarDatosReporte(Reporte reporte) {
        runOnUiThread(() -> {
            try {
                StringBuilder sb = new StringBuilder();
                sb.append("✅ Reporte encontrado\n\n");
                sb.append("🆔 ID: ").append(reporte.getId()).append("\n\n");
                sb.append("👤 Nombre: ").append(reporte.getNombre()).append("\n\n");
                sb.append("📱 Celular: ").append(reporte.getCelular()).append("\n\n");
                sb.append("✉️ Correo: ").append(reporte.getCorreo()).append("\n\n");
                sb.append("📍 Dirección: ").append(reporte.getDireccion()).append("\n\n");
                sb.append("🏘️ Colonia: ").append(reporte.getColonia()).append("\n\n");
                sb.append("🔧 Tipo: ").append(reporte.getTipo_reporte()).append("\n\n");
                sb.append("📝 Descripción: ").append(reporte.getDescripcion()).append("\n\n");
                sb.append("🖼️ Imagen: ").append(reporte.getImagen() != null ? "Adjunta" : "No disponible");

                tvDatosReporte.setText(sb.toString());
            } catch (Exception e) {
                showError("Error al mostrar datos");
                Log.e(TAG, "Error al mostrar reporte", e);
            }
        });
    }

    private void pegarDesdePortapapeles() {
        try {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null && clipboard.hasPrimaryClip()) {
                ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
                if (item.getText() != null) {
                    String text = item.getText().toString().trim();
                    if (!text.isEmpty()) {
                        etReporteId.setText(text);
                        showToast("ID pegado");
                        return;
                    }
                }
            }
            showToast("No hay contenido para pegar");
        } catch (Exception e) {
            showToast("Error al pegar");
            Log.e(TAG, "Error al pegar", e);
        }
    }

    private void showLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        tvDatosReporte.setVisibility(loading ? View.INVISIBLE : View.VISIBLE);
    }

    private void showError(String message) {
        tvDatosReporte.setText("❌ " + message);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseReference != null) {
            databaseReference.removeEventListener((ValueEventListener) this);
        }
    }
}