package com.diar.backend_diar.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "favoris", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"etudiant_id", "coloc_id"})
})
public class Favori {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "etudiant_id", nullable = false)
    private Etudiant etudiant;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "coloc_id", nullable = false)
    private Etudiant coloc;

    @Column(name = "date_ajout")
    private LocalDateTime dateAjout = LocalDateTime.now();

    public Favori() {
    }

    public Favori(Etudiant etudiant, Etudiant coloc) {
        this.etudiant = etudiant;
        this.coloc = coloc;
        this.dateAjout = LocalDateTime.now();
    }

    // ✅ Pour compatibilité avec l'ancien FavoriController si tu le gardes
    public Favori(String ownerEmail, Etudiant coloc) {
        this.dateAjout = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Etudiant getEtudiant() { return etudiant; }
    public void setEtudiant(Etudiant etudiant) { this.etudiant = etudiant; }

    public Etudiant getColoc() { return coloc; }
    public void setColoc(Etudiant coloc) { this.coloc = coloc; }

    // ✅ Alias pour que l'ancien code qui appelle getFavori() marche aussi
    public Etudiant getFavori() { return coloc; }
    public void setFavori(Etudiant favori) { this.coloc = favori; }

    public LocalDateTime getDateAjout() { return dateAjout; }
    public void setDateAjout(LocalDateTime dateAjout) { this.dateAjout = dateAjout; }
}