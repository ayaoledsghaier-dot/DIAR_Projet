
package com.example.diar;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Interface5 extends AppCompatActivity {

    private AutoCompleteTextView spinnerGenre;
    private AutoCompleteTextView spinnerUniversite;
    private AutoCompleteTextView spinnerNiveau;
    private AutoCompleteTextView spinnerFaculte;

    private MaterialButton btnSuivant;

    private String userEmail;
    private String userName;

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private final Handler handler =
            new Handler(Looper.getMainLooper());

    private static final String BASE_URL =
            "http://192.168.1.22:8083";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.interface5);

        // Récupération de la session
        SessionManager sessionManager = new SessionManager(this);

        userEmail = sessionManager.getEmail();
        userName = sessionManager.getNom();

        if (userEmail == null || userEmail.trim().isEmpty()) {
            userEmail = getIntent().getStringExtra("USER_EMAIL");
        }

        if (userName == null || userName.trim().isEmpty()) {
            userName = getIntent().getStringExtra("USER_NAME");
        }

        // Liaison des composants XML
        spinnerGenre = findViewById(R.id.spinnerGenre);
        spinnerUniversite = findViewById(R.id.spinnerUniversite);
        spinnerNiveau = findViewById(R.id.spinnerNiveau);
        spinnerFaculte = findViewById(R.id.spinnerFaculte);

        btnSuivant = findViewById(R.id.btnSuivant);

        // Bouton retour
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Vérification email
        if (userEmail == null || userEmail.trim().isEmpty()) {
            Toast.makeText(this,
                    "Email étudiant introuvable. Reconnectez-vous.",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setupSpinners();

        btnSuivant.setOnClickListener(v -> enregistrerProfil());
    }

    // Configuration des listes
    private void setupSpinners() {

        String[] genres = {
                "Sélectionnez le genre",
                "Homme",
                "Femme"
        };

        ArrayAdapter<String> adapterGenre = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                genres
        );

        spinnerGenre.setAdapter(adapterGenre);

        String[] universites = {
                "Sélectionnez l'université",
                "Université de Sousse",
                "Université de Monastir",
                "Université de Sfax",
                "Université de Tunis"
        };

        ArrayAdapter<String> adapterUniversite = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                universites
        );

        spinnerUniversite.setAdapter(adapterUniversite);

        String[] niveaux = {
                "Sélectionnez le niveau",
                "première année",
                "deuxième année",
                "troisième année",
                "Mastère"
        };

        ArrayAdapter<String> adapterNiveau = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                niveaux
        );

        spinnerNiveau.setAdapter(adapterNiveau);

        // Faculté désactivée au début
        spinnerFaculte.setEnabled(false);
        spinnerFaculte.setText(
                "Choisissez d'abord une université", false
        );

        // Changement selon l'université
        spinnerUniversite.setOnItemClickListener(
                (parent, view, position, id) -> {

                    String universite =
                            parent.getItemAtPosition(position).toString();

                    chargerFacultes(universite);
                }
        );
    }

    // Charger les établissements selon l'université
    private void chargerFacultes(String universite) {

        String[] facultes;

        if (universite.equals("Université de Sousse")) {

            facultes = new String[]{
                    "Sélectionnez votre établissement",
                    "Faculté de Médecine de Sousse",
                    "Faculté de Droit et des Sciences Politiques de Sousse",
                    "Faculté des Lettres et des Sciences Humaines de Sousse",
                    "Faculté des Sciences Économiques et de Gestion de Sousse",
                    "Institut Supérieur de Gestion de Sousse",
                    "Institut des Hautes Études Commerciales de Sousse",
                    "Institut Supérieur de Finance et de Fiscalité de Sousse",
                    "Institut Supérieur d'Informatique et des Technologies de Communication de Hammam Sousse",
                    "Institut Supérieur du Transport et de la Logistique de Sousse",
                    "Institut Supérieur des Beaux-Arts de Sousse",
                    "École Nationale d'Ingénieurs de Sousse",
                    "École Supérieure des Sciences et Techniques de la Santé de Sousse",
                    "Institut Supérieur de Musique de Sousse",
                    "Institut Supérieur des Sciences Appliquées et de Technologie de Sousse",
                    "Institut Supérieur Agronomique de Chott Meriem",
                    "Institut Supérieur des Sciences Infirmières de Sousse",
                    "École Supérieure des Sciences et des Technologies de Hammam Sousse"
            };

            spinnerFaculte.setEnabled(true);

        } else {

            facultes = new String[]{
                    "Liste non disponible pour cette université"
            };

            spinnerFaculte.setEnabled(false);
        }

        ArrayAdapter<String> adapterFaculte = new ArrayAdapter<>(
                Interface5.this,
                android.R.layout.simple_dropdown_item_1line,
                facultes
        );

        spinnerFaculte.setAdapter(adapterFaculte);

        spinnerFaculte.setText("", false);
    }

    // Enregistrement du profil
    private void enregistrerProfil() {

        String genre = spinnerGenre.getText().toString().trim();
        String universite = spinnerUniversite.getText().toString().trim();
        String niveau = spinnerNiveau.getText().toString().trim();
        String faculte = spinnerFaculte.getText().toString().trim();

        if (genre.isEmpty() ||
                genre.equals("Sélectionnez le genre")) {

            spinnerGenre.setError("Choisissez votre genre");
            return;
        }

        if (universite.isEmpty() ||
                universite.equals("Sélectionnez l'université")) {

            spinnerUniversite.setError("Choisissez votre université");
            return;
        }

        if (niveau.isEmpty() ||
                niveau.equals("Sélectionnez le niveau")) {

            spinnerNiveau.setError("Choisissez votre niveau");
            return;
        }

        // Vérifier la faculté pour Université de Sousse
        if (universite.equals("Université de Sousse")) {

            if (faculte.isEmpty() ||
                    faculte.equals("Sélectionnez votre établissement")) {

                spinnerFaculte.setError(
                        "Choisissez votre faculté ou institut"
                );
                return;
            }
        } else {
            faculte = "";
        }

        if (userEmail == null || userEmail.trim().isEmpty()) {
            Toast.makeText(this,
                    "Email étudiant manquant",
                    Toast.LENGTH_LONG).show();
            return;
        }

        btnSuivant.setEnabled(false);
        btnSuivant.setText("Enregistrement...");

        final String faculteFinale = faculte;

        executor.execute(() -> {

            HttpURLConnection conn = null;
            String serverMessage = "";

            try {

                URL url = new URL(
                        BASE_URL + "/api/etudiants/profil"
                );

                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);

                String boundary =
                        "Boundary_" + System.currentTimeMillis();

                conn.setRequestProperty(
                        "Content-Type",
                        "multipart/form-data; boundary=" + boundary
                );

                try (OutputStream outputStream = conn.getOutputStream();
                     PrintWriter writer = new PrintWriter(
                             new java.io.OutputStreamWriter(
                                     outputStream,
                                     StandardCharsets.UTF_8
                             ),
                             true
                     )) {

                    ajouterChamp(writer, boundary, "email", userEmail);
                    ajouterChamp(writer, boundary, "genre", genre);
                    ajouterChamp(writer, boundary, "universite", universite);
                    ajouterChamp(writer, boundary, "niveau", niveau);
                    ajouterChamp(writer, boundary, "faculte", faculteFinale);

                    writer.append("--")
                            .append(boundary)
                            .append("--\r\n");

                    writer.flush();
                }

                int responseCode = conn.getResponseCode();

                InputStream responseStream;

                if (responseCode >= 200 && responseCode < 300) {
                    responseStream = conn.getInputStream();
                } else {
                    responseStream = conn.getErrorStream();
                }

                if (responseStream != null) {

                    StringBuilder response = new StringBuilder();

                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(
                                    responseStream,
                                    StandardCharsets.UTF_8
                            ))) {

                        String line;

                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                    }

                    serverMessage = response.toString();
                }

                final boolean success =
                        responseCode >= 200 && responseCode < 300;

                final String finalMessage = serverMessage;

                handler.post(() -> {

                    btnSuivant.setEnabled(true);
                    btnSuivant.setText("Suivant");

                    if (success) {

                        Toast.makeText(
                                Interface5.this,
                                "Profil enregistré avec succès !",
                                Toast.LENGTH_LONG
                        ).show();

                        Intent intent = new Intent(
                                Interface5.this,
                                Interface6.class
                        );

                        intent.putExtra("USER_EMAIL", userEmail);
                        intent.putExtra("USER_NAME", userName);

                        startActivity(intent);
                        finish();

                    } else {

                        Log.e("Interface5",
                                "Erreur serveur : " + finalMessage);

                        Toast.makeText(
                                Interface5.this,
                                "Erreur d'enregistrement : " + finalMessage,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });

            } catch (Exception e) {

                Log.e("Interface5", "Erreur connexion", e);

                handler.post(() -> {

                    btnSuivant.setEnabled(true);
                    btnSuivant.setText("Suivant");

                    Toast.makeText(
                            Interface5.this,
                            "Impossible de contacter le serveur : "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });

            } finally {

                if (conn != null) {
                    conn.disconnect();
                }
            }
        });
    }

    // Ajouter un champ multipart
    private void ajouterChamp(
            PrintWriter writer,
            String boundary,
            String nom,
            String valeur
    ) throws Exception {

        writer.append("--")
                .append(boundary)
                .append("\r\n");

        writer.append(
                        "Content-Disposition: form-data; name=\""
                )
                .append(nom)
                .append("\"\r\n\r\n");

        writer.append(valeur != null ? valeur : "")
                .append("\r\n");

        writer.flush();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
        handler.removeCallbacksAndMessages(null);
    }
}
