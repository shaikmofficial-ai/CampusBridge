package com.mgr.campusbridge.controller;

import com.mgr.campusbridge.dto.request.AiActionRequest;
import com.mgr.campusbridge.service.AiAssistantService;
import com.mgr.campusbridge.service.GeminiApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
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
    private final GeminiApiService geminiApiService;

    @PostMapping("/action")
    public ResponseEntity<?> action(@AuthenticationPrincipal UserDetails userDetails,
                                    @RequestBody AiActionRequest request) {
        String email = userDetails != null ? userDetails.getUsername() : "anonymous";
        String markdown = aiAssistantService.runAction(email, request.getActionType(), request.getMessage());
        return ResponseEntity.ok(Map.of("result", markdown, "actionType",
                request.getActionType() == null ? "" : request.getActionType()));
    }

    /**
     * Diagnostic: confirms key presence, the configured model, and whether a
     * live one-line call succeeds. Never returns the key itself.
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        Map<String, Object> out = new HashMap<>();
        out.put("keyConfigured", geminiApiService.isConfigured());
        out.put("model", geminiApiService.getBaseUrl());
        String ping = geminiApiService.generate("Reply with the single word: OK");
        out.put("liveCallOk", ping != null);
        out.put("sample", ping);
        return ResponseEntity.ok(out);
    }
}
