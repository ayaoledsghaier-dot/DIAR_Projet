
package com.example.demo.service;

import com.example.demo.dto.ChatMessageRequest;
import com.example.demo.entity.Conversation;
import com.example.demo.entity.Message;
import com.example.demo.repository.ConversationRepository;
import com.example.demo.repository.MessageRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;

@Service
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    public ChatService(
            ConversationRepository conversationRepository,
            MessageRepository messageRepository) {

        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
    }

    @Transactional
    public Message envoyerMessage(ChatMessageRequest request) {

        // Vérifier les données
        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Requête vide"
            );
        }

        if (request.getEtudiantEmail() == null ||
                request.getEtudiantEmail().trim().isEmpty()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Email étudiant manquant"
            );
        }

        if (request.getProprietaireEmail() == null ||
                request.getProprietaireEmail().trim().isEmpty()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Email propriétaire manquant"
            );
        }

        if (request.getSenderEmail() == null ||
                request.getSenderEmail().trim().isEmpty()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Email expéditeur manquant"
            );
        }

        if (request.getSenderRole() == null ||
                request.getSenderRole().trim().isEmpty()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Rôle expéditeur manquant"
            );
        }

        if (request.getContenu() == null ||
                request.getContenu().trim().isEmpty()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Message vide"
            );
        }

        String etudiantEmail =
                request.getEtudiantEmail().trim();

        String proprietaireEmail =
                request.getProprietaireEmail().trim();

        String sender =
                request.getSenderEmail().trim();

        String role =
                request.getSenderRole().trim().toUpperCase();

        // Vérifier le rôle
        if (!role.equals("ETUDIANT") &&
                !role.equals("PROPRIETAIRE")) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Role invalide"
            );
        }

        // Vérifier l'expéditeur
        boolean expediteurValide =
                (role.equals("ETUDIANT") &&
                        sender.equalsIgnoreCase(etudiantEmail))
                ||
                (role.equals("PROPRIETAIRE") &&
                        sender.equalsIgnoreCase(proprietaireEmail));

        if (!expediteurValide) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Expediteur invalide"
            );
        }

        // Vérifier l'annonce
        if (request.getAnnonceId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "ID annonce manquant"
            );
        }

        // Chercher une conversation existante
        Conversation conversation =
                conversationRepository
                        .findFirstByEtudiantEmailIgnoreCaseAndProprietaireEmailIgnoreCaseAndAnnonceId(
                                etudiantEmail,
                                proprietaireEmail,
                                request.getAnnonceId()
                        )
                        .orElseGet(() -> {

                            Conversation nouvelle =
                                    new Conversation();

                            nouvelle.setEtudiantEmail(
                                    etudiantEmail
                            );

                            nouvelle.setEtudiantNom(
                                    request.getEtudiantNom()
                            );

                            nouvelle.setProprietaireEmail(
                                    proprietaireEmail
                            );

                            nouvelle.setProprietaireNom(
                                    request.getProprietaireNom()
                            );

                            nouvelle.setAnnonceId(
                                    request.getAnnonceId()
                            );

                            return conversationRepository.save(
                                    nouvelle
                            );
                        });

        // Créer le message
        Message message = new Message();

        message.setConversationId(conversation.getId());
        message.setSenderEmail(sender);
        message.setSenderRole(role);
        message.setContenu(request.getContenu().trim());

        return messageRepository.save(message);
    }

    // Récupérer les conversations
    public List<Conversation> getConversations(String email) {

        if (email == null || email.trim().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Email obligatoire"
            );
        }

        return conversationRepository
                .findByEtudiantEmailIgnoreCaseOrProprietaireEmailIgnoreCaseOrderByDateCreationDesc(
                        email.trim(),
                        email.trim()
                );
    }

    // Récupérer les messages
    public List<Message> getMessages(Long conversationId) {

        if (conversationId == null ||
                !conversationRepository.existsById(conversationId)) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Conversation introuvable"
            );
        }

        return messageRepository
                .findByConversationIdOrderByDateEnvoiAsc(
                        conversationId
                );
    }
}
