
package com.example.diar;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.checkbox.MaterialCheckBox;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Interface6 extends AppCompatActivity {

    // URL Backend
    private static final String BASE_URL =
            "http://192.168.1.22:8083";

    // Vues UI
    private ImageButton btnBack;
    private EditText etBudget;
    private MaterialButton btnEnregistrer;

    // Types de logement
    private MaterialCardView cardStudio;
    private MaterialCardView cardAppartement;
    private MaterialCardView cardChambre;
    private MaterialCardView cardMaison;

    private String selectedTypeLogement = null;

    // Type de location
    private MaterialCardView cardIndividuelle;
    private MaterialCardView cardColocation;

    private String selectedLocation = null;

    // Équipements
    private MaterialCheckBox cbWifi;
    private MaterialCheckBox cbClim;
    private MaterialCheckBox cbChauffage;
    private MaterialCheckBox cbCuisine;
    private MaterialCheckBox cbMachineLaver;
    private MaterialCheckBox cbParking;
    private MaterialCheckBox cbSalleDeBain;

    // Quartiers
    private TextView chipSahloul;
    private TextView chipKhezema;
    private TextView chipBouhdima;

    private String selectedQuartier = null;

    // Email étudiant
    private String userEmail;

    // Empêcher les envois multiples
    private boolean isSending = false;

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private final Handler handler =
            new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.interface6);

        // Récupération de l'email
        SessionManager sessionManager =
                new SessionManager(this);

        userEmail = sessionManager.getEmail();

        if (userEmail == null || userEmail.trim().isEmpty()) {
            userEmail = getIntent().getStringExtra("USER_EMAIL");
        }

        if (userEmail == null || userEmail.trim().isEmpty()) {
            Toast.makeText(
                    this,
                    "Email étudiant introuvable. Reconnectez-vous.",
                    Toast.LENGTH_LONG
            ).show();

            finish();
            return;
        }

        userEmail = userEmail.trim();

        initViews();
        setupListeners();
    }

    // Initialisation des vues
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

    // Gestion des clics
    private void setupListeners() {

        btnBack.setOnClickListener(v -> finish());

        // Types de logement
        cardStudio.setOnClickListener(v ->
                selectTypeLogement(cardStudio, "Studio"));

        cardAppartement.setOnClickListener(v ->
                selectTypeLogement(cardAppartement, "Appartement"));

        cardChambre.setOnClickListener(v ->
                selectTypeLogement(cardChambre, "Chambre"));

        cardMaison.setOnClickListener(v ->
                selectTypeLogement(cardMaison, "Maison"));

        // Types de location
        cardIndividuelle.setOnClickListener(v ->
                selectLocationType(cardIndividuelle, "Individuelle"));

        cardColocation.setOnClickListener(v ->
                selectLocationType(cardColocation, "Colocation"));

        // Quartiers
        chipSahloul.setOnClickListener(v ->
                selectQuartier(chipSahloul, "Sahloul"));

        chipKhezema.setOnClickListener(v ->
                selectQuartier(chipKhezema, "Khezema"));

        chipBouhdima.setOnClickListener(v ->
                selectQuartier(chipBouhdima, "Bouhdima"));

        // Enregistrer
        btnEnregistrer.setOnClickListener(v ->
                validerEtEnvoyer());
    }

    // Sélection du type de logement
    private void selectTypeLogement(
            MaterialCardView selectedCard,
            String type) {

        selectedTypeLogement = type;

        MaterialCardView[] cards = {
                cardStudio,
                cardAppartement,
                cardChambre,
                cardMaison
        };

        for (MaterialCardView card : cards) {

            if (card == selectedCard) {

                int strokeWidth = (int)
                        (2 * getResources().getDisplayMetrics().density);

                card.setStrokeWidth(strokeWidth);

                card.setStrokeColor(
                        Color.parseColor("#86EFAC"));

                card.setCardBackgroundColor(
                        Color.parseColor("#E6F4EA"));

            } else {

                card.setStrokeWidth(0);

                card.setCardBackgroundColor(
                        Color.parseColor("#F8FAFC"));
            }
        }
    }

    // Sélection du type de location
    private void selectLocationType(
            MaterialCardView selectedCard,
            String location) {

        selectedLocation = location;

        MaterialCardView[] cards = {
                cardIndividuelle,
                cardColocation
        };

        for (MaterialCardView card : cards) {

            if (card == selectedCard) {

                card.setStrokeWidth(
                        (int) (2 * getResources()
                                .getDisplayMetrics().density));

                card.setStrokeColor(
                        Color.parseColor("#86EFAC"));

                card.setCardBackgroundColor(
                        Color.parseColor("#E6F4EA"));

            } else {

                card.setStrokeWidth(0);

                card.setCardBackgroundColor(
                        Color.parseColor("#F8FAFC"));
            }
        }
    }

    // Sélection du quartier
    private void selectQuartier(
            TextView selectedChip,
            String quartier) {

        selectedQuartier = quartier;

        TextView[] chips = {
                chipSahloul,
                chipKhezema,
                chipBouhdima
        };

        for (TextView chip : chips) {

            if (chip == selectedChip) {

                chip.setBackgroundResource(
                        R.drawable.bg_chip_selected);

                chip.setTextColor(Color.BLACK);

            } else {

                chip.setBackgroundResource(
                        R.drawable.bg_chip_unselected);

                chip.setTextColor(
                        Color.parseColor("#64748B"));
            }
        }
    }

    // Validation des données
    private void validerEtEnvoyer() {

        if (isSending) {
            return;
        }

        // Type logement
        if (selectedTypeLogement == null) {

            Toast.makeText(
                    this,
                    "Veuillez sélectionner un type de logement",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        // Type location
        if (selectedLocation == null) {

            Toast.makeText(
                    this,
                    "Veuillez sélectionner un type de location",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        // Budget
        String budgetStr =
                etBudget.getText().toString().trim();

        if (budgetStr.isEmpty()) {

            etBudget.setError(
                    "Veuillez saisir votre budget maximal");

            etBudget.requestFocus();
            return;
        }

        double budgetMax;

        try {

            budgetMax = Double.parseDouble(
                    budgetStr.replace(",", "."));

        } catch (NumberFormatException e) {

            etBudget.setError("Format de budget invalide");
            etBudget.requestFocus();
            return;
        }

        if (budgetMax <= 0 || budgetMax > 500) {

            etBudget.setError(
                    "Le budget doit être entre 1 et 500 DT");

            etBudget.requestFocus();

            Toast.makeText(
                    this,
                    "Le budget maximal autorisé est de 500 DT",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        // Équipements
        StringBuilder equipementsList =
                new StringBuilder();

        if (cbWifi.isChecked())
            equipementsList.append("Wi-Fi, ");

        if (cbClim.isChecked())
            equipementsList.append("Climatisation, ");

        if (cbChauffage.isChecked())
            equipementsList.append("Chauffage, ");

        if (cbCuisine.isChecked())
            equipementsList.append("Cuisine équipée, ");

        if (cbMachineLaver.isChecked())
            equipementsList.append("Machine à laver, ");

        if (cbParking.isChecked())
            equipementsList.append("Parking, ");

        if (cbSalleDeBain.isChecked())
            equipementsList.append("Salle de bain privée, ");

        if (equipementsList.length() == 0) {

            Toast.makeText(
                    this,
                    "Veuillez sélectionner au moins un équipement",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        String equipementsFinal =
                equipementsList.toString().trim();

        if (equipementsFinal.endsWith(",")) {

            equipementsFinal =
                    equipementsFinal.substring(
                            0,
                            equipementsFinal.length() - 1
                    ).trim();
        }

        // Quartier
        if (selectedQuartier == null) {

            Toast.makeText(
                    this,
                    "Veuillez sélectionner un quartier préféré",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        // Email
        if (userEmail == null || userEmail.trim().isEmpty()) {

            Toast.makeText(
                    this,
                    "Email étudiant introuvable",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        // Envoi vers backend
        envoyerVersServeur(
                userEmail,
                selectedTypeLogement,
                selectedLocation,
                budgetMax,
                equipementsFinal,
                selectedQuartier
        );
    }

    // Enregistrement des préférences dans Spring Boot
    private void envoyerVersServeur(
            String email,
            String typeLogement,
            String location,
            double budget,
            String equipements,
            String quartier) {

        if (isSending) return;

        isSending = true;

        btnEnregistrer.setEnabled(false);
        btnEnregistrer.setText("Enregistrement...");

        executor.execute(() -> {

            HttpURLConnection conn = null;

            boolean success = false;
            String serverMessage = "";

            try {

                URL url = new URL(
                        BASE_URL + "/api/etudiants/preferences");

                conn = (HttpURLConnection)
                        url.openConnection();

                conn.setRequestMethod("POST");

                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);

                conn.setDoOutput(true);

                conn.setRequestProperty(
                        "Content-Type",
                        "application/x-www-form-urlencoded; charset=UTF-8"
                );

                String params =
                        "email=" + URLEncoder.encode(email, "UTF-8") +
                                "&typeLogement=" + URLEncoder.encode(typeLogement, "UTF-8") +
                                "&locationType=" + URLEncoder.encode(location, "UTF-8") +
                                "&budgetMax=" + budget +
                                "&equipements=" + URLEncoder.encode(equipements, "UTF-8") +
                                "&quartier=" + URLEncoder.encode(quartier, "UTF-8");

                try (OutputStream os = conn.getOutputStream()) {

                    os.write(params.getBytes("UTF-8"));
                    os.flush();
                }

                int responseCode = conn.getResponseCode();

                InputStream responseStream;

                if (responseCode >= 200 && responseCode < 300) {

                    responseStream = conn.getInputStream();

                } else {

                    responseStream = conn.getErrorStream();
                }

                if (responseStream != null) {

                    StringBuilder response =
                            new StringBuilder();

                    try (BufferedReader reader =
                                 new BufferedReader(
                                         new InputStreamReader(
                                                 responseStream,
                                                 "UTF-8"
                                         ))) {

                        String line;

                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                    }

                    serverMessage = response.toString();
                }

                success =
                        responseCode >= 200 &&
                                responseCode < 300;

            } catch (Exception e) {

                Log.e(
                        "Interface6",
                        "Erreur connexion serveur",
                        e
                );

                serverMessage = e.getMessage();

            } finally {

                if (conn != null) {
                    conn.disconnect();
                }
            }

            final boolean finalSuccess = success;
            final String finalMessage = serverMessage;

            handler.post(() -> {

                isSending = false;

                btnEnregistrer.setEnabled(true);
                btnEnregistrer.setText("Enregistrer");

                if (finalSuccess) {

                    Toast.makeText(
                            Interface6.this,
                            "Préférences enregistrées avec succès !",
                            Toast.LENGTH_LONG
                    ).show();

                    // Ouvrir l'interface de vérification
                    Intent intent = new Intent(
                            Interface6.this,
                            Interfaceverification.class
                    );

                    intent.putExtra(
                            "USER_EMAIL",
                            email
                    );

                    startActivity(intent);

                    finish();

                } else {

                    Log.e(
                            "Interface6",
                            "Erreur serveur : " + finalMessage
                    );

                    Toast.makeText(
                            Interface6.this,
                            "Erreur : " + finalMessage,
                            Toast.LENGTH_LONG
                    ).show();
                }
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        handler.removeCallbacksAndMessages(null);
        executor.shutdown();
    }
}
