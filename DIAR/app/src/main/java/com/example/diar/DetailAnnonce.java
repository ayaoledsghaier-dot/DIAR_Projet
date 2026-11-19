
package com.example.diar;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
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

    private static final String TAG = "DIAR_OWNER";

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

    private String proprietaireEmail = "";
    private String proprietaireNom = "";

    private long idAnnonce = -1;

    private String etudiantEmail = "";
    private String etudiantNom = "";

    private Button btnContacter;
    private Button btnDemanderVisite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_annonce);

        Configuration.getInstance()
                .setUserAgentValue(getPackageName());

        imgDetailAnnonce = findViewById(R.id.imgDetailAnnonce);

        tvDetailTitre = findViewById(R.id.tvDetailTitre);
        tvDetailPrix = findViewById(R.id.tvDetailPrix);
        tvDetailDescription = findViewById(R.id.tvDetailDescription);
        tvDetailPersonnes = findViewById(R.id.tvDetailPersonnes);
        tvDetailLatitude = findViewById(R.id.tvDetailLatitude);
        tvDetailLongitude = findViewById(R.id.tvDetailLongitude);

        layoutPhotos = findViewById(R.id.layoutPhotos);
        mapDetailAnnonce = findViewById(R.id.mapDetailAnnonce);

        btnContacter = findViewById(R.id.btnContacter);
        btnDemanderVisite = findViewById(R.id.btnDemanderVisite);

        Button btnRetourDetail = findViewById(R.id.btnRetourDetail);

        btnRetourDetail.setOnClickListener(v -> finish());

        mapDetailAnnonce.setTileSource(TileSourceFactory.MAPNIK);
        mapDetailAnnonce.setMultiTouchControls(true);

        // Session étudiant
        SessionManager session = new SessionManager(this);

        etudiantEmail = session.getEmail();

        etudiantNom = getIntent().getStringExtra("etudiantNom");

        if (etudiantNom == null || etudiantNom.trim().isEmpty()) {
            etudiantNom = "Étudiant";
        }

        idAnnonce = getIntent().getLongExtra("idAnnonce", -1);

        if (idAnnonce <= 0) {
            Toast.makeText(this,
                    "ID annonce introuvable",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Charger les informations avant de contacter
        btnContacter.setEnabled(false);

        btnContacter.setOnClickListener(v -> ouvrirChat());

        btnDemanderVisite.setOnClickListener(v ->
                demanderVisite()
        );

        chargerDetailAnnonce(idAnnonce);
    }

    private void chargerDetailAnnonce(long id) {

        String url = BASE_URL + "/api/annonces/" + id;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,

                response -> {
                    Log.d(TAG, "ANNONCE JSON = " + response.toString());

                    afficherDetails(response);

                    btnContacter.setEnabled(true);
                },

                error -> {
                    Log.e(TAG, "Erreur API annonce", error);

                    Toast.makeText(this,
                            "Erreur chargement détails",
                            Toast.LENGTH_LONG).show();
                }
        );

        Volley.newRequestQueue(this).add(request);
    }

    private void afficherDetails(JSONObject annonce) {

        tvDetailTitre.setText(
                annonce.optString("titre", "Sans titre")
        );

        tvDetailPrix.setText(
                annonce.optString("prix", "0") + " DT / mois"
        );

        tvDetailDescription.setText(
                annonce.optString("description", "Aucune description")
        );

        tvDetailPersonnes.setText(
                "Nombre de personnes : " +
                        annonce.optString("nombrePersonnes", "Non précisé")
        );

        double latitude = annonce.optDouble("latitude", 0);
        double longitude = annonce.optDouble("longitude", 0);

        tvDetailLatitude.setText("Latitude : " + latitude);
        tvDetailLongitude.setText("Longitude : " + longitude);

        // Récupérer les informations du propriétaire
        recupererInformationsProprietaire(annonce);

        Log.d(TAG, "EMAIL PROPRIETAIRE = " + proprietaireEmail);
        Log.d(TAG, "NOM PROPRIETAIRE = " + proprietaireNom);

        JSONArray photos = annonce.optJSONArray("photos");

        if (photos != null && photos.length() > 0) {
            afficherToutesLesPhotos(photos);
        }

        if (latitude >= -90 && latitude <= 90
                && longitude >= -180 && longitude <= 180
                && (latitude != 0 || longitude != 0)) {

            afficherLocalisation(latitude, longitude);
        }
    }

    private void recupererInformationsProprietaire(JSONObject annonce) {

        proprietaireEmail = "";
        proprietaireNom = "";

        // Essayer les différents noms possibles de l'email
        String[] champsEmail = {
                "proprietaireEmail",
                "emailProprietaire",
                "email",
                "ownerEmail",
                "mailProprietaire"
        };

        for (String champ : champsEmail) {

            String email = annonce.optString(champ, "");

            if (email != null &&
                    !email.trim().isEmpty() &&
                    !email.equals("null")) {

                proprietaireEmail = email.trim();
                break;
            }
        }

        // Nom du propriétaire
        String[] champsNom = {
                "proprietaireNom",
                "nomProprietaire",
                "nomCompletProprietaire"
        };

        for (String champ : champsNom) {

            String nom = annonce.optString(champ, "");

            if (nom != null &&
                    !nom.trim().isEmpty() &&
                    !nom.equals("null")) {

                proprietaireNom = nom.trim();
                break;
            }
        }

        // Vérifier l'objet propriétaire
        JSONObject proprietaire =
                annonce.optJSONObject("proprietaire");

        if (proprietaire != null) {

            if (proprietaireEmail.isEmpty()) {

                proprietaireEmail =
                        proprietaire.optString("email", "");

                if (proprietaireEmail.isEmpty()) {
                    proprietaireEmail =
                            proprietaire.optString("emailProprietaire", "");
                }
            }

            if (proprietaireNom.isEmpty()) {

                proprietaireNom =
                        proprietaire.optString("nomComplet", "");

                if (proprietaireNom.isEmpty()) {
                    proprietaireNom =
                            proprietaire.optString("nom", "");
                }
            }
        }
    }

    // Ouvrir Chat étudiant
    private void ouvrirChat() {

        if (etudiantEmail == null ||
                etudiantEmail.trim().isEmpty()) {

            Toast.makeText(this,
                    "Veuillez vous connecter",
                    Toast.LENGTH_LONG).show();
            return;
        }

        if (proprietaireEmail == null ||
                proprietaireEmail.trim().isEmpty()) {

            Toast.makeText(this,
                    "Email du propriétaire introuvable",
                    Toast.LENGTH_LONG).show();

            Log.e(TAG, "Email propriétaire absent du JSON");

            return;
        }

        if (etudiantEmail.equalsIgnoreCase(proprietaireEmail)) {

            Toast.makeText(this,
                    "Vous ne pouvez pas contacter votre propre annonce",
                    Toast.LENGTH_LONG).show();

            return;
        }

        android.content.Intent intent =
                new android.content.Intent(this, ChatEtudiant.class);

        intent.putExtra("etudiantEmail", etudiantEmail);
        intent.putExtra("etudiantNom", etudiantNom);

        intent.putExtra("proprietaireEmail", proprietaireEmail);

        intent.putExtra("proprietaireNom",
                proprietaireNom.isEmpty()
                        ? "Propriétaire"
                        : proprietaireNom);

        intent.putExtra("annonceId", idAnnonce);

        startActivity(intent);
    }

    private void demanderVisite() {

        Toast.makeText(this,
                "Fonction demande de visite à connecter au Backend",
                Toast.LENGTH_LONG).show();
    }

    private void afficherToutesLesPhotos(JSONArray photos) {

        layoutPhotos.removeAllViews();

        for (int i = 0; i < photos.length(); i++) {

            Bitmap bitmap = convertirPhoto(photos.optString(i, ""));

            if (bitmap == null) continue;

            if (i == 0) {
                imgDetailAnnonce.setImageBitmap(bitmap);
            }

            ImageView miniature = new ImageView(this);

            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(
                            dpToPx(100),
                            dpToPx(90)
                    );

            params.setMargins(5, 5, 5, 5);

            miniature.setLayoutParams(params);
            miniature.setScaleType(ImageView.ScaleType.CENTER_CROP);
            miniature.setImageBitmap(bitmap);

            miniature.setOnClickListener(v ->
                    imgDetailAnnonce.setImageBitmap(bitmap)
            );

            layoutPhotos.addView(miniature);
        }
    }

    private Bitmap convertirPhoto(String photoBase64) {

        try {

            if (photoBase64 == null ||
                    photoBase64.trim().isEmpty() ||
                    photoBase64.equals("null")) {
                return null;
            }

            String imageData = photoBase64.trim();

            if (imageData.contains(",")) {
                imageData = imageData.substring(
                        imageData.indexOf(",") + 1
                );
            }

            byte[] bytes = Base64.decode(
                    imageData,
                    Base64.DEFAULT
            );

            return BitmapFactory.decodeByteArray(
                    bytes, 0, bytes.length
            );

        } catch (Exception e) {
            Log.e(TAG, "Erreur conversion photo", e);
            return null;
        }
    }

    private void afficherLocalisation(double latitude, double longitude) {

        GeoPoint position = new GeoPoint(latitude, longitude);

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
