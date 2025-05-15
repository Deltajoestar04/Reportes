package com.example.reportes;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.io.ByteArrayOutputStream;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CrearReporteActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_GALLERY = 1;
    private EditText etNombre, etDescripcion, etDireccion, etCelular, etCorreo;
    private Spinner spinnerColonia, spinnerTipoReporte;
    private ImageView imageView;
    private Uri imageUri = null;
    private ProgressDialog progressDialog;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_reporte);

        // Inicializar vistas
        initViews();

        // Configurar spinners
        setupSpinners();

        // Configurar botones
        setupButtons();
    }

    private void initViews() {
        etNombre = findViewById(R.id.etNombre);
        etDescripcion = findViewById(R.id.etDescripcion);
        etDireccion = findViewById(R.id.etDireccion);
        etCelular = findViewById(R.id.etCelular);
        etCorreo = findViewById(R.id.etCorreo);
        spinnerColonia = findViewById(R.id.spinnerColonia);
        spinnerTipoReporte = findViewById(R.id.spinnerTipoReporte);
        imageView = findViewById(R.id.imgFoto);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Enviando reporte...");
        progressDialog.setCancelable(false);
    }

    private void setupSpinners() {
        ArrayAdapter<CharSequence> adapterColonias = ArrayAdapter.createFromResource(
                this, R.array.colonias, android.R.layout.simple_spinner_item);
        adapterColonias.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerColonia.setAdapter(adapterColonias);

        ArrayAdapter<CharSequence> adapterTipo = ArrayAdapter.createFromResource(
                this, R.array.tipo_reportes, android.R.layout.simple_spinner_item);
        adapterTipo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipoReporte.setAdapter(adapterTipo);
    }

    private void setupButtons() {
        Button btnAdjuntarImagen = findViewById(R.id.btnAdjuntarImagen);
        Button btnEnviarReporte = findViewById(R.id.btnEnviarReporte);

        btnAdjuntarImagen.setOnClickListener(v -> abrirGaleria());
        btnEnviarReporte.setOnClickListener(v -> enviarReporte());
    }

    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_IMAGE_GALLERY && data != null) {
            imageUri = data.getData();
            imageView.setImageURI(imageUri);
        }
    }

    private String convertirImagenABase64(Uri uri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
            byte[] bytes = stream.toByteArray();
            return Base64.encodeToString(bytes, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean validarCampos() {
        boolean valido = true;
        if (etNombre.getText().toString().trim().isEmpty()) {
            etNombre.setError("Nombre obligatorio");
            valido = false;
        }
        if (etDescripcion.getText().toString().trim().isEmpty()) {
            etDescripcion.setError("Descripción obligatoria");
            valido = false;
        }
        if (etDireccion.getText().toString().trim().isEmpty()) {
            etDireccion.setError("Dirección obligatoria");
            valido = false;
        }
        if (etCelular.getText().toString().trim().isEmpty()) {
            etCelular.setError("Celular obligatorio");
            valido = false;
        }
        if (etCorreo.getText().toString().trim().isEmpty()) {
            etCorreo.setError("Correo obligatorio");
            valido = false;
        }
        return valido;
    }

    private void enviarReporte() {
        if (!validarCampos()) return;

        progressDialog.show();

        String imagenBase64 = (imageUri != null) ? convertirImagenABase64(imageUri) : "";

        Reporte reporte = new Reporte(
                "", // El ID se generará automáticamente
                etNombre.getText().toString(),
                etDescripcion.getText().toString(),
                etDireccion.getText().toString(),
                etCelular.getText().toString(),
                etCorreo.getText().toString(),
                spinnerColonia.getSelectedItem().toString(),
                spinnerTipoReporte.getSelectedItem().toString(),
                imagenBase64
        );

        FirebaseDatabaseManager dbManager = new FirebaseDatabaseManager();
        dbManager.enviarReporte(reporte, new FirebaseDatabaseManager.DatabaseCallback() {
            @Override
            public void onSuccess(String reportId) {
                progressDialog.dismiss();
                mostrarDialogoIdReporte(reportId);
            }

            @Override
            public void onError(String errorMessage) {
                progressDialog.dismiss();
                mostrarMensaje("Error al enviar: " + errorMessage);
            }
        });
    }
    private void mostrarDialogoIdReporte(String reporteId) {
        // Inflar el layout del diálogo personalizado
        View dialogView = LayoutInflater.from(this).inflate(R.layout.activity_dialogo_id_reporte, null);

        // Configurar los elementos del diálogo
        TextView tvIdReporte = dialogView.findViewById(R.id.tvIdReporte);
        Button btnCopiar = dialogView.findViewById(R.id.btnCopiar);
        Button btnAceptar = dialogView.findViewById(R.id.btnAceptar);

        tvIdReporte.setText(reporteId);

        // Crear el diálogo
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        builder.setCancelable(false);

        final AlertDialog dialog = builder.create();

        // Configurar botón copiar
        btnCopiar.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("ID Reporte", reporteId);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(CrearReporteActivity.this, "ID copiado al portapapeles", Toast.LENGTH_SHORT).show();
        });

        // Configurar botón aceptar
        btnAceptar.setOnClickListener(v -> {
            dialog.dismiss();
            finish();
        });

        dialog.show();
    }

    private void mostrarMensaje(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
    }
}