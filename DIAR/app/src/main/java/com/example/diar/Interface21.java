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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.interface21);

        // Activer le bouton Messages
        NavigationProprietaire.setup(this, R.id.nav_prop_messages);

        // Récupérer les conversations
        conversationAhmed = findViewById(R.id.conversationAhmed);
        conversationMariem = findViewById(R.id.conversationMariem);
        conversationYassine = findViewById(R.id.conversationYassine);

        // Ouvrir les conversations
        ouvrirConversation(conversationAhmed, "Ahmed Ben Ali");
        ouvrirConversation(conversationMariem, "Mariem Trabelsi");
        ouvrirConversation(conversationYassine, "Yassine Mansour");

        // Barre de recherche
        configurerRecherche();
    }

    private void ouvrirConversation(LinearLayout conversation, String nom) {

        if (conversation == null) return;

        conversation.setOnClickListener(v -> {

            Intent intent = new Intent(
                    Interface21.this,
                    ChatProprietaire.class
            );

            intent.putExtra("nomEtudiant", nom);

            // Garder les informations du propriétaire
            intent.putExtra(
                    "nomProprietaire",
                    getIntent().getStringExtra("nomProprietaire")
            );

            intent.putExtra(
                    "emailProprietaire",
                    getIntent().getStringExtra("emailProprietaire")
            );

            startActivity(intent);
        });
    }

    private void configurerRecherche() {

        EditText recherche = findViewById(R.id.edtRechercheConversation);

        if (recherche == null) return;

        recherche.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(
                    CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s, int start, int before, int count) {

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

        if (nom.contains(recherche)) {
            conversation.setVisibility(View.VISIBLE);
        } else {
            conversation.setVisibility(View.GONE);
        }
    }
}