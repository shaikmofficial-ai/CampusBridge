package com.mgr.campusbridge.controller;

import com.mgr.campusbridge.dto.request.SendMessageRequest;
import com.mgr.campusbridge.dto.response.MessageResponse;
import com.mgr.campusbridge.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload SendMessageRequest request,
                            @AuthenticationPrincipal UserDetails userDetails) {
        MessageResponse response = messageService.sendMessage(
                userDetails.getUsername(), request);
        messagingTemplate.convertAndSend(
                "/topic/conversation." + request.getConversationId(), response);
    }
}