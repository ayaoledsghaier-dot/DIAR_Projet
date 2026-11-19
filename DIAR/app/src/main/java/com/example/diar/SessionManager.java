
package com.example.diar;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "DiarUserSession";
    private static final String KEY_EMAIL = "USER_EMAIL";
    private static final String KEY_NAME = "USER_NAME";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(
                PREF_NAME,
                Context.MODE_PRIVATE
        );
    }

    // Sauvegarder la session
    public void sauvegarderSession(String email, String nom) {
        prefs.edit()
                .putString(KEY_EMAIL, email)
                .putString(KEY_NAME, nom)
                .apply();
    }

    // Récupérer l'email
    public String getEmail() {
        return prefs.getString(KEY_EMAIL, null);
    }

    // Récupérer le nom
    public String getNom() {
        return prefs.getString(KEY_NAME, null);
    }

    // Déconnexion
    public void deconnecter() {
        prefs.edit().clear().apply();
    }
}
