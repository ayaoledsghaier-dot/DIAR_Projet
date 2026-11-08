package com.diar.backend_diar.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.diar.backend_diar.entity.Proprietaire;
import com.diar.backend_diar.repository.ProprietaireRepository; // <-- Zid hedhi zeda!
import java.util.Map;

@RestController
@RequestMapping("/api/proprietaires")
public class ProprietaireController {

    private static final Logger log =
            LoggerFactory.getLogger(ProprietaireController.class);

    private final ProprietaireRepository repository;

    public ProprietaireController(ProprietaireRepository repository) {
        this.repository = repository;
    }

    // INSCRIPTION PROPRIETAIRE
    @PostMapping("/verifier")
    public ResponseEntity<?> verifierProprietaire(
            @RequestBody Map<String, String> donnees) {

        log.info("API /verifier reçue");

        String nom = donnees.get("nom");
        String email = donnees.get("email");
        String motDePasse = donnees.get("motDePasse");
        String telephone = donnees.get("telephone");

        if (nom == null || nom.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Le nom est obligatoire."));
        }

        if (email == null ||
                !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Email invalide."));
        }

        if (motDePasse == null || motDePasse.length() < 8) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "Le mot de passe doit contenir au moins 8 caractères."
                    ));
        }

        if (telephone == null || telephone.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Le téléphone est obligatoire."));
        }

        if (repository.existsByEmail(email.trim())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Cet email existe déjà."));
        }

        Proprietaire proprietaire = new Proprietaire();
        proprietaire.setNom(nom.trim());
        proprietaire.setEmail(email.trim());
        proprietaire.setMotDePasse(motDePasse);
        proprietaire.setTelephone(telephone.trim());

        repository.save(proprietaire);

        log.info("Inscription réussie pour {}", email.trim());

        return ResponseEntity.ok(
                Map.of("message", "Proprietaire enregistre avec succes.")
        );
    }

    // CONNEXION PROPRIETAIRE
    @PostMapping("/connexion")
    public ResponseEntity<?> connecter(
            @RequestBody Map<String, String> donnees) {

        String email = donnees.get("email");
        String motDePasse = donnees.get("motDePasse");

        // Log pour vérifier si Android arrive jusqu'au backend.
        // Ma n'affichiwch el mot de passe fel logs.
        log.info("API /connexion reçue pour email={}", email);

        if (email == null || email.trim().isEmpty()
                || motDePasse == null || motDePasse.isEmpty()) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "Email et mot de passe obligatoires."
                    ));
        }

        Proprietaire proprietaire =
                repository.findByEmail(email.trim()).orElse(null);

        if (proprietaire == null ||
                !motDePasse.equals(proprietaire.getMotDePasse())) {

            log.warn("Échec de connexion pour {}", email.trim());

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "message",
                            "Email ou mot de passe incorrect."
                    ));
        }

        log.info("Connexion réussie pour {}", email.trim());

        return ResponseEntity.ok(
                Map.of(
                        "message", "Connexion réussie",
                        "nom", proprietaire.getNom(),
                        "email", proprietaire.getEmail()
                )
        );
    }
}
