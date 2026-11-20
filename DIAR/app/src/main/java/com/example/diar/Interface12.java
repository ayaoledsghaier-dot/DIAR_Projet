package com.example.diar;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
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

public class Interface12 extends AppCompatActivity {

    private RecyclerView recyclerViewProfils;
    private ProfilAdapter profilAdapter;
    private List<Etudiant> profilList = new ArrayList<>();
    private ImageButton btnBack;
    private String emailConnecte;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.interface12);

        recyclerViewProfils = findViewById(R.id.recyclerViewProfils);
        btnBack = findViewById(R.id.btnBack);
        recyclerViewProfils.setLayoutManager(new LinearLayoutManager(this));

        emailConnecte = getIntent().getStringExtra("USER_EMAIL");
        if (emailConnecte == null || emailConnecte.isEmpty()) {
            emailConnecte = "testtt@gmail.com";
        }

        profilAdapter = new ProfilAdapter(profilList, etudiant -> {
            Intent intent = new Intent(Interface12.this, Interface13.class);
            intent.putExtra("USER_EMAIL", emailConnecte);
            intent.putExtra("COLOC_ID", etudiant.getId());
            intent.putExtra("NOM", etudiant.getNomComplet());
            intent.putExtra("POURCENTAGE", etudiant.getPourcentageCompatible());
            intent.putExtra("FAC", etudiant.getUniversite());
            intent.putExtra("AGE", etudiant.getAge());
            intent.putExtra("BUDGET", etudiant.getBudgetMax());
            intent.putExtra("VILLE", etudiant.getQuartier());
            intent.putExtra("MODE", etudiant.getModeDeVie());
            intent.putExtra("COLOCS", etudiant.getNbColocataires());
            intent.putExtra("ANIMAUX", etudiant.getAnimauxAcceptes());
            intent.putExtra("PROPRETE", etudiant.getProprete());
            intent.putExtra("PHOTO", etudiant.getPhotoUrl());
            startActivity(intent);
        });
        recyclerViewProfils.setAdapter(profilAdapter);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        chargerCompatibles(emailConnecte);

        // ✅ PLUS DE BARRE ICI, on a tout enlevé
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (profilAdapter != null) {
            profilAdapter.notifyDataSetChanged();
        }
    }

    private void chargerCompatibles(String email) {
        OkHttpClient client = new OkHttpClient();
        String url = Config.BASE_URL + "/api/etudiants/compatibles?email=" + email;

        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(Interface12.this, "Erreur serveur: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();
                try {
                    JSONArray array = new JSONArray(json);
                    List<Etudiant> nouvelleListe = new ArrayList<>();

                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);

                        JSONObject eObj = obj.has("etudiant") ? obj.getJSONObject("etudiant") : obj;
                        int score = obj.has("pourcentage") ? obj.getInt("pourcentage") : 0;

                        Etudiant e = new Etudiant();
                        e.setId(eObj.optInt("id", -1));
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
                        e.setPourcentageCompatible((double) score);

                        nouvelleListe.add(e);
                    }

                    runOnUiThread(() -> {
                        profilList.clear();
                        profilList.addAll(nouvelleListe);
                        profilAdapter.notifyDataSetChanged();
                        if (nouvelleListe.isEmpty()) {
                            Toast.makeText(Interface12.this, "Aucun profil compatible", Toast.LENGTH_SHORT).show();
                        }
                    });

                } catch (Exception ex) {
                    ex.printStackTrace();
                    runOnUiThread(() -> Toast.makeText(Interface12.this, "Erreur parsing: " + ex.getMessage(), Toast.LENGTH_LONG).show());
                }
            }
        });
    }
}