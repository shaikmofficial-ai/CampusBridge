package com.mgr.campusbridge.controller;

import com.mgr.campusbridge.dto.request.SendMessageRequest;
import com.mgr.campusbridge.dto.request.StartConversationRequest;
import com.mgr.campusbridge.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MessageController {

    private final MessageService messageService;

    @GetMapping("/conversations")
    public ResponseEntity<?> getConversations(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(messageService.getConversations(userDetails.getUsername()));
    }

    @GetMapping("/{conversationId}")
    public ResponseEntity<?> getMessages(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long conversationId) {
        return ResponseEntity.ok(messageService.getMessages(
                userDetails.getUsername(), conversationId));
    }

    @PostMapping("/conversations/start")
    public ResponseEntity<?> startConversation(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody StartConversationRequest request) {
        return ResponseEntity.ok(messageService.startConversation(
                userDetails.getUsername(), request));
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody SendMessageRequest request) {
        return ResponseEntity.ok(messageService.sendMessage(
                userDetails.getUsername(), request));
    }
}