package com.mgr.campusbridge.service;

import com.mgr.campusbridge.dto.request.ProfileUpdateRequest;
import com.mgr.campusbridge.dto.response.ProfileResponse;
import com.mgr.campusbridge.entity.User;
import com.mgr.campusbridge.exception.ResourceNotFoundException;
import com.mgr.campusbridge.exception.UnauthorizedException;
import com.mgr.campusbridge.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private static final Logger log = LoggerFactory.getLogger(ProfileService.class);
    private static final String AVATAR_DIR = "uploads/avatars/";
    private static final Set<String> IMAGE_TYPES =
            Set.of("image/png", "image/jpeg", "image/gif", "image/webp");

    private final UserRepository userRepository;
    private final FileValidationService fileValidationService;
    private final VirusScanService virusScanService;
    private final SkillAnalyticsService skillAnalyticsService;

    public ProfileResponse getProfile(String email) {
        User user = findByEmail(email);
        return ProfileResponse.from(user);
    }

    public ProfileResponse getProfileById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return ProfileResponse.from(user);
    }

    /** Public portfolio view — hides private/administrative fields. */
    public com.mgr.campusbridge.dto.response.PublicProfileResponse getPublicProfile(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return com.mgr.campusbridge.dto.response.PublicProfileResponse.from(user);
    }

    @Transactional
    public ProfileResponse updateProfile(String email, ProfileUpdateRequest request) {
        User user = findByEmail(email);
        if (request.getBio() != null) user.setBio(request.getBio());
        if (request.getLinkedinUrl() != null) user.setLinkedinUrl(request.getLinkedinUrl());
        if (request.getGithubUrl() != null) user.setGithubUrl(request.getGithubUrl());
        if (request.getPortfolioUrl() != null) user.setPortfolioUrl(request.getPortfolioUrl());
        if (request.getSkills() != null) user.setSkills(request.getSkills());
        if (request.getAchievements() != null) user.setAchievements(request.getAchievements());
        User saved = userRepository.save(user);
        // Trigger: profile/skill edit -> record fresh PRI snapshot.
        skillAnalyticsService.recordSnapshot(saved);
        return ProfileResponse.from(saved);
    }

    @Transactional
    public ProfileResponse updateProfilePicture(String email, String pictureUrl) {
        User user = findByEmail(email);
        user.setProfilePictureUrl(pictureUrl);
        return ProfileResponse.from(userRepository.save(user));
    }

    /**
     * Validate (Tika), scan (VirusTotal), store an uploaded avatar image, and
     * save its public URL on the user. Replaces the old random-avatar approach.
     */
    @Transactional
    public ProfileResponse uploadProfilePicture(String email, MultipartFile file) {
        User user = findByEmail(email);

        // 1) Content-type validation — must be a real image (blocks fake extensions).
        String detected = fileValidationService.validate(file);
        if (!IMAGE_TYPES.contains(detected)) {
            throw new UnauthorizedException("Profile picture must be an image (PNG, JPG, GIF or WEBP).");
        }

        // 2) Malware scan (no-op unless VirusTotal is configured).
        try {
            VirusScanService.ScanResult scan = virusScanService.scanAndWait(
                    file.getBytes(), file.getOriginalFilename());
            if (scan.status() == VirusScanService.Status.MALICIOUS) {
                throw new UnauthorizedException(
                        "This image was flagged as malicious and cannot be uploaded.");
            }
        } catch (UnauthorizedException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Avatar virus scan skipped: {}", e.getMessage());
        }

        // 3) Store the file.
        try {
            String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
            String fileName = "user_" + user.getId() + "_" + System.currentTimeMillis()
                    + (ext != null ? "." + ext.toLowerCase() : "");
            Path dir = Paths.get(AVATAR_DIR);
            Files.createDirectories(dir);
            Files.copy(file.getInputStream(), dir.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
            // Public URL served by the static resource handler (see WebConfig).
            user.setProfilePictureUrl("/uploads/avatars/" + fileName);
        } catch (IOException e) {
            log.error("Failed to store avatar: {}", e.getMessage());
            throw new UnauthorizedException("Could not save the uploaded image.");
        }

        return ProfileResponse.from(userRepository.save(user));
    }

    private User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }
}