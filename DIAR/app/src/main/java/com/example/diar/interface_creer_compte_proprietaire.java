package com.example.diar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;

public class interface_creer_compte_proprietaire
        extends AppCompatActivity {

    // Correction : On retire le slash final pour éviter les doubles slashs dans les URL
    private static final String BASE_URL = "http://10.0.2.2:8083";

    private ImageView imageView;
    private Uri imageUri;

    private TextInputEditText etFullNameProp;
    private TextInputEditText etEmailProp;
    private TextInputEditText etPasswordProp;
    private TextInputEditText etPhoneProp;

    private RequestQueue requestQueue;
    private ActivityResultLauncher<String> galerieLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.interface_creer_compte_prop);

        imageView = findViewById(R.id.imageView);
        etFullNameProp = findViewById(R.id.etFullNameProp);
        etEmailProp = findViewById(R.id.etEmailProp);
        etPasswordProp = findViewById(R.id.etPasswordProp);
        etPhoneProp = findViewById(R.id.etPhoneProp);

        requestQueue = Volley.newRequestQueue(this);

        galerieLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        imageUri = uri;
                        imageView.setImageURI(uri);
                    }
                });

        imageView.setOnClickListener(v ->
                galerieLauncher.launch("image/*"));

        findViewById(R.id.btnBack).setOnClickListener(v ->
                finish());

        findViewById(R.id.btnSubmitProp).setOnClickListener(v ->
                inscrireProprietaire());
    }

    private String getText(TextInputEditText editText) {
        return editText.getText() == null
                ? ""
                : editText.getText().toString().trim();
    }

    private void inscrireProprietaire() {
        String nom = getText(etFullNameProp);
        String email = getText(etEmailProp);
        String motDePasse = getText(etPasswordProp);
        String telephone = getText(etPhoneProp);

        if (nom.isEmpty() || email.isEmpty()
                || motDePasse.isEmpty() || telephone.isEmpty()) {
            Toast.makeText(this,
                    "Veuillez remplir tous les champs.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS
                .matcher(email).matches()) {
            Toast.makeText(this,
                    "Adresse email invalide.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        if (motDePasse.length() < 8) {
            Toast.makeText(this,
                    "Le mot de passe doit contenir au moins 8 caractères.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        findViewById(R.id.btnSubmitProp).setEnabled(false);

        try {
            JSONObject donnees = new JSONObject();
            donnees.put("nom", nom);
            donnees.put("email", email);
            donnees.put("motDePasse", motDePasse);
            donnees.put("telephone", telephone);

            // Vérifiez bien si votre route backend est "/api/proprietaires/verifier" 
            // ou si c'est "/api/proprietaires" (pour un POST d'ajout)
            String urlInscription = BASE_URL + "/api/proprietaires/verifier";

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    urlInscription,
                    donnees,

                    response -> {
                        findViewById(R.id.btnSubmitProp)
                                .setEnabled(true);

                        String message = response.optString(
                                "message",
                                "Compte propriétaire créé avec succès !");

                        Toast.makeText(this, message,
                                Toast.LENGTH_LONG).show();

                        Intent intent = new Intent(
                                interface_creer_compte_proprietaire.this,
                                Interface3.class);

                        startActivity(intent);
                        finish();
                    },

                    error -> {
                        findViewById(R.id.btnSubmitProp)
                                .setEnabled(true);

                        String message;

                        if (error.networkResponse != null) {
                            int code = error.networkResponse.statusCode;
                            String details = "";

                            try {
                                if (error.networkResponse.data != null) {
                                    details = new String(
                                            error.networkResponse.data,
                                            java.nio.charset.StandardCharsets.UTF_8);
                                }
                            } catch (Exception ignored) {
                                details = "";
                            }

                            message = "Erreur HTTP " + code
                                    + (details.isEmpty()
                                    ? ""
                                    : " : " + details);

                        } else {
                            message = "Erreur réseau : "
                                    + error.getClass().getSimpleName()
                                    + ". Vérifie l'adresse IP, le Wi-Fi "
                                    + "et que Spring Boot est démarré.";
                        }

                        Toast.makeText(this, message,
                                Toast.LENGTH_LONG).show();
                    }
            );

            requestQueue.add(request);

        } catch (Exception e) {
            findViewById(R.id.btnSubmitProp).setEnabled(true);

            Toast.makeText(this,
                    "Erreur : " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }
}