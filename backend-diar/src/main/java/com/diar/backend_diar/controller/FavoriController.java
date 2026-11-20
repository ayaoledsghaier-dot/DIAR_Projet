package com.diar.backend_diar.controller;

import com.diar.backend_diar.entity.Etudiant;
import com.diar.backend_diar.entity.Favori;
import com.diar.backend_diar.repository.EtudiantRepository;
import com.diar.backend_diar.repository.FavoriRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/etudiants/favoris")
public class FavoriController {

    private final EtudiantRepository etudiantRepository;
    private final FavoriRepository favoriRepository;

    public FavoriController(EtudiantRepository etudiantRepository, FavoriRepository favoriRepository) {
        this.etudiantRepository = etudiantRepository;
        this.favoriRepository = favoriRepository;
    }

    @GetMapping
    public ResponseEntity<?> getFavoris(@RequestParam String email) {
        try {
            Etudiant moi = etudiantRepository.findByEmail(email.trim().toLowerCase());
            if (moi == null) return ResponseEntity.status(404).body(Map.of("error", "Etudiant introuvable"));
            List<Favori> favoris = favoriRepository.findByEtudiantId(moi.getId());

            List<Map<String, Object>> result = new ArrayList<>();
            for (Favori f : favoris) {
                Etudiant coloc = f.getColoc();
                if (coloc == null) continue;

                int score = 0;
                if (equalsIgnore(moi.getGenre(), coloc.getGenre())) score += 10;
                if (equalsIgnore(moi.getQuartier(), coloc.getQuartier())) score += 20;
                double diffBudget = Math.abs(coloc.getBudgetMax() - moi.getBudgetMax());
                if (diffBudget <= 50) score += 15;
                else if (diffBudget <= 100) score += 8;
                if (equalsIgnore(moi.getTypeLogement(), coloc.getTypeLogement())) score += 10;
                if (moi.getAge() != null && coloc.getAge() != null) {
                    int diffAge = Math.abs(coloc.getAge() - moi.getAge());
                    if (diffAge <= 2) score += 10;
                    else if (diffAge <= 4) score += 5;
                }
                if (equalsIgnore(moi.getModeDeVie(), coloc.getModeDeVie())) score += 10;
                if (equalsIgnore(moi.getUniversite(), coloc.getUniversite())) score += 5;
                score += calculEquipements(moi.getEquipements(), coloc.getEquipements(), 5);
                if (equalsIgnore(moi.getProprete(), coloc.getProprete())) score += 5;
                if (equalsIgnore(moi.getAnimauxAcceptes(), coloc.getAnimauxAcceptes())) score += 5;
                if (equalsIgnore(moi.getNbColocataires(), coloc.getNbColocataires())) score += 5;
                if (score > 100) score = 100;

                Map<String, Object> colocMap = new HashMap<>();
                colocMap.put("id", coloc.getId());
                colocMap.put("nomComplet", coloc.getNomComplet());
                colocMap.put("email", coloc.getEmail());
                colocMap.put("universite", coloc.getUniversite());
                colocMap.put("age", coloc.getAge());
                colocMap.put("budgetMax", coloc.getBudgetMax());
                colocMap.put("quartier", coloc.getQuartier());
                colocMap.put("modeDeVie", coloc.getModeDeVie());
                colocMap.put("nbColocataires", coloc.getNbColocataires());
                colocMap.put("animauxAcceptes", coloc.getAnimauxAcceptes());
                colocMap.put("proprete", coloc.getProprete());
                colocMap.put("photoUrl", coloc.getPhotoUrl());

                Map<String, Object> item = new HashMap<>();
                item.put("id", f.getId());
                item.put("etudiant", colocMap);
                item.put("pourcentageCompatible", score);
                result.add(item);
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> ajouterFavori(@RequestParam String email, @RequestParam Long colocId) {
        try {
            Etudiant moi = etudiantRepository.findByEmail(email.trim().toLowerCase());
            Etudiant coloc = etudiantRepository.findById(colocId).orElse(null);
            if (moi == null || coloc == null) return ResponseEntity.status(404).body(Map.of("error", "Etudiant ou coloc introuvable"));
            if (favoriRepository.existsByEtudiantIdAndColocId(moi.getId(), colocId)) {
                return ResponseEntity.ok(Map.of("message", "Déjà en favori"));
            }
            favoriRepository.save(new Favori(moi, coloc));
            return ResponseEntity.ok(Map.of("message", "Ajouté aux favoris"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping
    public ResponseEntity<?> retirerFavori(@RequestParam String email, @RequestParam Long colocId) {
        try {
            Etudiant moi = etudiantRepository.findByEmail(email.trim().toLowerCase());
            if (moi == null) return ResponseEntity.status(404).body(Map.of("error", "Etudiant introuvable"));
            favoriRepository.deleteByEtudiantIdAndColocId(moi.getId(), colocId);
            return ResponseEntity.ok(Map.of("message", "Retiré des favoris"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/check")
    public ResponseEntity<?> checkFavori(@RequestParam String email, @RequestParam Long colocId) {
        try {
            Etudiant moi = etudiantRepository.findByEmail(email.trim().toLowerCase());
            if (moi == null) return ResponseEntity.ok(Map.of("isFavori", false));
            boolean exists = favoriRepository.existsByEtudiantIdAndColocId(moi.getId(), colocId);
            return ResponseEntity.ok(Map.of("isFavori", exists));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("isFavori", false));
        }
    }

    private boolean equalsIgnore(String a, String b) {
        if (a == null || b == null) return false;
        a = a.trim();
        b = b.trim();
        if (a.isEmpty() || b.isEmpty()) return false;
        return a.equalsIgnoreCase(b);
    }

    private int calculEquipements(String equipementsMoi, String equipementsAutre, int maxPoints) {
        if (equipementsMoi == null || equipementsAutre == null) return 0;
        equipementsMoi = equipementsMoi.trim();
        equipementsAutre = equipementsAutre.trim();
        if (equipementsMoi.isEmpty() || equipementsAutre.isEmpty()) return 0;
        String[] mesEquipements = equipementsMoi.split(",");
        String[] sesEquipements = equipementsAutre.split(",");
        int enCommun = 0;
        for (String monEq : mesEquipements) {
            monEq = monEq.trim();
            if (monEq.isEmpty()) continue;
            for (String sonEq : sesEquipements) {
                if (monEq.equalsIgnoreCase(sonEq.trim())) {
                    enCommun++;
                    break;
                }
            }
        }
        if (enCommun == 0) return 0;
        double ratio = (double) enCommun / mesEquipements.length;
        return (int) Math.round(ratio * maxPoints);
    }
}