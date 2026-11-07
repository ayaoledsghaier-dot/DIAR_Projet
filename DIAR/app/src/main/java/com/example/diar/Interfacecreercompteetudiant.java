package com.example.diar;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.Toast;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.interface_creer_compte_etudiant);

        // Association des EditText
        etNomComplet = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnCreerCompte = findViewById(R.id.btnSubmit);

        btnCreerCompte.setOnClickListener(v -> {
            String nom = etNomComplet.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword != null ? etConfirmPassword.getText().toString().trim() : "";

            // Réinitialisation des couleurs des fonds par défaut avant chaque vérification
            resetFieldColors();

            // 1. Si absolument rien n'est saisi
            if (nom.isEmpty() && email.isEmpty() && password.isEmpty() && confirmPassword.isEmpty()) {
                Toast.makeText(Interfacecreercompteetudiant.this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                setFieldError(etNomComplet);
                setFieldError(etEmail);
                setFieldError(etPassword);
                setFieldError(etConfirmPassword);
                return;
            }

            // 2. Validation du nom complet
            if (nom.isEmpty()) {
                Toast.makeText(Interfacecreercompteetudiant.this, "Veuillez entrer votre nom complet", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(Interfacecreercompteetudiant.this, "Le nom ne doit contenir que des lettres (Format : Nom Prénom)", Toast.LENGTH_LONG).show();
                    setFieldError(etNomComplet);
                    etNomComplet.requestFocus();
                    return;
                }
            }

            if (espaceCount < 1 || espaceCount > 2) {
                Toast.makeText(Interfacecreercompteetudiant.this, "Format incorrect ! (1 ou 2 espaces autorisés)", Toast.LENGTH_LONG).show();
                setFieldError(etNomComplet);
                etNomComplet.requestFocus();
                return;
            }
            setFieldSuccess(etNomComplet); // Champ valide -> Vert clair

            // 3. Validation de l'e-mail
            if (email.isEmpty()) {
                Toast.makeText(Interfacecreercompteetudiant.this, "Veuillez entrer votre e-mail", Toast.LENGTH_SHORT).show();
                setFieldError(etEmail);
                etEmail.requestFocus();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(Interfacecreercompteetudiant.this, "Format d'e-mail invalide (Exemple : nomdomaine@gmail.com)", Toast.LENGTH_LONG).show();
                setFieldError(etEmail);
                etEmail.requestFocus();
                return;
            }
            setFieldSuccess(etEmail); // Champ valide -> Vert clair

            // 4. Validation du mot de passe
            if (password.isEmpty()) {
                Toast.makeText(Interfacecreercompteetudiant.this, "Veuillez entrer votre mot de passe", Toast.LENGTH_SHORT).show();
                setFieldError(etPassword);
                etPassword.requestFocus();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(Interfacecreercompteetudiant.this, "Le mot de passe doit contenir au moins 6 caractères", Toast.LENGTH_LONG).show();
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
                Toast.makeText(Interfacecreercompteetudiant.this, "Le mot de passe doit contenir au moins 3 chiffres", Toast.LENGTH_LONG).show();
                setFieldError(etPassword);
                etPassword.requestFocus();
                return;
            }
            setFieldSuccess(etPassword); // Champ valide -> Vert clair

            // 5. Validation de la confirmation du mot de passe
            if (confirmPassword.isEmpty()) {
                Toast.makeText(Interfacecreercompteetudiant.this, "Veuillez confirmer votre mot de passe", Toast.LENGTH_SHORT).show();
                setFieldError(etConfirmPassword);
                etConfirmPassword.requestFocus();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(Interfacecreercompteetudiant.this, "Les mots de passe ne correspondent pas", Toast.LENGTH_LONG).show();
                setFieldError(etConfirmPassword);
                etConfirmPassword.requestFocus();
                return;
            }
            setFieldSuccess(etConfirmPassword); // Champ valide -> Vert clair

            // Si tout est valide, on lance l'appel réseau vers Spring Boot
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Handler handler = new Handler(Looper.getMainLooper());

            executor.execute(() -> {
                final boolean[] isSuccess = {false};
                final String[] serverMessage = {""};
                try {
                    // Correction de l'URL avec l'IP correcte et le port 8082
                    URL url = new URL("http://192.168.1.132:8082/api/etudiants");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json; utf-8");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setDoOutput(true);

                    // On envoie l'e-mail nettoyé en minuscules au serveur
                    String emailClean = email.trim().toLowerCase();
                    String jsonInputString = "{\"nomComplet\": \"" + nom + "\", \"email\": \"" + emailClean + "\", \"motDePasse\": \"" + password + "\"}";

                    // Sécurisation de la taille du flux pour éviter le "unexpected end of stream"
                    conn.setFixedLengthStreamingMode(jsonInputString.getBytes("utf-8").length);

                    try (OutputStream os = conn.getOutputStream()) {
                        byte[] input = jsonInputString.getBytes("utf-8");
                        os.write(input, 0, input.length);
                        os.flush();
                    }

                    int responseCode = conn.getResponseCode();
                    if (responseCode == 201 || responseCode == 200) {
                        isSuccess[0] = true;
                    } else {
                        if (conn.getErrorStream() != null) {
                            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "utf-8"))) {
                                StringBuilder response = new StringBuilder();
                                String responseLine;
                                while ((responseLine = br.readLine()) != null) {
                                    response.append(responseLine.trim());
                                }
                                serverMessage[0] = response.toString();
                            }
                        }
                        Log.e("ErreurAPI", "Code: " + responseCode + " | Msg: " + serverMessage[0]);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    serverMessage[0] = "Exception : " + e.getMessage();
                }

                handler.post(() -> {
                    if (isSuccess[0]) {
                        // --- SAUVEGARDE GLOBALE (E-mail propre + Nom complet) ---
                        String emailClean = email.trim().toLowerCase();
                        SessionManager sessionManager = new SessionManager(this);
                        sessionManager.sauvegarderSession(emailClean, nom);

                        Toast.makeText(this, "Compte créé avec succès !", Toast.LENGTH_LONG).show();

                        // Délai personnalisé de 2.5 secondes avant de changer d'interface
                        handler.postDelayed(() -> {
                            Intent intent = new Intent(Interfacecreercompteetudiant.this, Interface5.class);
                            intent.putExtra("USER_EMAIL", emailClean);
                            intent.putExtra("USER_NAME", nom);
                            startActivity(intent);
                            finish();
                        }, 2500);

                    } else {
                        Toast.makeText(this, "Erreur : " + serverMessage[0], Toast.LENGTH_LONG).show();
                    }
                });
            });
        });
    }

    // Colore tout le fond du champ en ROUGE clair (Erreur)
    private void setFieldError(EditText editText) {
        if (editText != null) {
            editText.setBackgroundColor(Color.parseColor("#FFCDD2")); // Rouge très clair
        }
    }

    // Colore tout le fond du champ en VERT clair (Succès)
    private void setFieldSuccess(EditText editText) {
        if (editText != null) {
            editText.setBackgroundColor(Color.parseColor("#C8E6C9")); // Vert très clair
        }
    }

    // Réinitialise le fond du champ par défaut
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
}