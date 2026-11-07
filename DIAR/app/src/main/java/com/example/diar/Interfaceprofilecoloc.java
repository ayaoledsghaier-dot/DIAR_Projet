package com.example.diar;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageButton;
import android.widget.Toast;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Interfaceprofilecoloc extends AppCompatActivity {

    private ChipGroup chipGroupLifestyle, chipGroupRoommates, chipGroupPets;
    private MaterialButton btnSave;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.interfacecoloc); // Remplacez par le nom exact de votre fichier XML (ex: R.layout.activity_profil_colocataire)

        // 1. Liaison des vues XML
        chipGroupLifestyle = findViewById(R.id.chipGroupLifestyle);
        chipGroupRoommates = findViewById(R.id.chipGroupRoommates);
        chipGroupPets = findViewById(R.id.chipGroupPets);
        ChipGroup chipGroupProprete = findViewById(R.id.chipGroupProprete);
        btnSave = findViewById(R.id.btnSave);
        btnBack = findViewById(R.id.btnBack);

        // 2. Gestion du bouton retour
        btnBack.setOnClickListener(v -> finish());

        // 3. Gestion du clic sur le bouton d'enregistrement
        btnSave.setOnClickListener(v -> collecterEtEnvoyerDonnees());
    }

    private void collecterEtEnvoyerDonnees() {
        // Récupération de l'e-mail de l'étudiant connecté transmis par l'Intent
        String email = getIntent().getStringExtra("USER_EMAIL");
        if (email == null || email.isEmpty()) {
            Toast.makeText(this, "Erreur : E-mail introuvable", Toast.LENGTH_SHORT).show();
            return;
        }

        // --- Récupération des choix multiples (Mode de vie) ---
        StringBuilder modeDeVieBuilder = new StringBuilder();
        for (int i = 0; i < chipGroupLifestyle.getChildCount(); i++) {
            if (chipGroupLifestyle.getChildAt(i) instanceof Chip) {
                Chip chip = (Chip) chipGroupLifestyle.getChildAt(i);
                if (chip.isChecked()) {
                    if (modeDeVieBuilder.length() > 0) {
                        modeDeVieBuilder.append(", ");
                    }
                    modeDeVieBuilder.append(chip.getText().toString());
                }
            }
        }
        String modeDeVie = modeDeVieBuilder.toString();

        // --- Récupération du nombre de colocataires acceptés (Sélection unique) ---
        String nbColocataires = "";
        int selectedRoommateId = chipGroupRoommates.getCheckedChipId();
        if (selectedRoommateId != android.view.View.NO_ID) {
            Chip selectedChip = findViewById(selectedRoommateId);
            if (selectedChip != null) {
                nbColocataires = selectedChip.getText().toString();
            }
        }

        // --- Récupération de la propreté (Sélection unique) ---
        String proprete = "";
        ChipGroup chipGroupProprete = findViewById(R.id.chipGroupProprete);
        int checkedIdProprete = chipGroupProprete.getCheckedChipId();
        if (checkedIdProprete != android.view.View.NO_ID) {
            Chip selectedChip = findViewById(checkedIdProprete);
            if (selectedChip != null) {
                proprete = selectedChip.getText().toString();
            }
        }

        // --- Récupération de l'acceptation des animaux (Sélection unique) ---
        String animauxAcceptes = "";
        int selectedPetId = chipGroupPets.getCheckedChipId();
        if (selectedPetId != android.view.View.NO_ID) {
            Chip selectedPetChip = findViewById(selectedPetId);
            if (selectedPetChip != null) {
                animauxAcceptes = selectedPetChip.getText().toString();
            }
        }

        // Validation simple : s'assurer que l'utilisateur a rempli au moins un champ
        if (modeDeVie.isEmpty() && nbColocataires.isEmpty() && proprete.isEmpty() && animauxAcceptes.isEmpty()) {
            Toast.makeText(this, "Veuillez sélectionner au moins une préférence.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Envoi des données vers le serveur Spring Boot en arrière-plan (avec le paramètre proprete ajouté)
        envoyerDonneesAuServeur(email, modeDeVie, nbColocataires, proprete, animauxAcceptes);
    }

    private void envoyerDonneesAuServeur(String email, String modeDeVie, String nbColocataires, String proprete, String animauxAcceptes) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        final boolean[] success = {false};
        final String[] messageReponse = {""};

        executor.execute(() -> {
            try {
                // URL de votre endpoint Spring Boot
                URL url = new URL("http://192.168.1.132:8082/api/etudiants/profil-colocataire");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; utf-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);

                // Construction de l'objet JSON à envoyer
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("email", email);
                jsonParam.put("modeDeVie", modeDeVie);
                jsonParam.put("nbColocataires", nbColocataires);
                jsonParam.put("proprete", proprete); // Ajouté ici pour correspondre au backend
                jsonParam.put("animauxAcceptes", animauxAcceptes);

                // Écriture des données dans la requête
                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonParam.toString().getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int responseCode = conn.getResponseCode();
                if (responseCode == 200 || responseCode == 201) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    br.close();
                    success[0] = true;
                    messageReponse[0] = "Critères enregistrés avec succès !";
                } else {
                    messageReponse[0] = "Erreur serveur (Code " + responseCode + ")";
                }
            } catch (Exception e) {
                e.printStackTrace();
                messageReponse[0] = "Erreur de connexion : " + e.getMessage();
            }

            // Retour sur le thread principal pour informer l'utilisateur
            handler.post(() -> {
                Toast.makeText(Interfaceprofilecoloc.this, messageReponse[0], Toast.LENGTH_LONG).show();
                if (success[0]) {
                    // Redirection vers l'Interface 7 (Accueil) après l'enregistrement du profil coloc
                    Intent intent = new Intent(Interfaceprofilecoloc.this, Interface7.class);
                    intent.putExtra("USER_EMAIL", email);
                    startActivity(intent);
                    finish();
                }
            });
        });
    }
}