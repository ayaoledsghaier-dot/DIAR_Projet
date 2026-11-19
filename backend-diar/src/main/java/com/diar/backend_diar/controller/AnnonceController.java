
package com.diar.backend_diar.controller;

import com.diar.backend_diar.entity.Annonce;
import com.diar.backend_diar.entity.Etudiant;
import com.diar.backend_diar.repository.AnnonceRepository;
import com.diar.backend_diar.repository.EtudiantRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/annonces")
@CrossOrigin
public class AnnonceController {

    private final AnnonceRepository repository;
    private final EtudiantRepository etudiantRepository;

    private static final double RAYON_KM = 35.0;

    public AnnonceController(
            AnnonceRepository repository,
            EtudiantRepository etudiantRepository) {
        this.repository = repository;
        this.etudiantRepository = etudiantRepository;
    }

    // Ajouter une annonce propriétaire
    @PostMapping
    public ResponseEntity<?> ajouterAnnonce(
            @RequestBody Annonce annonce) {

        if (annonce == null
                || annonce.getTitre() == null
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

    // Afficher les annonces selon l'université de l'étudiant
    @GetMapping
    public ResponseEntity<?> afficherAnnonces(
            @RequestParam("email") String email) {

        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("Email étudiant obligatoire.");
        }

        Etudiant etudiant =
                etudiantRepository.findByEmail(email.trim());

        if (etudiant == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Étudiant introuvable.");
        }

        String universite = etudiant.getUniversite();

        double[] centre = obtenirCentreUniversite(universite);

        if (centre == null) {
            return ResponseEntity.badRequest()
                    .body("Université non reconnue : " + universite);
        }

        List<Annonce> toutesLesAnnonces = repository.findAll();

        List<Annonce> annoncesFiltrees = new ArrayList<>();

        for (Annonce annonce : toutesLesAnnonces) {

            if (annonce.getLatitude() == null
                    || annonce.getLongitude() == null) {
                continue;
            }

            double distance = calculerDistance(
                    centre[0],
                    centre[1],
                    annonce.getLatitude(),
                    annonce.getLongitude()
            );

            if (distance <= RAYON_KM) {
                annoncesFiltrees.add(annonce);
            }
        }

        System.out.println("Étudiant : " + email);
        System.out.println("Université : " + universite);
        System.out.println("Annonces trouvées : "
                + annoncesFiltrees.size());

        return ResponseEntity.ok(annoncesFiltrees);
    }

    // Coordonnées des villes universitaires
    private double[] obtenirCentreUniversite(String universite) {

        if (universite == null) {
            return null;
        }

        switch (universite.trim()) {

            case "Université de Sousse":
                return new double[]{35.8256, 10.6084};

            case "Université de Monastir":
                return new double[]{35.7643, 10.8113};

            case "Université de Sfax":
                return new double[]{34.7406, 10.7603};

            case "Université de Tunis":
                return new double[]{36.8065, 10.1815};

            default:
                return null;
        }
    }

    // Calcul de la distance entre deux coordonnées GPS
    private double calculerDistance(
            double lat1,
            double lon1,
            double lat2,
            double lon2) {

        final int R = 6371;

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a =
                Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2)
                * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(
                Math.sqrt(a),
                Math.sqrt(1 - a)
        );

        return R * c;
    }

    // Récupérer une annonce par ID
 
// Récupérer une annonce par ID avec les informations propriétaire
@GetMapping("/{id}")
public ResponseEntity<?> afficherAnnonce(@PathVariable Long id) {

    return repository.findById(id)
            .<ResponseEntity<?>>map(annonce -> {

                Map<String, Object> resultat = new HashMap<>();

                resultat.put("id", annonce.getId());
                resultat.put("titre", annonce.getTitre());
                resultat.put("description", annonce.getDescription());
                resultat.put("prix", annonce.getPrix());
                resultat.put("latitude", annonce.getLatitude());
                resultat.put("longitude", annonce.getLongitude());
                resultat.put("nombrePersonnes", annonce.getNombrePersonnes());
                resultat.put("photos", annonce.getPhotos());

                // Email du propriétaire enregistré avec l'annonce
                resultat.put(
                        "proprietaireEmail",
                        annonce.getEmailProprietaire() == null
                                ? ""
                                : annonce.getEmailProprietaire()
                );

                resultat.put("proprietaireNom", "");

                System.out.println("ID annonce : " + annonce.getId());
                System.out.println("Email propriétaire : "
                        + annonce.getEmailProprietaire());

                return ResponseEntity.ok(resultat);
            })
            .orElseGet(() -> ResponseEntity.notFound().build());
}


    // Supprimer une annonce
    @DeleteMapping("/{id}")
    public ResponseEntity<?> supprimerAnnonce(
            @PathVariable Long id) {

        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        repository.deleteById(id);

        return ResponseEntity.ok("Annonce supprimée.");
    }
}
