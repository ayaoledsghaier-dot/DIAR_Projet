
package com.diar.backend_diar.controller;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.diar.backend_diar.dto.EtudiantAvecScore;
import com.diar.backend_diar.entity.Etudiant;
import com.diar.backend_diar.repository.EtudiantRepository;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/etudiants")
public class EtudiantController {

    private final EtudiantRepository etudiantRepository;

    public EtudiantController(EtudiantRepository etudiantRepository) {
        this.etudiantRepository = etudiantRepository;
    }

    // =====================================================
    // 1. INSCRIPTION INITIALE
    // =====================================================

    @PostMapping
    public ResponseEntity<?> creerEtudiant(
            @RequestBody Etudiant etudiant) {

        try {
            if (etudiant == null ||
                    etudiant.getEmail() == null ||
                    etudiant.getEmail().trim().isEmpty()) {

                return ResponseEntity.badRequest()
                        .body("L'e-mail est obligatoire !");
            }

            String emailClean = etudiant.getEmail().trim();

            if (etudiantRepository.existsByEmail(emailClean)) {
                return ResponseEntity.status(409)
                        .body("Ce compte existe déjà !");
            }

            etudiant.setEmail(emailClean);

            if (etudiant.getStatutVerification() == null) {
                etudiant.setStatutVerification("NON_VERIFIE");
            }

            etudiant.setBudgetMax(0.0);

            Etudiant nouvelEtudiant =
                    etudiantRepository.save(etudiant);

            return ResponseEntity.status(201)
                    .body(nouvelEtudiant);

        } catch (Exception e) {
            e.printStackTrace();

            return ResponseEntity.status(500)
                    .body("Erreur serveur : " + e.getMessage());
        }
    }

    // =====================================================
    // 2. RECUPERATION DU PROFIL ETUDIANT (GET)
    // =====================================================

    @GetMapping("/profil")
    public ResponseEntity<?> obtenirProfil(
            @RequestParam("email") String email) {

        try {
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("L'email est obligatoire.");
            }

            Etudiant etudiant =
                    etudiantRepository.findByEmail(email.trim());

            if (etudiant == null) {
                return ResponseEntity.status(404)
                        .body("Étudiant introuvable.");
            }

            Map<String, Object> profil = new HashMap<>();

            profil.put("email", etudiant.getEmail());
            profil.put("nomComplet", etudiant.getNomComplet());
            profil.put("genre", etudiant.getGenre());
            profil.put("universite", etudiant.getUniversite());
            profil.put("niveau", etudiant.getNiveau());
            profil.put("faculte", etudiant.getFaculte());
            profil.put("photoUrl", etudiant.getPhotoUrl());

            return ResponseEntity.ok(profil);

        } catch (Exception e) {
            e.printStackTrace();

            return ResponseEntity.status(500)
                    .body("Erreur serveur : " + e.getMessage());
        }
    }

    // =====================================================
    // 3. MODIFICATION DU PROFIL ETUDIANT (POST)
    // =====================================================

    @PostMapping(
            value = "/profil",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> mettreAJourProfil(
            @RequestParam("email") String email,
            @RequestParam("genre") String genre,
            @RequestParam("universite") String universite,
            @RequestParam("niveau") String niveau,
            @RequestParam(value = "faculte", required = false) String faculte,
            @RequestParam(value = "photo", required = false) MultipartFile photo) {

        try {
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("L'email est obligatoire.");
            }

            Etudiant etudiant =
                    etudiantRepository.findByEmail(email.trim());

            if (etudiant == null) {
                return ResponseEntity.status(404)
                        .body("Étudiant introuvable.");
            }

            etudiant.setGenre(genre);
            etudiant.setUniversite(universite);
            etudiant.setNiveau(niveau);
            etudiant.setFaculte(
                    faculte == null ? "" : faculte.trim()
            );

            if (photo != null && !photo.isEmpty()) {

                String contentType = photo.getContentType();

                String extension;

                if ("image/jpeg".equalsIgnoreCase(contentType)) {
                    extension = ".jpg";
                } else if ("image/png".equalsIgnoreCase(contentType)) {
                    extension = ".png";
                } else if ("image/webp".equalsIgnoreCase(contentType)) {
                    extension = ".webp";
                } else {
                    return ResponseEntity.badRequest()
                            .body("Format photo non accepté !");
                }

                if (photo.getSize() > 5 * 1024 * 1024) {
                    return ResponseEntity.badRequest()
                            .body("Photo trop volumineuse (5 MB maximum).");
                }

                Path dossier = Paths.get("uploads", "etudiants");

                Files.createDirectories(dossier);

                String nomFichier =
                        UUID.randomUUID() + extension;

                Path destination = dossier.resolve(nomFichier);

                try (InputStream input = photo.getInputStream()) {
                    Files.copy(
                            input,
                            destination,
                            StandardCopyOption.REPLACE_EXISTING
                    );
                }

                etudiant.setPhotoUrl(
                        "/uploads/etudiants/" + nomFichier
                );
            }

            Etudiant resultat =
                    etudiantRepository.save(etudiant);

            return ResponseEntity.ok(resultat);

        } catch (Exception e) {
            e.printStackTrace();

            return ResponseEntity.status(500)
                    .body("Erreur serveur : " + e.getMessage());
        }
    }

    // =====================================================
    // 4. ETUDIANTS COMPATIBLES
    // =====================================================

    @GetMapping("/compatibles")
    public ResponseEntity<List<EtudiantAvecScore>>
            obtenirProfilsCompatibles(
                    @RequestParam("email") String email) {

        try {
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(null);
            }

            email = email.trim();

            Etudiant etudiantConnecte =
                    etudiantRepository.findByEmail(email);

            if (etudiantConnecte == null) {
                return ResponseEntity.status(404).body(null);
            }

            List<Etudiant> tousLesEtudiants =
                    etudiantRepository.findAll();

            List<EtudiantAvecScore> resultats =
                    new ArrayList<>();

            String genreConnecte = etudiantConnecte.getGenre();

            for (Etudiant autre : tousLesEtudiants) {

                // Ne pas afficher son propre profil
                if (autre.getEmail() != null &&
                        autre.getEmail().equalsIgnoreCase(email)) {
                    continue;
                }

                // Filtre par genre
                if (genreConnecte != null &&
                        !genreConnecte.equalsIgnoreCase(autre.getGenre())) {
                    continue;
                }

                int score = 20;

                // Université identique
                if (etudiantConnecte.getUniversite() != null &&
                        etudiantConnecte.getUniversite()
                                .equalsIgnoreCase(autre.getUniversite())) {
                    score += 25;
                }

                // Quartier identique
                if (etudiantConnecte.getQuartier() != null &&
                        etudiantConnecte.getQuartier()
                                .equalsIgnoreCase(autre.getQuartier())) {
                    score += 20;
                }

                // Type de logement identique
                if (etudiantConnecte.getTypeLogement() != null &&
                        etudiantConnecte.getTypeLogement()
                                .equalsIgnoreCase(autre.getTypeLogement())) {
                    score += 15;
                }

                // Budget proche
                if (Math.abs(
        autre.getBudgetMax()
        - etudiantConnecte.getBudgetMax()
) <= 50) {
                    score += 10;
                }

                // Propreté identique
                if (etudiantConnecte.getProprete() != null &&
                        etudiantConnecte.getProprete()
                                .equalsIgnoreCase(autre.getProprete())) {
                    score += 10;
                }

                if (score > 100) {
                    score = 100;
                }

                resultats.add(
                        new EtudiantAvecScore(autre, score)
                );
            }

            // Tri du plus compatible au moins compatible
            resultats.sort(
                    (a, b) -> Integer.compare(
                            b.getPourcentage(),
                            a.getPourcentage()
                    )
            );

            return ResponseEntity.ok(resultats);

        } catch (Exception e) {
            e.printStackTrace();

            return ResponseEntity.status(500).body(null);
        }
    }

    // =====================================================
    // 5. ENREGISTREMENT DE LA PHOTO D'INSCRIPTION
    // =====================================================

    @PostMapping(
            value = "/photo",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> enregistrerPhotoInscription(
            @RequestParam("email") String email,
            @RequestParam("photo") MultipartFile photo) {

        try {
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("Email obligatoire !");
            }

            if (photo == null || photo.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("Photo obligatoire !");
            }

            if (photo.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.badRequest()
                        .body("Photo trop volumineuse (5 MB maximum).");
            }

            String contentType = photo.getContentType();

            String extension;

            if ("image/jpeg".equalsIgnoreCase(contentType)) {
                extension = ".jpg";
            } else if ("image/png".equalsIgnoreCase(contentType)) {
                extension = ".png";
            } else if ("image/webp".equalsIgnoreCase(contentType)) {
                extension = ".webp";
            } else {
                return ResponseEntity.badRequest()
                        .body("Format photo non accepté !");
            }

            Etudiant etudiant =
                    etudiantRepository.findByEmail(email.trim());

            if (etudiant == null) {
                return ResponseEntity.status(404)
                        .body("Étudiant introuvable !");
            }

            Path dossier = Paths.get("uploads", "etudiants");

            Files.createDirectories(dossier);

            String nomFichier =
                    UUID.randomUUID() + extension;

            Path destination = dossier.resolve(nomFichier);

            try (InputStream input = photo.getInputStream()) {
                Files.copy(
                        input,
                        destination,
                        StandardCopyOption.REPLACE_EXISTING
                );
            }

            etudiant.setPhotoUrl(
                    "/uploads/etudiants/" + nomFichier
            );

            etudiantRepository.save(etudiant);

            Map<String, String> resultat = new HashMap<>();

            resultat.put(
                    "message",
                    "Photo enregistrée avec succès !"
            );

            resultat.put(
                    "photoUrl",
                    etudiant.getPhotoUrl()
            );

            return ResponseEntity.ok(resultat);

        } catch (Exception e) {
            e.printStackTrace();

            return ResponseEntity.status(500)
                    .body(
                            "Erreur lors de l'enregistrement de la photo : "
                                    + e.getMessage()
                    );
        }
    }
}
