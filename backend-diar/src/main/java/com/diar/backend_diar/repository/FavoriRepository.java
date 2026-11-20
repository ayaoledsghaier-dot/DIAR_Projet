package com.diar.backend_diar.repository;

import com.diar.backend_diar.entity.Favori;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface FavoriRepository extends JpaRepository<Favori, Long> {

    List<Favori> findByEtudiantId(Long etudiantId);

    boolean existsByEtudiantIdAndColocId(Long etudiantId, Long colocId);

    @Modifying
    @Transactional
    void deleteByEtudiantIdAndColocId(Long etudiantId, Long colocId);

    // ✅ Pour compatibilité avec l'ancien FavoriController
    @Query("SELECT f FROM Favori f WHERE f.etudiant.email = :email")
    List<Favori> findByOwnerEmail(@Param("email") String email);

    @Query("SELECT COUNT(f) > 0 FROM Favori f WHERE f.etudiant.email = :email AND f.coloc.id = :favoriId")
    boolean existsByOwnerEmailAndFavoriId(@Param("email") String email, @Param("favoriId") Long favoriId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Favori f WHERE f.etudiant.email = :email AND f.coloc.id = :favoriId")
    void deleteByOwnerEmailAndFavoriId(@Param("email") String email, @Param("favoriId") Long favoriId);
}