package com.example.diar;

import android.content.Context;
import android.util.AttributeSet;
import android.view.autofill.AutofillValue;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.textfield.TextInputEditText;

public class NonAutofillEditText extends TextInputEditText {

    public NonAutofillEditText(@NonNull Context context) {
        super(context);
    }

    public NonAutofillEditText(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public NonAutofillEditText(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void autofill(AutofillValue values) {
        // On bloque l'injection automatique de l'autofill Google sur ce champ précis.
        // L'e-mail de l'utilisateur ne sera donc plus jamais écrasé !
    }
}