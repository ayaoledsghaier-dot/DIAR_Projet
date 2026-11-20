package com.example.diar;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Interfacecreercompteetudiant extends AppCompatActivity {

    private TextInputEditText etFullName;
    private NonAutofillEditText etEmail; // <-- ON GARDE TA CLASSE ICI
    private TextInputEditText etPassword, etConfirmPassword;
    private MaterialButton btnSubmit;
    private ImageButton btnBack;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.interface_creer_compte_etudiant);

        // Liaison XML
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        btnSubmit.setOnClickListener(v -> verifierEtEnregistrer());
    }

    private void verifierEtEnregistrer() {
        String fullName = etFullName.getText() != null ? etFullName.getText().toString().trim() : "";
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim().toLowerCase() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
        String confirmPassword = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString().trim() : "";

        // --- Contrôles de saisie ---
        if (fullName.isEmpty()) {
            etFullName.setError("Veuillez entrer votre nom complet");
            etFullName.requestFocus();
            return;
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Veuillez entrer une adresse e-mail valide");
            etEmail.requestFocus();
            return;
        }

        if (password.isEmpty() || password.length() < 6) {
            etPassword.setError("Le mot de passe doit contenir au moins 6 caractères");
            etPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Les mots de passe ne correspondent pas");
            etConfirmPassword.requestFocus();
            return;
        }

        btnSubmit.setEnabled(false);
        btnSubmit.setText("Création en cours...");

        executor.execute(() -> {
            HttpURLConnection conn = null;
            try {
                // URL Backend : POST /api/etudiants
                URL url = new URL(Config.BASE_URL + "/api/etudiants");
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setRequestProperty("Accept", "application/json");

                // JSON avec les NOMS EXACTS attendus par le Backend
                String json = "{"
                        + "\"nomComplet\":\"" + escapeJson(fullName) + "\","
                        + "\"email\":\"" + escapeJson(email) + "\","
                        + "\"motDePasse\":\"" + escapeJson(password) + "\""
                        + "}";

                Log.d("Inscription", "JSON envoyé : " + json);
                Log.d("Inscription", "URL : " + url.toString());

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(json.getBytes(StandardCharsets.UTF_8));
                    os.flush();
                }

                int responseCode = conn.getResponseCode();
                InputStream responseStream = (responseCode >= 200 && responseCode < 300)
                        ? conn.getInputStream()
                        : conn.getErrorStream();

                StringBuilder response = new StringBuilder();
                if (responseStream != null) {
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(responseStream, StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                    }
                }

                String serverMessage = response.toString();
                boolean success = responseCode >= 200 && responseCode < 300;

                Log.d("Inscription", "Code : " + responseCode + " Réponse : " + serverMessage);

                handler.post(() -> {
                    btnSubmit.setEnabled(true);
                    btnSubmit.setText("Suivant");

                    if (success) {
                        // 🆕 C'EST LA LIGNE QUI MANQUAIT - Sinon Interface5 perd l'email
                        SessionManager sessionManager = new SessionManager(Interfacecreercompteetudiant.this);
                        sessionManager.sauvegarderSession(email, fullName);

                        Toast.makeText(Interfacecreercompteetudiant.this,
                                "Compte créé avec succès !", Toast.LENGTH_LONG).show();

                        Intent intent = new Intent(Interfacecreercompteetudiant.this, Interface5.class);
                        intent.putExtra("USER_EMAIL", email);
                        intent.putExtra("USER_NAME", fullName);
                        startActivity(intent);
                        finish();
                    } else {
                        if (responseCode == 409) {
                            etEmail.setError("Ce compte existe déjà !");
                            Toast.makeText(Interfacecreercompteetudiant.this,
                                    "Ce compte existe déjà ! Essayez de vous connecter.",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(Interfacecreercompteetudiant.this,
                                    "Erreur (" + responseCode + ") : " + serverMessage,
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });

            } catch (Exception e) {
                Log.e("Inscription", "Erreur connexion", e);
                handler.post(() -> {
                    btnSubmit.setEnabled(true);
                    btnSubmit.setText("Suivant");
                    Toast.makeText(Interfacecreercompteetudiant.this,
                            "Impossible de contacter le serveur : " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        });
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
        handler.removeCallbacksAndMessages(null);
    }
}