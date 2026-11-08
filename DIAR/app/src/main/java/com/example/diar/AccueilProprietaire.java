package com.example.diar;

import android.content.Intent;
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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;

import org.json.JSONArray;
import org.json.JSONObject;

public class AccueilProprietaire extends AppCompatActivity {

    private static final String BASE_URL =
            "http://192.168.1.22:8083";

    private ShapeableImageView imgOwnerProfile;

    private TextView tvOwnerGreeting;
    private TextView tvListingsCount;
    private TextView tvViewsCount;
    private TextView tvRequestsCount;
    private TextView tvVisitsCount;

    private LinearLayout layoutEmptyListings;
    private LinearLayout layoutListings;

    private ActivityResultLauncher<String> photoPicker;

    private RequestQueue requestQueue;

    private String nomProprietaire;
    private String emailProprietaire;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.interface18);

        // Navigation propriétaire
        NavigationProprietaire.setup(
                this,
                R.id.nav_prop_accueil
        );

        // Récupérer les vues
        imgOwnerProfile = findViewById(R.id.imgOwnerProfile);

        tvOwnerGreeting = findViewById(R.id.tvOwnerGreeting);

        tvListingsCount = findViewById(R.id.tvListingsCount);
        tvViewsCount = findViewById(R.id.tvViewsCount);
        tvRequestsCount = findViewById(R.id.tvRequestsCount);
        tvVisitsCount = findViewById(R.id.tvVisitsCount);

        layoutEmptyListings = findViewById(R.id.layoutEmptyListings);
        layoutListings = findViewById(R.id.layoutListings);

        // Initialiser Volley
        requestQueue = Volley.newRequestQueue(getApplicationContext());

        // Récupérer les informations du propriétaire
        nomProprietaire = getIntent().getStringExtra("nomProprietaire");
        emailProprietaire = getIntent().getStringExtra("emailProprietaire");

        // Afficher le nom
        if (nomProprietaire != null && !nomProprietaire.trim().isEmpty()) {
            tvOwnerGreeting.setText("Bonjour " + nomProprietaire + " 👋");
        } else {
            tvOwnerGreeting.setText("Bonjour 👋");
        }

        // Initialiser les statistiques
        tvListingsCount.setText("0");
        tvViewsCount.setText("0");
        tvRequestsCount.setText("0");
        tvVisitsCount.setText("0");

        // Sélectionner une photo de profil
        photoPicker = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        imgOwnerProfile.setImageURI(uri);
                    }
                }
        );

        imgOwnerProfile.setOnClickListener(v ->
                photoPicker.launch("image/*")
        );

        // Bouton Ajouter une annonce
        Button btnAddListing = findViewById(R.id.btnAddListing);

        if (btnAddListing != null) {
            btnAddListing.setOnClickListener(v -> {

                Intent intent = new Intent(
                        AccueilProprietaire.this,
                        AjouterAnnonce.class
                );

                intent.putExtra(
                        "nomProprietaire",
                        nomProprietaire
                );

                intent.putExtra(
                        "emailProprietaire",
                        emailProprietaire
                );

                startActivity(intent);
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (requestQueue != null) {
            chargerAnnonces();
        }
    }

    // Charger les annonces depuis Spring Boot
    private void chargerAnnonces() {

        String url = BASE_URL + "/api/annonces";

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,

                this::afficherAnnonces,

                error -> {
                    Toast.makeText(
                            AccueilProprietaire.this,
                            "Impossible de charger les annonces. Vérifie Spring Boot et le Wi-Fi.",
                            Toast.LENGTH_LONG
                    ).show();
                }
        );

        requestQueue.add(request);
    }

    // Afficher les annonces
    private void afficherAnnonces(JSONArray annonces) {

        layoutListings.removeAllViews();

        int nombreAnnonces = annonces.length();

        tvListingsCount.setText(String.valueOf(nombreAnnonces));

        if (nombreAnnonces == 0) {

            layoutEmptyListings.setVisibility(View.VISIBLE);
            layoutListings.setVisibility(View.GONE);

            return;
        }

        layoutEmptyListings.setVisibility(View.GONE);
        layoutListings.setVisibility(View.VISIBLE);

        for (int i = 0; i < nombreAnnonces; i++) {

            JSONObject annonce = annonces.optJSONObject(i);

            if (annonce == null) {
                continue;
            }

            long id = annonce.optLong("id", -1);

            String titre = annonce.optString(
                    "titre",
                    "Sans titre"
            );

            String description = annonce.optString(
                    "description",
                    "Aucune description"
            );

            String prix = annonce.optString(
                    "prix",
                    "0"
            );

            JSONArray photos = annonce.optJSONArray("photos");

            String photoBase64 = "";

            if (photos != null && photos.length() > 0) {
                photoBase64 = photos.optString(0, "");
            }

            ajouterCarteAnnonce(
                    id,
                    titre,
                    description,
                    prix,
                    photoBase64
            );
        }
    }

    // Créer une carte annonce
    private void ajouterCarteAnnonce(
            long id,
            String titre,
            String description,
            String prix,
            String photoBase64
    ) {

        MaterialCardView carte = new MaterialCardView(this);

        LinearLayout.LayoutParams carteParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        carteParams.setMargins(0, 0, 0, dp(14));

        carte.setLayoutParams(carteParams);
        carte.setRadius(dp(16));
        carte.setCardElevation(dp(3));

        carte.setCardBackgroundColor(
                android.graphics.Color.WHITE
        );

        LinearLayout contenu = new LinearLayout(this);

        contenu.setOrientation(LinearLayout.VERTICAL);

        contenu.setPadding(
                dp(14),
                dp(14),
                dp(14),
                dp(14)
        );

        // Afficher la photo
        if (photoBase64 != null
                && !photoBase64.trim().isEmpty()
                && !photoBase64.equals("null")) {

            try {

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

                Bitmap bitmap = BitmapFactory.decodeByteArray(
                        imageBytes,
                        0,
                        imageBytes.length
                );

                if (bitmap != null) {

                    ImageView image = new ImageView(this);

                    LinearLayout.LayoutParams imageParams =
                            new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    dp(180)
                            );

                    image.setLayoutParams(imageParams);

                    image.setScaleType(
                            ImageView.ScaleType.CENTER_CROP
                    );

                    image.setImageBitmap(bitmap);

                    contenu.addView(image);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Titre
        TextView tvTitre = creerTexte(
                titre,
                18,
                true,
                "#0F172A"
        );

        tvTitre.setPadding(
                0,
                dp(10),
                0,
                dp(5)
        );

        contenu.addView(tvTitre);

        // Prix
        TextView tvPrix = creerTexte(
                prix + " DT / mois",
                16,
                true,
                "#0D9488"
        );

        tvPrix.setPadding(
                0,
                0,
                0,
                dp(8)
        );

        contenu.addView(tvPrix);

        // Description
        TextView tvDescription = creerTexte(
                description,
                14,
                false,
                "#475569"
        );

        tvDescription.setPadding(
                0,
                0,
                0,
                dp(8)
        );

        contenu.addView(tvDescription);

        // Localisation
        TextView tvAdresse = creerTexte(
                "📍 Voir la localisation dans le détail",
                14,
                false,
                "#64748B"
        );

        contenu.addView(tvAdresse);

        carte.addView(contenu);

        // Ouvrir le détail de l'annonce
        carte.setClickable(true);
        carte.setFocusable(true);

        carte.setOnClickListener(v -> {

            if (id == -1) {

                Toast.makeText(
                        AccueilProprietaire.this,
                        "ID annonce introuvable",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            Intent intent = new Intent(
                    AccueilProprietaire.this,
                    DetailAnnonce.class
            );

            intent.putExtra("idAnnonce", id);

            startActivity(intent);
        });

        layoutListings.addView(carte);
    }

    // Créer un TextView
    private TextView creerTexte(
            String texte,
            int taille,
            boolean gras,
            String couleur
    ) {

        TextView textView = new TextView(this);

        textView.setText(texte);
        textView.setTextSize(taille);

        textView.setTextColor(
                android.graphics.Color.parseColor(couleur)
        );

        if (gras) {
            textView.setTypeface(
                    null,
                    android.graphics.Typeface.BOLD
            );
        }

        return textView;
    }

    // Convertir dp
    private int dp(int valeur) {

        return (int) (
                valeur * getResources()
                        .getDisplayMetrics().density
        );
    }
}