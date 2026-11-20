package com.example.diar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.io.IOException;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ProfilAdapter extends RecyclerView.Adapter<ProfilAdapter.ProfilViewHolder> {

    public interface OnClick {
        void onClick(Etudiant e);
    }

    private List<Etudiant> liste;
    private OnClick listener;
    private String emailConnecte;
    private final OkHttpClient client = new OkHttpClient();

    public ProfilAdapter(List<Etudiant> liste, String emailConnecte, OnClick listener) {
        this.liste = liste;
        this.emailConnecte = emailConnecte;
        this.listener = listener;
    }

    public ProfilAdapter(List<Etudiant> liste, OnClick listener) {
        this.liste = liste;
        this.emailConnecte = "salma@test.com";
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProfilViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_colocataire, parent, false);
        return new ProfilViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfilViewHolder h, int position) {
        Etudiant e = liste.get(position);
        Context context = h.itemView.getContext();
        int colocId = e.getId();

        h.txtNom.setText(e.getNomComplet());
        if (e.getPourcentageCompatible() != null) {
            h.txtCompatibilite.setText(e.getPourcentageCompatible().intValue() + "% compatible");
        } else {
            h.txtCompatibilite.setText("—% compatible");
        }
        h.txtUniversiteAge.setText(e.getUniversite() + " • " + e.getAge() + " ans");
        h.txtBudget.setText("Budget : " + e.getBudgetMax() + " DT");

        String photoUrl = e.getPhotoUrl();
        if (photoUrl != null && !photoUrl.isEmpty() && !photoUrl.equals("null")) {
            Glide.with(context)
                    .load(Config.BASE_URL + photoUrl)
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .circleCrop()
                    .into(h.imgProfil);
        } else {
            h.imgProfil.setImageResource(R.drawable.ic_profile);
        }

        h.ivFavori.setImageResource(e.isFavori() ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);

        h.ivFavori.setOnClickListener(v -> toggleFavoriBackend(h, e, colocId));

        h.itemView.setOnClickListener(v -> listener.onClick(e));
    }

    private void toggleFavoriBackend(ProfilViewHolder h, Etudiant e, int colocId) {
        Context context = h.itemView.getContext();
        if (colocId == -1) {
            Toast.makeText(context, "Erreur : coloc introuvable", Toast.LENGTH_SHORT).show();
            return;
        }
        String url = Config.BASE_URL + "/api/etudiants/favoris?email=" + emailConnecte + "&colocId=" + colocId;
        Request.Builder builder = new Request.Builder().url(url);
        if (e.isFavori()) {
            builder.delete();
        } else {
            RequestBody body = RequestBody.create("", MediaType.parse("application/json"));
            builder.post(body);
        }
        client.newCall(builder.build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException ex) {
                h.itemView.post(() -> Toast.makeText(context, "Erreur serveur : " + ex.getMessage(), Toast.LENGTH_SHORT).show());
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();
                if (response.isSuccessful()) {
                    e.setFavori(!e.isFavori());
                    boolean nouveau = e.isFavori();
                    h.itemView.post(() -> {
                        h.ivFavori.setImageResource(nouveau ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);
                        Toast.makeText(context, nouveau ? e.getNomComplet() + " ajouté aux favoris ❤" : e.getNomComplet() + " retiré des favoris", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    h.itemView.post(() -> Toast.makeText(context, "Erreur : " + response.code() + " " + json, Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return liste.size();
    }

    public static class ProfilViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProfil, ivFavori;
        TextView txtNom, txtCompatibilite, txtUniversiteAge, txtBudget;
        public ProfilViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProfil = itemView.findViewById(R.id.imgProfil);
            ivFavori = itemView.findViewById(R.id.ivFavori);
            txtNom = itemView.findViewById(R.id.txtNom);
            txtCompatibilite = itemView.findViewById(R.id.txtCompatibilite);
            txtUniversiteAge = itemView.findViewById(R.id.txtUniversiteAge);
            txtBudget = itemView.findViewById(R.id.txtBudget);
        }
    }
}