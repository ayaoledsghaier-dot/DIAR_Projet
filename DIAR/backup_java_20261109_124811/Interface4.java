
package com.example.diar;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.card.MaterialCardView;

public class Interface4 extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.interface4);

        // Activer la barre de navigation
        setupBottomNavigation();

        // Bouton retour
        setupBackButton(Interface3.class);

        // Récupérer les deux cartes
        MaterialCardView cardStudent = findViewById(R.id.cardStudent);
        MaterialCardView cardOwner = findViewById(R.id.cardOwner);

        // Cliquer sur Étudiant
        cardStudent.setOnClickListener(v -> {
            Intent intent = new Intent(Interface4.this,
                    Interfacecreercompteetudiant.class);
            startActivity(intent);
        });

        // Cliquer sur Propriétaire
        cardOwner.setOnClickListener(v -> {
            Intent intent = new Intent(Interface4.this,
                    Interface5.class);
            startActivity(intent);
        });
    }
}
