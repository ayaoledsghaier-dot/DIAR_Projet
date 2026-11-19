
package com.diar.backend_diar.repository;

import com.diar.backend_diar.entity.Annonce;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnnonceRepository extends JpaRepository<Annonce, Long> {

    boolean existsByTitreAndEmailProprietaire(
            String titre,
            String emailProprietaire
    );
}
