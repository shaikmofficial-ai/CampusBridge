package com.mgr.campusbridge.controller;

import com.mgr.campusbridge.service.MentorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Student discovery for mentors/alumni — skill-based matching.
 * Mentors/alumni use this to scout students by skill; never returns mentors.
 */
@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StudentController {

    private final MentorService mentorService;

    @GetMapping
    @PreAuthorize("hasAnyRole('MENTOR','ALUMNI','ADMIN')")
    public ResponseEntity<?> discoverStudents(
            @RequestParam(required = false) String skill,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(mentorService.discoverStudents(skill, keyword));
    }
}
