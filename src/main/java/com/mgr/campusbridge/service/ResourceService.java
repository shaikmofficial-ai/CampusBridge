package com.mgr.campusbridge.service;

import com.mgr.campusbridge.dto.response.ResourceResponse;
import com.mgr.campusbridge.entity.Resource;
import com.mgr.campusbridge.entity.SavedResource;
import com.mgr.campusbridge.entity.User;
import com.mgr.campusbridge.exception.ResourceNotFoundException;
import com.mgr.campusbridge.repository.ResourceRepository;
import com.mgr.campusbridge.repository.SavedResourceRepository;
import com.mgr.campusbridge.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private final SavedResourceRepository savedResourceRepository;
    private final UserRepository userRepository;
    private final FileValidationService fileValidationService;
    private final VirusScanService virusScanService;

    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(ResourceService.class);

    private static final String UPLOAD_DIR = "uploads/resources/";

    public List<ResourceResponse> getAllResources(String userEmail) {
        User user = findByEmail(userEmail);
        return resourceRepository.findAllByOrderByUploadedAtDesc()
                .stream()
                .map(r -> ResourceResponse.from(r, savedResourceRepository.existsByUserAndResource(user, r)))
                .collect(Collectors.toList());
    }

    public List<ResourceResponse> getByType(String userEmail, String type) {
        User user = findByEmail(userEmail);
        Resource.ResourceType resourceType = Resource.ResourceType.valueOf(type.toUpperCase());
        return resourceRepository.findByTypeOrderByUploadedAtDesc(resourceType)
                .stream()
                .map(r -> ResourceResponse.from(r, savedResourceRepository.existsByUserAndResource(user, r)))
                .collect(Collectors.toList());
    }

    @Transactional
    public ResourceResponse uploadResource(String email, String title, String description,
                                           String department, String type,
                                           MultipartFile file) throws IOException {
        User uploader = findByEmail(email);

        // 1) Block fake extensions / disallowed types by inspecting real content.
        fileValidationService.validate(file);

        // 2) Malware scan (VirusTotal). When enabled, we wait for the verdict
        //    and block the upload if any engine flags the file.
        try {
            VirusScanService.ScanResult scan = virusScanService.scanAndWait(
                    file.getBytes(), file.getOriginalFilename());
            if (scan.status() == VirusScanService.Status.MALICIOUS) {
                throw new com.mgr.campusbridge.exception.UnauthorizedException(
                        "This file was flagged as malicious (" + scan.malicious()
                                + " engines) and cannot be uploaded.");
            }
            log.info("VirusTotal scan for '{}': status={} malicious={} analysisId={}",
                    file.getOriginalFilename(), scan.status(), scan.malicious(), scan.analysisId());
        } catch (com.mgr.campusbridge.exception.UnauthorizedException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Virus scan skipped due to error: {}", e.getMessage());
        }

        String fileName = System.currentTimeMillis() + "_"
                + StringUtils.cleanPath(file.getOriginalFilename());
        Path uploadPath = Paths.get(UPLOAD_DIR);
        Files.createDirectories(uploadPath);
        Files.copy(file.getInputStream(), uploadPath.resolve(fileName),
                StandardCopyOption.REPLACE_EXISTING);

        Resource resource = Resource.builder()
                .title(title)
                .description(description)
                .department(department)
                .type(Resource.ResourceType.valueOf(type.toUpperCase()))
                .filePath(UPLOAD_DIR + fileName)
                .fileSize(formatSize(file.getSize()))
                .uploader(uploader)
                .downloadCount(0)
                .build();

        return ResourceResponse.from(resourceRepository.save(resource), false);
    }

    public FileSystemResource downloadResource(Long resourceId) {
        Resource resource = findResourceById(resourceId);
        resource.setDownloadCount(resource.getDownloadCount() + 1);
        resourceRepository.save(resource);
        return new FileSystemResource(resource.getFilePath());
    }

    @Transactional
    public void saveResource(String email, Long resourceId) {
        User user = findByEmail(email);
        Resource resource = findResourceById(resourceId);
        if (!savedResourceRepository.existsByUserAndResource(user, resource)) {
            savedResourceRepository.save(SavedResource.builder()
                    .user(user).resource(resource).build());
        }
    }

    @Transactional
    public void unsaveResource(String email, Long resourceId) {
        User user = findByEmail(email);
        Resource resource = findResourceById(resourceId);
        savedResourceRepository.findByUserAndResource(user, resource)
                .ifPresent(savedResourceRepository::delete);
    }

    public List<ResourceResponse> getSavedResources(String email) {
        User user = findByEmail(email);
        return savedResourceRepository.findSavedResourcesByUser(user)
                .stream()
                .map(r -> ResourceResponse.from(r, true))
                .collect(Collectors.toList());
    }

    private String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return (bytes / 1024) + " KB";
        return (bytes / (1024 * 1024)) + " MB";
    }

    private Resource findResourceById(Long id) {
        return resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found: " + id));
    }

    private User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }
}