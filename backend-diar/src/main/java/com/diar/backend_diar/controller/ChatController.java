
package com.example.demo.controller;

import com.example.demo.dto.ChatMessageRequest;
import com.example.demo.entity.Conversation;
import com.example.demo.entity.Message;
import com.example.demo.service.ChatService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    // Tester le Controller
    @GetMapping("/test")
    public ResponseEntity<String> testChat() {
        return ResponseEntity.ok("CHAT API WORKING");
    }

    // Envoyer un message
    @PostMapping("/send")
    public ResponseEntity<Message> envoyerMessage(
            @RequestBody ChatMessageRequest request) {

        Message message = chatService.envoyerMessage(request);

        return ResponseEntity.ok(message);
    }

    // Récupérer les conversations
    @GetMapping("/conversations")
    public ResponseEntity<List<Conversation>> getConversations(
            @RequestParam String email) {

        List<Conversation> conversations =
                chatService.getConversations(email);

        return ResponseEntity.ok(conversations);
    }

    // Récupérer les messages
    @GetMapping("/messages/{id}")
    public ResponseEntity<List<Message>> getMessages(
            @PathVariable("id") Long id) {

        List<Message> messages =
                chatService.getMessages(id);

        return ResponseEntity.ok(messages);
    }
}
