
package com.example.diar;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class DetailAnnonce extends AppCompatActivity {

    private static final String BASE_URL =
            "http://192.168.1.22:8083";

    private ImageView imgDetailAnnonce;

    private TextView tvDetailTitre;
    private TextView tvDetailPrix;
    private TextView tvDetailDescription;
    private TextView tvDetailPersonnes;
    private TextView tvDetailLatitude;
    private TextView tvDetailLongitude;

    private LinearLayout layoutPhotos;
    private MapView mapDetailAnnonce;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_annonce);

        // Configuration de la carte
        Configuration.getInstance()
                .setUserAgentValue(getPackageName());

        // Récupération des vues XML
        imgDetailAnnonce = findViewById(R.id.imgDetailAnnonce);

        tvDetailTitre = findViewById(R.id.tvDetailTitre);
        tvDetailPrix = findViewById(R.id.tvDetailPrix);
        tvDetailDescription = findViewById(R.id.tvDetailDescription);
        tvDetailPersonnes = findViewById(R.id.tvDetailPersonnes);
        tvDetailLatitude = findViewById(R.id.tvDetailLatitude);
        tvDetailLongitude = findViewById(R.id.tvDetailLongitude);

        Button btnRetourDetail =
                findViewById(R.id.btnRetourDetail);

        btnRetourDetail.setOnClickListener(v -> finish());

        // Nouvelles vues ajoutées dans XML
        layoutPhotos = findViewById(R.id.layoutPhotos);
        mapDetailAnnonce = findViewById(R.id.mapDetailAnnonce);

        mapDetailAnnonce.setTileSource(
                TileSourceFactory.MAPNIK
        );

        mapDetailAnnonce.setMultiTouchControls(true);

        // Récupérer ID annonce
        long idAnnonce =
                getIntent().getLongExtra("idAnnonce", -1);

        if (idAnnonce == -1) {
            Toast.makeText(this,
                    "ID annonce introuvable",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        chargerDetailAnnonce(idAnnonce);
    }

    private void chargerDetailAnnonce(long id) {

        String url = BASE_URL + "/api/annonces/" + id;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,

                response -> afficherDetails(response),

                error -> {
                    Toast.makeText(
                            DetailAnnonce.this,
                            "Erreur de chargement des détails",
                            Toast.LENGTH_LONG
                    ).show();
                }
        );

        Volley.newRequestQueue(this).add(request);
    }

    private void afficherDetails(JSONObject annonce) {

        String titre = annonce.optString(
                "titre", "Sans titre");

        String description = annonce.optString(
                "description", "Aucune description");

        String prix = annonce.optString(
                "prix", "0");

        String personnes = annonce.optString(
                "nombrePersonnes", "Non précisé");

        double latitude = annonce.optDouble("latitude", 0);
        double longitude = annonce.optDouble("longitude", 0);

        // Afficher les données
        tvDetailTitre.setText(titre);
        tvDetailPrix.setText(prix + " DT / mois");
        tvDetailDescription.setText(description);

        tvDetailPersonnes.setText(
                "Nombre de personnes : " + personnes);

        tvDetailLatitude.setText(
                "Latitude : " + latitude);

        tvDetailLongitude.setText(
                "Longitude : " + longitude);

        // Récupérer toutes les photos
        JSONArray photos = annonce.optJSONArray("photos");

        if (photos != null && photos.length() > 0) {
            afficherToutesLesPhotos(photos);
        }

        // Afficher la localisation
        if (latitude >= -90 && latitude <= 90
                && longitude >= -180 && longitude <= 180
                && (latitude != 0 || longitude != 0)) {

            afficherLocalisation(latitude, longitude);
        }
    }

    // Afficher toutes les photos
    private void afficherToutesLesPhotos(JSONArray photos) {

        layoutPhotos.removeAllViews();

        for (int i = 0; i < photos.length(); i++) {

            String photoBase64 = photos.optString(i, "");

            Bitmap bitmap = convertirPhoto(photoBase64);

            if (bitmap == null) {
                continue;
            }

            // Première photo en grand
            if (i == 0) {
                imgDetailAnnonce.setImageBitmap(bitmap);
            }

            // Créer miniature
            ImageView miniature = new ImageView(this);

            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(
                            dpToPx(100),
                            dpToPx(90)
                    );

            params.setMargins(
                    dpToPx(5),
                    dpToPx(5),
                    dpToPx(5),
                    dpToPx(5)
            );

            miniature.setLayoutParams(params);
            miniature.setScaleType(
                    ImageView.ScaleType.CENTER_CROP
            );

            miniature.setImageBitmap(bitmap);

            // Changer la grande photo au clic
            miniature.setOnClickListener(v ->
                    imgDetailAnnonce.setImageBitmap(bitmap)
            );

            layoutPhotos.addView(miniature);
        }
    }

    // Convertir Base64 en Bitmap
    private Bitmap convertirPhoto(String photoBase64) {

        try {
            if (photoBase64 == null
                    || photoBase64.trim().isEmpty()
                    || photoBase64.equals("null")) {
                return null;
            }

            String imageData = photoBase64.trim();

            if (imageData.contains(",")) {
                imageData = imageData.substring(
                        imageData.indexOf(",") + 1
                );
            }

            byte[] imageBytes = Base64.decode(
                    imageData,
                    Base64.DEFAULT
            );

            return BitmapFactory.decodeByteArray(
                    imageBytes,
                    0,
                    imageBytes.length
            );

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Afficher la localisation sur la carte
    private void afficherLocalisation(
            double latitude,
            double longitude) {

        GeoPoint position = new GeoPoint(
                latitude,
                longitude
        );

        mapDetailAnnonce.getController().setZoom(15.0);
        mapDetailAnnonce.getController().setCenter(position);

        Marker marker = new Marker(mapDetailAnnonce);

        marker.setPosition(position);
        marker.setTitle("Localisation de l'annonce");

        marker.setAnchor(
                Marker.ANCHOR_CENTER,
                Marker.ANCHOR_BOTTOM
        );

        mapDetailAnnonce.getOverlays().clear();
        mapDetailAnnonce.getOverlays().add(marker);

        mapDetailAnnonce.invalidate();
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources()
                .getDisplayMetrics().density);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mapDetailAnnonce != null) {
            mapDetailAnnonce.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mapDetailAnnonce != null) {
            mapDetailAnnonce.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mapDetailAnnonce != null) {
            mapDetailAnnonce.onDetach();
        }
    }
}
