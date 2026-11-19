
package com.example.diar;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

public class AnnonceAdapter extends RecyclerView.Adapter<AnnonceAdapter.AnnonceViewHolder> {

    private final ArrayList<JSONObject> listeComplete = new ArrayList<>();
    private final ArrayList<JSONObject> listeAffichee = new ArrayList<>();

    public AnnonceAdapter(Context context, String baseUrl) {
        // Constructeur
    }

    public void setAnnonces(ArrayList<JSONObject> annonces) {

        listeComplete.clear();
        listeAffichee.clear();

        if (annonces != null) {
            listeComplete.addAll(annonces);
            listeAffichee.addAll(annonces);
        }

        notifyDataSetChanged();
    }

    public void filter(String recherche) {

        listeAffichee.clear();

        String mot = recherche == null
                ? ""
                : recherche.trim().toLowerCase(Locale.ROOT);

        if (mot.isEmpty()) {
            listeAffichee.addAll(listeComplete);
        } else {

            for (JSONObject annonce : listeComplete) {

                String titre = annonce.optString("titre", "");
                String description = annonce.optString("description", "");

                if (titre.toLowerCase(Locale.ROOT).contains(mot)
                        || description.toLowerCase(Locale.ROOT).contains(mot)) {

                    listeAffichee.add(annonce);
                }
            }
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AnnonceViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_annonce, parent, false);

        return new AnnonceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull AnnonceViewHolder holder,
            int position) {

        JSONObject annonce = listeAffichee.get(position);

        // Informations annonce
        holder.titre.setText(
                annonce.optString("titre", "Annonce sans titre")
        );

        holder.prix.setText(
                annonce.optString("prix", "0") + " DT / mois"
        );

        holder.description.setText(
                annonce.optString("description", "Aucune description")
        );

        double latitude = annonce.optDouble("latitude", 0);
        double longitude = annonce.optDouble("longitude", 0);

        holder.localisation.setText(
                "📍 " + latitude + ", " + longitude
        );

        // Photo par défaut
        holder.image.setImageResource(R.drawable.appart);

        // Affichage photo Base64
        JSONArray photos = annonce.optJSONArray("photos");

        if (photos != null && photos.length() > 0) {

            String photo = photos.optString(0, "");

            try {

                if (photo != null
                        && !photo.trim().isEmpty()
                        && !"null".equals(photo)) {

                    String data = photo.trim();

                    if (data.contains(",")) {
                        data = data.substring(data.indexOf(",") + 1);
                    }

                    byte[] bytes = Base64.decode(
                            data,
                            Base64.DEFAULT
                    );

                    BitmapFactory.Options options =
                            new BitmapFactory.Options();

                    options.inSampleSize = 2;

                    Bitmap bitmap = BitmapFactory.decodeByteArray(
                            bytes,
                            0,
                            bytes.length,
                            options
                    );

                    if (bitmap != null) {
                        holder.image.setImageBitmap(bitmap);
                    }

                }

            } catch (Exception e) {

                Log.e("DIAR_IMAGE", "Erreur affichage photo", e);
            }
        }

        // Bouton Voir les détails
        holder.btnDetails.setOnClickListener(v -> {

            long idAnnonce = annonce.optLong("id", -1);

            if (idAnnonce <= 0) {
                Toast.makeText(
                        v.getContext(),
                        "ID annonce invalide",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            Intent intent = new Intent(
                    v.getContext(),
                    DetailAnnonce.class
            );

            intent.putExtra("idAnnonce", idAnnonce);

            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return listeAffichee.size();
    }

    static class AnnonceViewHolder extends RecyclerView.ViewHolder {

        ImageView image;
        TextView titre;
        TextView prix;
        TextView description;
        TextView localisation;
        Button btnDetails;

        AnnonceViewHolder(@NonNull View itemView) {

            super(itemView);

            image = itemView.findViewById(R.id.ivAnnoncePhoto);
            titre = itemView.findViewById(R.id.tvAnnonceTitre);
            prix = itemView.findViewById(R.id.tvAnnoncePrix);
            description = itemView.findViewById(R.id.tvAnnonceDescription);
            localisation = itemView.findViewById(R.id.tvAnnonceLocalisation);
            btnDetails = itemView.findViewById(R.id.btnDetails);
        }
    }
}
