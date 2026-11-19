package com.example.diar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;

public class Interface4 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.interface4);

        // Bouton retour vers Interface3
        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                startActivity(new Intent(Interface4.this, Interface3.class));
                finish();
            });
        }

        // Récupérer les deux cartes
        MaterialCardView cardStudent = findViewById(R.id.cardStudent);
        MaterialCardView cardOwner = findViewById(R.id.cardOwner);

        // Cliquer sur Étudiant
        cardStudent.setOnClickListener(v -> {
            Intent intent = new Intent(Interface4.this, Interfacecreercompteetudiant.class);
            startActivity(intent);
        });

        // Cliquer sur Propriétaire
        cardOwner.setOnClickListener(v -> {
            Intent intent = new Intent(Interface4.this, interface_creer_compte_proprietaire.class);
            startActivity(intent);
        });
    }
}