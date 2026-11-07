package com.example.diar;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

public class ProfilAdapter extends RecyclerView.Adapter<ProfilAdapter.ProfilViewHolder> {

    private List<Etudiant> listeEtudiants;

    public ProfilAdapter(List<Etudiant> listeEtudiants) {
        this.listeEtudiants = listeEtudiants;
    }

    @NonNull
    @Override
    public ProfilViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_profil_coloc, parent, false);
        return new ProfilViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfilViewHolder holder, int position) {
        Etudiant etudiant = listeEtudiants.get(position);

        // Nom et âge
        String nom = etudiant.getNomComplet() != null ? etudiant.getNomComplet() : "Étudiant";
        int age = etudiant.getAge() > 0 ? etudiant.getAge() : 21;
        holder.txtNom.setText(nom + ", " + age + " ans");

        // Affichage dynamique du pourcentage de compatibilité
        int score = etudiant.getPourcentage();
        holder.txtCompatibilite.setText(score + "% Compatible");

        // Université
        String universite = etudiant.getUniversite() != null ? etudiant.getUniversite() : "Université de Sousse";
        holder.txtUniversiteAge.setText(universite);

        // Budget et propreté
        double budget = etudiant.getBudgetMax() > 0 ? etudiant.getBudgetMax() : 300.0;
        String proprete = etudiant.getProprete() != null && !etudiant.getProprete().isEmpty() ? " • Propreté : " + etudiant.getProprete() : "";
        holder.txtBudget.setText("Budget : " + budget + " DT" + proprete);

        // 🖼️ Chargement de la photo de profil avec Glide
        if (etudiant.getPhotoUrl() != null && !etudiant.getPhotoUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(etudiant.getPhotoUrl())
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .into(holder.imgProfil);
        } else {
            holder.imgProfil.setImageResource(R.drawable.ic_launcher_foreground);
        }

        /// Gestion du clic sur une carte de profil pour ouvrir les détails
        holder.itemView.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION) {
                // On récupère l'étudiant de cette position sans réécrire "Etudiant" devant
                Etudiant etudiantClique = listeEtudiants.get(currentPosition);

                Context context = v.getContext();
                Intent intent = new Intent(context, Interface13.class);

                intent.putExtra("NOM", etudiantClique.getNomComplet());
                intent.putExtra("POURCENTAGE", etudiantClique.getPourcentage());
                intent.putExtra("UNIVERSITE", etudiantClique.getUniversite());
                intent.putExtra("AGE", etudiantClique.getAge());
                intent.putExtra("BUDGET", etudiantClique.getBudgetMax());
                intent.putExtra("QUARTIER", etudiantClique.getQuartier());
                intent.putExtra("MODE_DE_VIE", etudiantClique.getModeDeVie());
                intent.putExtra("PROPRETE", etudiantClique.getProprete());
                intent.putExtra("ANIMAUX", etudiantClique.getAnimauxAcceptes());
                intent.putExtra("PHOTO_URL", etudiant.getPhotoUrl());

                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listeEtudiants.size();
    }

    public static class ProfilViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView imgProfil;
        TextView txtNom, txtCompatibilite, txtUniversiteAge, txtBudget;

        public ProfilViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProfil = itemView.findViewById(R.id.imgProfil);
            txtNom = itemView.findViewById(R.id.txtNom);
            txtCompatibilite = itemView.findViewById(R.id.txtCompatibilite);
            txtUniversiteAge = itemView.findViewById(R.id.txtUniversiteAge);
            txtBudget = itemView.findViewById(R.id.txtBudget);
        }
    }
}