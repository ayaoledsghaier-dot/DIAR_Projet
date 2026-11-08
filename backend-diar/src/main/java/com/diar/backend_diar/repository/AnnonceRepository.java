package com.diar.backend_diar.repository; // ou com.diar.backend_diar si tu les mets tous ensemble

import com.diar.backend_diar.entity.Annonce;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnnonceRepository extends JpaRepository<Annonce, Long> {
}