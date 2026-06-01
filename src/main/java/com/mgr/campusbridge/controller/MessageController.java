package com.mgr.campusbridge.controller;

import com.mgr.campusbridge.dto.request.MessageRequest;
import com.mgr.campusbridge.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.*;
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
        return ResponseEntity.ok(messageService.getUserConversations(userDetails.getUsername()));
    }

    @GetMapping("/conversations/{id}")
    public ResponseEntity<?> getMessages(@PathVariable Long id) {
        return ResponseEntity.ok(messageService.getMessages(id));
    }

    @PostMapping("/send")
    public ResponseEntity<?> send(@AuthenticationPrincipal UserDetails userDetails,
                                  @RequestBody MessageRequest request) {
        return ResponseEntity.ok(messageService.sendMessage(
                userDetails.getUsername(),
                request.getConversationId(),
                request.getContent()));
    }

    @PostMapping("/start/{userId}")
    public ResponseEntity<?> startConversation(@AuthenticationPrincipal UserDetails userDetails,
                                               @PathVariable Long userId) {
        return ResponseEntity.ok(messageService.startConversation(
                userDetails.getUsername(), userId));
    }
}