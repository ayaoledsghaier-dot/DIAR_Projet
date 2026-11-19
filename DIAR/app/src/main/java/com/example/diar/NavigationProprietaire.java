
package com.example.diar;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class NavigationProprietaire {

    private static final int VERT = Color.parseColor("#1B8A5A");
    private static final int GRIS = Color.parseColor("#64748B");

    public static void setup(Activity activity, int pageActive) {

        // Afficher la barre propriétaire
        View barreProp = activity.findViewById(
                R.id.barre_navigation_proprietaire
        );

        View barreEtudiant = activity.findViewById(
                R.id.barre_navigation_etudiant
        );

        if (barreProp != null) {
            barreProp.setVisibility(View.VISIBLE);
        }

        if (barreEtudiant != null) {
            barreEtudiant.setVisibility(View.GONE);
        }

        int[] itemIds = {
                R.id.nav_prop_accueil,
                R.id.nav_prop_annonces,
                R.id.nav_prop_messages,
                R.id.nav_prop_profil
        };

        int[] iconIds = {
                R.id.ic_nav_prop_accueil,
                R.id.ic_nav_prop_annonces,
                R.id.ic_nav_prop_messages,
                R.id.ic_nav_prop_profil
        };

        int[] textIds = {
                R.id.tv_nav_prop_accueil,
                R.id.tv_nav_prop_annonces,
                R.id.tv_nav_prop_messages,
                R.id.tv_nav_prop_profil
        };

        int[] indicatorIds = {
                R.id.indicator_nav_prop_accueil,
                R.id.indicator_nav_prop_annonces,
                R.id.indicator_nav_prop_messages,
                R.id.indicator_nav_prop_profil
        };

        for (int i = 0; i < itemIds.length; i++) {

            final int destinationId = itemIds[i];

            boolean active = destinationId == pageActive;

            View item = activity.findViewById(itemIds[i]);
            ImageView icon = activity.findViewById(iconIds[i]);
            TextView text = activity.findViewById(textIds[i]);
            View indicator = activity.findViewById(indicatorIds[i]);

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

            if (indicator != null) {
                indicator.setVisibility(
                        active ? View.VISIBLE : View.INVISIBLE
                );
            }

            if (item != null) {
                item.setOnClickListener(v ->
                        ouvrir(activity, pageActive, destinationId)
                );
            }
        }
    }

    private static void ouvrir(
            Activity activity,
            int pageActive,
            int pageDemandee) {

        if (pageActive == pageDemandee) {
            return;
        }

        Class<?> destination;

        if (pageDemandee == R.id.nav_prop_accueil) {

            destination = AccueilProprietaire.class;

        } else if (pageDemandee == R.id.nav_prop_annonces) {

            destination = interface20.class;

        } else if (pageDemandee == R.id.nav_prop_messages) {

            destination = Interface21.class;

        } else if (pageDemandee == R.id.nav_prop_profil) {

            destination = interface22.class;

        } else {
            return;
        }

        Intent intent = new Intent(activity, destination);

        intent.putExtra("typeCompte", "PROPRIETAIRE");

        intent.putExtra(
                "nomProprietaire",
                activity.getIntent().getStringExtra("nomProprietaire")
        );

        intent.putExtra(
                "emailProprietaire",
                activity.getIntent().getStringExtra("emailProprietaire")
        );

        activity.startActivity(intent);
        activity.finish();
    }
}
