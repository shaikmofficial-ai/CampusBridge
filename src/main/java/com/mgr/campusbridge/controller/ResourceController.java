package com.mgr.campusbridge.controller;

import com.mgr.campusbridge.service.ResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ResourceController {

    private final ResourceService resourceService;

    @GetMapping
    public ResponseEntity<?> getAll(@RequestParam(required = false) String type) {
        if (type != null) return ResponseEntity.ok(resourceService.getByType(type));
        return ResponseEntity.ok(resourceService.getAllResources());
    }

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@AuthenticationPrincipal UserDetails userDetails,
                                    @RequestParam String title,
                                    @RequestParam String department,
                                    @RequestParam String type,
                                    @RequestParam MultipartFile file) throws IOException {
        return ResponseEntity.ok(resourceService.uploadResource(
                userDetails.getUsername(), title, department, type, file));
    }

    @PostMapping("/{id}/download")
    public ResponseEntity<?> download(@PathVariable Long id) {
        return ResponseEntity.ok(resourceService.incrementDownload(id));
    }
}