package com.diar.backend_diar.repository;

import com.diar.backend_diar.entity.Proprietaire;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ProprietaireRepository
        extends JpaRepository<Proprietaire, Long> {

    boolean existsByEmail(String email);

    Optional<Proprietaire> findByEmail(String email);
}