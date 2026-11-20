package com.example.diar;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Interface11 extends AppCompatActivity {

    private String emailConnecte;
    private RecyclerView recyclerView;
    private ProfilAdapter adapter;
    private List<Etudiant> favorisList = new ArrayList<>();
    private final OkHttpClient client = new OkHttpClient();

    // ✅ AJOUT - Les onglets, c'est ça qui te manquait
    private LinearLayout tabLogements, tabColocataires;
    private TextView tvTabLogements, tvTabColocataires;
    private View underlineLogements, underlineColocataires;
    private View scrollLogements, layoutColocsFavoris, layoutEmptyColocs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.interface11);

        emailConnecte = getIntent().getStringExtra("USER_EMAIL");
        if (emailConnecte == null || emailConnecte.isEmpty()) {
            emailConnecte = "salma@test.com";
        }

        // ✅ Bouton retour - il manquait aussi
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // ✅ ONGLETS - On les récupère
        tabLogements = findViewById(R.id.tabLogements);
        tabColocataires = findViewById(R.id.tabColocataires);
        tvTabLogements = findViewById(R.id.tvTabLogements);
        tvTabColocataires = findViewById(R.id.tvTabColocataires);
        underlineLogements = findViewById(R.id.underlineLogements);
        underlineColocataires = findViewById(R.id.underlineColocataires);
        scrollLogements = findViewById(R.id.scrollLogements);
        layoutColocsFavoris = findViewById(R.id.layoutColocsFavoris);
        layoutEmptyColocs = findViewById(R.id.layoutEmptyColocs);

        // ✅ CLIC SUR "Logements 🏠" -> Affiche les logements, cache les colocs
        tabLogements.setOnClickListener(v -> {
            // Couleurs
            tvTabLogements.setTextColor(Color.parseColor("#00796B"));
            tvTabLogements.setTypeface(null, android.graphics.Typeface.BOLD);
            tvTabColocataires.setTypeface(null, android.graphics.Typeface.NORMAL);
            tvTabColocataires.setTextColor(Color.parseColor("#A0AEC0"));
            // Soulignements
            underlineLogements.setBackgroundColor(Color.parseColor("#00796B"));
            underlineColocataires.setBackgroundColor(Color.parseColor("#FFFFFF"));
            // Contenus
            scrollLogements.setVisibility(View.VISIBLE);
            layoutColocsFavoris.setVisibility(View.GONE);
        });

        // ✅ CLIC SUR "Colocataires 👥" -> Cache les logements, affiche les colocs favoris
        tabColocataires.setOnClickListener(v -> {
            // Couleurs
            tvTabColocataires.setTextColor(Color.parseColor("#00796B"));
            tvTabColocataires.setTypeface(null, android.graphics.Typeface.BOLD);
            tvTabLogements.setTextColor(Color.parseColor("#A0AEC0"));
            tvTabLogements.setTypeface(null, android.graphics.Typeface.NORMAL);
            // Soulignements
            underlineColocataires.setBackgroundColor(Color.parseColor("#00796B"));
            underlineLogements.setBackgroundColor(Color.parseColor("#FFFFFF"));
            // Contenus
            scrollLogements.setVisibility(View.GONE);
            layoutColocsFavoris.setVisibility(View.VISIBLE);
            // Recharge les favoris au cas où
            chargerFavoris();
        });

        recyclerView = findViewById(R.id.recyclerViewFavorisColocs);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ProfilAdapter(favorisList, emailConnecte, etudiant -> {
            Intent intent = new Intent(Interface11.this, Interface13.class);
            intent.putExtra("USER_EMAIL", emailConnecte);
            intent.putExtra("COLOC_ID", etudiant.getId());
            intent.putExtra("NOM", etudiant.getNomComplet());
            intent.putExtra("POURCENTAGE", etudiant.getPourcentageCompatible() != null ? etudiant.getPourcentageCompatible() : 0.0);
            intent.putExtra("FAC", etudiant.getUniversite());
            intent.putExtra("AGE", etudiant.getAge() != null ? etudiant.getAge() : 0);
            intent.putExtra("BUDGET", etudiant.getBudgetMax() != null ? etudiant.getBudgetMax() : 0.0);
            intent.putExtra("VILLE", etudiant.getQuartier());
            intent.putExtra("MODE", etudiant.getModeDeVie());
            intent.putExtra("COLOCS", etudiant.getNbColocataires());
            intent.putExtra("ANIMAUX", etudiant.getAnimauxAcceptes());
            intent.putExtra("PROPRETE", etudiant.getProprete());
            intent.putExtra("PHOTO", etudiant.getPhotoUrl());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);
        chargerFavoris();
    }

    @Override
    protected void onResume() {
        super.onResume();
        chargerFavoris();
    }

    private void chargerFavoris() {
        String url = Config.BASE_URL + "/api/etudiants/favoris?email=" + emailConnecte;
        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(Interface11.this, "Erreur serveur : " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    runOnUiThread(() -> Toast.makeText(Interface11.this, "Erreur : " + response.code(), Toast.LENGTH_SHORT).show());
                    return;
                }
                String json = response.body().string();
                try {
                    JSONArray array = new JSONArray(json);
                    List<Etudiant> nouvelleListe = new ArrayList<>();
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        JSONObject eObj = obj.has("etudiant") ? obj.getJSONObject("etudiant") : obj;
                        JSONObject favObj = obj.has("etudiant") ? obj : null;
                        Etudiant e = new Etudiant();
                        e.setId(eObj.optLong("id", -1));
                        e.setNomComplet(eObj.optString("nomComplet", ""));
                        e.setEmail(eObj.optString("email", ""));
                        e.setUniversite(eObj.optString("universite", ""));
                        e.setAge(eObj.optInt("age", 0));
                        e.setBudgetMax(eObj.optDouble("budgetMax", 0));
                        e.setQuartier(eObj.optString("quartier", ""));
                        e.setModeDeVie(eObj.optString("modeDeVie", ""));
                        e.setNbColocataires(eObj.optString("nbColocataires", ""));
                        e.setAnimauxAcceptes(eObj.optString("animauxAcceptes", ""));
                        e.setProprete(eObj.optString("proprete", ""));
                        e.setPhotoUrl(eObj.optString("photoUrl", ""));
                        double score = 0;
                        if (favObj != null && favObj.has("pourcentageCompatible")) score = favObj.optDouble("pourcentageCompatible", 0);
                        else if (obj.has("pourcentage")) score = obj.optDouble("pourcentage", 0);
                        else if (eObj.has("pourcentageCompatible")) score = eObj.optDouble("pourcentageCompatible", 0);
                        e.setPourcentageCompatible(score);
                        e.setFavori(true);
                        nouvelleListe.add(e);
                    }
                    runOnUiThread(() -> {
                        favorisList.clear();
                        favorisList.addAll(nouvelleListe);
                        adapter.notifyDataSetChanged();

                        // ✅ Affiche le message "Aucun colocataire favori" si la liste est vide
                        if (layoutEmptyColocs != null) {
                            if (nouvelleListe.isEmpty()) {
                                layoutEmptyColocs.setVisibility(View.VISIBLE);
                                recyclerView.setVisibility(View.GONE);
                            } else {
                                layoutEmptyColocs.setVisibility(View.GONE);
                                recyclerView.setVisibility(View.VISIBLE);
                            }
                        }

                        Toast.makeText(Interface11.this, "Favoris trouvés : " + nouvelleListe.size(), Toast.LENGTH_SHORT).show();
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}