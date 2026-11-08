package com.diar.backend_diar.controller;

import com.diar.backend_diar.entity.Annonce;
import com.diar.backend_diar.repository.AnnonceRepository; // <-- Zid hedhi l'import
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/annonces")
@CrossOrigin
public class AnnonceController {

    private final AnnonceRepository repository;

    public AnnonceController(AnnonceRepository repository) {
        this.repository = repository;
    }

    // Ajouter une annonce
    @PostMapping
    public ResponseEntity<?> ajouterAnnonce(@RequestBody Annonce annonce) {

        if (annonce.getTitre() == null
                || annonce.getTitre().isBlank()
                || annonce.getPrix() == null
                || annonce.getPrix() <= 0
                || annonce.getLatitude() == null
                || annonce.getLongitude() == null) {

            return ResponseEntity.badRequest()
                    .body("Informations obligatoires manquantes.");
        }

        Annonce nouvelleAnnonce = repository.save(annonce);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(nouvelleAnnonce);
    }

    // Afficher toutes les annonces
    @GetMapping
    public ResponseEntity<List<Annonce>> afficherAnnonces() {

        List<Annonce> annonces = repository.findAll();

        System.out.println("Nombre d'annonces : " + annonces.size());

        return ResponseEntity.ok(annonces);
    }

    // Afficher une annonce par ID
    @GetMapping("/{id}")
    public ResponseEntity<?> afficherAnnonce(@PathVariable Long id) {

        System.out.println(">>> Demande détail annonce ID = " + id);

        return repository.findById(id)
                .<ResponseEntity<?>>map(annonce -> {
                    System.out.println(">>> Annonce trouvée : " + id);
                    return ResponseEntity.ok(annonce);
                })
                .orElseGet(() -> {
                    System.out.println(">>> Annonce introuvable : " + id);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body("Annonce avec ID " + id + " introuvable.");
                });
    }

    // Supprimer une annonce par ID
    @DeleteMapping("/{id}")
    public ResponseEntity<?> supprimerAnnonce(@PathVariable Long id) {

        System.out.println(">>> Demande suppression annonce ID = " + id);

        return repository.findById(id)
                .<ResponseEntity<?>>map(annonce -> {

                    repository.delete(annonce);

                    System.out.println(">>> Annonce supprimée : " + id);

                    return ResponseEntity.ok(
                            "Annonce supprimée avec succès."
                    );
                })
                .orElseGet(() -> {
                    System.out.println(">>> Annonce introuvable : " + id);

                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body("Annonce avec ID " + id + " introuvable.");
                });
    }
}
