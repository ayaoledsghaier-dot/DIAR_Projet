package com.example.diar;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Interface5 extends AppCompatActivity {

    private EditText etNomComplet;
    private AutoCompleteTextView spinnerGenre, spinnerUniversite, spinnerNiveau;
    private MaterialButton btnSuivant;
    private ImageView ivProfilePhoto;
    private String userEmail;
    private String userName;
    private Uri imageUri = null;
    private File photoFileToSend = null;

    // Lanceur pour ouvrir la galerie du téléphone avec redimensionnement et compression sécurisés
    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    if (ivProfilePhoto != null && imageUri != null) {
                        try {
                            // 1. Lire les dimensions de l'image sans saturer la mémoire
                            InputStream imageStream = getContentResolver().openInputStream(imageUri);
                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inJustDecodeBounds = true;
                            BitmapFactory.decodeStream(imageStream, null, options);
                            imageStream.close();

                            // Calculer le facteur de réduction (inSampleSize)
                            int scaleFactor = 1;
                            int maxHeight = 1024;
                            int maxWidth = 1024;
                            if (options.outHeight > maxHeight || options.outWidth > maxWidth) {
                                final int heightRatio = Math.round((float) options.outHeight / (float) maxHeight);
                                final int widthRatio = Math.round((float) options.outWidth / (float) maxWidth);
                                scaleFactor = heightRatio < widthRatio ? heightRatio : widthRatio;
                            }

                            // 2. Décoder l'image avec le facteur de réduction calculé
                            imageStream = getContentResolver().openInputStream(imageUri);
                            options.inJustDecodeBounds = false;
                            options.inSampleSize = scaleFactor;
                            Bitmap bitmap = BitmapFactory.decodeStream(imageStream, null, options);
                            imageStream.close();

                            // Afficher l'image optimisée dans l'aperçu
                            ivProfilePhoto.setImageBitmap(bitmap);

                            // 3. Enregistrer l'image compressée dans le cache pour l'envoi vers Spring Boot
                            photoFileToSend = new File(getCacheDir(), "profil_temp.jpg");
                            FileOutputStream fos = new FileOutputStream(photoFileToSend);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);
                            fos.flush();
                            fos.close();

                            Toast.makeText(this, "Photo mise a jour !", Toast.LENGTH_SHORT).show();

                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Erreur lors du traitement de la photo", Toast.LENGTH_SHORT).show();
                            photoFileToSend = null;
                        }
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.interface5);

        // --- CORRECTION : Récupération automatique de l'e-mail et du nom via le SessionManager ---
        SessionManager sessionManager = new SessionManager(this);
        userEmail = sessionManager.getEmail();
        userName = sessionManager.getNom();

        // Association des composants XML
        etNomComplet = findViewById(R.id.etNomComplet);
        spinnerGenre = findViewById(R.id.spinnerGenre);
        spinnerUniversite = findViewById(R.id.spinnerUniversite);
        spinnerNiveau = findViewById(R.id.spinnerNiveau);
        btnSuivant = findViewById(R.id.btnSuivant);
        ivProfilePhoto = findViewById(R.id.imageView);

        // Affichage du nom complet et verrouillage du champ (fermé à la modification)
        if (userName != null && !userName.isEmpty()) {
            etNomComplet.setText(userName);
        }
        etNomComplet.setEnabled(false);

        // Remplissage automatique des listes déroulantes
        setupSpinners();

        // Au clic sur l'icône/image de profil, on ouvre la galerie
        if (ivProfilePhoto != null) {
            ivProfilePhoto.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                galleryLauncher.launch(intent);
            });
        }

        btnSuivant.setOnClickListener(v -> {
            String genre = spinnerGenre.getText() != null ? spinnerGenre.getText().toString().trim() : "";
            String universite = spinnerUniversite.getText() != null ? spinnerUniversite.getText().toString().trim() : "";
            String niveau = spinnerNiveau.getText() != null ? spinnerNiveau.getText().toString().trim() : "";

            if (genre.isEmpty() || genre.contains("Sélectionnez") ||
                    universite.isEmpty() || universite.contains("Sélectionnez") ||
                    niveau.isEmpty() || niveau.contains("Sélectionnez")) {
                Toast.makeText(this, "Veuillez sélectionner toutes les informations", Toast.LENGTH_SHORT).show();
                return;
            }

            ExecutorService executor = Executors.newSingleThreadExecutor();
            Handler handler = new Handler(Looper.getMainLooper());

            executor.execute(() -> {
                final boolean[] isSuccess = {false};
                final String[] serverMessage = {""};

                try {
                    URL url = new URL("http://192.168.1.132:8082/api/etudiants/profil");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);

                    // Utilisation du format Multipart pour envoyer du texte et un fichier image
                    String boundary = "Boundary_" + System.currentTimeMillis();
                    conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

                    OutputStream outputStream = conn.getOutputStream();
                    PrintWriter writer = new PrintWriter(new java.io.OutputStreamWriter(outputStream, "UTF-8"), true);

                    // 1. Champ email
                    writer.append("--").append(boundary).append("\r\n");
                    writer.append("Content-Disposition: form-data; name=\"email\"\r\n\r\n");
                    writer.append(userEmail != null ? userEmail : "").append("\r\n");

                    // 2. Champ genre
                    writer.append("--").append(boundary).append("\r\n");
                    writer.append("Content-Disposition: form-data; name=\"genre\"\r\n\r\n");
                    writer.append(genre).append("\r\n");

                    // 3. Champ universite
                    writer.append("--").append(boundary).append("\r\n");
                    writer.append("Content-Disposition: form-data; name=\"universite\"\r\n\r\n");
                    writer.append(universite).append("\r\n");

                    // 4. Champ niveau
                    writer.append("--").append(boundary).append("\r\n");
                    writer.append("Content-Disposition: form-data; name=\"niveau\"\r\n\r\n");
                    writer.append(niveau).append("\r\n");

                    // 5. Envoi du fichier photo sécurisé (s'il existe)
                    if (photoFileToSend != null && photoFileToSend.exists()) {
                        writer.append("--").append(boundary).append("\r\n");
                        writer.append("Content-Disposition: form-data; name=\"photo\"; filename=\"profil.jpg\"\r\n");
                        writer.append("Content-Type: image/jpeg\r\n\r\n");
                        writer.flush();

                        try (InputStream fileInputStream = new java.io.FileInputStream(photoFileToSend)) {
                            byte[] buffer = new byte[4096];
                            int bytesRead;
                            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                                outputStream.write(buffer, 0, bytesRead);
                            }
                        }
                        outputStream.flush();
                        writer.append("\r\n");
                    }

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
                        Log.e("ErreurProfil", "Code: " + responseCode + " | Msg: " + serverMessage[0]);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    serverMessage[0] = "Exception : " + e.getMessage();
                }

                handler.post(() -> {
                    if (isSuccess[0]) {
                        Toast.makeText(this, "Profil et photo mis à jour avec succès !", Toast.LENGTH_LONG).show();

                        // Délai personnalisé de 2,5 secondes avant de basculer vers l'interface de vérification
                        handler.postDelayed(() -> {
                            Intent intent = new Intent(Interface5.this, Interfaceverification.class);
                            intent.putExtra("USER_EMAIL", userEmail);
                            startActivity(intent);
                            finish();
                        }, 2500);

                    } else {
                        Toast.makeText(this, "Erreur d'enregistrement : " + serverMessage[0], Toast.LENGTH_LONG).show();
                    }
                });
            });
        });
    }

    private void setupSpinners() {
        String[] genres = {"Sélectionnez le genre", "Homme", "Femme"};
        ArrayAdapter<String> adapterGenre = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, genres);
        spinnerGenre.setAdapter(adapterGenre);

        String[] universites = {"Sélectionnez l'université", "Institut supérieur de gestion de Sousse", "Université de Monastir", "Université de Sfax", "Université de Tunis"};
        ArrayAdapter<String> adapterUniversite = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, universites);
        spinnerUniversite.setAdapter(adapterUniversite);

        String[] niveaux = {"Sélectionnez le niveau", "première année", "deuxième année", "troisième année", "Mastère"};
        ArrayAdapter<String> adapterNiveau = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, niveaux);
        spinnerNiveau.setAdapter(adapterNiveau);
    }
}