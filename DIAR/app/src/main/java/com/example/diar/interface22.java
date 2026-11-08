package com.example.diar;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class interface22 extends AppCompatActivity {

    private EditText edtNom;
    private EditText edtEmail;
    private EditText edtTelephone;
    private TextView tvNom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.interface22);

        // Activer le bouton Profil
        NavigationProprietaire.setup(this, R.id.nav_prop_profil);

        // Récupérer les champs
        edtNom = findViewById(R.id.edtNomProfil);
        edtEmail = findViewById(R.id.edtEmailProfil);
        edtTelephone = findViewById(R.id.edtTelephoneProfil);
        tvNom = findViewById(R.id.tvNomProfil);

        // Récupérer les informations du propriétaire
        String nom = getIntent().getStringExtra("nomProprietaire");
        String email = getIntent().getStringExtra("emailProprietaire");

        if (nom != null) {
            edtNom.setText(nom);
            tvNom.setText(nom);
        }

        if (email != null) {
            edtEmail.setText(email);
        }

        // Bouton Modifier le profil
        findViewById(R.id.btnModifierProfil).setOnClickListener(v -> {

            String nouveauNom = edtNom.getText().toString().trim();
            String nouvelEmail = edtEmail.getText().toString().trim();
            String telephone = edtTelephone.getText().toString().trim();

            if (nouveauNom.isEmpty() || nouvelEmail.isEmpty()) {
                Toast.makeText(
                        this,
                        "Veuillez remplir le nom et l'email",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            if (!nouvelEmail.contains("@")) {
                Toast.makeText(
                        this,
                        "Adresse email invalide",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            // Modification locale seulement, sans backend
            tvNom.setText(nouveauNom);

            Toast.makeText(
                    this,
                    "Modifications appliquées à l'écran",
                    Toast.LENGTH_SHORT
            ).show();
        });

        // Bouton Déconnexion
        findViewById(R.id.btnDeconnexionProfil).setOnClickListener(v -> {

            Intent intent = new Intent(this, Interface3.class);

            intent.setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
            );

            startActivity(intent);
            finish();
        });
    }
}