package com.mgr.campusbridge.controller;

import com.mgr.campusbridge.dto.request.ForumPostRequest;
import com.mgr.campusbridge.service.ForumService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/forum")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ForumController {

    private final ForumService forumService;

    @GetMapping("/public")
    public ResponseEntity<?> getPublicPosts() {
        return ResponseEntity.ok(forumService.getPublicPosts());
    }

    @GetMapping("/groups")
    public ResponseEntity<?> getGroups() {
        return ResponseEntity.ok(forumService.getPrivateGroups());
    }

    @PostMapping("/posts")
    public ResponseEntity<?> createPost(@AuthenticationPrincipal UserDetails userDetails,
                                        @RequestBody ForumPostRequest request) {
        return ResponseEntity.ok(forumService.createPost(userDetails.getUsername(), request));
    }

    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<?> addComment(@AuthenticationPrincipal UserDetails userDetails,
                                        @PathVariable Long postId,
                                        @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(forumService.addComment(
                userDetails.getUsername(), postId, body.get("content")));
    }

    @GetMapping("/posts/{postId}")
    public ResponseEntity<?> viewPost(@PathVariable Long postId) {
        return ResponseEntity.ok(forumService.viewPost(postId));
    }
}