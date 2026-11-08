
package com.example.diar;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Interfacecreercompteetudiant extends AppCompatActivity {

    private EditText etNomComplet, etEmail, etPassword, etConfirmPassword;
    private MaterialButton btnCreerCompte;
    private ImageView imageView;

    private Uri selectedImageUri;
    private ActivityResultLauncher<String> galleryLauncher;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.interface_creer_compte_etudiant);

        // Association des composants
        etNomComplet = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnCreerCompte = findViewById(R.id.btnSubmit);
        imageView = findViewById(R.id.imageView);

        // Ouvrir la galerie
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        imageView.setImageURI(uri);
                        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    }
                }
        );

        // Cliquer sur l'image pour choisir une photo
        imageView.setOnClickListener(v ->
                galleryLauncher.launch("image/*")
        );

        // Bouton retour
        ImageView btnBack = findViewById(R.id.btnBack);
        // Le bouton retour est un ImageButton dans le XML
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        btnCreerCompte.setOnClickListener(v -> validerInscription());
    }

    private void validerInscription() {

        String nom = etNomComplet.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        resetFieldColors();

        // 1. Champs vides
        if (nom.isEmpty() && email.isEmpty() &&
                password.isEmpty() && confirmPassword.isEmpty()) {

            Toast.makeText(this, "Veuillez remplir tous les champs",
                    Toast.LENGTH_SHORT).show();

            setFieldError(etNomComplet);
            setFieldError(etEmail);
            setFieldError(etPassword);
            setFieldError(etConfirmPassword);
            return;
        }

        // 2. Nom complet
        if (nom.isEmpty()) {
            Toast.makeText(this, "Veuillez entrer votre nom complet",
                    Toast.LENGTH_SHORT).show();
            setFieldError(etNomComplet);
            etNomComplet.requestFocus();
            return;
        }

        int espaceCount = 0;

        for (int i = 0; i < nom.length(); i++) {
            char c = nom.charAt(i);

            if (c == ' ') {
                espaceCount++;
            } else if (!Character.isLetter(c)) {
                Toast.makeText(this,
                        "Le nom doit contenir uniquement des lettres",
                        Toast.LENGTH_LONG).show();
                setFieldError(etNomComplet);
                etNomComplet.requestFocus();
                return;
            }
        }

        if (espaceCount < 1 || espaceCount > 2) {
            Toast.makeText(this,
                    "Format incorrect ! (1 ou 2 espaces autorisés)",
                    Toast.LENGTH_LONG).show();
            setFieldError(etNomComplet);
            etNomComplet.requestFocus();
            return;
        }

        setFieldSuccess(etNomComplet);

        // 3. Email
        if (email.isEmpty()) {
            Toast.makeText(this, "Veuillez entrer votre e-mail",
                    Toast.LENGTH_SHORT).show();
            setFieldError(etEmail);
            etEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this,
                    "Format d'e-mail invalide",
                    Toast.LENGTH_LONG).show();
            setFieldError(etEmail);
            etEmail.requestFocus();
            return;
        }

        setFieldSuccess(etEmail);

        // 4. Mot de passe
        if (password.isEmpty()) {
            Toast.makeText(this, "Veuillez entrer votre mot de passe",
                    Toast.LENGTH_SHORT).show();
            setFieldError(etPassword);
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this,
                    "Le mot de passe doit contenir au moins 6 caractères",
                    Toast.LENGTH_LONG).show();
            setFieldError(etPassword);
            etPassword.requestFocus();
            return;
        }

        int chiffreCount = 0;

        for (int i = 0; i < password.length(); i++) {
            if (Character.isDigit(password.charAt(i))) {
                chiffreCount++;
            }
        }

        if (chiffreCount < 3) {
            Toast.makeText(this,
                    "Le mot de passe doit contenir au moins 3 chiffres",
                    Toast.LENGTH_LONG).show();
            setFieldError(etPassword);
            etPassword.requestFocus();
            return;
        }

        setFieldSuccess(etPassword);

        // 5. Confirmation
        if (confirmPassword.isEmpty()) {
            Toast.makeText(this,
                    "Veuillez confirmer votre mot de passe",
                    Toast.LENGTH_SHORT).show();
            setFieldError(etConfirmPassword);
            etConfirmPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this,
                    "Les mots de passe ne correspondent pas",
                    Toast.LENGTH_LONG).show();
            setFieldError(etConfirmPassword);
            etConfirmPassword.requestFocus();
            return;
        }

        setFieldSuccess(etConfirmPassword);

        // 6. Photo de profil
        if (selectedImageUri == null) {
            Toast.makeText(this,
                    "Veuillez choisir une photo de profil",
                    Toast.LENGTH_LONG).show();
            return;
        }

        // 7. Appel API
        btnCreerCompte.setEnabled(false);

        String emailClean = email.toLowerCase();

        executor.execute(() -> {

            boolean isSuccess = false;
            String serverMessage = "";

            HttpURLConnection conn = null;

            try {
                URL url = new URL("http://192.168.1.22:8083/api/etudiants");


                conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("POST");
                conn.setRequestProperty(
                        "Content-Type", "application/json; charset=UTF-8"
                );
                conn.setRequestProperty("Accept", "application/json");
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);
                conn.setDoOutput(true);

                String jsonInputString =
                        "{\"nomComplet\":\"" + nom +
                                "\",\"email\":\"" + emailClean +
                                "\",\"motDePasse\":\"" + password + "\"}";

                byte[] input = jsonInputString.getBytes("UTF-8");

                conn.setFixedLengthStreamingMode(input.length);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(input);
                    os.flush();
                }

                int responseCode = conn.getResponseCode();

                BufferedReader br;

                if (responseCode >= 200 && responseCode < 300) {
                    isSuccess = true;
                    br = new BufferedReader(
                            new InputStreamReader(conn.getInputStream(), "UTF-8")
                    );
                } else {
                    br = new BufferedReader(
                            new InputStreamReader(conn.getErrorStream(), "UTF-8")
                    );
                }

                if (br != null) {
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }

                    serverMessage = response.toString();
                    br.close();
                }

                Log.d("InscriptionEtudiant",
                        "Code: " + responseCode + " | " + serverMessage);

            } catch (Exception e) {
                Log.e("InscriptionEtudiant", "Erreur API", e);
                serverMessage = e.getMessage() != null
                        ? e.getMessage() : "Erreur de connexion";
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }

            final boolean success = isSuccess;
            final String message = serverMessage;

            handler.post(() -> {

                btnCreerCompte.setEnabled(true);

                if (success) {

                    SessionManager sessionManager = new SessionManager(this);
                    sessionManager.sauvegarderSession(emailClean, nom);

                    Toast.makeText(this,
                            "Compte créé avec succès !",
                            Toast.LENGTH_LONG).show();

                    handler.postDelayed(() -> {

                        Intent intent = new Intent(
                                Interfacecreercompteetudiant.this,
                                Interface5.class
                        );

                        intent.putExtra("USER_EMAIL", emailClean);
                        intent.putExtra("USER_NAME", nom);

                        startActivity(intent);
                        finish();

                    }, 2500);

                } else {

                    Toast.makeText(this,
                            "Erreur : " + message,
                            Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    // Couleur rouge en cas d'erreur
    private void setFieldError(EditText editText) {
        if (editText != null) {
            editText.setBackgroundColor(Color.parseColor("#FFCDD2"));
        }
    }

    // Couleur verte en cas de succès
    private void setFieldSuccess(EditText editText) {
        if (editText != null) {
            editText.setBackgroundColor(Color.parseColor("#C8E6C9"));
        }
    }

    // Réinitialisation des couleurs
    private void resetFieldColors() {
        setNeutralField(etNomComplet);
        setNeutralField(etEmail);
        setNeutralField(etPassword);
        setNeutralField(etConfirmPassword);
    }

    private void setNeutralField(EditText editText) {
        if (editText != null) {
            editText.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
