
package com.example.diar;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class NavigationEtudiant {

    private static final int VERT = Color.parseColor("#2E9E5B");
    private static final int GRIS = Color.parseColor("#5F6B7A");

    public static void setup(Activity activity, int pageActive) {

        // Afficher la barre étudiant
        View barreEtudiant = activity.findViewById(
                R.id.barre_navigation_etudiant
        );

        View barreProp = activity.findViewById(
                R.id.barre_navigation_proprietaire
        );

        if (barreEtudiant != null) {
            barreEtudiant.setVisibility(View.VISIBLE);
        }

        if (barreProp != null) {
            barreProp.setVisibility(View.GONE);
        }

        int[] itemIds = {
                R.id.nav_accueil,
                R.id.nav_carte,
                R.id.nav_favoris,
                R.id.nav_chat,
                R.id.nav_profil
        };

        int[] iconIds = {
                R.id.icon_accueil,
                R.id.icon_carte,
                R.id.icon_favoris,
                R.id.icon_chat,
                R.id.icon_profil
        };

        int[] textIds = {
                R.id.text_accueil,
                R.id.text_carte,
                R.id.text_favoris,
                R.id.text_chat,
                R.id.text_profil
        };

        for (int i = 0; i < itemIds.length; i++) {

            final int index = i;

            boolean active = i == pageActive;

            View item = activity.findViewById(itemIds[i]);
            ImageView icon = activity.findViewById(iconIds[i]);
            TextView text = activity.findViewById(textIds[i]);

            if (icon != null) {
                icon.setColorFilter(active ? VERT : GRIS);
            }

            if (text != null) {
                text.setTextColor(active ? VERT : GRIS);

                text.setTypeface(
                        null,
                        active ? Typeface.BOLD : Typeface.NORMAL
                );
            }

            if (item != null) {
                item.setOnClickListener(v ->
                        ouvrir(activity, index, pageActive)
                );
            }
        }
    }

    private static void ouvrir(
            Activity activity,
            int nouvellePage,
            int pageActive) {

        if (nouvellePage == pageActive) {
            return;
        }

        Class<?>[] destinations = {
                Interface7.class,
                Interface9.class,
                Interface11.class,
                Interface21.class,
                interface22.class
        };

        if (nouvellePage < 0 || nouvellePage >= destinations.length) {
            return;
        }

        Intent intent = new Intent(
                activity,
                destinations[nouvellePage]
        );

        intent.putExtra("typeCompte", "ETUDIANT");
        intent.putExtra("typeUtilisateur", "etudiant");

        SessionManager sessionManager =
                new SessionManager(activity);

        String email = sessionManager.getEmail();

        if (email == null) {
            email = activity.getIntent().getStringExtra("emailEtudiant");
        }

        intent.putExtra("emailEtudiant", email);

        activity.startActivity(intent);
        activity.finish();
    }
}
