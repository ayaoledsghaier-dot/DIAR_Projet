
package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "conversations")
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String etudiantEmail;
    private String etudiantNom;

    private String proprietaireEmail;
    private String proprietaireNom;

    private Long annonceId;

    private LocalDateTime dateCreation;

    @PrePersist
    public void prePersist() {
        if (dateCreation == null) {
            dateCreation = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public String getEtudiantEmail() {
        return etudiantEmail;
    }

    public void setEtudiantEmail(String email) {
        this.etudiantEmail = email;
    }

    public String getEtudiantNom() {
        return etudiantNom;
    }

    public void setEtudiantNom(String nom) {
        this.etudiantNom = nom;
    }

    public String getProprietaireEmail() {
        return proprietaireEmail;
    }

    public void setProprietaireEmail(String email) {
        this.proprietaireEmail = email;
    }

    public String getProprietaireNom() {
        return proprietaireNom;
    }

    public void setProprietaireNom(String nom) {
        this.proprietaireNom = nom;
    }

    public Long getAnnonceId() {
        return annonceId;
    }

    public void setAnnonceId(Long annonceId) {
        this.annonceId = annonceId;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }
}
