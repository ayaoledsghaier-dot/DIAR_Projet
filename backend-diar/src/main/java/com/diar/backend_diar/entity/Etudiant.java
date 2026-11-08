package com.diar.backend_diar.entity; 

import jakarta.persistence.*;

@Entity
@Table(name = "etudiants")
public class Etudiant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nomComplet;

    @Column(unique = true)
    private String email;

    private String motDePasse;

    private String genre;
    private String universite;
    private String niveau;
    private String photoUrl;
    private String statutVerification; // Champ ajouté pour l'OCR

    // Nouveaux champs pour les préférences de logement
    private String typeLogement;
    private String locationType;
    private Double budgetMax;
    private String equipements;
    private String quartier;

    // Nouveaux champs pour le profil colocataire
    private String modeDeVie;
    private String nbColocataires;
    private String animauxAcceptes;
    private String proprete;

    public Etudiant() {
    }

    public Etudiant(String nomComplet, String email, String motDePasse, String genre, String universite, String niveau,
            String photoUrl) {
        this.nomComplet = nomComplet;
        this.email = email;
        this.motDePasse = motDePasse;
        this.genre = genre;
        this.universite = universite;
        this.niveau = niveau;
        this.photoUrl = photoUrl;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNomComplet() {
        return nomComplet;
    }

    public void setNomComplet(String nomComplet) {
        this.nomComplet = nomComplet;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getUniversite() {
        return universite;
    }

    public void setUniversite(String universite) {
        this.universite = universite;
    }

    public String getNiveau() {
        return niveau;
    }

    public void setNiveau(String niveau) {
        this.niveau = niveau;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getStatutVerification() {
        return statutVerification;
    }

    public void setStatutVerification(String statutVerification) {
        this.statutVerification = statutVerification;
    }

    // Getters et Setters pour les préférences de logement
    public String getTypeLogement() {
        return typeLogement;
    }

    public void setTypeLogement(String typeLogement) {
        this.typeLogement = typeLogement;
    }

    public String getLocationType() {
        return locationType;
    }

    public void setLocationType(String locationType) {
        this.locationType = locationType;
    }

    public double getBudgetMax() {
        return budgetMax;
    }

    public void setBudgetMax(double budgetMax) {
        this.budgetMax = budgetMax;
    }

    public String getEquipements() {
        return equipements;
    }

    public void setEquipements(String equipements) {
        this.equipements = equipements;
    }

    public String getQuartier() {
        return quartier;
    }

    public void setQuartier(String quartier) {
        this.quartier = quartier;
    }

    // ==========================================
    // GETTERS ET SETTERS AJOUTÉS POUR LE PROFIL COLOCATAIRE
    // ==========================================
    public String getModeDeVie() {
        return modeDeVie;
    }

    public void setModeDeVie(String modeDeVie) {
        this.modeDeVie = modeDeVie;
    }

    public String getNbColocataires() {
        return nbColocataires;
    }

    public void setNbColocataires(String nbColocataires) {
        this.nbColocataires = nbColocataires;
    }

    public String getAnimauxAcceptes() {
        return animauxAcceptes;
    }

    public void setAnimauxAcceptes(String animauxAcceptes) {
        this.animauxAcceptes = animauxAcceptes;
    }

    // Ajoute les getters et setters correspondants
    public String getProprete() {
        return proprete;
    }

    public void setProprete(String proprete) {
        this.proprete = proprete;
    }
}