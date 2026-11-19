
package com.example.diar;

import android.app.Activity;
import android.view.View;

public class GestionNavigation {

    public static void afficher(
            Activity activity,
            int pageEtudiant,
            int pageProprietaire) {

        String type = activity.getIntent()
                .getStringExtra("typeCompte");

        View barreEtudiant = activity.findViewById(
                R.id.barre_navigation_etudiant);

        View barreProprietaire = activity.findViewById(
                R.id.barre_navigation_proprietaire);

        if ("ETUDIANT".equals(type)) {

            if (barreEtudiant != null) {
                barreEtudiant.setVisibility(View.VISIBLE);
            }

            if (barreProprietaire != null) {
                barreProprietaire.setVisibility(View.GONE);
            }

            NavigationEtudiant.setup(activity, pageEtudiant);

        } else if ("PROPRIETAIRE".equals(type)) {

            if (barreEtudiant != null) {
                barreEtudiant.setVisibility(View.GONE);
            }

            if (barreProprietaire != null) {
                barreProprietaire.setVisibility(View.VISIBLE);
            }

            NavigationProprietaire.setup(activity, pageProprietaire);

        } else {
            if (barreEtudiant != null) {
                barreEtudiant.setVisibility(View.GONE);
            }

            if (barreProprietaire != null) {
                barreProprietaire.setVisibility(View.GONE);
            }
        }
    }
}
