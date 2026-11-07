package com.diar.backend_diar.repository;

import com.diar.backend_diar.entity.Etudiant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EtudiantRepository extends JpaRepository<Etudiant, Long> {
    boolean existsByEmail(String email);
    Etudiant findByEmail(String email);
}