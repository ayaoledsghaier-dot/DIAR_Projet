package com.example.diar;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Interface5 extends AppCompatActivity {

    private TextInputEditText etAge;
    private AutoCompleteTextView spinnerGenre, spinnerUniversite, spinnerFac, spinnerNiveau;
    private MaterialButton btnSuivant;
    private ImageView imageView;

    private Uri imageUri = null;
    private byte[] photoBytes = null;
    private ActivityResultLauncher<String> imagePickerLauncher;

    private String userEmail, userName;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.interface5);

        SessionManager sessionManager = new SessionManager(this);
        userEmail = sessionManager.getEmail();
        userName = sessionManager.getNom();

        if (userEmail == null || userEmail.trim().isEmpty())
            userEmail = getIntent().getStringExtra("USER_EMAIL");
        if (userName == null || userName.trim().isEmpty())
            userName = getIntent().getStringExtra("USER_NAME");

        // Toujours en minuscule comme le Backend
        if (userEmail != null) userEmail = userEmail.trim().toLowerCase();

        etAge = findViewById(R.id.etAge);
        // ✅ CORRIGÉ : On utilise les champs de la classe, pas des variables locales
        spinnerGenre = findViewById(R.id.spinnerGenre);
        spinnerUniversite = findViewById(R.id.spinnerUniversite);
        spinnerFac = findViewById(R.id.spinnerFac);
        spinnerNiveau = findViewById(R.id.spinnerNiveau);

        btnSuivant = findViewById(R.id.btnSuivant);
        imageView = findViewById(R.id.imageView);

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                            Bitmap resized = redimensionner(bitmap, 800);
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            resized.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                            photoBytes = baos.toByteArray();
                            imageUri = uri;
                            imageView.setImageBitmap(resized);
                            Toast.makeText(this, "Photo sélectionnée !", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Erreur photo : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        imageView.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        if (userEmail == null || userEmail.isEmpty()) {
            Toast.makeText(this, "Email introuvable. Reconnectez-vous.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setupSpinners();
        btnSuivant.setOnClickListener(v -> enregistrerProfil());
    }

    private void setupSpinners() {
        // Genre
        ArrayAdapter<String> adapterGenre = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                getResources().getStringArray(R.array.liste_genres)
        );
        spinnerGenre.setAdapter(adapterGenre);

        // Université - Université de Sousse, Monastir...
        ArrayAdapter<String> adapterUniversite = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                getResources().getStringArray(R.array.liste_universites)
        );
        spinnerUniversite.setAdapter(adapterUniversite);

        // Niveau
        ArrayAdapter<String> adapterNiveau = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                getResources().getStringArray(R.array.liste_niveaux)
        );
        spinnerNiveau.setAdapter(adapterNiveau);

        // ✅ NOUVEAU : Fac - au début vide, on met les Facs de Sousse par défaut
        updateFacList(R.array.fac_sousse);

        // ✅ NOUVEAU : Quand tu choisis une Université, la liste Fac change toute seule
        spinnerUniversite.setOnItemClickListener((parent, view, position, id) -> {
            String universiteChoisie = parent.getItemAtPosition(position).toString();
            // On vide l'ancien choix de Fac
            spinnerFac.setText("", false);

            int facArrayId;
            if (universiteChoisie.contains("Sousse")) {
                facArrayId = R.array.fac_sousse;
            } else if (universiteChoisie.contains("Monastir")) {
                facArrayId = R.array.fac_monastir;
            } else if (universiteChoisie.contains("Tunis El Manar")) {
                facArrayId = R.array.fac_tunis_manar;
            } else if (universiteChoisie.contains("Sfax")) {
                facArrayId = R.array.fac_sfax;
            } else if (universiteChoisie.contains("Manouba")) {
                facArrayId = R.array.fac_manouba;
            } else if (universiteChoisie.contains("Carthage")) {
                facArrayId = R.array.fac_carthage;
            } else {
                facArrayId = R.array.fac_sousse;
            }
            updateFacList(facArrayId);
        });
    }

    private void updateFacList(int facArrayId) {
        ArrayAdapter<String> adapterFac = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                getResources().getStringArray(facArrayId)
        );
        spinnerFac.setAdapter(adapterFac);
    }

    private void enregistrerProfil() {
        String ageStr = etAge.getText() != null ? etAge.getText().toString().trim() : "";
        String genre = spinnerGenre.getText().toString().trim();
        String universite = spinnerUniversite.getText().toString().trim();
        String fac = spinnerFac.getText().toString().trim();
        String niveau = spinnerNiveau.getText().toString().trim();

        if (ageStr.isEmpty()) { etAge.setError("Veuillez entrer votre âge"); return; }
        int age;
        try {
            age = Integer.parseInt(ageStr);
            if (age < 18 || age > 40) { etAge.setError("Âge entre 18 et 40 ans"); return; }
        } catch (NumberFormatException e) { etAge.setError("Nombre valide"); return; }
        if (genre.isEmpty() || genre.equals("Sélectionnez le genre")) { spinnerGenre.setError("Choisissez votre genre"); return; }
        if (universite.isEmpty() || universite.equals("Sélectionnez l'université")) { spinnerUniversite.setError("Choisissez votre université"); return; }
        if (fac.isEmpty()) { spinnerFac.setError("Choisissez votre Fac / Institut"); return; }
        if (niveau.isEmpty() || niveau.equals("Sélectionnez le niveau")) { spinnerNiveau.setError("Choisissez votre niveau"); return; }

        btnSuivant.setEnabled(false);
        btnSuivant.setText("Enregistrement...");

        executor.execute(() -> {
            try {
                String reponseProfil = postProfil(genre, universite, fac, niveau, age);
                Log.d("Interface5", "Profil OK : " + reponseProfil);

                if (photoBytes != null) {
                    String reponsePhoto = postPhoto();
                    Log.d("Interface5", "Photo OK : " + reponsePhoto);
                }

                handler.post(() -> {
                    btnSuivant.setEnabled(true);
                    btnSuivant.setText("Suivant");
                    Toast.makeText(this, "Profil enregistré avec succès !", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(this, Interface6.class);
                    intent.putExtra("USER_EMAIL", userEmail);
                    intent.putExtra("USER_NAME", userName);
                    startActivity(intent);
                    finish();
                });

            } catch (Exception e) {
                Log.e("Interface5", "Erreur", e);
                handler.post(() -> {
                    btnSuivant.setEnabled(true);
                    btnSuivant.setText("Suivant");
                    Toast.makeText(this, "Erreur : " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private String postProfil(String genre, String universite, String fac, String niveau, int age) throws Exception {
        URL url = new URL(Config.BASE_URL + "/api/etudiants/profil");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(15000);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        // ✅ IMPORTANT pour garder ton 100% qui marche :
        // Le Backend compare le champ "universite". On envoie la FAC comme universite
        // (ex: ISG Sousse) pour que Yasmine et Salma restent à 100%
        // Et on envoie aussi l'université générale + la fac en plus
        String body = "email=" + URLEncoder.encode(userEmail, "UTF-8")
                + "&genre=" + URLEncoder.encode(genre, "UTF-8")
                + "&universite=" + URLEncoder.encode(fac, "UTF-8")
                + "&universiteGenerale=" + URLEncoder.encode(universite, "UTF-8")
                + "&fac=" + URLEncoder.encode(fac, "UTF-8")
                + "&niveau=" + URLEncoder.encode(niveau, "UTF-8")
                + "&age=" + age;

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }
        return lireReponse(conn);
    }

    private String postPhoto() throws Exception {
        String boundary = "Boundary_" + System.currentTimeMillis();
        URL url = new URL(Config.BASE_URL + "/api/etudiants/photo");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setConnectTimeout(20000);
        conn.setReadTimeout(20000);
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        try (OutputStream os = conn.getOutputStream()) {
            String LINE = "\r\n";
            os.write(("--" + boundary + LINE).getBytes(StandardCharsets.UTF_8));
            os.write("Content-Disposition: form-data; name=\"email\"".concat(LINE + LINE).getBytes(StandardCharsets.UTF_8));
            os.write(userEmail.getBytes(StandardCharsets.UTF_8));
            os.write(LINE.getBytes(StandardCharsets.UTF_8));
            os.write(("--" + boundary + LINE).getBytes(StandardCharsets.UTF_8));
            os.write("Content-Disposition: form-data; name=\"photo\"; filename=\"profil.jpg\"".concat(LINE).getBytes(StandardCharsets.UTF_8));
            os.write("Content-Type: image/jpeg".concat(LINE + LINE).getBytes(StandardCharsets.UTF_8));
            os.write(photoBytes);
            os.write(LINE.getBytes(StandardCharsets.UTF_8));
            os.write(("--" + boundary + "--" + LINE).getBytes(StandardCharsets.UTF_8));
            os.flush();
        }
        return lireReponse(conn);
    }

    private String lireReponse(HttpURLConnection conn) throws Exception {
        int code = conn.getResponseCode();
        InputStream stream = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
        StringBuilder sb = new StringBuilder();
        if (stream != null) {
            try (BufferedReader r = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                String line; while ((line = r.readLine()) != null) sb.append(line);
            }
        }
        conn.disconnect();
        if (code < 200 || code >= 300) throw new Exception("Code " + code + " : " + sb);
        return sb.toString();
    }

    private Bitmap redimensionner(Bitmap bitmap, int maxSize) {
        int w = bitmap.getWidth(), h = bitmap.getHeight();
        if (w <= maxSize && h <= maxSize) return bitmap;
        float ratio = (float) w / h;
        int newW, newH;
        if (ratio > 1) { newW = maxSize; newH = (int) (maxSize / ratio); }
        else { newH = maxSize; newW = (int) (maxSize * ratio); }
        return Bitmap.createScaledBitmap(bitmap, newW, newH, true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}