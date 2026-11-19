
package com.example.diar;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

public abstract class ChatBaseActivity extends AppCompatActivity {

    private LinearLayout layoutMessages;
    private EditText edtMessage;
    private ScrollView scrollMessages;

    private String etudiantEmail;
    private String etudiantNom;
    private String proprietaireEmail;
    private String proprietaireNom;
    private String currentUserEmail;
    private String currentUserRole;

    private long annonceId;
    private long conversationId = -1;

    protected abstract String getUserRole();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        layoutMessages = findViewById(R.id.layoutMessages);
        edtMessage = findViewById(R.id.edtMessage);
        scrollMessages = findViewById(R.id.scrollMessages);

        findViewById(R.id.btnRetourMessages)
                .setOnClickListener(v -> finish());

        Button btnEnvoyer =
                findViewById(R.id.btnEnvoyerMessage);

        btnEnvoyer.setOnClickListener(v -> envoyerMessage());

        // Informations de la conversation
        etudiantEmail = getIntent().getStringExtra("etudiantEmail");
        etudiantNom = getIntent().getStringExtra("etudiantNom");

        proprietaireEmail =
                getIntent().getStringExtra("proprietaireEmail");

        proprietaireNom =
                getIntent().getStringExtra("proprietaireNom");

        annonceId = getIntent().getLongExtra("annonceId", -1);

        currentUserRole = getUserRole();

        if ("ETUDIANT".equals(currentUserRole)) {
            currentUserEmail = etudiantEmail;
        } else {
            currentUserEmail = proprietaireEmail;
        }

        String nomContact;

        if ("ETUDIANT".equals(currentUserRole)) {
            nomContact = proprietaireNom;
        } else {
            nomContact = etudiantNom;
        }

        TextView tvNom = findViewById(R.id.tvNomContact);

        tvNom.setText(
                nomContact == null ? "Contact" : nomContact
        );

        if (currentUserEmail == null ||
                etudiantEmail == null ||
                proprietaireEmail == null ||
                annonceId <= 0) {

            Toast.makeText(
                    this,
                    "Informations de conversation manquantes",
                    Toast.LENGTH_LONG
            ).show();

            finish();
            return;
        }

        // Charger les messages si conversation existante
        conversationId = getIntent().getLongExtra(
                "conversationId", -1
        );

        if (conversationId > 0) {
            chargerMessages();
        }
    }

    // Envoyer un message au Backend
    private void envoyerMessage() {

        String contenu = edtMessage.getText()
                .toString().trim();

        if (contenu.isEmpty()) {
            Toast.makeText(
                    this,
                    "Écrivez un message",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        try {
            JSONObject data = new JSONObject();

            data.put("etudiantEmail", etudiantEmail);
            data.put("etudiantNom", etudiantNom);

            data.put("proprietaireEmail", proprietaireEmail);
            data.put("proprietaireNom", proprietaireNom);

            data.put("annonceId", annonceId);

            data.put("senderEmail", currentUserEmail);
            data.put("senderRole", currentUserRole);
            data.put("contenu", contenu);

            ChatApi.envoyerMessage(
                    this,
                    data,
                    new ChatApi.ObjectCallback() {

                        @Override
                        public void onSuccess(JSONObject response) {

                            edtMessage.setText("");

                            // Récupérer l'identifiant de conversation
                            conversationId = response.optLong(
                                    "conversationId", conversationId
                            );

                            chargerMessages();

                            Toast.makeText(
                                    ChatBaseActivity.this,
                                    "Message envoyé",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }

                        @Override
                        public void onError(String error) {

                            Toast.makeText(
                                    ChatBaseActivity.this,
                                    "Erreur : " + error,
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }
            );

        } catch (Exception e) {
            Toast.makeText(
                    this,
                    e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    // Charger les messages depuis PostgreSQL
    private void chargerMessages() {

        if (conversationId <= 0) return;

        ChatApi.getMessages(
                this,
                conversationId,
                new ChatApi.ArrayCallback() {

                    @Override
                    public void onSuccess(JSONArray response) {

                        layoutMessages.removeAllViews();

                        for (int i = 0; i < response.length(); i++) {

                            JSONObject message =
                                    response.optJSONObject(i);

                            if (message == null) continue;

                            String contenu =
                                    message.optString("contenu");

                            String sender =
                                    message.optString("senderEmail");

                            ajouterBulle(
                                    contenu,
                                    sender.equals(currentUserEmail)
                            );
                        }

                        scrollMessages.post(() ->
                                scrollMessages.fullScroll(
                                        ScrollView.FOCUS_DOWN
                                )
                        );
                    }

                    @Override
                    public void onError(String error) {

                        Toast.makeText(
                                ChatBaseActivity.this,
                                "Erreur chargement : " + error,
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    // Créer les bulles des messages
    private void ajouterBulle(
            String contenu,
            boolean messageEnvoye) {

        TextView bulle = new TextView(this);

        bulle.setText(contenu);
        bulle.setTextSize(14);
        bulle.setTextColor(Color.WHITE);
        bulle.setPadding(14, 10, 14, 10);

        GradientDrawable fond = new GradientDrawable();

        fond.setColor(
                messageEnvoye
                        ? Color.parseColor("#1B8A5A")
                        : Color.parseColor("#64748B")
        );

        fond.setCornerRadius(24);
        bulle.setBackground(fond);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        params.gravity = messageEnvoye
                ? Gravity.END
                : Gravity.START;

        params.topMargin = 12;

        layoutMessages.addView(bulle, params);
    }
}
