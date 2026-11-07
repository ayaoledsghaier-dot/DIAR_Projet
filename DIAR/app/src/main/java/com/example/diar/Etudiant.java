package com.example.diar;

public class Etudiant {

    private String nomComplet;
    private String email;
    private int age;
    private String universite;
    private double budgetMax;
    private String quartier;
    private int pourcentage;
    private String proprete;
    private String modeDeVie;
    private String nbColocataires;
    private String animauxAcceptes;
    private String photoUrl;

    // 1. Constructeur vide (nécessaire pour certaines initialisations)
    public Etudiant() {
    }

    // 2. Constructeur complet
    public Etudiant(String nomComplet, String email, String universite, int age, double budgetMax, String modeDeVie, String nbColocataires, String proprete, String animauxAcceptes) {
        this.nomComplet = nomComplet;
        this.email = email;
        this.universite = universite;
        this.age = age;
        this.budgetMax = budgetMax;
        this.modeDeVie = modeDeVie;
        this.nbColocataires = nbColocataires;
        this.proprete = proprete;
        this.animauxAcceptes = animauxAcceptes;
    }

    // --- Getters et Setters ---

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

    public int getAge() {
        return age;
    }
    public void setAge(int age) {
        this.age = age;
    }

    public String getUniversite() {
        return universite;
    }
    public void setUniversite(String universite) {
        this.universite = universite;
    }

    public double getBudgetMax() {
        return budgetMax;
    }
    public void setBudgetMax(double budgetMax) {
        this.budgetMax = budgetMax;
    }

    public String getQuartier() {
        return quartier;
    }
    public void setQuartier(String quartier) {
        this.quartier = quartier;
    }

    public int getPourcentage() {
        return pourcentage;
    }
    public void setPourcentage(int pourcentage) {
        this.pourcentage = pourcentage;
    }

    public String getProprete() {
        return proprete;
    }
    public void setProprete(String proprete) {
        this.proprete = proprete;
    }

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

    public String getPhotoUrl() {
        return photoUrl;
    }
    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}