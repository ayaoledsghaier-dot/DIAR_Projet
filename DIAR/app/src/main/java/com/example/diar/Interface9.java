
package com.example.diar;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class Interface9 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.interface9);

        // Navigation étudiant : Carte active
        NavigationEtudiant.setup(this, 1);
    }
}
