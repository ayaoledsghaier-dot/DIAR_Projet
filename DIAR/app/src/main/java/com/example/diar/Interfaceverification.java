package com.example.diar;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.OutputStreamWriter;

public class Interfaceverification extends AppCompatActivity {

    private MaterialCardView cardUploadBox;
    private ImageView ivPreview;
    private TextView btnBack; // Corrigé en TextView pour correspondre au XML
    private MaterialButton btnVerify;
    private String userEmail;
    private Uri documentUri = null;
    private File documentFileToSend = null;

    // pour la lecture et vérification de l'attestation
    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    documentUri = result.getData().getData();
                    if (documentUri != null) {
                        try {
                            // --- VÉRIFICATION DU TYPE DE FICHIER ---
                            String mimeType = getContentResolver().getType(documentUri);
                            if (mimeType == null || mimeType.equals("application/pdf") || !mimeType.startsWith("image/")) {
                                cardUploadBox.setCardBackgroundColor(Color.parseColor("#FFCDD2")); // Rouge clair
                                Toast.makeText(this, "Les fichiers PDF et non-images ne sont pas acceptés !", Toast.LENGTH_LONG).show();
                                documentUri = null;
                                documentFileToSend = null;
                                return; // On arrête le traitement ici
                            }
                            // ----------------------------------------

                            InputStream imageStream = getContentResolver().openInputStream(documentUri);
                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inJustDecodeBounds = true;
                            BitmapFactory.decodeStream(imageStream, null, options);
                            imageStream.close();

                            int scaleFactor = 1;
                            int maxHeight = 1024;
                            int maxWidth = 1024;
                            if (options.outHeight > maxHeight || options.outWidth > maxWidth) {
                                final int heightRatio = Math.round((float) options.outHeight / (float) maxHeight);
                                final int widthRatio = Math.round((float) options.outWidth / (float) maxWidth);
                                scaleFactor = heightRatio < widthRatio ? heightRatio : widthRatio;
                            }

                            imageStream = getContentResolver().openInputStream(documentUri);
                            options.inJustDecodeBounds = false;
                            options.inSampleSize = scaleFactor;
                            Bitmap bitmap = BitmapFactory.decodeStream(imageStream, null, options);
                            imageStream.close();

                            ivPreview.setVisibility(View.VISIBLE);
                            ivPreview.setImageBitmap(bitmap);

                            documentFileToSend = new File(getCacheDir(), "attestation_temp.jpg");
                            FileOutputStream fos = new FileOutputStream(documentFileToSend);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);
                            fos.flush();
                            fos.close();

                            cardUploadBox.setCardBackgroundColor(Color.parseColor("#C8E6C9")); // Vert clair si succès
                            Toast.makeText(this, "Attestation prête !", Toast.LENGTH_SHORT).show();

                        } catch (Exception e) {
                            e.printStackTrace();
                            cardUploadBox.setCardBackgroundColor(Color.parseColor("#FFCDD2")); // Rouge clair si erreur
                            Toast.makeText(this, "Erreur lors du traitement de l'image", Toast.LENGTH_SHORT).show();
                            documentFileToSend = null;
                        }
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.interface_verif_etudiant);

        userEmail = getIntent().getStringExtra("USER_EMAIL");
        if (userEmail == null || userEmail.isEmpty()) {
            Toast.makeText(this, "Erreur : E-mail introuvable !", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        cardUploadBox = findViewById(R.id.cardUploadBox);
        ivPreview = findViewById(R.id.ivPreview);
        btnVerify = findViewById(R.id.btnVerify);
        btnBack = findViewById(R.id.btnBack);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        cardUploadBox.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            galleryLauncher.launch(intent);
        });

        btnVerify.setOnClickListener(v -> {
            if (documentFileToSend == null || !documentFileToSend.exists()) {
                cardUploadBox.setCardBackgroundColor(Color.parseColor("#FFCDD2"));
                Toast.makeText(this, "Veuillez importer votre attestation", Toast.LENGTH_SHORT).show();
                return;
            }

            ExecutorService executor = Executors.newSingleThreadExecutor();
            Handler handler = new Handler(Looper.getMainLooper());

            executor.execute(() -> {
                final boolean[] isSuccess = {false};
                final String[] serverMessage = {""};

                try {
                    URL url = new URL("http://192.168.1.22:8083/api/etudiants/verifier");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);

                    String boundary = "Boundary_" + System.currentTimeMillis();
                    conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

                    OutputStream outputStream = conn.getOutputStream();
                    PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"), true);

                    writer.append("--").append(boundary).append("\r\n");
                    writer.append("Content-Disposition: form-data; name=\"email\"\r\n\r\n");
                    writer.append(userEmail).append("\r\n");

                    writer.append("--").append(boundary).append("\r\n");
                    writer.append("Content-Disposition: form-data; name=\"carte\"; filename=\"attestation.jpg\"\r\n");
                    writer.append("Content-Type: image/jpeg\r\n\r\n");
                    writer.flush();

                    try (InputStream fileInputStream = new FileInputStream(documentFileToSend)) {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                    }
                    outputStream.flush();
                    writer.append("\r\n");
                    writer.append("--").append(boundary).append("--\r\n");
                    writer.flush();

                    int responseCode = conn.getResponseCode();
                    if (responseCode == 200) {
                        isSuccess[0] = true;
                    } else {
                        if (conn.getErrorStream() != null) {
                            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "utf-8"))) {
                                StringBuilder response = new StringBuilder();
                                String line;
                                while ((line = br.readLine()) != null) {
                                    response.append(line.trim());
                                }
                                serverMessage[0] = response.toString();
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    serverMessage[0] = "Exception : " + e.getMessage();
                }

                handler.post(() -> {
                    if (isSuccess[0]) {
                        Toast.makeText(this, "Vérification réussie avec succès !", Toast.LENGTH_LONG).show();

                        // Délai personnalisé de 2,5 secondes (2500 ms) avant redirection
                        handler.postDelayed(() -> {
                            Intent intent = new Intent(Interfaceverification.this, Interface6.class);
                            // Si vous avez besoin de passer l'email ou d'autres données à l'interface 6 :
                            // intent.putExtra("USER_EMAIL", userEmail);
                            startActivity(intent);
                            finish(); // Ferme l'activité actuelle pour ne pas y revenir en faisant "Retour"
                        }, 2500);

                    } else {
                        Toast.makeText(this, "Erreur de vérification : " + serverMessage[0], Toast.LENGTH_LONG).show();
                    }
                });
            });
        });
    }
}