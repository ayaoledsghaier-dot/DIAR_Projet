
package com.diar.backend_diar.controller;

import com.diar.backend_diar.entity.Etudiant;
import com.diar.backend_diar.entity.Proprietaire;
import com.diar.backend_diar.repository.EtudiantRepository;
import com.diar.backend_diar.repository.ProprietaireRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log =
            LoggerFactory.getLogger(AuthController.class);

    private final EtudiantRepository etudiantRepository;
    private final ProprietaireRepository proprietaireRepository;

    public AuthController(
            EtudiantRepository etudiantRepository,
            ProprietaireRepository proprietaireRepository) {

        this.etudiantRepository = etudiantRepository;
        this.proprietaireRepository = proprietaireRepository;
    }

    @PostMapping("/connexion")
    public ResponseEntity<?> connecter(
            @RequestBody Map<String, String> donnees) {

        String email = donnees.get("email");
        String motDePasse = donnees.get("motDePasse");

        if (email == null || email.trim().isEmpty()
                || motDePasse == null || motDePasse.isEmpty()) {

            return ResponseEntity.badRequest().body(
                    Map.of("message", "Email et mot de passe obligatoires.")
            );
        }

        email = email.trim();

        log.info("Tentative de connexion : {}", email);

        // =========================================
        // 1. RECHERCHER DANS LA TABLE ETUDIANTS
        // =========================================

        Etudiant etudiant = etudiantRepository.findByEmail(email);

        if (etudiant != null) {

            // Vérifier le mot de passe
            if (etudiant.getMotDePasse() == null ||
                    !motDePasse.equals(etudiant.getMotDePasse())) {

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of(
                                "message",
                                "Email ou mot de passe incorrect."
                        ));
            }

            // Vérifier la validation OCR
            String statut = etudiant.getStatutVerification();

            if (statut == null ||
                    !statut.trim().equalsIgnoreCase("VERIFIE")) {

                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of(
                                "message",
                                "Votre compte étudiant n'est pas encore validé."
                        ));
            }

            // Connexion étudiant réussie
            Map<String, Object> resultat = new HashMap<>();

            resultat.put("role", "ETUDIANT");
            resultat.put("nom", etudiant.getNomComplet());
            resultat.put("email", etudiant.getEmail());
            resultat.put("message", "Connexion étudiant réussie");

            log.info("Connexion étudiant réussie : {}", email);

            return ResponseEntity.ok(resultat);
        }

        // =========================================
        // 2. RECHERCHER DANS LA TABLE PROPRIETAIRES
        // =========================================

        Proprietaire proprietaire =
                proprietaireRepository.findByEmail(email).orElse(null);

        if (proprietaire != null) {

            if (proprietaire.getMotDePasse() == null ||
                    !motDePasse.equals(proprietaire.getMotDePasse())) {

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of(
                                "message",
                                "Email ou mot de passe incorrect."
                        ));
            }

            // Connexion propriétaire réussie
            Map<String, Object> resultat = new HashMap<>();

            resultat.put("role", "PROPRIETAIRE");
            resultat.put("nom", proprietaire.getNom());
            resultat.put("email", proprietaire.getEmail());
            resultat.put("message", "Connexion propriétaire réussie");

            log.info("Connexion propriétaire réussie : {}", email);

            return ResponseEntity.ok(resultat);
        }

        // =========================================
        // 3. AUCUN COMPTE TROUVE
        // =========================================

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                        "message",
                        "Aucun compte trouvé avec cet email."
                ));
    }
}
