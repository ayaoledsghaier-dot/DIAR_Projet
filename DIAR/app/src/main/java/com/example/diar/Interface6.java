package com.example.diar;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.checkbox.MaterialCheckBox;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Interface6 extends AppCompatActivity {

    // Vues UI
    private ImageButton btnBack;
    private EditText etBudget;
    private MaterialButton btnEnregistrer;

    // Cartes Type de logement (Aucun par défaut)
    private MaterialCardView cardStudio, cardAppartement, cardChambre, cardMaison;
    private String selectedTypeLogement = null;

    // Cartes Location (Aucun par défaut)
    private MaterialCardView cardIndividuelle, cardColocation;
    private String selectedLocation = null;

    // Checkboxes Équipements
    private MaterialCheckBox cbWifi, cbClim, cbChauffage, cbCuisine, cbMachineLaver, cbParking, cbSalleDeBain;

    // Quartiers (Chips) (Aucun par défaut)
    private TextView chipSahloul, chipKhezema, chipBouhdima;
    private String selectedQuartier = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.interface6);

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        etBudget = findViewById(R.id.etBudget);
        btnEnregistrer = findViewById(R.id.btnEnregistrer);

        // Types de logement
        cardStudio = findViewById(R.id.cardStudio);
        cardAppartement = findViewById(R.id.cardAppartement);
        cardChambre = findViewById(R.id.cardChambre);
        cardMaison = findViewById(R.id.cardMaison);

        // Location
        cardIndividuelle = findViewById(R.id.cardIndividuelle);
        cardColocation = findViewById(R.id.cardColocation);

        // Équipements
        cbWifi = findViewById(R.id.cbWifi);
        cbClim = findViewById(R.id.cbClim);
        cbChauffage = findViewById(R.id.cbChauffage);
        cbCuisine = findViewById(R.id.cbCuisine);
        cbMachineLaver = findViewById(R.id.cbMachineLaver);
        cbParking = findViewById(R.id.cbParking);
        cbSalleDeBain = findViewById(R.id.cbSalleDeBain);

        // Quartiers
        chipSahloul = findViewById(R.id.chipSahloul);
        chipKhezema = findViewById(R.id.chipKhezema);
        chipBouhdima = findViewById(R.id.chipBouhdima);
    }

    private void setupListeners() {
        // Bouton retour
        btnBack.setOnClickListener(v -> finish());

        // Sélection Type de Logement
        cardStudio.setOnClickListener(v -> selectTypeLogement(cardStudio, "Studio"));
        cardAppartement.setOnClickListener(v -> selectTypeLogement(cardAppartement, "Appartement"));
        cardChambre.setOnClickListener(v -> selectTypeLogement(cardChambre, "Chambre"));
        cardMaison.setOnClickListener(v -> selectTypeLogement(cardMaison, "Maison"));

        // Sélection Location
        cardIndividuelle.setOnClickListener(v -> selectLocationType(cardIndividuelle, "Individuelle"));
        cardColocation.setOnClickListener(v -> selectLocationType(cardColocation, "Colocation"));

        // Sélection Quartiers
        chipSahloul.setOnClickListener(v -> selectQuartier(chipSahloul, "Sahloul"));
        chipKhezema.setOnClickListener(v -> selectQuartier(chipKhezema, "Khezema"));
        chipBouhdima.setOnClickListener(v -> selectQuartier(chipBouhdima, "Bouhdima"));

        // Bouton Enregistrer
        btnEnregistrer.setOnClickListener(v -> validerEtEnvoyer());
    }

    // Gestion visuelle de la sélection des types de logement (Bordure verte + fond coloré)
    private void selectTypeLogement(MaterialCardView selectedCard, String type) {
        selectedTypeLogement = type;
        MaterialCardView[] cards = {cardStudio, cardAppartement, cardChambre, cardMaison};
        for (MaterialCardView card : cards) {
            if (card == selectedCard) {
                int strokeWidth = getResources().getDisplayMetrics().density == 0 ? 1 : (int)(1 * getResources().getDisplayMetrics().density);
                card.setStrokeWidth(strokeWidth);
                card.setStrokeColor(android.graphics.Color.parseColor("#86EFAC")); // Vert assorti au XML
                card.setCardBackgroundColor(android.graphics.Color.parseColor("#E6F4EA"));
            } else {
                card.setStrokeWidth(0);
                card.setCardBackgroundColor(android.graphics.Color.parseColor("#F8FAFC"));
            }
        }
    }

    // Gestion visuelle de la sélection du type de location (Bordure verte + fond coloré)
    private void selectLocationType(MaterialCardView selectedCard, String location) {
        selectedLocation = location;
        MaterialCardView[] cards = {cardIndividuelle, cardColocation};
        for (MaterialCardView card : cards) {
            if (card == selectedCard) {
                card.setStrokeWidth(1);
                card.setStrokeColor(android.graphics.Color.parseColor("#86EFAC")); // Vert assorti au XML
                card.setCardBackgroundColor(android.graphics.Color.parseColor("#E6F4EA"));
            } else {
                card.setStrokeWidth(0);
                card.setCardBackgroundColor(android.graphics.Color.parseColor("#F8FAFC"));
            }
        }
    }

    // Gestion visuelle de la sélection des quartiers
    private void selectQuartier(TextView selectedChip, String quartier) {
        selectedQuartier = quartier;
        TextView[] chips = {chipSahloul, chipKhezema, chipBouhdima};
        for (TextView chip : chips) {
            if (chip == selectedChip) {
                chip.setBackgroundResource(R.drawable.bg_chip_selected);
                chip.setTextColor(android.graphics.Color.BLACK);
            } else {
                chip.setBackgroundResource(R.drawable.bg_chip_unselected);
                chip.setTextColor(android.graphics.Color.parseColor("#64748B"));
            }
        }
    }

    private void validerEtEnvoyer() {
        // 1. Contrôle : Type de logement obligatoire
        if (selectedTypeLogement == null) {
            Toast.makeText(this, "Veuillez sélectionner un type de logement", Toast.LENGTH_LONG).show();
            return;
        }

        // 2. Contrôle : Type de location obligatoire
        if (selectedLocation == null) {
            Toast.makeText(this, "Veuillez sélectionner un type de location", Toast.LENGTH_LONG).show();
            return;
        }

        // 3. Contrôle : Budget maximal (max 500 dinars)
        String budgetStr = etBudget.getText().toString().trim();
        if (budgetStr.isEmpty()) {
            etBudget.setError("Veuillez saisir votre budget maximal");
            etBudget.requestFocus();
            return;
        }

        double budgetMax;
        try {
            budgetMax = Double.parseDouble(budgetStr);
        } catch (NumberFormatException e) {
            etBudget.setError("Format de budget invalide");
            etBudget.requestFocus();
            return;
        }

        if (budgetMax <= 0 || budgetMax > 500.0) {
            etBudget.setError("Le budget maximal ne doit pas dépasser 500 DT");
            etBudget.requestFocus();
            Toast.makeText(this, "Le budget maximal autorisé est de 500 DT", Toast.LENGTH_LONG).show();
            return;
        }

        // 4. Collecte et contrôle des équipements (au moins un requis)
        StringBuilder equipementsList = new StringBuilder();
        if (cbWifi.isChecked()) equipementsList.append("Wi-Fi, ");
        if (cbClim.isChecked()) equipementsList.append("Climatisation, ");
        if (cbChauffage.isChecked()) equipementsList.append("Chauffage, ");
        if (cbCuisine.isChecked()) equipementsList.append("Cuisine équipée, ");
        if (cbMachineLaver.isChecked()) equipementsList.append("Machine à laver, ");
        if (cbParking.isChecked()) equipementsList.append("Parking, ");
        if (cbSalleDeBain.isChecked()) equipementsList.append("Salle de bain privée, ");

        if (equipementsList.length() == 0) {
            Toast.makeText(this, "Veuillez sélectionner au moins un équipement", Toast.LENGTH_LONG).show();
            return;
        }

        String equipementsFinal = equipementsList.toString().trim();
        if (equipementsFinal.endsWith(",")) {
            equipementsFinal = equipementsFinal.substring(0, equipementsFinal.length() - 1);
        }

        // 5. Contrôle : Quartier préféré obligatoire
        if (selectedQuartier == null) {
            Toast.makeText(this, "Veuillez sélectionner un quartier préféré", Toast.LENGTH_LONG).show();
            return;
        }

        // --- RÉCUPÉRATION DE L'E-MAIL VIA LE SESSION MANAGER ---
        SessionManager sessionManager = new SessionManager(this);
        String email = sessionManager.getEmail();

        // Sécurité de secours si la session est vide
        if (email == null || email.isEmpty()) {
            email = getIntent().getStringExtra("USER_EMAIL");
            if (email == null) {
                email = "eya@example.com";
            }
        }

        // Appel réseau en arrière-plan
        envoyerVersServeur(email, selectedTypeLogement, selectedLocation, budgetMax, equipementsFinal, selectedQuartier);
    }

    private void envoyerVersServeur(String email, String typeLogement, String location, double budget, String equipements, String quartier) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        final boolean[] success = {false};
        final String[] serveurReponse = {""};

        executor.execute(() -> {
            try {
                URL url = new URL("http://192.168.1.132:8082/api/etudiants/preferences");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setDoOutput(true);

                String params = "email=" + URLEncoder.encode(email, "UTF-8") +
                        "&typeLogement=" + URLEncoder.encode(typeLogement, "UTF-8") +
                        "&locationType=" + URLEncoder.encode(location, "UTF-8") +
                        "&budgetMax=" + budget +
                        "&equipements=" + URLEncoder.encode(equipements, "UTF-8") +
                        "&quartier=" + URLEncoder.encode(quartier, "UTF-8");

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(params.getBytes("utf-8"));
                }

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    success[0] = true;
                } else {
                    try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getErrorStream(), "utf-8"))) {
                        StringBuilder response = new StringBuilder();
                        String responseLine;
                        while ((responseLine = br.readLine()) != null) {
                            response.append(responseLine.trim());
                        }
                        serveurReponse[0] = response.toString();
                    } catch (Exception e) {
                        serveurReponse[0] = "Code HTTP : " + responseCode;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                serveurReponse[0] = "Exception : " + e.getMessage();
            }

            handler.post(() -> {
                if (success[0]) {
                    Toast.makeText(Interface6.this, "Préférences enregistrées avec succès !", Toast.LENGTH_LONG).show();

                    // --- REDIRECTION CONDITIONNELLE SELON LE CHOIX DE LOCATION ---
                    Intent intent;
                    if ("Colocation".equals(selectedLocation)) {
                        // Si Colocation -> Redirection vers l'interface profil colocataire
                        intent = new Intent(Interface6.this, Interfaceprofilecoloc.class);
                    } else {
                        // Si Individuelle -> Redirection directe vers l'accueil (Interface 7)
                        intent = new Intent(Interface6.this, Interface7.class);
                        finish(); // Ferme Interface6 pour éviter de revenir en arrière
                    }

                    intent.putExtra("USER_EMAIL", email);
                    startActivity(intent);

                } else {
                    Toast.makeText(Interface6.this, "Erreur : " + serveurReponse[0], Toast.LENGTH_LONG).show();
                }
            });
        });
    }
}