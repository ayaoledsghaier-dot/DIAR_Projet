package com.diar.backend_diar.service;

import com.diar.backend_diar.entity.Etudiant;
import com.diar.backend_diar.repository.EtudiantRepository;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
public class EtudiantService {

    private final EtudiantRepository etudiantRepository;

    public EtudiantService(EtudiantRepository etudiantRepository) {
        this.etudiantRepository = etudiantRepository;
    }

    public List<Etudiant> getAllEtudiants() {
        return etudiantRepository.findAll();
    }

    public Etudiant saveEtudiant(Etudiant etudiant) {
        return etudiantRepository.save(etudiant);
    }

    public String extraireTexteDeImage(MultipartFile file) {
        Tesseract tesseract = new Tesseract();
        
        try {
            // Indiquer le chemin vers les données de langue de Tesseract (dossier 'tessdata')
            tesseract.setDatapath("C:/tessdata"); 
            tesseract.setLanguage("fra"); // Langue française

            // Convertir le MultipartFile en fichier temporaire
            File convFile = convertMultiPartToFile(file);
            
            // Extraire le texte de l'image
            String texteExtrait = tesseract.doOCR(convFile);
            
            // Nettoyer le fichier temporaire
            convFile.delete();
            
            return texteExtrait;
        } catch (TesseractException | IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    private File convertMultiPartToFile(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        File convFile = new File(originalFilename != null ? originalFilename : "temp_attestation.jpg");
        file.transferTo(convFile);
        return convFile;
    }
}