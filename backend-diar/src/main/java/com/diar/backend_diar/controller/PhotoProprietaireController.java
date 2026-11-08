package com.diar.backend_diar.controller;

import com.diar.backend_diar.entity.Proprietaire;             // <-- Zid hedhi
import com.diar.backend_diar.repository.ProprietaireRepository; // <-- W hedhi zada

import org.springframework.core.io.Resource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/proprietaires")
public class PhotoProprietaireController {
    private final ProprietaireRepository repository;

    private final Path dossierPhotos = Path.of(
            System.getProperty("user.home"),
            "diar-uploads",
            "proprietaires"
    );

    public PhotoProprietaireController(
            ProprietaireRepository repository) {
        this.repository = repository;
    }

    // Enregistrer ou remplacer la photo du propriétaire
    @PostMapping(
        value = "/photo",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> enregistrerPhoto(
            @RequestParam("email") String email,
            @RequestParam("photo") MultipartFile photo) {

        try {
            if (email == null || email.isBlank()) {
                return ResponseEntity.badRequest().body(
                    Map.of("message", "Email obligatoire.")
                );
            }

            Proprietaire proprietaire = repository
                    .findByEmail(email.trim())
                    .orElse(null);

            if (proprietaire == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Compte introuvable."));
            }

            if (photo == null || photo.isEmpty()) {
                return ResponseEntity.badRequest().body(
                    Map.of("message", "Choisissez une photo.")
                );
            }

            if (photo.getSize() > 5L * 1024 * 1024) {
                return ResponseEntity.badRequest().body(
                    Map.of("message", "La photo ne doit pas dépasser 5 Mo.")
                );
            }

            String type = photo.getContentType();
            String extension;

            if ("image/jpeg".equalsIgnoreCase(type)) {
                extension = ".jpg";
            } else if ("image/png".equalsIgnoreCase(type)) {
                extension = ".png";
            } else if ("image/webp".equalsIgnoreCase(type)) {
                extension = ".webp";
            } else {
                return ResponseEntity.badRequest().body(
                    Map.of("message",
                        "Formats acceptés : JPG, PNG ou WEBP.")
                );
            }

            Files.createDirectories(dossierPhotos);

            String ancienNom = proprietaire.getPhotoPath();
            String nouveauNom = UUID.randomUUID() + extension;

            Path destination = dossierPhotos.resolve(nouveauNom)
                    .normalize();

            if (!destination.startsWith(dossierPhotos.normalize())) {
                return ResponseEntity.badRequest().body(
                    Map.of("message", "Nom de fichier invalide.")
                );
            }

            photo.transferTo(destination);

            proprietaire.setPhotoPath(nouveauNom);
            repository.save(proprietaire);

            // Supprimer l'ancienne photo après la sauvegarde
            if (ancienNom != null && !ancienNom.isBlank()) {
                try {
                    Files.deleteIfExists(
                        dossierPhotos.resolve(ancienNom).normalize()
                    );
                } catch (IOException ignored) {
                    // La nouvelle photo est déjà enregistrée.
                }
            }

            return ResponseEntity.ok(Map.of(
                "message", "Photo enregistrée avec succès.",
                "photoUrl", "/api/proprietaires/photo/" + nouveauNom
            ));

        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(
                Map.of("message", "Erreur lors de l'enregistrement de la photo.")
            );
        }
    }

    // Récupérer les informations du profil
    @GetMapping("/profil")
    public ResponseEntity<?> profil(@RequestParam("email") String email) {

        Proprietaire proprietaire = repository
                .findByEmail(email.trim())
                .orElse(null);

        if (proprietaire == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                Map.of("message", "Compte introuvable.")
            );
        }

        Map<String, String> resultat = new HashMap<>();
        resultat.put("nom", proprietaire.getNom());
        resultat.put("email", proprietaire.getEmail());

        String nomPhoto = proprietaire.getPhotoPath();

        if (nomPhoto != null && !nomPhoto.isBlank()) {
            resultat.put(
                "photoUrl",
                "/api/proprietaires/photo/" + nomPhoto
            );
        }

        return ResponseEntity.ok(resultat);
    }

    // Envoyer la photo au téléphone
    @GetMapping("/photo/{nomFichier}")
    public ResponseEntity<Resource> afficherPhoto(
            @PathVariable String nomFichier) {

        // Accepter uniquement les noms générés par ce contrôleur
        if (nomFichier == null ||
                !nomFichier.matches(
                    "[a-fA-F0-9-]+\\.(jpg|png|webp)")) {
            return ResponseEntity.badRequest().build();
        }

        try {
            Path fichier = dossierPhotos.resolve(nomFichier).normalize();

            if (!fichier.startsWith(dossierPhotos.normalize())
                    || !Files.isRegularFile(fichier)) {
                return ResponseEntity.notFound().build();
            }

            String extension = nomFichier.substring(
                nomFichier.lastIndexOf('.') + 1
            );

            MediaType type = switch (extension.toLowerCase()) {
                case "png" -> MediaType.IMAGE_PNG;
                case "webp" -> MediaType.parseMediaType("image/webp");
                default -> MediaType.IMAGE_JPEG;
            };

            Resource ressource = new FileSystemResource(fichier);

            return ResponseEntity.ok()
                    .contentType(type)
                    .body(ressource);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
