package com.example.diar;


import android.os.Bundle;

public class Interface11 extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.interface11); // <-- C'est ici qu'on lie la classe Java au fichier XML !

        setupBottomNavigation(); // Active les clics de la barre du bas
    }
}