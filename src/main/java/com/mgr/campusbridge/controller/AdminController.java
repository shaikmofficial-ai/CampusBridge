package com.mgr.campusbridge.controller;

import com.mgr.campusbridge.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        return ResponseEntity.ok(adminService.getStats());
    }

    @GetMapping("/pending")
    public ResponseEntity<?> getPending() {
        return ResponseEntity.ok(adminService.getPendingUsers());
    }

    /** Full member directory for the admin data grid. */
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    /**
     * Search members by name, email, or register number (case-insensitive,
     * partial match). Frontend sends ?query=...
     */
    @GetMapping("/users/search")
    public ResponseEntity<?> searchUsers(@RequestParam(value = "query", required = false) String query) {
        return ResponseEntity.ok(adminService.searchUsers(query));
    }

    /** Ban a user (account_state -> BANNED). */
    @PostMapping("/users/{id}/ban")
    public ResponseEntity<?> banUser(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.setBanned(id, true));
    }

    /** Lift a ban (account_state -> ACTIVE). */
    @PostMapping("/users/{id}/unban")
    public ResponseEntity<?> unbanUser(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.setBanned(id, false));
    }

    @PostMapping("/approve/{id}")
    public ResponseEntity<?> approve(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.approveUser(id));
    }

    @PostMapping("/reject/{id}")
    public ResponseEntity<?> reject(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.rejectUser(id));
    }

    @GetMapping("/reports")
    public ResponseEntity<?> getReports() {
        return ResponseEntity.ok(adminService.getOpenReports());
    }

    @PostMapping("/reports/{id}/resolve")
    public ResponseEntity<?> resolveReport(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.resolveReport(id));
    }

    /** Permanently delete a forum post (moderation). */
    @DeleteMapping("/forum/posts/{id}")
    public ResponseEntity<?> deleteForumPost(@PathVariable Long id) {
        adminService.deleteForumPost(id);
        return ResponseEntity.noContent().build();
    }

    /** Permanently delete a resource (moderation). */
    @DeleteMapping("/resources/{id}")
    public ResponseEntity<?> deleteResource(@PathVariable Long id) {
        adminService.deleteResource(id);
        return ResponseEntity.noContent().build();
    }
}