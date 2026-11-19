
package com.diar.backend_diar.config;

import com.diar.backend_diar.entity.Annonce;
import com.diar.backend_diar.entity.Etudiant;
import com.diar.backend_diar.entity.Proprietaire;

import com.diar.backend_diar.repository.AnnonceRepository;
import com.diar.backend_diar.repository.EtudiantRepository;
import com.diar.backend_diar.repository.ProprietaireRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements CommandLineRunner {

    private final EtudiantRepository etudiantRepository;
    private final ProprietaireRepository proprietaireRepository;
    private final AnnonceRepository annonceRepository;

    public DataInitializer(
            EtudiantRepository etudiantRepository,
            ProprietaireRepository proprietaireRepository,
            AnnonceRepository annonceRepository) {

        this.etudiantRepository = etudiantRepository;
        this.proprietaireRepository = proprietaireRepository;
        this.annonceRepository = annonceRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {

        // ==================================
        // 1. CREATION DES 5 ETUDIANTS
        // ==================================

        ajouterEtudiant("Ahmed Ben Ali", "ahmed@gmail.com",
                "Diar1234", "Homme", "Université de Sousse",
                "ISG", "Licence 3", 450, "Sousse");

        ajouterEtudiant("Mariem Trabelsi", "mariem@gmail.com",
                "Diar1234", "Femme", "Université de Sousse",
                "FSEG", "Master 1", 400, "Sousse");

        ajouterEtudiant("Youssef Sami", "youssef@gmail.com",
                "Diar1234", "Homme", "Université de Monastir",
                "FMM", "Licence 2", 350, "Monastir");

        ajouterEtudiant("Ines Khalfallah", "ines@gmail.com",
                "Diar1234", "Femme", "Université de Sousse",
                "ISG", "Licence 2", 500, "Sousse");

        ajouterEtudiant("Sami Mansour", "sami@gmail.com",
                "Diar1234", "Homme", "Université de Tunis",
                "FST", "Master 1", 450, "El Manar");

        // ==================================
        // 2. CREATION DES 5 PROPRIETAIRES
        // ==================================

        ajouterProprietaire("Mohamed Ali", "prop1@gmail.com", "22111222");
        ajouterProprietaire("Salma Ben Amor", "prop2@gmail.com", "22111333");
        ajouterProprietaire("Hatem Trabelsi", "prop3@gmail.com", "22111444");
        ajouterProprietaire("Amira Saidi", "prop4@gmail.com", "22111555");
        ajouterProprietaire("Walid Mansour", "prop5@gmail.com", "22111666");

        // ==================================
        // 3. CREATION DES 10 ANNONCES
        // ==================================

        // Annonces a Sousse

        ajouterAnnonce(
                "Studio meuble pres ISG Sousse",
                "Studio fictif meuble, cuisine equipee, wifi et salle de bain.",
                350, 35.8288, 10.6405, 1, "prop1@gmail.com"
        );

        ajouterAnnonce(
                "Appartement S+1 a Khzema",
                "Appartement fictif S+1, proche des commerces et transports.",
                450, 35.8395, 10.6140, 2, "prop1@gmail.com"
        );

        ajouterAnnonce(
                "Chambre etudiante a Sahloul",
                "Chambre meublee dans un appartement partage.",
                250, 35.8290, 10.6080, 1, "prop2@gmail.com"
        );

        ajouterAnnonce(
                "Studio a Hammam Sousse",
                "Studio calme avec kitchenette et balcon.",
                320, 35.8605, 10.6030, 1, "prop2@gmail.com"
        );

        ajouterAnnonce(
                "Appartement proche centre Sousse",
                "Appartement S+2 avec deux chambres et cuisine.",
                500, 35.8250, 10.6360, 2, "prop3@gmail.com"
        );

        // Annonces a Monastir

        ajouterAnnonce(
                "Studio pres faculte de Monastir",
                "Studio fictif adapte aux etudiants, meuble et equipe.",
                280, 35.7643, 10.8113, 1, "prop3@gmail.com"
        );

        ajouterAnnonce(
                "Appartement S+1 Monastir centre",
                "Appartement proche des transports et des commerces.",
                380, 35.7770, 10.8260, 2, "prop4@gmail.com"
        );

        ajouterAnnonce(
                "Chambre meublee Monastir",
                "Chambre dans un appartement partage avec cuisine.",
                220, 35.7690, 10.8150, 1, "prop4@gmail.com"
        );

        // Annonces a Tunis

        ajouterAnnonce(
                "Studio pres FST El Manar",
                "Studio meuble proche de la faculte des sciences.",
                400, 36.8320, 10.1640, 1, "prop5@gmail.com"
        );

        ajouterAnnonce(
                "Appartement S+1 El Manar",
                "Appartement confortable avec balcon et cuisine equipee.",
                480, 36.8410, 10.1700, 2, "prop5@gmail.com"
        );

        System.out.println("==================================");
        System.out.println("DIAR : INITIALISATION TERMINEE !");
        System.out.println("5 etudiants et 5 proprietaires");
        System.out.println("10 annonces de test");
        System.out.println("==================================");
    }

    // ==================================
    // METHODE POUR AJOUTER UN ETUDIANT
    // ==================================

    private void ajouterEtudiant(
            String nom, String email, String password,
            String genre, String universite, String faculte,
            String niveau, double budget, String quartier) {

        if (etudiantRepository.existsByEmail(email)) {
            return;
        }

        Etudiant e = new Etudiant();

        e.setNomComplet(nom);
        e.setEmail(email);
        e.setMotDePasse(password);
        e.setGenre(genre);
        e.setUniversite(universite);
        e.setFaculte(faculte);
        e.setNiveau(niveau);
        e.setBudgetMax(budget);
        e.setQuartier(quartier);
        e.setStatutVerification("VERIFIE");

        etudiantRepository.save(e);
    }

    // ==================================
    // METHODE POUR AJOUTER UN PROPRIETAIRE
    // ==================================

    private void ajouterProprietaire(
            String nom, String email, String telephone) {

        if (proprietaireRepository.existsByEmail(email)) {
            return;
        }

        Proprietaire p = new Proprietaire();

        p.setNom(nom);
        p.setEmail(email);
        p.setMotDePasse("Diar1234");
        p.setTelephone(telephone);

        proprietaireRepository.save(p);
    }

    // ==================================
    // METHODE POUR AJOUTER UNE ANNONCE
    // ==================================

    private void ajouterAnnonce(
            String titre, String description,
            double prix, double latitude, double longitude,
            int nombrePersonnes, String emailProprietaire) {

        if (annonceRepository.existsByTitreAndEmailProprietaire(
                titre, emailProprietaire)) {
            return;
        }

        Annonce a = new Annonce();

        a.setTitre(titre);
        a.setDescription(description);
        a.setPrix(prix);
        a.setLatitude(latitude);
        a.setLongitude(longitude);
        a.setNombrePersonnes(nombrePersonnes);
        a.setEmailProprietaire(emailProprietaire);

        annonceRepository.save(a);
    }
}
