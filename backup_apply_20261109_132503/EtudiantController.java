package com.diar.backend_diar.controller;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

import com.diar.backend_diar.dto.EtudiantAvecScore;
import com.diar.backend_diar.entity.Etudiant;
import com.diar.backend_diar.repository.EtudiantRepository;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.http.MediaType;
import java.util.UUID;

@SuppressWarnings("unused")
@RestController
@RequestMapping("/api/etudiants")
public class EtudiantController {

    private final EtudiantRepository etudiantRepository;

    public EtudiantController(EtudiantRepository etudiantRepository) {
        this.etudiantRepository = etudiantRepository;
    }

    // 1. Inscription initiale (Création de l'étudiant avec nom, email et mot de
    // passe)
    @PostMapping
    public ResponseEntity<?> creerEtudiant(@RequestBody Etudiant etudiant) {
        try {
            if (etudiant == null || etudiant.getEmail() == null || etudiant.getEmail().trim().isEmpty()) {
                return ResponseEntity.status(400).body("L'e-mail est obligatoire !");
            }

            String emailClean = etudiant.getEmail().trim();
            if (etudiantRepository.existsByEmail(emailClean)) {
                return ResponseEntity.status(409).body("Ce compte existe déjà !");
            }

            etudiant.setEmail(emailClean);

            // Sécurité anti-crash : Initialisation des valeurs par défaut pour éviter les
            // erreurs de contrainte en BDD
            if (etudiant.getStatutVerification() == null) {
                etudiant.setStatutVerification("NON_VERIFIE");
            }
            etudiant.setBudgetMax(0.0);

            Etudiant nouvelEtudiant = etudiantRepository.save(etudiant);
            return ResponseEntity.status(201).body(nouvelEtudiant);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Erreur serveur lors de la création : " + e.getMessage());
        }
    }

    // 2. Récupération du profil et des informations de l'étudiant (Interface 7)
    @GetMapping("/profil")
    public ResponseEntity<?> obtenirProfilParEmail(@RequestParam("email") String email) {
        try {
            if (email != null) {
                email = email.trim();
            } else {
                return ResponseEntity.status(400).body("L'e-mail est obligatoire pour récupérer le profil.");
            }

            Etudiant etudiant = etudiantRepository.findByEmail(email);
            if (etudiant == null) {
                return ResponseEntity.status(404).body("Étudiant introuvable pour l'email : " + email);
            }

            return ResponseEntity.ok(etudiant);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Erreur serveur : " + e.getMessage());
        }
    }

    // 3. Mise à jour du profil (Interface 5 : genre, université, niveau et photo)
    @PostMapping("/profil")
    public ResponseEntity<?> mettreAJourProfil(
            @RequestParam("email") String email,
            @RequestParam("genre") String genre,
            @RequestParam("universite") String universite,
            @RequestParam("niveau") String niveau,
            @RequestParam(value = "photo", required = false) MultipartFile photo) {

        try {
            if (email != null)
                email = email.trim();
            Etudiant etudiant = etudiantRepository.findByEmail(email);
            if (etudiant == null) {
                List<Etudiant> tous = etudiantRepository.findAll();
                if (!tous.isEmpty()) {
                    etudiant = tous.get(0);
                } else {
                    return ResponseEntity.status(404).body("Étudiant introuvable pour l'email : " + email);
                }
            }

            etudiant.setGenre(genre);
            etudiant.setUniversite(universite);
            etudiant.setNiveau(niveau);

            if (photo != null && !photo.isEmpty()) {
                String fileName = System.currentTimeMillis() + "_" + photo.getOriginalFilename();
                Path path = Paths.get("uploads/" + fileName);
                Files.createDirectories(path.getParent());
                Files.write(path, photo.getBytes());
                etudiant.setPhotoUrl(fileName);
            }

            Etudiant etudiantMisAJour = etudiantRepository.save(etudiant);
            return ResponseEntity.ok(etudiantMisAJour);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Erreur serveur : " + e.getMessage());
        }
    }

    // 4. Vérification automatique par OCR de l'attestation d'inscription
    @PostMapping("/verifier")
    public ResponseEntity<?> verifierEtudiant(
            @RequestParam("email") String email,
            @RequestParam("carte") MultipartFile fichierAttestation) {

        File tempFile = null;
        try {
            if (email != null)
                email = email.trim();
            Etudiant etudiant = etudiantRepository.findByEmail(email);
            if (etudiant == null) {
                List<Etudiant> tous = etudiantRepository.findAll();
                if (!tous.isEmpty()) {
                    etudiant = tous.get(0);
                } else {
                    return ResponseEntity.status(404).body("Étudiant introuvable avec cet e-mail.");
                }
            }

            tempFile = File.createTempFile("attestation_", ".jpg");
            Files.copy(fichierAttestation.getInputStream(), tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            Tesseract tesseract = new Tesseract();
            tesseract.setDatapath("C:/tessdata");
            tesseract.setLanguage("fra");

            String texteExtrait = tesseract.doOCR(tempFile);
            String texteMaj = texteExtrait.toUpperCase();

            System.out.println("=== TEXTE EXTRAIT PAR TESSERACT ===");
            System.out.println(texteMaj);
            System.out.println("==================================");

            // 1. VÉRIFICATION DES MOTS-CLÉS GLOBAUX (Séparée pour un message simple direct)
            boolean contientMotsCles = texteMaj.contains("ATTESTATION") ||
                    texteMaj.contains("INSCRIPTION") ||
                    texteMaj.contains("UNIVERSITE") ||
                    texteMaj.contains("INSTITUT") ||
                    texteMaj.contains("FACULTE");

            if (!contientMotsCles) {
                etudiant.setStatutVerification("NON_VERIFIE");
                etudiantRepository.save(etudiant);
                return ResponseEntity.status(400).body("Il ne s'agit pas d'une attestation d'inscription.");
            }

            // Liste pour accumuler toutes les autres erreurs de validation si c'est bien
            // une attestation
            List<String> erreurs = new java.util.ArrayList<>();

            // 2. VÉRIFICATION DE L'ANNÉE UNIVERSITAIRE
            LocalDate aujourdHui = LocalDate.now();
            int anneeActuelle = aujourdHui.getYear();
            int moisActuel = aujourdHui.getMonthValue();

            String anneeUniversitaire1, anneeUniversitaire2;
            if (moisActuel >= 9) {
                anneeUniversitaire1 = anneeActuelle + "-" + (anneeActuelle + 1);
                anneeUniversitaire2 = anneeActuelle + "/" + (anneeActuelle + 1);
            } else {
                anneeUniversitaire1 = (anneeActuelle - 1) + "-" + anneeActuelle;
                anneeUniversitaire2 = (anneeActuelle - 1) + "/" + anneeActuelle;
            }

            boolean anneePresente = texteMaj.contains(anneeUniversitaire1) || texteMaj.contains(anneeUniversitaire2);

            if (!anneePresente) {
                erreurs.add("L'attestation n'est pas valide pour l'année universitaire en cours (" + anneeUniversitaire1
                        + ").");
            }

            // 3. VÉRIFICATION DU NOM COMPLET (Sans Regex)
            String nomEtudiant = etudiant.getNomComplet() != null ? etudiant.getNomComplet().toUpperCase().trim() : "";
            boolean nomPresent = false;

            if (!nomEtudiant.isEmpty()) {
                String[] motsNom = nomEtudiant.split(" ");
                int motsTrouves = 0;
                int totalMots = 0;

                for (String mot : motsNom) {
                    mot = mot.trim();
                    if (mot.length() >= 3) {
                        totalMots++;
                        if (texteMaj.contains(mot)) {
                            motsTrouves++;
                        }
                    }
                }

                if (totalMots == 0 || motsTrouves >= 1) {
                    nomPresent = true;
                }
            } else {
                nomPresent = true;
            }

            if (!nomPresent) {
                erreurs.add("Le nom complet ne correspond pas au document.");
            }

            // 4. VÉRIFICATION STRICTE DU NIVEAU D'ÉTUDE (Sans Regex)
            String niveauEtudiant = etudiant.getNiveau() != null ? etudiant.getNiveau().toUpperCase().trim() : "";
            boolean niveauPresent = true;

            if (!niveauEtudiant.isEmpty()) {
                String[] motsNiveau = niveauEtudiant.split(" ");
                int motsTotal = 0;
                int motsTrouves = 0;

                for (String mot : motsNiveau) {
                    mot = mot.trim();
                    // On ignore les mots de liaison trop courts
                    if (mot.length() >= 2 && !mot.equals("DE") && !mot.equals("LA") && !mot.equals("ET")
                            && !mot.equals("EN") && !mot.equals("DU")) {
                        motsTotal++;
                        if (texteMaj.contains(mot)) {
                            motsTrouves++;
                        }
                    }
                }

                // Pour que le niveau soit valide, TOUS les mots significatifs (ex: le chiffre
                // et "année")
                // doivent être présents dans le texte extrait par l'OCR.
                if (motsTotal > 0 && motsTrouves < motsTotal) {
                    niveauPresent = false;
                }
            }

            if (!niveauPresent) {
                erreurs.add("Le niveau d'étude ne correspond pas au document.");
            }
            // 5. VÉRIFICATION DE L'UNIVERSITÉ (Sans Regex)
            String universiteEtudiante = etudiant.getUniversite() != null
                    ? etudiant.getUniversite().toUpperCase().trim()
                    : "";
            boolean universitePresente = true;

            if (!universiteEtudiante.isEmpty()) {
                String[] motsUniv = universiteEtudiante.split(" ");
                boolean trouveUniv = false;
                for (String mot : motsUniv) {
                    mot = mot.trim();
                    if (mot.length() > 3 && texteMaj.contains(mot)) {
                        trouveUniv = true;
                        break;
                    }
                }
                universitePresente = trouveUniv;
            }

            if (!universitePresente) {
                erreurs.add("L'université ne correspond pas au document.");
            }

            // S'il y a des erreurs, on les retourne toutes ensemble
            if (!erreurs.isEmpty()) {
                etudiant.setStatutVerification("NON_VERIFIE");
                etudiantRepository.save(etudiant);
                return ResponseEntity.status(400).body("Échec de vérification : " + String.join(" | ", erreurs));
            }

            // 6. VALIDATION FINALE RÉUSSIE
            etudiant.setStatutVerification("VERIFIE");
            etudiantRepository.save(etudiant);
            return ResponseEntity.ok("Vérification automatique réussie ! Compte validé.");

        } catch (TesseractException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Erreur lors de l'analyse OCR : " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Erreur serveur : " + e.getMessage());
        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    // 5. Enregistrement des préférences de logement
    @PostMapping("/preferences")
    public ResponseEntity<?> enregistrerPreferences(
            @RequestParam(value = "email", required = false) String email,
            @RequestParam("typeLogement") String typeLogement,
            @RequestParam("locationType") String locationType,
            @RequestParam("budgetMax") double budgetMax,
            @RequestParam("equipements") String equipements,
            @RequestParam("quartier") String quartier) {

        try {
            if (email != null) {
                email = email.trim();
            }

            System.out.println("=== EMAIL REÇU POUR PRÉFÉRENCES : [" + email + "] ===");

            Etudiant etudiant = null;
            if (email != null && !email.isEmpty()) {
                etudiant = etudiantRepository.findByEmail(email);
            }

            if (etudiant == null) {
                System.out.println("⚠️ Email introuvable ou vide, utilisation du premier étudiant en BDD par défaut.");
                List<Etudiant> tousLesEtudiants = etudiantRepository.findAll();
                if (!tousLesEtudiants.isEmpty()) {
                    etudiant = tousLesEtudiants.get(0);
                }
            }

            if (etudiant == null) {
                System.out.println("❌ ERREUR : Aucun étudiant trouvé dans la base de données !");
                return ResponseEntity.status(404).body("Aucun étudiant trouvé dans la base de données.");
            }

            etudiant.setTypeLogement(typeLogement);
            etudiant.setLocationType(locationType);
            etudiant.setBudgetMax(budgetMax);
            etudiant.setEquipements(equipements);
            etudiant.setQuartier(quartier);

            etudiantRepository.save(etudiant);

            System.out.println("✅ Préférences enregistrées avec succès pour l'étudiant : " + etudiant.getEmail());
            return ResponseEntity.ok("Préférences enregistrées avec succès !");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Erreur serveur : " + e.getMessage());
        }
    }

    // 6. Enregistrement du profil colocataire
    @PostMapping("/profil-colocataire")
    public ResponseEntity<?> enregistrerProfilColocataire(@RequestBody Map<String, String> requestBody) {
        try {
            String email = requestBody.get("email");
            String modeDeVie = requestBody.get("modeDeVie");
            String nbColocataires = requestBody.get("nbColocataires");
            String animauxAcceptes = requestBody.get("animauxAcceptes");

            // 🆕 Récupération du nouveau critère propreté
            String proprete = requestBody.get("proprete");

            if (email != null) {
                email = email.trim();
            }

            Etudiant etudiant = null;
            if (email != null && !email.isEmpty()) {
                etudiant = etudiantRepository.findByEmail(email);
            }

            if (etudiant == null) {
                List<Etudiant> tousLesEtudiants = etudiantRepository.findAll();
                if (!tousLesEtudiants.isEmpty()) {
                    etudiant = tousLesEtudiants.get(0);
                }
            }

            if (etudiant == null) {
                return ResponseEntity.status(404).body("Aucun étudiant trouvé dans la base de données.");
            }

            // Mise à jour de tous les champs (y compris la propreté)
            etudiant.setModeDeVie(modeDeVie);
            etudiant.setNbColocataires(nbColocataires);
            etudiant.setAnimauxAcceptes(animauxAcceptes);
            etudiant.setProprete(proprete); // 🆕 Enregistrement

            etudiantRepository.save(etudiant);

            return ResponseEntity.ok("Profil colocataire enregistré avec succès !");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Erreur serveur : " + e.getMessage());
        }
    }

    @GetMapping("/compatibles")
    public ResponseEntity<List<EtudiantAvecScore>> obtenirProfilsCompatibles(@RequestParam("email") String email) {
        System.out.println(">>> Email reçu depuis Android : [" + email + "]");
        try {
            Etudiant etudiantConnecte = etudiantRepository.findByEmail(email);
            if (etudiantConnecte == null) {
                return ResponseEntity.status(404).body(null);
            }

            List<Etudiant> tousLesEtudiants = etudiantRepository.findAll();
            List<EtudiantAvecScore> resultats = new ArrayList<>();

            String genreConnecte = etudiantConnecte.getGenre(); // "Femme" ou "Homme"

            for (Etudiant autre : tousLesEtudiants) {
                // 1. Ne pas se comparer soi-même
                if (autre.getEmail() != null && autre.getEmail().equalsIgnoreCase(email)) {
                    continue;
                }

                // 2. FILTRE STRICT PAR GENRE : Si l'étudiant connecté est une femme, afficher
                // uniquement les femmes (et vice-versa)
                if (genreConnecte != null && !genreConnecte.equalsIgnoreCase(autre.getGenre())) {
                    continue; // Ignore ce profil s'il ne respecte pas la règle de genre
                }

                int score = 20; // Score de base minimum

                // 3. Université identique (+25 points)
                if (etudiantConnecte.getUniversite() != null &&
                        etudiantConnecte.getUniversite().equalsIgnoreCase(autre.getUniversite())) {
                    score += 25;
                }

                // 4. Quartier identique (+20 points)
                if (etudiantConnecte.getQuartier() != null &&
                        etudiantConnecte.getQuartier().equalsIgnoreCase(autre.getQuartier())) {
                    score += 20;
                }

                // 5. Type de logement identique (+15 points)
                if (etudiantConnecte.getTypeLogement() != null &&
                        etudiantConnecte.getTypeLogement().equalsIgnoreCase(autre.getTypeLogement())) {
                    score += 15;
                }

                // 6. Budget max proche (écart de 50 DT max -> +10 points)
                if (Math.abs(autre.getBudgetMax() - etudiantConnecte.getBudgetMax()) <= 50) {
                    score += 10;
                }

                // 7. Propreté (+10 points)
                if (etudiantConnecte.getProprete() != null &&
                        etudiantConnecte.getProprete().equalsIgnoreCase(autre.getProprete())) {
                    score += 10;
                }

                // Plafonner le score à 100%
                if (score > 100)
                    score = 100;

                // Ajouter le résultat dans la liste
                resultats.add(new EtudiantAvecScore(autre, score));
            }

            // Trier par pourcentage décroissant (du plus compatible au moins compatible)
            resultats.sort((a, b) -> Integer.compare(b.getPourcentage(), a.getPourcentage()));

            return ResponseEntity.ok(resultats);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }
    // 7. Enregistrement de la photo de profil lors de l'inscription
@PostMapping(value = "/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public ResponseEntity<?> enregistrerPhotoInscription(
        @RequestParam("email") String email,
        @RequestParam("photo") MultipartFile photo) {

    try {
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Email obligatoire !");
        }

        if (photo == null || photo.isEmpty()) {
            return ResponseEntity.badRequest().body("Photo obligatoire !");
        }

        if (photo.getSize() > 5 * 1024 * 1024) {
            return ResponseEntity.badRequest()
                    .body("Photo trop volumineuse (5 MB maximum).");
        }

        String contentType = photo.getContentType();
        String extension;

        if ("image/jpeg".equals(contentType)) {
            extension = ".jpg";
        } else if ("image/png".equals(contentType)) {
            extension = ".png";
        } else if ("image/webp".equals(contentType)) {
            extension = ".webp";
        } else {
            return ResponseEntity.badRequest()
                    .body("Format photo non accepté !");
        }

        String emailClean = email.trim();

        Etudiant etudiant = etudiantRepository.findByEmail(emailClean);

        if (etudiant == null) {
            return ResponseEntity.status(404)
                    .body("Étudiant introuvable !");
        }

        Path dossier = Paths.get("uploads", "etudiants");
        Files.createDirectories(dossier);

        String nomFichier = UUID.randomUUID().toString() + extension;

        Path destination = dossier.resolve(nomFichier);

        try (java.io.InputStream input = photo.getInputStream()) {
            Files.copy(input, destination,
                    StandardCopyOption.REPLACE_EXISTING);
        }

        etudiant.setPhotoUrl("/uploads/etudiants/" + nomFichier);

        etudiantRepository.save(etudiant);

        Map<String, String> resultat = new HashMap<>();
        resultat.put("message", "Photo enregistrée avec succès !");
        resultat.put("photoUrl", etudiant.getPhotoUrl());

        return ResponseEntity.ok(resultat);

    } catch (Exception e) {
        e.printStackTrace();

        return ResponseEntity.status(500)
                .body("Erreur lors de l'enregistrement de la photo : "
                        + e.getMessage());
    }
}
}