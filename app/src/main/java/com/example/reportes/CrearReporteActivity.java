package com.example.reportes;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.UUID;

public class CrearReporteActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_GALLERY = 1;
    private static final int REQUEST_IMAGE_CAMERA = 2;

    private EditText etNombre, etDescripcion;
    private Spinner spinnerColonia, spinnerTipoReporte;
    private ImageView imageView;
    private Uri imageUri = null;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_reporte);

        // Vincular vistas
        etNombre = findViewById(R.id.etNombre);
        etDescripcion = findViewById(R.id.etDescripcion);
        spinnerColonia = findViewById(R.id.spinnerColonia);
        spinnerTipoReporte = findViewById(R.id.spinnerTipoReporte);
        imageView = findViewById(R.id.imgFoto);
        Button btnAdjuntarImagen = findViewById(R.id.btnAdjuntarImagen);
        Button btnEnviarReporte = findViewById(R.id.btnEnviarReporte);

        // Configurar spinners
        ArrayAdapter<CharSequence> adapterColonias = ArrayAdapter.createFromResource(
                this, R.array.colonias, android.R.layout.simple_spinner_item);
        adapterColonias.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerColonia.setAdapter(adapterColonias);

        ArrayAdapter<CharSequence> adapterTipo = ArrayAdapter.createFromResource(
                this, R.array.tipo_reportes, android.R.layout.simple_spinner_item);
        adapterTipo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipoReporte.setAdapter(adapterTipo);

        // Botón para adjuntar imagen
        btnAdjuntarImagen.setOnClickListener(view -> {
            // Elegir imagen desde galería
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_IMAGE_GALLERY);
        });

        // Botón para enviar reporte
        btnEnviarReporte.setOnClickListener(view -> {
            if (validarCampos()) {
                // Generar ID único para el reporte
                String reporteId = generarIdUnico();

                // Mostrar el ID generado (puedes asociarlo con el reporte o almacenarlo)
                Toast.makeText(this, "Reporte enviado con ID: " + reporteId, Toast.LENGTH_SHORT).show();

                // Aquí puedes enviar el reporte a tu backend o base de datos
                // En este punto, el reporteId puede ser guardado junto con los otros datos (nombre, descripción, etc.)

                finish(); // Cierra la actividad
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_GALLERY && data != null) {
                imageUri = data.getData();
                imageView.setImageURI(imageUri);
            } else if (requestCode == REQUEST_IMAGE_CAMERA && data != null) {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                imageView.setImageBitmap(photo);
            }
        }
    }

    private boolean validarCampos() {
        boolean valido = true;

        if (etNombre.getText().toString().trim().isEmpty()) {
            etNombre.setError("Este campo es obligatorio");
            valido = false;
        }

        if (etDescripcion.getText().toString().trim().isEmpty()) {
            etDescripcion.setError("Este campo es obligatorio");
            valido = false;
        }

        return valido;
    }

    // Método para generar un ID único para el reporte
    private String generarIdUnico() {
        // Usamos UUID para generar un ID único
        return UUID.randomUUID().toString();
    }
}
