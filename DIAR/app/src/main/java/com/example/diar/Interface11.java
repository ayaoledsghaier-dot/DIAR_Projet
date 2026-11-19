
package com.example.diar;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class Interface11 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.interface11);

        NavigationEtudiant.setup(this, 2);
    }
}
