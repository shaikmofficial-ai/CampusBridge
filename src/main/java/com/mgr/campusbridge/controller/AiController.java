package com.mgr.campusbridge.controller;

import com.mgr.campusbridge.dto.request.AiActionRequest;
import com.mgr.campusbridge.service.AiAssistantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Unified AI assistant endpoint backing the dashboard quick-actions and chat.
 */
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AiController {

    private final AiAssistantService aiAssistantService;

    @PostMapping("/action")
    public ResponseEntity<?> action(@AuthenticationPrincipal UserDetails userDetails,
                                    @RequestBody AiActionRequest request) {
        String email = userDetails != null ? userDetails.getUsername() : "anonymous";
        String markdown = aiAssistantService.runAction(email, request.getActionType(), request.getMessage());
        return ResponseEntity.ok(Map.of("result", markdown, "actionType",
                request.getActionType() == null ? "" : request.getActionType()));
    }
}
