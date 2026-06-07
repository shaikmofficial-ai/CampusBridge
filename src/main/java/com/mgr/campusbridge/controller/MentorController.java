package com.mgr.campusbridge.controller;

import com.mgr.campusbridge.dto.request.MentorPlacementRequest;
import com.mgr.campusbridge.dto.request.MentorProfileRequest;
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

    /** A single mentor's profile (incl. alumni fields + placedCount). */
    @GetMapping("/{mentorId}/profile")
    public ResponseEntity<?> getMentorProfile(@PathVariable Long mentorId) {
        return ResponseEntity.ok(mentorService.getMentorByUserId(mentorId));
    }

    /** Mentor edits their own profile (designation, company, alumni fields...). */
    @PutMapping("/profile")
    public ResponseEntity<?> updateMyMentorProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody MentorProfileRequest request) {
        return ResponseEntity.ok(mentorService.upsertMyProfile(userDetails.getUsername(), request));
    }

    @PostMapping("/{mentorId}/connect")
    public ResponseEntity<?> connect(@AuthenticationPrincipal UserDetails userDetails,
                                     @PathVariable Long mentorId) {
        return ResponseEntity.ok(mentorService.sendConnectionRequest(
                userDetails.getUsername(), mentorId));
    }

    // --- Placement tracker ("Students Placed Under Guidance") ---

    @GetMapping("/{mentorId}/placements")
    public ResponseEntity<?> getPlacements(@PathVariable Long mentorId) {
        return ResponseEntity.ok(mentorService.getPlacements(mentorId));
    }

    @PostMapping("/placements")
    public ResponseEntity<?> addPlacement(@AuthenticationPrincipal UserDetails userDetails,
                                          @RequestBody MentorPlacementRequest request) {
        return ResponseEntity.ok(mentorService.addPlacement(userDetails.getUsername(), request));
    }

    @DeleteMapping("/placements/{id}")
    public ResponseEntity<?> deletePlacement(@AuthenticationPrincipal UserDetails userDetails,
                                             @PathVariable Long id) {
        mentorService.deletePlacement(userDetails.getUsername(), id);
        return ResponseEntity.noContent().build();
    }
}
