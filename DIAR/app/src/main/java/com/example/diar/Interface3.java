package com.example.diar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class Interface3 extends AppCompatActivity {

    private static final String TAG = "CONNEXION";

    private static final String URL_PROPRIETAIRE = Config.BASE_URL + "/api/proprietaires/connexion";
    private static final String URL_ETUDIANT = Config.BASE_URL + "/api/etudiants/connexion";

    private EditText etEmail;
    private EditText etPassword;
    private MaterialButton btnLogin;
    private MaterialButton btnRegister;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.interface3);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        requestQueue = Volley.newRequestQueue(getApplicationContext());

        btnRegister.setOnClickListener(v ->
                startActivity(new Intent(Interface3.this, Interface4.class)));

        btnLogin.setOnClickListener(v -> connecter());
    }

    private void connecter() {
        String email = etEmail.getText().toString().trim();
        String motDePasse = etPassword.getText().toString();

        if (email.isEmpty() || motDePasse.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs.", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject donnees = new JSONObject();
        try {
            donnees.put("email", email);
            donnees.put("motDePasse", motDePasse);
        } catch (Exception e) {
            Toast.makeText(this, "Erreur lors de la pr\u00e9paration des donn\u00e9es.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "Erreur JSON", e);
            return;
        }

        btnLogin.setEnabled(false);
        essayerProprietaire(donnees, email);
    }

    // ETAPE 1 : connexion proprietaire
    private void essayerProprietaire(JSONObject donnees, String email) {
        envoyer(URL_PROPRIETAIRE, donnees,
                response -> {
                    btnLogin.setEnabled(true);
                    Log.d(TAG, "Proprietaire OK : " + response);

                    Intent intent = new Intent(Interface3.this, AccueilProprietaire.class);
                    intent.putExtra("nomProprietaire", response.optString("nom", ""));
                    intent.putExtra("emailProprietaire", response.optString("email", email));
                    startActivity(intent);
                    finish();
                },
                error -> {
                    if (statusCode(error) == 401) {
                        // Pas un proprietaire : on tente le compte etudiant
                        essayerEtudiant(donnees, email);
                    } else {
                        afficherErreur(URL_PROPRIETAIRE, error);
                    }
                });
    }

    // ETAPE 2 : connexion etudiant
    private void essayerEtudiant(JSONObject donnees, String email) {
        envoyer(URL_ETUDIANT, donnees,
                response -> {
                    btnLogin.setEnabled(true);
                    Log.d(TAG, "Etudiant OK : " + response);

                    String emailRetour = response.optString("email", email.toLowerCase());
                    String nom = response.optString("nom", "");
                    String etape = response.optString("etape", "ACCUEIL");

                    // Interface5, 6 et 7 lisent l'email dans la session
                    new SessionManager(Interface3.this).sauvegarderSession(emailRetour, nom);

                    Intent intent = new Intent(Interface3.this, ecranPourEtape(etape));
                    intent.putExtra("USER_EMAIL", emailRetour);
                    intent.putExtra("USER_NAME", nom);
                    startActivity(intent);
                    finish();
                },
                error -> afficherErreur(URL_ETUDIANT, error));
    }

    // Reprend l'inscription la ou l'etudiant s'est arrete
    private Class<?> ecranPourEtape(String etape) {
        if ("PROFIL".equals(etape)) return Interface5.class;
        if ("PREFERENCES".equals(etape)) return Interface6.class;
        if ("VERIFICATION".equals(etape)) return Interfaceverification.class;
        if ("PROFIL_COLOC".equals(etape)) return Interfaceprofilecoloc.class;
        return Interface7.class;
    }

    private void envoyer(String url, JSONObject donnees,
                         Response.Listener<JSONObject> onSuccess,
                         Response.ErrorListener onError) {
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST, url, donnees, onSuccess, onError);

        request.setRetryPolicy(new DefaultRetryPolicy(
                15000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        request.setTag(TAG);
        requestQueue.add(request);
    }

    private int statusCode(VolleyError error) {
        return error.networkResponse != null ? error.networkResponse.statusCode : -1;
    }

    private void afficherErreur(String url, VolleyError error) {
        btnLogin.setEnabled(true);

        int code = statusCode(error);
        String details = "";
        if (error.networkResponse != null && error.networkResponse.data != null) {
            details = new String(error.networkResponse.data, StandardCharsets.UTF_8);
        }
        Log.e(TAG, "URL : " + url + " | HTTP : " + code + " | R\u00e9ponse : " + details, error);

        String message;
        if (code == 401) {
            message = "Email ou mot de passe incorrect.";
        } else if (code == 400) {
            message = "Donn\u00e9es incorrectes.";
        } else if (code == 404) {
            message = "Route API introuvable.";
        } else if (code == -1) {
            message = "Connexion impossible. V\u00e9rifie le Backend et ADB Reverse.";
        } else {
            message = "Erreur serveur HTTP : " + code;
        }
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (requestQueue != null) {
            requestQueue.cancelAll(TAG);
        }
    }
}