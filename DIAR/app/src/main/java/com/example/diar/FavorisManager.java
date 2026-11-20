package com.example.diar;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.HashSet;
import java.util.Set;

public class FavorisManager {
    private static final String PREFS = "favoris_prefs";
    private static final String KEY = "ids_favoris";

    public static Set<String> getFavoris(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return new HashSet<>(prefs.getStringSet(KEY, new HashSet<>()));
    }

    public static boolean estFavori(Context context, int colocId) {
        return getFavoris(context).contains(String.valueOf(colocId));
    }

    public static void toggleFavori(Context context, int colocId) {
        Set<String> favoris = getFavoris(context);
        String idStr = String.valueOf(colocId);
        if (favoris.contains(idStr)) {
            favoris.remove(idStr);
        } else {
            favoris.add(idStr);
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().putStringSet(KEY, favoris).apply();
    }
}