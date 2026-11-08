
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
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class Interface3 extends AppCompatActivity {

    private static final String TAG = "CONNEXION";

    // URL du Backend avec ADB Reverse
    private static final String URL = "http://192.168.1.22:8083/api/proprietaires/connexion";

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

        // Bouton inscription
        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(
                    Interface3.this,
                    Interface4.class
            );
            startActivity(intent);
        });

        // Bouton connexion
        btnLogin.setOnClickListener(v -> connecter());
    }

    private void connecter() {

        String email = etEmail.getText().toString().trim();
        String motDePasse = etPassword.getText().toString();

        if (email.isEmpty() || motDePasse.isEmpty()) {
            Toast.makeText(
                    this,
                    "Veuillez remplir tous les champs.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        btnLogin.setEnabled(false);

        JSONObject donnees = new JSONObject();

        try {
            donnees.put("email", email);
            donnees.put("motDePasse", motDePasse);

        } catch (Exception e) {

            btnLogin.setEnabled(true);

            Toast.makeText(
                    this,
                    "Erreur lors de la préparation des données.",
                    Toast.LENGTH_LONG
            ).show();

            Log.e(TAG, "Erreur JSON", e);
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                URL,
                donnees,

                // SUCCESS
                response -> {

                    btnLogin.setEnabled(true);

                    Log.d(TAG, "Réponse API : " + response);

                    String nom = response.optString("nom", "");
                    String emailRetour = response.optString("email", email);

                    Intent intent = new Intent(
                            Interface3.this,
                            AccueilProprietaire.class
                    );

                    intent.putExtra("nomProprietaire", nom);
                    intent.putExtra("emailProprietaire", emailRetour);

                    startActivity(intent);
                    finish();
                },

                // ERROR
                error -> {

                    btnLogin.setEnabled(true);

                    int statusCode = error.networkResponse != null
                            ? error.networkResponse.statusCode : -1;

                    String details = "";

                    if (error.networkResponse != null
                            && error.networkResponse.data != null) {

                        details = new String(
                                error.networkResponse.data,
                                StandardCharsets.UTF_8
                        );
                    }

                    Log.e(
                            TAG,
                            "URL : " + URL
                                    + " | HTTP : " + statusCode
                                    + " | Réponse : " + details,
                            error
                    );

                    String message;

                    if (statusCode == 401) {
                        message = "Email ou mot de passe incorrect.";

                    } else if (statusCode == 400) {
                        message = "Données incorrectes.";

                    } else if (statusCode == 404) {
                        message = "Route API introuvable.";

                    } else if (statusCode == -1) {
                        message = "Connexion impossible. Vérifie le Backend et ADB Reverse.";

                    } else {
                        message = "Erreur serveur HTTP : " + statusCode;
                    }

                    Toast.makeText(
                            Interface3.this,
                            message,
                            Toast.LENGTH_LONG
                    ).show();
                }
        );

        request.setRetryPolicy(
                new DefaultRetryPolicy(
                        15000,
                        0,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
        );

        request.setTag(TAG);
        requestQueue.add(request);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (requestQueue != null) {
            requestQueue.cancelAll(TAG);
        }
    }
}
