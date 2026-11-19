
package com.example.demo.dto;

public class ChatMessageRequest {

    private String etudiantEmail;
    private String etudiantNom;

    private String proprietaireEmail;
    private String proprietaireNom;

    private Long annonceId;

    private String senderEmail;
    private String senderRole;

    private String contenu;

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

    public void setAnnonceId(Long id) {
        this.annonceId = id;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public void setSenderEmail(String email) {
        this.senderEmail = email;
    }

    public String getSenderRole() {
        return senderRole;
    }

    public void setSenderRole(String role) {
        this.senderRole = role;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }
}
