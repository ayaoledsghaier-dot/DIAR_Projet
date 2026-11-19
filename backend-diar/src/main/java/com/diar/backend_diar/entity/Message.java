
package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long conversationId;

    @Column(nullable = false)
    private String senderEmail;

    @Column(nullable = false)
    private String senderRole;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String contenu;

    private LocalDateTime dateEnvoi;

    @PrePersist
    public void prePersist() {
        if (dateEnvoi == null) {
            dateEnvoi = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long id) {
        this.conversationId = id;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public void setSenderEmail(String email) {
        this.senderEmail = email;
    }

    public String getSenderRole() {
        return senderRole;
    }

    public void setSenderRole(String role) {
        this.senderRole = role;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public LocalDateTime getDateEnvoi() {
        return dateEnvoi;
    }
}
