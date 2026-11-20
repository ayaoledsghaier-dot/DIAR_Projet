package com.example.diar;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.LinearLayout;

public class NavigationHelper {

    public static void setup(Activity activity) {

        LinearLayout accueil = activity.findViewById(R.id.navAccueil);
        LinearLayout carte = activity.findViewById(R.id.navCarte);
        LinearLayout profil = activity.findViewById(R.id.navProfil);
        View chat = activity.findViewById(R.id.navChat); // Utilisation de View pour éviter les erreurs de type

        if (accueil != null) {
            accueil.setOnClickListener(v ->
                    navigate(activity, Interface7.class));
        }

        if (carte != null) {
            carte.setOnClickListener(v ->
                    navigate(activity, Interface9.class));
        }

        if (profil != null) {
            profil.setOnClickListener(v ->
                    navigate(activity, Interface17.class));
        }

        if (chat != null) {
            chat.setOnClickListener(v ->
                    navigate(activity, Interface14.class));
        }
    }

    private static void navigate(
            Activity currentActivity,
            Class<?> targetActivity) {

        if (currentActivity.getClass().equals(targetActivity)) {
            return;
        }

        Intent intent = new Intent(currentActivity, targetActivity);
        // Ajout des drapeaux pour une navigation fluide et propre
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        currentActivity.startActivity(intent);
        currentActivity.overridePendingTransition(0, 0);
    }
}