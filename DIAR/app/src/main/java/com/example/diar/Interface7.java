package com.example.diar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Interface7 extends AppCompatActivity {

    private TextView tvBonjour, tvUniversite;
    private ImageView ivUserPhoto;
    private ImageView ivColoc1, ivColoc2;
    private LinearLayout layoutColocataires;
    private TextView tvSeeAllColoc, tvColoc1Nom, tvColoc1Pct, tvColoc2Nom, tvColoc2Pct;
    private View cardColoc1, cardColoc2;

    private String emailConnecte;
    private OkHttpClient client = new OkHttpClient();

    private JSONObject etuColoc1 = null, etuColoc2 = null;
    private int pctColoc1 = 0, pctColoc2 = 0;
    private int idColoc1 = -1, idColoc2 = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.interface7);

        emailConnecte = getIntent().getStringExtra("USER_EMAIL");
        if (emailConnecte == null || emailConnecte.isEmpty()) {
            emailConnecte = "salma@test.com";
        }

        tvBonjour = findViewById(R.id.tvBonjour);
        tvUniversite = findViewById(R.id.tvUniversite);
        ivUserPhoto = findViewById(R.id.ivUserPhoto);
        ivColoc1 = findViewById(R.id.ivColoc1);
        ivColoc2 = findViewById(R.id.ivColoc2);

        layoutColocataires = findViewById(R.id.layoutColocataires);
        tvSeeAllColoc = findViewById(R.id.tvSeeAllColoc);
        tvColoc1Nom = findViewById(R.id.tvColoc1Nom);
        tvColoc1Pct = findViewById(R.id.tvColoc1Pct);
        tvColoc2Nom = findViewById(R.id.tvColoc2Nom);
        tvColoc2Pct = findViewById(R.id.tvColoc2Pct);
        cardColoc1 = findViewById(R.id.cardColoc1);
        cardColoc2 = findViewById(R.id.cardColoc2);

        findViewById(R.id.tvSeeAllRecommanded).setOnClickListener(v -> {
            Toast.makeText(this, "Liste logements", Toast.LENGTH_SHORT).show();
        });

        tvSeeAllColoc.setOnClickListener(v -> {
            Intent intent = new Intent(Interface7.this, Interface12.class);
            intent.putExtra("USER_EMAIL", emailConnecte);
            startActivity(intent);
        });

        cardColoc1.setOnClickListener(v -> {
            if (etuColoc1 != null) ouvrirDetail(etuColoc1, pctColoc1);
        });
        cardColoc2.setOnClickListener(v -> {
            if (etuColoc2 != null) ouvrirDetail(etuColoc2, pctColoc2);
        });

        // ✅ NOUVEAU : BARRE DE NAVIGATION EN BAS
        // ✅ Accueil - on est déjà ici
        LinearLayout navAccueil = findViewById(R.id.navAccueil);
        if (navAccueil != null) {
            navAccueil.setOnClickListener(v -> {
                // Déjà sur Interface 7
            });
        }

        // ✅ Carte
        LinearLayout navCarte = findViewById(R.id.navCarte);
        if (navCarte != null) {
            navCarte.setOnClickListener(v -> {
                Toast.makeText(this, "Carte bientôt disponible", Toast.LENGTH_SHORT).show();
            });
        }

        // ✅ COEUR -> OUVRE INTERFACE 11 (Favoris)
        LinearLayout navFavoris = findViewById(R.id.navFavoris);
        if (navFavoris != null) {
            navFavoris.setOnClickListener(v -> {
                Intent intent = new Intent(Interface7.this, Interface11.class);
                intent.putExtra("USER_EMAIL", emailConnecte);
                startActivity(intent);
            });
        }

        // ✅ Chat
        LinearLayout navChat = findViewById(R.id.navChat);
        if (navChat != null) {
            navChat.setOnClickListener(v -> {
                Toast.makeText(this, "Chat bientôt disponible", Toast.LENGTH_SHORT).show();
            });
        }

        // ✅ Profil - on est déjà sur le profil, ou tu peux rester ici
        LinearLayout navProfil = findViewById(R.id.navProfil);
        if (navProfil != null) {
            navProfil.setOnClickListener(v -> {
                // Déjà sur Interface 7 (Profil / Accueil)
            });
        }

        // ✅ COEUR SUR LA CARTE APPARTEMENT -> OUVRE AUSSI INTERFACE 11
        ImageView ivFavoriteAppart = findViewById(R.id.ivFavoriteAppart);
        if (ivFavoriteAppart != null) {
            ivFavoriteAppart.setOnClickListener(v -> {
                Intent intent = new Intent(Interface7.this, Interface11.class);
                intent.putExtra("USER_EMAIL", emailConnecte);
                startActivity(intent);
            });
        }

        chargerProfil();
        verifierEtAfficherColocs();
    }

    // ✅ CORRIGÉ : ic_profile partout, plus de profile
    private void chargerPhoto(ImageView imageView, String photoUrl) {
        if (photoUrl != null && !photoUrl.isEmpty() && !photoUrl.equals("null")) {
            String urlComplete = Config.BASE_URL + photoUrl;
            Glide.with(this)
                    .load(urlComplete)
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .circleCrop()
                    .into(imageView);
        } else {
            imageView.setImageResource(R.drawable.ic_profile);
        }
    }

    private void ouvrirDetail(JSONObject etu, int pourcentage) {
        try {
            Intent intent = new Intent(Interface7.this, Interface13.class);
            intent.putExtra("USER_EMAIL", emailConnecte);
            intent.putExtra("COLOC_ID", etu.optInt("id", -1));
            intent.putExtra("NOM", etu.optString("nomComplet", ""));
            intent.putExtra("POURCENTAGE", (double) pourcentage);
            intent.putExtra("FAC", etu.optString("universite", ""));
            intent.putExtra("AGE", etu.optInt("age", 0));
            intent.putExtra("BUDGET", etu.optDouble("budgetMax", 0));
            intent.putExtra("VILLE", etu.optString("quartier", ""));
            intent.putExtra("MODE", etu.optString("modeDeVie", ""));
            intent.putExtra("COLOCS", etu.optString("nbColocataires", ""));
            intent.putExtra("ANIMAUX", etu.optString("animauxAcceptes", ""));
            intent.putExtra("PROPRETE", etu.optString("proprete", ""));
            intent.putExtra("PHOTO", etu.optString("photoUrl", ""));
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void chargerProfil() {
        Request req = new Request.Builder()
                .url(Config.BASE_URL + "/api/etudiants/profil?email=" + emailConnecte)
                .build();

        client.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) { }

            @Override public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) return;
                String json = response.body().string();
                try {
                    JSONObject obj = new JSONObject(json);
                    String nom = obj.optString("nomComplet", "");
                    String prenom = nom.isEmpty() ? "" : nom.split(" ")[0];
                    String univ = obj.optString("universite", "Université de Sousse");
                    String photoUrl = obj.optString("photoUrl", "");

                    runOnUiThread(() -> {
                        if (!prenom.isEmpty()) tvBonjour.setText("Bonjour " + prenom + " 👋");
                        tvUniversite.setText(univ);
                        chargerPhoto(ivUserPhoto, photoUrl);
                    });
                } catch (Exception e) { e.printStackTrace(); }
            }
        });
    }

    private void verifierEtAfficherColocs() {
        Request req = new Request.Builder()
                .url(Config.BASE_URL + "/api/etudiants/profil?email=" + emailConnecte)
                .build();

        client.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) { }

            @Override public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) return;
                String json = response.body().string();
                try {
                    JSONObject obj = new JSONObject(json);
                    String locationType = obj.optString("locationType", "");

                    if ("Colocation".equalsIgnoreCase(locationType)) {
                        runOnUiThread(() -> {
                            layoutColocataires.setVisibility(View.VISIBLE);
                            charger2PremiersColocs();
                        });
                    } else {
                        runOnUiThread(() -> layoutColocataires.setVisibility(View.GONE));
                    }
                } catch (Exception e) { e.printStackTrace(); }
            }
        });
    }

    private void charger2PremiersColocs() {
        Request req = new Request.Builder()
                .url(Config.BASE_URL + "/api/etudiants/compatibles?email=" + emailConnecte)
                .build();

        client.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) { }

            @Override public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) return;
                String json = response.body().string();
                try {
                    JSONArray arr = new JSONArray(json);

                    runOnUiThread(() -> {
                        if (arr.length() == 0) {
                            layoutColocataires.setVisibility(View.GONE);
                            return;
                        }

                        try {
                            JSONObject o = arr.getJSONObject(0);
                            JSONObject etu = o.getJSONObject("etudiant");
                            String nomComplet = etu.getString("nomComplet");
                            String prenom = nomComplet.split(" ")[0];
                            int pct = o.getInt("pourcentage");

                            idColoc1 = etu.optInt("id", -1);
                            etuColoc1 = etu;
                            pctColoc1 = pct;
                            tvColoc1Nom.setText(prenom);
                            tvColoc1Pct.setText(pct + "%");
                            chargerPhoto(ivColoc1, etu.optString("photoUrl", ""));
                            cardColoc1.setVisibility(View.VISIBLE);
                        } catch (Exception e) { e.printStackTrace(); }

                        if (arr.length() > 1) {
                            try {
                                JSONObject o = arr.getJSONObject(1);
                                JSONObject etu = o.getJSONObject("etudiant");
                                String nomComplet = etu.getString("nomComplet");
                                String prenom = nomComplet.split(" ")[0];
                                int pct = o.getInt("pourcentage");

                                idColoc2 = etu.optInt("id", -1);
                                etuColoc2 = etu;
                                pctColoc2 = pct;
                                tvColoc2Nom.setText(prenom);
                                tvColoc2Pct.setText(pct + "%");
                                chargerPhoto(ivColoc2, etu.optString("photoUrl", ""));
                                cardColoc2.setVisibility(View.VISIBLE);
                            } catch (Exception e) { e.printStackTrace(); }
                        } else {
                            cardColoc2.setVisibility(View.GONE);
                        }
                    });
                } catch (Exception e) { e.printStackTrace(); }
            }
        });
    }
}