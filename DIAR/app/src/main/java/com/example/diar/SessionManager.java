package com.example.diar;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "DiarUserSession";
    private static final String KEY_EMAIL = "USER_EMAIL";
    private static final String KEY_NAME = "USER_NAME"; // Clé pour stocker le nom
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    // Sauvegarder l'e-mail et le nom complet en même temps
    public void sauvegarderSession(String email, String nom) {
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_NAME, nom);
        editor.apply();
    }

    // Récupérer l'e-mail
    public String getEmail() {
        return prefs.getString(KEY_EMAIL, null);
    }

    // Récupérer le nom complet dans n'importe quelle interface
    public String getNom() {
        return prefs.getString(KEY_NAME, null);
    }

    // Effacer la session (déconnexion)
    public void deconnecter() {
        editor.clear();
        editor.apply();
    }
}