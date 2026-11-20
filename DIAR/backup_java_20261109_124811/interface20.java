package com.example.diar;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class interface20 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.interface20);

        // Activer le bouton Mes demandes
        NavigationProprietaire.setup(this, R.id.nav_prop_annonces);
    }
}