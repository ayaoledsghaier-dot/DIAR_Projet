package com.example.diar;

import android.os.Bundle;

public class Interface4 extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.interface4); // Lie la classe au fichier XML ci-dessus

        // Active automatiquement la barre de navigation du bas
        setupBottomNavigation();

        // Active automatiquement la flèche de retour en haut à gauche
        setupBackButton(Interface3.class);
    }
}