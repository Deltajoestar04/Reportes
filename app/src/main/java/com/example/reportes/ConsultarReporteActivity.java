package com.example.reportes;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
    private ImageView imgReporte;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consultar_reporte);

        etReporteId = findViewById(R.id.etReporteId);
        tvDatosReporte = findViewById(R.id.tvDatosReporte);
        progressBar = findViewById(R.id.progressBar);
        imgReporte = findViewById(R.id.imgReporte);
        Button btnBuscar = findViewById(R.id.btnBuscarReporte);
        Button btnPegar = findViewById(R.id.btnPegar);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("reportes");

        btnBuscar.setOnClickListener(v -> buscarReporte());
        btnPegar.setOnClickListener(v -> pegarDesdePortapapeles());

        String idReporte = getIntent().getStringExtra("REPORTE_ID");
        if (idReporte != null && !idReporte.isEmpty()) {
            etReporteId.setText(idReporte);
            buscarReporte();
        }
    }

    private void buscarReporte() {
        String reporteId = etReporteId.getText().toString().trim();

        if (reporteId.isEmpty()) {
            mostrarToast("Por favor ingrese un ID de reporte");
            return;
        }

        mostrarCarga(true);
        Log.d(TAG, "Buscando reporte ID: " + reporteId);

        databaseReference.child(reporteId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mostrarCarga(false);

                if (!dataSnapshot.exists()) {
                    mostrarError("No se encontró ningún reporte con ese ID");
                    Log.w(TAG, "Reporte no encontrado: " + reporteId);
                    return;
                }

                try {
                    Reporte reporte = dataSnapshot.getValue(Reporte.class);
                    if (reporte == null) {
                        mostrarError("Error al interpretar los datos del reporte");
                        Log.e(TAG, "Datos nulos para ID: " + reporteId);
                        return;
                    }

                    mostrarDatosReporte(reporte);
                    Log.d(TAG, "Reporte mostrado correctamente: " + reporte.getId());

                } catch (Exception e) {
                    mostrarError("Error al procesar los datos del reporte");
                    Log.e(TAG, "Error al parsear reporte", e);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                mostrarCarga(false);
                String errorMsg = "Error de Firebase: " + databaseError.getMessage();
                mostrarError(errorMsg);
                Log.e(TAG, errorMsg);
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void mostrarDatosReporte(Reporte reporte) {
        runOnUiThread(() -> {
            try {
                // texto del reporte
                StringBuilder sb = new StringBuilder();
                sb.append("Reporte encontrado\n\n");
                sb.append("ID: ").append(reporte.getId()).append("\n\n");
                sb.append("Nombre: ").append(reporte.getNombre()).append("\n\n");
                sb.append("Celular: ").append(reporte.getCelular()).append("\n\n");
                sb.append("Correo: ").append(reporte.getCorreo()).append("\n\n");
                sb.append("Dirección: ").append(reporte.getDireccion()).append("\n\n");
                sb.append("Colonia: ").append(reporte.getColonia()).append("\n\n");
                sb.append("Tipo de Reporte: ").append(reporte.getTipo_reporte()).append("\n\n");
                sb.append("Descripción: ").append(reporte.getDescripcion());

                tvDatosReporte.setText(sb.toString());
                tvDatosReporte.setTextColor(getResources().getColor(android.R.color.black));

                //imagen
                if (reporte.getImagen() != null && !reporte.getImagen().isEmpty()) {
                    try {
                        Bitmap bitmap = decodificarImagenBase64(reporte.getImagen());
                        if (bitmap != null) {
                            imgReporte.setImageBitmap(bitmap);
                            imgReporte.setVisibility(View.VISIBLE);
                        } else {
                            imgReporte.setVisibility(View.GONE);
                            Log.w(TAG, "La imagen no pudo ser decodificada");
                        }
                    } catch (OutOfMemoryError e) {
                        imgReporte.setVisibility(View.GONE);
                        Log.e(TAG, "Memoria insuficiente para cargar la imagen", e);
                        mostrarToast("Imagen demasiado grande para mostrar");
                    }
                } else {
                    imgReporte.setVisibility(View.GONE);
                }

            } catch (Exception e) {
                Log.e(TAG, "Error al mostrar datos", e);
                tvDatosReporte.setText("Error al formatear los datos del reporte");
            }
        });
    }

    private Bitmap decodificarImagenBase64(String base64String) {
        try {
            String base64Image = base64String.contains(",") ?
                    base64String.split(",")[1] : base64String;
            byte[] decodedBytes = Base64.decode(base64Image, Base64.DEFAULT);

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length, options);

            int scale = calcularFactorEscalado(options);

            BitmapFactory.Options scaledOptions = new BitmapFactory.Options();
            scaledOptions.inSampleSize = scale;
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length, scaledOptions);

        } catch (Exception e) {
            Log.e(TAG, "Error al decodificar imagen Base64", e);
            return null;
        }
    }

    private int calcularFactorEscalado(BitmapFactory.Options options) {
        final int REQUIRED_SIZE = 800;
        int width = options.outWidth;
        int height = options.outHeight;
        int scale = 1;
        while (width / scale / 2 >= REQUIRED_SIZE && height / scale / 2 >= REQUIRED_SIZE) {
            scale *= 2;
        }

        return scale;
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
                        mostrarToast("ID pegado desde portapapeles");
                        return;
                    }
                }
            }
            mostrarToast("No hay contenido para pegar");
        } catch (Exception e) {
            mostrarToast("Error al pegar desde portapapeles");
            Log.e(TAG, "Error al pegar", e);
        }
    }

    private void mostrarCarga(boolean mostrar) {
        progressBar.setVisibility(mostrar ? View.VISIBLE : View.GONE);
        tvDatosReporte.setVisibility(mostrar ? View.INVISIBLE : View.VISIBLE);
        imgReporte.setVisibility(View.GONE); // Ocultar imagen durante carga
    }

    private void mostrarError(String mensaje) {
        tvDatosReporte.setText("❌ " + mensaje);
        tvDatosReporte.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        imgReporte.setVisibility(View.GONE);
    }

    private void mostrarToast(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Liberar recursos
        if (imgReporte != null) {
            imgReporte.setImageBitmap(null);
        }
        if (databaseReference != null) {
            databaseReference.removeEventListener((ValueEventListener) this);
        }
    }
}