package com.mgr.campusbridge.controller;

import com.mgr.campusbridge.service.MentorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mentors")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MentorController {

    private final MentorService mentorService;

    @GetMapping
    public ResponseEntity<?> getMentors(
            @RequestParam(required = false) String domain,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(mentorService.getAllMentors(domain, keyword));
    }

    @PostMapping("/{mentorId}/connect")
    public ResponseEntity<?> connect(@AuthenticationPrincipal UserDetails userDetails,
                                     @PathVariable Long mentorId) {
        return ResponseEntity.ok(mentorService.sendConnectionRequest(
                userDetails.getUsername(), mentorId));
    }
}