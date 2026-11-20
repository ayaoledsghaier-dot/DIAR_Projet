package com.example.diar;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;

public abstract class BaseActivity extends AppCompatActivity {

    /**
     * Configuration des clics pour la barre de navigation du bas
     */
    protected void setupBottomNavigation() {

        LinearLayout navAccueil = findViewById(R.id.navAccueil);
        LinearLayout navCarte = findViewById(R.id.navCarte);
        View navChat = findViewById(R.id.navChat);
        LinearLayout navProfil = findViewById(R.id.navProfil);

        Log.d("DEBUG_NAV", "Activity : " + getClass().getSimpleName());
        Log.d("DEBUG_NAV", "Accueil : " + (navAccueil != null));
        Log.d("DEBUG_NAV", "Carte : " + (navCarte != null));
        Log.d("DEBUG_NAV", "Chat : " + (navChat != null));
        Log.d("DEBUG_NAV", "Profil : " + (navProfil != null));

        if (navAccueil != null) {
            navAccueil.setOnClickListener(v -> navigateTo(Interface7.class));
        }

        if (navCarte != null) {
            navCarte.setOnClickListener(v -> navigateTo(Interface9.class));
        }

        if (navChat != null) {
            navChat.setOnClickListener(v -> navigateTo(Interface14.class));
        }

        if (navProfil != null) {
            navProfil.setOnClickListener(v -> navigateTo(Interface17.class));
        }
    }

    protected void setupBackButton() {
        setupBackButton(MainActivity1.class);
    }

    protected void setupBackButton(Class<?> targetActivity) {
        View btnBack = findViewById(R.id.btnBack);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (this.getClass().equals(targetActivity)) {
                    finish();
                } else {
                    navigateTo(targetActivity);
                }
            });
        }
    }

    /**
     * Méthode de navigation universelle et fluide
     */
    private void navigateTo(Class<?> targetActivity) {
        if (!this.getClass().equals(targetActivity)) {
            Intent intent = new Intent(this, targetActivity);
            // Évite d'empiler les mêmes activités en boucle et garde un historique propre
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(0, 0);
        }
    }
}