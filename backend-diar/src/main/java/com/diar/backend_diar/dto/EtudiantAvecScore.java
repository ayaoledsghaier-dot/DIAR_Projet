package com.diar.backend_diar.dto;

import com.diar.backend_diar.entity.Etudiant;

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