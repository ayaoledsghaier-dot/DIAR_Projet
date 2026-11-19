
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

    private boolean proprietaire;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.interface22);

        // 1. Déterminer le type de compte
        String type = getIntent().getStringExtra("typeCompte");

        if (type == null) {
            type = getIntent().getStringExtra("typeUtilisateur");
        }

        proprietaire =
                "PROPRIETAIRE".equalsIgnoreCase(type)
                        || "proprietaire".equalsIgnoreCase(type)
                        || getIntent().getStringExtra("emailProprietaire") != null;

        // 2. Configurer la navigation
        if (proprietaire) {
            NavigationProprietaire.setup(
                    this,
                    R.id.nav_prop_profil
            );
        } else {
            NavigationEtudiant.setup(this, 4);
        }

        // 3. Récupérer les champs
        edtNom = findViewById(R.id.edtNomProfil);
        edtEmail = findViewById(R.id.edtEmailProfil);
        edtTelephone = findViewById(R.id.edtTelephoneProfil);
        tvNom = findViewById(R.id.tvNomProfil);

        // 4. Charger les informations
        if (proprietaire) {

            String nom = getIntent().getStringExtra(
                    "nomProprietaire"
            );

            String email = getIntent().getStringExtra(
                    "emailProprietaire"
            );

            if (nom != null && edtNom != null && tvNom != null) {
                edtNom.setText(nom);
                tvNom.setText(nom);
            } else if (tvNom != null) {
                tvNom.setText("Profil propriétaire");
            }

            if (email != null && edtEmail != null) {
                edtEmail.setText(email);
            }

        } else {

            SessionManager sessionManager =
                    new SessionManager(this);

            String emailEtudiant = sessionManager.getEmail();

            if (emailEtudiant != null && edtEmail != null) {
                edtEmail.setText(emailEtudiant);
            }

            if (tvNom != null) {
                tvNom.setText("Profil étudiant");
            }
        }

        // 5. Modifier le profil
        if (findViewById(R.id.btnModifierProfil) != null) {

            findViewById(R.id.btnModifierProfil)
                    .setOnClickListener(v -> {

                        String nouveauNom = edtNom.getText()
                                .toString().trim();

                        String nouvelEmail = edtEmail.getText()
                                .toString().trim();

                        String telephone = edtTelephone.getText()
                                .toString().trim();

                        if (nouveauNom.isEmpty()
                                || nouvelEmail.isEmpty()) {

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

                        if (tvNom != null) {
                            tvNom.setText(nouveauNom);
                        }

                        Toast.makeText(
                                this,
                                "Modifications appliquées à l'écran",
                                Toast.LENGTH_SHORT
                        ).show();
                    });
        }

        // 6. Déconnexion
        if (findViewById(R.id.btnDeconnexionProfil) != null) {

            findViewById(R.id.btnDeconnexionProfil)
                    .setOnClickListener(v -> {

                        Intent intent = new Intent(
                                interface22.this,
                                Interface3.class
                        );

                        intent.setFlags(
                                Intent.FLAG_ACTIVITY_NEW_TASK |
                                        Intent.FLAG_ACTIVITY_CLEAR_TASK
                        );

                        startActivity(intent);
                        finish();
                    });
        }
    }
}
