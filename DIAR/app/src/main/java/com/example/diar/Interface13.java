package com.example.diar;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ImageView;
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

public class Interface13 extends AppCompatActivity {

    private TextView tvName, tvMatchPercentage, tvUniversiteAge, tvBudget, tvLocalisation;
    private ViewGroup chipGroupModeDeVie, chipGroupPreferences;
    private ImageView imgProfile;

    private String emailConnecte;
    private int colocId = -1; // ✅ UNE SEULE FOIS, pas de doublon
    private OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.interface13);

        tvName = findViewById(R.id.tvName);
        tvMatchPercentage = findViewById(R.id.tvMatchPercentage);
        tvUniversiteAge = findViewById(R.id.tvUniversiteAge);
        tvBudget = findViewById(R.id.tvBudget);
        tvLocalisation = findViewById(R.id.tvLocalisation);
        chipGroupModeDeVie = findViewById(R.id.chipGroupModeDeVie);
        chipGroupPreferences = findViewById(R.id.chipGroupPreferences);
        imgProfile = findViewById(R.id.imgProfile);

        emailConnecte = getIntent().getStringExtra("USER_EMAIL");
        colocId = getIntent().getIntExtra("COLOC_ID", -1);

        String nom = getIntent().getStringExtra("NOM");
        if (nom != null) {
            afficherDepuisExtras();
        }
        else if (colocId != -1) {
            chargerInfosParId();
            chargerPourcentage();
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        findViewById(R.id.btnContact).setOnClickListener(v -> {
            String n = tvName.getText() != null ? tvName.getText().toString() : "";
            Toast.makeText(this, "Contacter " + n, Toast.LENGTH_SHORT).show();
        });
    }

    private void chargerPhoto(String photoUrl) {
        if (photoUrl != null && !photoUrl.isEmpty() && !photoUrl.equals("null")) {
            String urlComplete = Config.BASE_URL + photoUrl;
            Glide.with(this)
                    .load(urlComplete)
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .circleCrop()
                    .into(imgProfile);
        } else {
            imgProfile.setImageResource(R.drawable.ic_profile);
        }
    }

    private void afficherDepuisExtras() {
        String nom = getIntent().getStringExtra("NOM");
        double pourcentage = getIntent().getDoubleExtra("POURCENTAGE", 0);
        String fac = getIntent().getStringExtra("FAC");
        int age = getIntent().getIntExtra("AGE", 0);
        double budget = getIntent().getDoubleExtra("BUDGET", 0);
        String ville = getIntent().getStringExtra("VILLE");
        String mode = getIntent().getStringExtra("MODE");
        String colocs = getIntent().getStringExtra("COLOCS");
        String animaux = getIntent().getStringExtra("ANIMAUX");
        String proprete = getIntent().getStringExtra("PROPRETE");
        String photo = getIntent().getStringExtra("PHOTO");

        if (nom != null) tvName.setText(nom);
        tvMatchPercentage.setText((int) pourcentage + "% compatible");
        tvUniversiteAge.setText(fac + "  •  " + age + " ans");
        tvBudget.setText("Budget : " + (int) budget + " DT");
        if (ville != null) tvLocalisation.setText(ville);
        chargerPhoto(photo);

        if (chipGroupModeDeVie.getChildCount() > 0 && mode != null) {
            TextView chip1 = (TextView) chipGroupModeDeVie.getChildAt(0);
            chip1.setText(mode);
        }
        if (chipGroupPreferences.getChildCount() > 0 && colocs != null) {
            TextView c1 = (TextView) chipGroupPreferences.getChildAt(0);
            c1.setText("Colocataire(s) : " + colocs);
        }
        if (chipGroupPreferences.getChildCount() > 1 && animaux != null) {
            TextView c2 = (TextView) chipGroupPreferences.getChildAt(1);
            c2.setText("Animaux : " + animaux);
        }
        if (chipGroupPreferences.getChildCount() > 2 && proprete != null) {
            TextView c3 = (TextView) chipGroupPreferences.getChildAt(2);
            c3.setText("Propreté : " + proprete);
        }
    }

    private void chargerInfosParId() {
        Request req = new Request.Builder()
                .url(Config.BASE_URL + "/api/etudiants/" + colocId)
                .build();

        client.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {}
            @Override public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) return;
                String json = response.body().string();
                try {
                    JSONObject obj = new JSONObject(json);
                    runOnUiThread(() -> {
                        String nom = obj.optString("nomComplet", "...");
                        String fac = obj.optString("universite", "...");
                        String ageStr = obj.optString("age", "0");
                        String ville = obj.optString("quartier", "...");
                        double budget = obj.optDouble("budgetMax", 0);

                        String mode = obj.optString("modeDeVie", "...");
                        String colocs = obj.optString("nbColocataires", "...");
                        String animaux = obj.optString("animauxAcceptes", "...");
                        String proprete = obj.optString("proprete", "...");
                        String photoUrl = obj.optString("photoUrl", "");

                        tvName.setText(nom);
                        tvUniversiteAge.setText(fac + "  •  " + ageStr + " ans");
                        tvBudget.setText("Budget : " + (int) budget + " DT");
                        tvLocalisation.setText(ville);
                        chargerPhoto(photoUrl);

                        if (chipGroupModeDeVie.getChildCount() > 0) {
                            ((TextView) chipGroupModeDeVie.getChildAt(0)).setText(mode);
                        }
                        if (chipGroupPreferences.getChildCount() > 0) {
                            ((TextView) chipGroupPreferences.getChildAt(0)).setText("Colocataire(s) : " + colocs);
                        }
                        if (chipGroupPreferences.getChildCount() > 1) {
                            ((TextView) chipGroupPreferences.getChildAt(1)).setText("Animaux : " + animaux);
                        }
                        if (chipGroupPreferences.getChildCount() > 2) {
                            ((TextView) chipGroupPreferences.getChildAt(2)).setText("Propreté : " + proprete);
                        }
                    });
                } catch (Exception e) { e.printStackTrace(); }
            }
        });
    }

    private void chargerPourcentage() {
        if (emailConnecte == null) return;
        Request req = new Request.Builder()
                .url(Config.BASE_URL + "/api/etudiants/compatibles?email=" + emailConnecte)
                .build();

        client.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {}
            @Override public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) return;
                String json = response.body().string();
                try {
                    JSONArray arr = new JSONArray(json);
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject o = arr.getJSONObject(i);
                        JSONObject etu = o.getJSONObject("etudiant");
                        if (etu.getInt("id") == colocId) {
                            int pct = o.getInt("pourcentage");
                            runOnUiThread(() -> tvMatchPercentage.setText(pct + "% compatible"));
                            break;
                        }
                    }
                } catch (Exception e) { e.printStackTrace(); }
            }
        });
    }
}