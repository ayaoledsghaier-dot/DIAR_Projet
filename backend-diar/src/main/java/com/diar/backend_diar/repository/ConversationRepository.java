
package com.example.demo.repository;

import com.example.demo.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ConversationRepository
        extends JpaRepository<Conversation, Long> {

    Optional<Conversation>
    findFirstByEtudiantEmailIgnoreCaseAndProprietaireEmailIgnoreCaseAndAnnonceId(
            String etudiantEmail,
            String proprietaireEmail,
            Long annonceId
    );

    List<Conversation>
    findByEtudiantEmailIgnoreCaseOrProprietaireEmailIgnoreCaseOrderByDateCreationDesc(
            String etudiantEmail,
            String proprietaireEmail
    );
}
