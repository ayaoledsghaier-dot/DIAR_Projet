package com.example.diar;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.imageview.ShapeableImageView;

public class Interface13 extends AppCompatActivity {

    private ImageButton btnBack;
    private ShapeableImageView imgProfile; // Déclaration de l'image de profil
    private TextView tvName, tvMatchPercentage, tvUniversiteAge, tvBudget, tvLocalisation;
    private ChipGroup chipGroupModeDeVie, chipGroupPreferences;
    private MaterialButton btnContact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.interface13);

        // 1. Liaison des vues XML
        btnBack = findViewById(R.id.btnBack);
        imgProfile = findViewById(R.id.imgProfile); // Liaison de l'ImageView
        tvName = findViewById(R.id.tvName);
        tvMatchPercentage = findViewById(R.id.tvMatchPercentage);
        tvUniversiteAge = findViewById(R.id.tvUniversiteAge);
        tvBudget = findViewById(R.id.tvBudget);
        tvLocalisation = findViewById(R.id.tvLocalisation);
        chipGroupModeDeVie = findViewById(R.id.chipGroupModeDeVie);
        chipGroupPreferences = findViewById(R.id.chipGroupPreferences);
        btnContact = findViewById(R.id.btnContact);

        // 2. Gestion du bouton Retour
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // 3. Récupération des données envoyées par l'Intent
        String nom = getIntent().getStringExtra("NOM");
        int pourcentage = getIntent().getIntExtra("POURCENTAGE", 0);
        String universite = getIntent().getStringExtra("UNIVERSITE");
        int age = getIntent().getIntExtra("AGE", 21);
        double budget = getIntent().getDoubleExtra("BUDGET", 0.0);
        String quartier = getIntent().getStringExtra("QUARTIER");
        String modeDeVie = getIntent().getStringExtra("MODE_DE_VIE");
        String proprete = getIntent().getStringExtra("PROPRETE");
        String animaux = getIntent().getStringExtra("ANIMAUX");
        String photoUrl = getIntent().getStringExtra("PHOTO_URL"); // Récupération de l'URL de la photo

        // 4. Injection dans les TextViews et affichage de la photo
        if (tvName != null) {
            tvName.setText(nom != null ? nom : "Nom indisponible");
        }

        if (tvMatchPercentage != null) {
            tvMatchPercentage.setText("✿ " + pourcentage + "% compatible");
        }

        if (tvUniversiteAge != null) {
            String univ = (universite != null && !universite.isEmpty()) ? universite : "Université";
            tvUniversiteAge.setText(univ + "  •  " + age + " ans");
        }

        if (tvBudget != null) {
            tvBudget.setText("Budget : " + budget + " DT");
        }

        if (tvLocalisation != null) {
            tvLocalisation.setText(quartier != null ? quartier : "Non spécifié");
        }

        // Chargement de la photo de l'étudiant via Glide
        if (imgProfile != null && photoUrl != null && !photoUrl.isEmpty()) {
            Glide.with(this)
                    .load(photoUrl)
                    .placeholder(R.drawable.coloc) // Image par défaut pendant le chargement
                    .error(R.drawable.coloc)       // Image par défaut en cas d'erreur
                    .into(imgProfile);
        }

        // 5. Injection dynamique du Mode de vie (avec conservation du style et des couleurs du XML)
        if (chipGroupModeDeVie != null) {
            chipGroupModeDeVie.removeAllViews(); // Supprime les chips statiques du XML
            if (modeDeVie != null && !modeDeVie.isEmpty()) {
                String[] elements = modeDeVie.split(",");
                for (String element : elements) {
                    Chip chip = new Chip(this);
                    chip.setText(element.trim());

                    // Forcer les couleurs et le style harmonisés (Correction du multiplicateur *)
                    chip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#F1F5F9")));
                    chip.setTextColor(Color.parseColor("#475569"));
                    chip.setChipCornerRadius(getResources().getDisplayMetrics().density * 18);

                    chipGroupModeDeVie.addView(chip);
                }
            }
        }

        // 6. Injection dynamique des Préférences (Propreté + Animaux)
        if (chipGroupPreferences != null) {
            chipGroupPreferences.removeAllViews(); // Supprime les chips statiques du XML

            // Ajout du chip Propreté
            if (proprete != null && !proprete.isEmpty()) {
                Chip chipProprete = new Chip(this);
                chipProprete.setText("Propreté : " + proprete.trim());

                chipProprete.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#F1F5F9")));
                chipProprete.setTextColor(Color.parseColor("#475569"));
                chipProprete.setChipCornerRadius(getResources().getDisplayMetrics().density * 18);

                chipGroupPreferences.addView(chipProprete);
            }

            // Ajout du chip Animaux
            if (animaux != null && !animaux.isEmpty()) {
                Chip chipAnimaux = new Chip(this);
                chipAnimaux.setText("Animaux : " + animaux.trim());

                chipAnimaux.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#F1F5F9")));
                chipAnimaux.setTextColor(Color.parseColor("#475569"));
                chipAnimaux.setChipCornerRadius(getResources().getDisplayMetrics().density * 18);

                chipGroupPreferences.addView(chipAnimaux);
            }
        }

        // 7. Gestion du bouton "Contacter"
        if (btnContact != null) {
            btnContact.setOnClickListener(v -> {
                Toast.makeText(this, "Action Contacter pour " + (nom != null ? nom : "ce profil"), Toast.LENGTH_SHORT).show();
            });
        }
    }
}