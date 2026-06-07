package com.mgr.campusbridge.controller;

import com.mgr.campusbridge.service.EducationalResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Trending technical tutorials feed, backed by the free Dev.to API.
 * Exposed under /api/resources/trending so it sits alongside the resource hub.
 */
@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class EducationalResourceController {

    private final EducationalResourceService educationalResourceService;

    /** GET /api/resources/trending?tag=react&perPage=10 */
    @GetMapping("/trending")
    public ResponseEntity<?> getTrending(
            @RequestParam(required = false) String tag,
            @RequestParam(required = false, defaultValue = "10") int perPage) {
        return ResponseEntity.ok(educationalResourceService.getTrending(tag, perPage));
    }
}
