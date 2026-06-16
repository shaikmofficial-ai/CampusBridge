package com.mgr.campusbridge.controller;

import com.mgr.campusbridge.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Peer profile discovery ("Instagram-style"). Returns a target user's public
 * portfolio strictly by the path id — never the authenticated session user —
 * with private/admin fields stripped.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserPortfolioController {

    private final ProfileService profileService;

    @GetMapping("/portfolio/{userId}")
    public ResponseEntity<?> getPortfolio(@PathVariable Long userId) {
        return ResponseEntity.ok(profileService.getPublicProfile(userId));
    }
}
