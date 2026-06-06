package com.mgr.campusbridge.controller;

import com.mgr.campusbridge.dto.request.MentorJobRequest;
import com.mgr.campusbridge.service.MentorJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mentor-jobs")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MentorJobController {

    private final MentorJobService mentorJobService;

    /** Anyone logged in can view mentor-posted openings. */
    @GetMapping
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(mentorJobService.getAll());
    }

    /** Only mentors/admins can post. */
    @PostMapping
    @PreAuthorize("hasAnyRole('MENTOR','ADMIN')")
    public ResponseEntity<?> create(@AuthenticationPrincipal UserDetails userDetails,
                                    @RequestBody MentorJobRequest request) {
        return ResponseEntity.ok(mentorJobService.create(userDetails.getUsername(), request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@AuthenticationPrincipal UserDetails userDetails,
                                    @PathVariable Long id) {
        mentorJobService.delete(userDetails.getUsername(), id);
        return ResponseEntity.noContent().build();
    }
}
