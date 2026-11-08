
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

        // Apparence des éléments
        for (int i = 0; i < itemIds.length; i++) {

            boolean active = itemIds[i] == pageActive;

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
        }

        // Accueil
        View accueil = activity.findViewById(R.id.nav_prop_accueil);
        if (accueil != null) {
            accueil.setOnClickListener(v ->
                    ouvrir(activity, AccueilProprietaire.class,
                            pageActive, R.id.nav_prop_accueil));
        }

        // Mes demandes
        View annonces = activity.findViewById(R.id.nav_prop_annonces);
        if (annonces != null) {
            annonces.setOnClickListener(v ->
                    ouvrir(activity, interface20.class,
                            pageActive, R.id.nav_prop_annonces));
        }

        // Messages
        View messages = activity.findViewById(R.id.nav_prop_messages);
        if (messages != null) {
            messages.setOnClickListener(v ->
                    ouvrir(activity, Interface21.class,
                            pageActive, R.id.nav_prop_messages));
        }

        // Profil
        View profil = activity.findViewById(R.id.nav_prop_profil);
        if (profil != null) {
            profil.setOnClickListener(v ->
                    ouvrir(activity, interface22.class,
                            pageActive, R.id.nav_prop_profil));
        }
    }

    private static void ouvrir(
            Activity activity,
            Class<?> destination,
            int pageActive,
            int pageDemandee) {

        if (pageActive == pageDemandee) {
            return;
        }

        Intent intent = new Intent(activity, destination);

        intent.putExtra(
                "nomProprietaire",
                activity.getIntent().getStringExtra("nomProprietaire")
        );

        intent.putExtra(
                "emailProprietaire",
                activity.getIntent().getStringExtra("emailProprietaire")
        );

        activity.startActivity(intent);
    }
}
