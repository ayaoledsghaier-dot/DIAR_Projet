
package com.example.diar;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Interface7 extends AppCompatActivity {

    private static final String TAG = "DIAR_DEBUG";
    private static final String BASE_URL = "http://192.168.1.22:8083";

    private TextView tvBonjour, tvUniversite;
    private ImageView ivUserPhoto;
    private EditText etSearch;
    private RecyclerView rvAnnonces;

    private AnnonceAdapter annonceAdapter;

    private final ExecutorService executor =
            Executors.newFixedThreadPool(2);

    private final Handler handler =
            new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.interface7);

        NavigationEtudiant.setup(this, 0);

        tvBonjour = findViewById(R.id.tvBonjour);
        tvUniversite = findViewById(R.id.tvUniversite);
        ivUserPhoto = findViewById(R.id.ivUserPhoto);
        etSearch = findViewById(R.id.etSearch);
        rvAnnonces = findViewById(R.id.rvAnnonces);

        // Configuration RecyclerView
        LinearLayoutManager layoutManager =
                new LinearLayoutManager(this);

        rvAnnonces.setLayoutManager(layoutManager);
        rvAnnonces.setHasFixedSize(false);
        rvAnnonces.setNestedScrollingEnabled(false);

        // Créer Adapter AVANT de le connecter
        annonceAdapter = new AnnonceAdapter(this, BASE_URL);
        rvAnnonces.setAdapter(annonceAdapter);

        // Récupération session étudiant
        SessionManager sessionManager = new SessionManager(this);
        String email = sessionManager.getEmail();

        Log.d(TAG, "Email récupéré = " + email);

        if (email == null || email.trim().isEmpty()) {
            Toast.makeText(this,
                    "Veuillez vous connecter.",
                    Toast.LENGTH_LONG).show();

            tvBonjour.setText("Bonjour 👋");
            return;
        }

        email = email.trim();

        chargerAnnonces(email);
        chargerDonneesUtilisateur(email);

        // Recherche
        etSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(
                    CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s, int start, int before, int count) {

                annonceAdapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    // =====================================
    // CHARGER LES ANNONCES
    // =====================================

    private void chargerAnnonces(String email) {

        executor.execute(() -> {

            HttpURLConnection conn = null;

            try {
                String emailEncode = URLEncoder.encode(email, "UTF-8");

                String urlString =
                        BASE_URL + "/api/annonces?email=" + emailEncode;

                Log.d(TAG, "URL annonces = " + urlString);

                URL url = new URL(urlString);
                conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);

                int code = conn.getResponseCode();

                Log.d(TAG, "HTTP annonces = " + code);

                InputStream inputStream;

                if (code >= 200 && code < 300) {
                    inputStream = conn.getInputStream();
                } else {
                    inputStream = conn.getErrorStream();
                }

                if (inputStream == null) {
                    throw new Exception("Réponse serveur vide");
                }

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(inputStream, "UTF-8")
                );

                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                reader.close();

                String json = response.toString().trim();

                if (code < 200 || code >= 300) {
                    throw new Exception("Erreur HTTP " + code + " : " + json);
                }

                ArrayList<JSONObject> annonces = parserAnnonces(json);

                Log.d(TAG, "Nombre annonces reçues = " + annonces.size());

                handler.post(() -> {

                    if (isFinishing() || isDestroyed()) return;

                    annonceAdapter.setAnnonces(annonces);

                    Log.d(TAG, "Nombre annonces Adapter = "
                            + annonceAdapter.getItemCount());

                    rvAnnonces.post(() -> {

                        Log.d(TAG, "RecyclerView hauteur = "
                                + rvAnnonces.getHeight());

                        Log.d(TAG, "RecyclerView enfants visibles = "
                                + rvAnnonces.getChildCount());
                    });

                    if (annonces.isEmpty()) {
                        Toast.makeText(
                                Interface7.this,
                                "Aucune annonce disponible.",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });

            } catch (Exception e) {

                Log.e(TAG, "Erreur chargement annonces", e);

                handler.post(() -> {
                    if (!isFinishing() && !isDestroyed()) {
                        Toast.makeText(
                                Interface7.this,
                                "Erreur annonces : " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });

            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        });
    }

    // =====================================
    // PARSER JSON
    // =====================================

    private ArrayList<JSONObject> parserAnnonces(String json)
            throws Exception {

        ArrayList<JSONObject> annonces = new ArrayList<>();

        if (json == null || json.trim().isEmpty()) {
            return annonces;
        }

        Object result = new JSONTokener(json).nextValue();

        if (result instanceof JSONArray) {

            JSONArray array = (JSONArray) result;

            for (int i = 0; i < array.length(); i++) {
                JSONObject annonce = array.optJSONObject(i);

                if (annonce != null) {
                    annonces.add(annonce);
                }
            }

        } else if (result instanceof JSONObject) {

            JSONObject object = (JSONObject) result;

            if (object.opt("content") instanceof JSONArray) {

                JSONArray array = object.getJSONArray("content");

                for (int i = 0; i < array.length(); i++) {
                    JSONObject annonce = array.optJSONObject(i);

                    if (annonce != null) {
                        annonces.add(annonce);
                    }
                }

            } else if (object.has("id") && object.has("titre")) {
                annonces.add(object);
            }
        }

        return annonces;
    }

    // =====================================
    // CHARGER PROFIL ETUDIANT
    // =====================================

    private void chargerDonneesUtilisateur(String email) {

        executor.execute(() -> {

            HttpURLConnection conn = null;

            try {
                String emailEncode = URLEncoder.encode(email, "UTF-8");

                String urlString =
                        BASE_URL + "/api/etudiants/profil?email=" + emailEncode;

                Log.d(TAG, "URL profil = " + urlString);

                URL url = new URL(urlString);
                conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);

                int code = conn.getResponseCode();

                Log.d(TAG, "HTTP profil = " + code);

                if (code != HttpURLConnection.HTTP_OK) {
                    throw new Exception("Erreur profil HTTP " + code);
                }

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), "UTF-8")
                );

                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                reader.close();

                JSONObject profil =
                        new JSONObject(response.toString());

                String nom = profil.optString("nomComplet", "Étudiant");
                String universite = profil.optString("universite", "");
                String photoUrl = profil.optString("photoUrl", "");

                Bitmap bitmap = null;

                if (!photoUrl.isEmpty() && !photoUrl.equals("null")) {

                    String imageUrl;

                    if (photoUrl.startsWith("http://")
                            || photoUrl.startsWith("https://")) {

                        imageUrl = photoUrl;

                    } else if (photoUrl.startsWith("/")) {

                        imageUrl = BASE_URL + photoUrl;

                    } else {

                        imageUrl = BASE_URL + "/uploads/" + photoUrl;
                    }

                    HttpURLConnection imageConnection = null;

                    try {
                        URL image = new URL(imageUrl);

                        imageConnection =
                                (HttpURLConnection) image.openConnection();

                        imageConnection.setConnectTimeout(10000);
                        imageConnection.setReadTimeout(10000);

                        try (InputStream imageStream =
                                     imageConnection.getInputStream()) {

                            bitmap = BitmapFactory.decodeStream(imageStream);
                        }

                    } catch (Exception imageError) {
                        Log.e(TAG, "Erreur photo étudiant", imageError);

                    } finally {
                        if (imageConnection != null) {
                            imageConnection.disconnect();
                        }
                    }
                }

                final Bitmap finalBitmap = bitmap;
                final String finalNom = nom;
                final String finalUniversite = universite;

                handler.post(() -> {

                    if (isFinishing() || isDestroyed()) return;

                    tvBonjour.setText("Bonjour " + finalNom + " 👋");
                    tvUniversite.setText(finalUniversite);

                    if (finalBitmap != null) {
                        ivUserPhoto.setImageBitmap(finalBitmap);
                    }
                });

            } catch (Exception e) {

                Log.e(TAG, "Erreur chargement profil", e);

                handler.post(() -> {
                    if (!isFinishing() && !isDestroyed()) {
                        tvBonjour.setText("Bonjour 👋");
                    }
                });

            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}
