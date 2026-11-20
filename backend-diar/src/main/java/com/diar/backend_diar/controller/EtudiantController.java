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

    // 1. Inscription initiale - CORRIGÉE
    @PostMapping({ "", "/register" })
    public ResponseEntity<?> creerEtudiant(@RequestBody Map<String, String> body) {
        try {
            String nomComplet = body.get("nomComplet");
            if (nomComplet == null)
                nomComplet = body.get("nom"); // compatibilité avec ton Android actuel
            String email = body.get("email");
            String motDePasse = body.get("motDePasse");
            if (motDePasse == null)
                motDePasse = body.get("password"); // compatibilité

            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.status(400).body("L'e-mail est obligatoire !");
            }
            if (nomComplet == null || nomComplet.trim().isEmpty()) {
                return ResponseEntity.status(400).body("Le nom complet est obligatoire !");
            }
            if (motDePasse == null || motDePasse.length() < 6) {
                return ResponseEntity.status(400).body("Mot de passe trop court !");
            }

            String emailClean = email.trim().toLowerCase(); // IMPORTANT : comme dans ta méthode connexion
            if (etudiantRepository.existsByEmail(emailClean)) {
                return ResponseEntity.status(409).body("Ce compte existe déjà !");
            }

            Etudiant etudiant = new Etudiant();
            etudiant.setNomComplet(nomComplet.trim());
            etudiant.setEmail(emailClean);
            etudiant.setMotDePasse(motDePasse); // attention : en clair pour l'instant, comme ton code connexion
            etudiant.setStatutVerification("NON_VERIFIE");
            etudiant.setBudgetMax(0.0);

            Etudiant nouvelEtudiant = etudiantRepository.save(etudiant);
            return ResponseEntity.status(201).body(nouvelEtudiant);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Erreur serveur : " + e.getMessage());
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
            @RequestParam(value = "age", required = false) Integer age) {

        try {
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.status(400).body("Email obligatoire !");
            }
            // IMPORTANT : lowercase comme à l'inscription et à la connexion
            String emailClean = email.trim().toLowerCase();
            Etudiant etudiant = etudiantRepository.findByEmail(emailClean);

            // NE JAMAIS prendre le premier étudiant, sinon photo sur le mauvais compte
            if (etudiant == null) {
                return ResponseEntity.status(404).body("Étudiant introuvable pour l'email : " + emailClean);
            }

            etudiant.setGenre(genre);
            etudiant.setUniversite(universite);
            etudiant.setNiveau(niveau);
            if (age != null) {
                etudiant.setAge(age); // vérifie que ton entité Etudiant a bien un champ age
            }

            Etudiant saved = etudiantRepository.save(etudiant);
            return ResponseEntity.ok(saved);

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
                System.out.println("⚠ Email introuvable ou vide, utilisation du premier étudiant en BDD par défaut.");
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
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.status(400).body(null);
            }
            String emailClean = email.trim().toLowerCase();
            Etudiant etudiantConnecte = etudiantRepository.findByEmail(emailClean);
            if (etudiantConnecte == null) {
                return ResponseEntity.status(404).body(null);
            }

            List<Etudiant> tousLesEtudiants = etudiantRepository.findAll();
            List<EtudiantAvecScore> resultats = new ArrayList<>();

            String genreConnecte = etudiantConnecte.getGenre();

            // ✅ CORRIGÉ : UNE SEULE BOUCLE (avant tu en avais 2 imbriquées)
            for (Etudiant autre : tousLesEtudiants) {
                // 1. Ne pas se comparer soi-même
                if (autre.getEmail() != null && autre.getEmail().equalsIgnoreCase(emailClean)) {
                    continue;
                }

                // 2. GENRE : FILTRE STRICT + POINTS
                if (genreConnecte != null && !genreConnecte.trim().isEmpty()
                        && !genreConnecte.equalsIgnoreCase(autre.getGenre())) {
                    continue;
                }

                // 3. FILTRE COLOCATION : que ceux qui veulent la coloc
                if (autre.getLocationType() != null && !autre.getLocationType().trim().isEmpty()
                        && !autre.getLocationType().equalsIgnoreCase("Colocation")) {
                    continue;
                }

                // 4. FILTRE PROFIL INCOMPLET
                if (autre.getQuartier() == null || autre.getQuartier().trim().isEmpty()) {
                    continue;
                }

                int score = 0;

                // --- GENRE : +10 ---
                if (equalsIgnore(etudiantConnecte.getGenre(), autre.getGenre())) {
                    score += 10;
                }

                // --- QUARTIER : +20 ---
                if (equalsIgnore(etudiantConnecte.getQuartier(), autre.getQuartier())) {
                    score += 20;
                }

                // --- BUDGET : +15 si écart <= 50, +8 si écart <= 100 ---
                double diffBudget = Math.abs(autre.getBudgetMax() - etudiantConnecte.getBudgetMax());
                if (diffBudget <= 50) {
                    score += 15;
                } else if (diffBudget <= 100) {
                    score += 8;
                }

                // --- TYPE LOGEMENT : +10 ---
                if (equalsIgnore(etudiantConnecte.getTypeLogement(), autre.getTypeLogement())) {
                    score += 10;
                }

                // --- ÂGE : +10 si écart <= 2 ans, +5 si écart <= 4 ans ---
                if (etudiantConnecte.getAge() != null && autre.getAge() != null) {
                    int diffAge = Math.abs(autre.getAge() - etudiantConnecte.getAge());
                    if (diffAge <= 2) {
                        score += 10;
                    } else if (diffAge <= 4) {
                        score += 5;
                    }
                }

                // --- MODE DE VIE : +10 (Profil Coloc) ---
                if (equalsIgnore(etudiantConnecte.getModeDeVie(), autre.getModeDeVie())) {
                    score += 10;
                }

                // --- UNIVERSITÉ : +5 ---
                if (equalsIgnore(etudiantConnecte.getUniversite(), autre.getUniversite())) {
                    score += 5;
                }

                // --- ÉQUIPEMENTS : +5 au prorata ---
                score += calculEquipements(etudiantConnecte.getEquipements(), autre.getEquipements(), 5);

                // --- PROPRETÉ : +5 ---
                if (equalsIgnore(etudiantConnecte.getProprete(), autre.getProprete())) {
                    score += 5;
                }

                // --- ANIMAUX : +5 ---
                if (equalsIgnore(etudiantConnecte.getAnimauxAcceptes(), autre.getAnimauxAcceptes())) {
                    score += 5;
                }

                // --- NB COLOCATAIRES : +5 ---
                if (equalsIgnore(etudiantConnecte.getNbColocataires(), autre.getNbColocataires())) {
                    score += 5;
                }

                // Plafonner à 100
                if (score > 100)
                    score = 100;

                // ✅ CORRIGÉ : UN SEUL add (avant tu en avais 2)
                resultats.add(new EtudiantAvecScore(autre, score));
            }

            // Trier du plus compatible au moins compatible
            resultats.sort((a, b) -> Integer.compare(b.getPourcentage(), a.getPourcentage()));

            return ResponseEntity.ok(resultats);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    // --- Méthodes utilitaires ---

    private boolean equalsIgnore(String a, String b) {
        if (a == null || b == null)
            return false;
        a = a.trim();
        b = b.trim();
        if (a.isEmpty() || b.isEmpty())
            return false;
        return a.equalsIgnoreCase(b);
    }

    // ✅ CORRIGÉ : ajout du paramètre maxPoints (avant la méthode prenait 2 params mais tu l'appelais avec 3)
    private int calculEquipements(String equipementsMoi, String equipementsAutre, int maxPoints) {
        if (equipementsMoi == null || equipementsAutre == null)
            return 0;
        equipementsMoi = equipementsMoi.trim();
        equipementsAutre = equipementsAutre.trim();
        if (equipementsMoi.isEmpty() || equipementsAutre.isEmpty())
            return 0;

        // On découpe "Wi-Fi, Climatisation" -> ["Wi-Fi", "Climatisation"]
        String[] mesEquipements = equipementsMoi.split(",");
        String[] sesEquipements = equipementsAutre.split(",");

        int enCommun = 0;
        for (String monEq : mesEquipements) {
            monEq = monEq.trim();
            if (monEq.isEmpty())
                continue;
            for (String sonEq : sesEquipements) {
                if (monEq.equalsIgnoreCase(sonEq.trim())) {
                    enCommun++;
                    break;
                }
            }
        }

        if (enCommun == 0)
            return 0;
        // Proportion : si tu as 2 équipements et vous avez 2 en commun -> maxPoints
        // Si tu as 4 et vous avez 2 en commun -> la moitié de maxPoints
        double ratio = (double) enCommun / mesEquipements.length;
        return (int) Math.round(ratio * maxPoints);
    }

    // 7. Enregistrement de la photo de profil lors de l'inscription
    @PostMapping(value = "/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> enregistrerPhotoInscription(
            @RequestParam("email") String email,
            @RequestParam("photo") MultipartFile photo) {
        try {
            if (email == null || email.trim().isEmpty())
                return ResponseEntity.badRequest().body("Email obligatoire !");
            if (photo == null || photo.isEmpty())
                return ResponseEntity.badRequest().body("Photo obligatoire !");

            String emailClean = email.trim().toLowerCase();
            Etudiant etudiant = etudiantRepository.findByEmail(emailClean);
            if (etudiant == null)
                return ResponseEntity.status(404).body("Étudiant introuvable !");

            // On accepte tout ce qui commence par image/
            String contentType = photo.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body("Format non accepté !");
            }
            String extension = ".jpg";
            if (contentType.contains("png"))
                extension = ".png";
            else if (contentType.contains("webp"))
                extension = ".webp";

            Path dossier = Paths.get("uploads", "etudiants");
            Files.createDirectories(dossier);
            String nomFichier = UUID.randomUUID().toString() + extension;
            Path destination = dossier.resolve(nomFichier);
            Files.copy(photo.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);

            // TOUJOURS le même format : /uploads/etudiants/xxx.jpg
            etudiant.setPhotoUrl("/uploads/etudiants/" + nomFichier);
            etudiantRepository.save(etudiant);

            Map<String, String> resultat = new HashMap<>();
            resultat.put("message", "Photo enregistrée !");
            resultat.put("photoUrl", etudiant.getPhotoUrl());
            return ResponseEntity.ok(resultat);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Erreur photo : " + e.getMessage());
        }
    }

    // ==========================================
    // CONNEXION ETUDIANT
    // ==========================================
    @PostMapping("/connexion")
    public ResponseEntity<?> connecter(@RequestBody Map<String, String> donnees) {

        String email = donnees.get("email");
        String motDePasse = donnees.get("motDePasse");

        if (email == null || email.trim().isEmpty()
                || motDePasse == null || motDePasse.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Email et mot de passe obligatoires."));
        }

        // L'app enregistre les emails en minuscules a l'inscription
        String emailClean = email.trim().toLowerCase();

        Etudiant etudiant = etudiantRepository.findByEmail(emailClean);

        if (etudiant == null || !motDePasse.equals(etudiant.getMotDePasse())) {
            return ResponseEntity.status(401)
                    .body(Map.of("message", "Email ou mot de passe incorrect."));
        }

        // Prochaine etape de l'inscription a reprendre (ou ACCUEIL si tout est fait)
        String etape;
        if (estVide(etudiant.getGenre()) || estVide(etudiant.getUniversite())
                || estVide(etudiant.getNiveau())) {
            etape = "PROFIL";
        } else if (estVide(etudiant.getTypeLogement())) {
            etape = "PREFERENCES";
        } else if (!"VERIFIE".equals(etudiant.getStatutVerification())) {
            etape = "VERIFICATION";
        } else if (estVide(etudiant.getModeDeVie())) {
            etape = "PROFIL_COLOC";
        } else {
            etape = "ACCUEIL";
        }

        // HashMap (et pas Map.of) car nomComplet peut etre null
        Map<String, Object> reponse = new HashMap<>();
        reponse.put("message", "Connexion r\u00e9ussie");
        reponse.put("role", "ETUDIANT");
        reponse.put("nom", etudiant.getNomComplet() != null ? etudiant.getNomComplet() : "");
        reponse.put("email", etudiant.getEmail());
        reponse.put("etape", etape);

        return ResponseEntity.ok(reponse);
    }

    private static boolean estVide(String s) {
        return s == null || s.trim().isEmpty();
    }
}