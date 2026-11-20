package com.example.diar;

public class Etudiant {

    private int id;
    private String nomComplet;
    private String email;
    private String motDePasse;
    private String genre;
    private String universite;
    private String niveau;
    private String photoUrl;
    private String statutVerification;
    private Integer age;

    private String typeLogement;
    private String locationType;
    private Double budgetMax;
    private String equipements;
    private String quartier;

    private String modeDeVie;
    private String nbColocataires;
    private String animauxAcceptes;
    private String proprete;

    private Double pourcentageCompatible;
    private boolean isFavori = false;

    public Etudiant() {
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public void setId(long id) { this.id = (int) id; }

    public String getNomComplet() { return nomComplet; }
    public void setNomComplet(String nomComplet) { this.nomComplet = nomComplet; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public String getUniversite() { return universite; }
    public void setUniversite(String universite) { this.universite = universite; }

    public String getNiveau() { return niveau; }
    public void setNiveau(String niveau) { this.niveau = niveau; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public String getStatutVerification() { return statutVerification; }
    public void setStatutVerification(String statutVerification) { this.statutVerification = statutVerification; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    public void setAge(int age) { this.age = age; }

    public String getTypeLogement() { return typeLogement; }
    public void setTypeLogement(String typeLogement) { this.typeLogement = typeLogement; }

    public String getLocationType() { return locationType; }
    public void setLocationType(String locationType) { this.locationType = locationType; }

    public Double getBudgetMax() { return budgetMax; }
    public void setBudgetMax(Double budgetMax) { this.budgetMax = budgetMax; }
    public void setBudgetMax(double budgetMax) { this.budgetMax = budgetMax; }

    public String getEquipements() { return equipements; }
    public void setEquipements(String equipements) { this.equipements = equipements; }

    public String getQuartier() { return quartier; }
    public void setQuartier(String quartier) { this.quartier = quartier; }

    public String getModeDeVie() { return modeDeVie; }
    public void setModeDeVie(String modeDeVie) { this.modeDeVie = modeDeVie; }

    public String getNbColocataires() { return nbColocataires; }
    public void setNbColocataires(String nbColocataires) { this.nbColocataires = nbColocataires; }

    public String getAnimauxAcceptes() { return animauxAcceptes; }
    public void setAnimauxAcceptes(String animauxAcceptes) { this.animauxAcceptes = animauxAcceptes; }

    public String getProprete() { return proprete; }
    public void setProprete(String proprete) { this.proprete = proprete; }

    public Double getPourcentageCompatible() { return pourcentageCompatible; }
    public void setPourcentageCompatible(Double pourcentageCompatible) { this.pourcentageCompatible = pourcentageCompatible; }
    public void setPourcentageCompatible(double pourcentageCompatible) { this.pourcentageCompatible = pourcentageCompatible; }

    public boolean isFavori() { return isFavori; }
    public void setFavori(boolean favori) { isFavori = favori; }
}