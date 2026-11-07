package com.example.diar;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Interface12 extends AppCompatActivity {

    private RecyclerView recyclerViewProfils;
    private ProfilAdapter profilAdapter;
    private List<Etudiant> profilList;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.interface12);

        // 1. Liaison des vues XML
        recyclerViewProfils = findViewById(R.id.recyclerViewProfils);
        btnBack = findViewById(R.id.btnBack);

        // 2. Configuration du RecyclerView
        recyclerViewProfils.setLayoutManager(new LinearLayoutManager(this));
        profilList = new ArrayList<>();
        profilAdapter = new ProfilAdapter(profilList);
        recyclerViewProfils.setAdapter(profilAdapter);

        // 3. Gestion du bouton retour
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // 4. Récupération de l'e-mail de l'étudiant connecté transmis par l'Intent
        String email = getIntent().getStringExtra("USER_EMAIL");
        if (email == null || email.isEmpty()) {
            email = "testtt@gmail.com"; // Valeur par défaut de secours pour les tests
        }

        // 5. Charger les profils compatibles depuis le serveur Spring Boot
        chargerProfilsCompatibles(email);
    }

    private void chargerProfilsCompatibles(String email) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                // URL de ton endpoint Spring Boot
                URL url = new URL("http://192.168.1.132:8082/api/etudiants/compatibles?email=" + email);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    br.close();

                    // Parsing du tableau JSON reçu du serveur (Format DTO : EtudiantAvecScore)
                    JSONArray jsonArray = new JSONArray(response.toString());
                    profilList.clear();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        // 1. Récupération de l'objet global contenant le pourcentage et l'étudiant
                        JSONObject itemObj = jsonArray.getJSONObject(i);

                        // Récupération du pourcentage calculé par le backend
                        int pourcentage = itemObj.optInt("pourcentage", 50);

                        // 2. Extraction de l'objet imbriqué "etudiant"
                        JSONObject obj = itemObj.getJSONObject("etudiant");

                        // Récupération des champs de l'entité Etudiant
                        String nom = obj.optString("nomComplet", "Étudiant");
                        String userEmail = obj.optString("email", "");
                        String universite = obj.optString("universite", "Université de Sousse");
                        int age = obj.optInt("age", 21);
                        double budget = obj.optDouble("budgetMax", 300.0);
                        String quartier = obj.optString("quartier", "Non spécifié");
                        String modeDeVie = obj.optString("modeDeVie", "");
                        String nbColocataires = obj.optString("nbColocataires", "");
                        String proprete = obj.optString("proprete", "");
                        String animauxAcceptes = obj.optString("animauxAcceptes", "");
                        String photoUrl = obj.optString("photoUrl", ""); // <-- Récupération de l'URL de la photo

                        Etudiant etudiant = new Etudiant(nom, userEmail, universite, age, budget, modeDeVie, nbColocataires, proprete, animauxAcceptes);

                        // Injection des valeurs complémentaires dans l'objet Etudiant
                        etudiant.setPourcentage(pourcentage);
                        etudiant.setQuartier(quartier);
                        etudiant.setPhotoUrl(photoUrl); // <-- Assignation de la photo

                        profilList.add(etudiant);
                    }
                } else {
                    handler.post(() -> Toast.makeText(this, "Erreur serveur : " + responseCode, Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                e.printStackTrace();
                handler.post(() -> Toast.makeText(this, "Erreur de connexion : " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            // Mise à jour de l'UI sur le thread principal
            handler.post(() -> profilAdapter.notifyDataSetChanged());
        });
    }
}