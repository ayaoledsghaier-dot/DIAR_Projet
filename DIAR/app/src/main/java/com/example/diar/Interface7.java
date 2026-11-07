package com.example.diar;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Interface7 extends AppCompatActivity {

    private TextView tvBonjour, tvUniversite;
    private ImageView ivUserPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.interface7);

        // Liaison des vues XML
        tvBonjour = findViewById(R.id.tvBonjour);
        tvUniversite = findViewById(R.id.tvUniversite);
        ivUserPhoto = findViewById(R.id.ivUserPhoto);

        // Récupération directe et globale de l'e-mail du compte connecté
        SessionManager sessionManager = new SessionManager(this);
        String email = sessionManager.getEmail();

        android.util.Log.d("DEBUG_EMAIL", "E-mail récupéré de la session : " + email);

        if (email != null && !email.trim().isEmpty()) {
            chargerDonneesUtilisateur(email.trim());
        } else {
            tvBonjour.setText("Erreur : Aucun utilisateur connecté 👋");
        }
    }

    private void chargerDonneesUtilisateur(String email) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        final String[] nomUtilisateur = {"Utilisateur"};
        final String[] universiteUtilisateur = {"Université de Sousse"};
        final String[] photoUrl = {null}; // Initialisé à null proprement
        final boolean[] success = {false};

        executor.execute(() -> {
            try {
                // 1. Récupération du profil JSON
                URL url = new URL("http://192.168.1.132:8082/api/etudiants/profil?email=" + URLEncoder.encode(email, "UTF-8"));
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

                    JSONObject jsonObject = new JSONObject(response.toString());

                    if (jsonObject.has("nomComplet") && !jsonObject.isNull("nomComplet")) {
                        nomUtilisateur[0] = jsonObject.getString("nomComplet");
                    }
                    if (jsonObject.has("universite") && !jsonObject.isNull("universite")) {
                        universiteUtilisateur[0] = jsonObject.getString("universite");
                    }
                    if (jsonObject.has("photoUrl") && !jsonObject.isNull("photoUrl")) {
                        photoUrl[0] = jsonObject.getString("photoUrl");
                        android.util.Log.d("DEBUG_PHOTO", "Nom du fichier trouvé en BDD : " + photoUrl[0]);
                    } else {
                        android.util.Log.d("DEBUG_PHOTO", "Le champ photoUrl est vide ou null dans le JSON !");
                    }

                    success[0] = true;
                } else {
                    android.util.Log.e("DEBUG_PHOTO", "Erreur HTTP : " + responseCode);
                }
            } catch (Exception e) {
                android.util.Log.e("DEBUG_PHOTO", "Erreur lors de l'appel profil : " + e.getMessage());
                e.printStackTrace();
            }

            // 2. Téléchargement de l'image si le nom du fichier existe
            Bitmap bitmapPhoto = null;
            if (success[0] && photoUrl[0] != null && !photoUrl[0].trim().isEmpty()) {
                try {
                    String imageUrl = "http://192.168.1.132:8082/uploads/" + photoUrl[0];
                    android.util.Log.d("DEBUG_PHOTO", "Tentative de téléchargement de l'image : " + imageUrl);

                    URL imageConnectionUrl = new URL(imageUrl);
                    InputStream inputStream = (InputStream) imageConnectionUrl.getContent();
                    bitmapPhoto = BitmapFactory.decodeStream(inputStream);

                    if (bitmapPhoto != null) {
                        android.util.Log.d("DEBUG_PHOTO", "Image téléchargée et décodée avec succès !");
                    } else {
                        android.util.Log.d("DEBUG_PHOTO", "Le Bitmap décodé est null (flux invalide)");
                    }
                } catch (Exception e) {
                    android.util.Log.e("DEBUG_PHOTO", "Erreur lors du téléchargement de l'image : " + e.getMessage());
                    e.printStackTrace();
                }
            }

            final Bitmap finalBitmap = bitmapPhoto;

            // 3. Mise à jour de l'interface graphique sur le thread principal
            handler.post(() -> {
                if (success[0]) {
                    tvBonjour.setText("Bonjour " + nomUtilisateur[0] + " 👋");
                    tvUniversite.setText(universiteUtilisateur[0]);

                    if (finalBitmap != null) {
                        ivUserPhoto.setImageBitmap(finalBitmap);
                        android.util.Log.d("DEBUG_PHOTO", "Photo appliquée avec succès dans l'interface !");
                    } else {
                        android.util.Log.d("DEBUG_PHOTO", "Aucune photo valide à afficher, conservation de l'image par défaut.");
                    }
                } else {
                    tvBonjour.setText("Erreur de chargement 👋");
                }
            });
        });
    }
}