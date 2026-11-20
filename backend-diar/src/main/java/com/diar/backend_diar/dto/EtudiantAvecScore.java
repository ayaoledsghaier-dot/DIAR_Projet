package com.diar.backend_diar.dto;

import com.diar.backend_diar.entity.Etudiant;


// DTO qui associe un étudiant à son score de compatibilité (0-100%) calculé avec l'étudiant connecté.
// Utilisé pour envoyer à Android la liste des profils triés du plus compatible au moins compatible.
public class EtudiantAvecScore {
    private Etudiant etudiant;
    private int pourcentage;

    public EtudiantAvecScore(Etudiant etudiant, int pourcentage) {
        this.etudiant = etudiant;
        this.pourcentage = pourcentage;
    }

    public Etudiant getEtudiant() {
        return etudiant;
    }

    public void setEtudiant(Etudiant etudiant) {
        this.etudiant = etudiant;
    }

    public int getPourcentage() {
        return pourcentage;
    }

    public void setPourcentage(int pourcentage) {
        this.pourcentage = pourcentage;
    }
}