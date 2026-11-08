package com.example.diar;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ChatProprietaire extends AppCompatActivity {

    private LinearLayout layoutMessages;
    private EditText edtMessage;
    private ScrollView scrollMessages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_proprietaire);

        String nom = getIntent().getStringExtra("nomEtudiant");
        if (nom == null) nom = "Étudiant";

        TextView tvNom = findViewById(R.id.tvNomEtudiant);
        tvNom.setText(nom);

        layoutMessages = findViewById(R.id.layoutMessages);
        edtMessage = findViewById(R.id.edtMessage);
        scrollMessages = findViewById(R.id.scrollMessages);

        findViewById(R.id.btnRetourMessages).setOnClickListener(v -> finish());

        Button btnEnvoyer = findViewById(R.id.btnEnvoyerMessage);
        btnEnvoyer.setOnClickListener(v -> envoyerMessage());
    }

    private void envoyerMessage() {
        String message = edtMessage.getText().toString().trim();

        if (message.isEmpty()) {
            Toast.makeText(this, "Écrivez un message", Toast.LENGTH_SHORT).show();
            return;
        }

        TextView bulle = new TextView(this);
        bulle.setText(message);
        bulle.setTextColor(Color.WHITE);
        bulle.setTextSize(14);
        bulle.setTypeface(null, Typeface.NORMAL);
        bulle.setPadding(14, 10, 14, 10);
        bulle.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                Color.parseColor("#1B8A5A")));

        android.graphics.drawable.GradientDrawable fond =
                new android.graphics.drawable.GradientDrawable();
        fond.setColor(Color.parseColor("#1B8A5A"));
        fond.setCornerRadius(24);
        bulle.setBackground(fond);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.END;
        params.topMargin = 12;

        layoutMessages.addView(bulle, params);
        edtMessage.setText("");

        scrollMessages.post(() ->
                scrollMessages.fullScroll(ScrollView.FOCUS_DOWN));
    }
}