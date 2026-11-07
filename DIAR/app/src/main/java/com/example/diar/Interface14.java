package com.example.diar;

import android.os.Bundle;

public class Interface14 extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.interface14);

        // 1. Active la barre de navigation du bas (Accueil, Carte, Chat, Profil)
        setupBottomNavigation();

        // 2. Active la flèche de retour (btnBack) en haut à gauche
        setupBackButton();
    }
}