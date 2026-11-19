
package com.example.diar;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

public class Interface21 extends AppCompatActivity {

    private LinearLayout conversationAhmed;
    private LinearLayout conversationMariem;
    private LinearLayout conversationYassine;

    private boolean proprietaire;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.interface21);

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
                    R.id.nav_prop_messages
            );
        } else {
            NavigationEtudiant.setup(this, 3);
        }

        // 3. Récupérer les conversations
        conversationAhmed = findViewById(R.id.conversationAhmed);
        conversationMariem = findViewById(R.id.conversationMariem);
        conversationYassine = findViewById(R.id.conversationYassine);

        // 4. Ouvrir les conversations
        ouvrirConversation(conversationAhmed, "Ahmed Ben Ali");
        ouvrirConversation(conversationMariem, "Mariem Trabelsi");
        ouvrirConversation(conversationYassine, "Yassine Mansour");

        // 5. Configurer la recherche
        configurerRecherche();
    }

    private void ouvrirConversation(
            LinearLayout conversation,
            String nom) {

        if (conversation == null) return;

        conversation.setOnClickListener(v -> {

            Intent intent = new Intent(
                    Interface21.this,
                    ChatProprietaire.class
            );

            intent.putExtra("nomEtudiant", nom);

            intent.putExtra(
                    "typeCompte",
                    proprietaire ? "PROPRIETAIRE" : "ETUDIANT"
            );

            intent.putExtra(
                    "typeUtilisateur",
                    proprietaire ? "proprietaire" : "etudiant"
            );

            if (proprietaire) {

                intent.putExtra(
                        "nomProprietaire",
                        getIntent().getStringExtra("nomProprietaire")
                );

                intent.putExtra(
                        "emailProprietaire",
                        getIntent().getStringExtra("emailProprietaire")
                );

            } else {

                SessionManager sessionManager =
                        new SessionManager(this);

                intent.putExtra(
                        "emailEtudiant",
                        sessionManager.getEmail()
                );
            }

            startActivity(intent);
        });
    }

    private void configurerRecherche() {

        EditText recherche =
                findViewById(R.id.edtRechercheConversation);

        if (recherche == null) return;

        recherche.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(
                    CharSequence s,
                    int start,
                    int count,
                    int after) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s,
                    int start,
                    int before,
                    int count) {

                String texte = s.toString()
                        .trim()
                        .toLowerCase();

                filtrerConversation(
                        conversationAhmed,
                        "ahmed ben ali",
                        texte
                );

                filtrerConversation(
                        conversationMariem,
                        "mariem trabelsi",
                        texte
                );

                filtrerConversation(
                        conversationYassine,
                        "yassine mansour",
                        texte
                );
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void filtrerConversation(
            LinearLayout conversation,
            String nom,
            String recherche) {

        if (conversation == null) return;

        conversation.setVisibility(
                nom.contains(recherche)
                        ? View.VISIBLE
                        : View.GONE
        );
    }
}
