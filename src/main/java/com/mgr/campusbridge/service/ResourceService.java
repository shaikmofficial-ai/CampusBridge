package com.mgr.campusbridge.service;

import com.mgr.campusbridge.entity.*;
import com.mgr.campusbridge.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.nio.file.*;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;
    private static final String UPLOAD_DIR = "uploads/resources/";

    public List<Resource> getAllResources() {
        return resourceRepository.findAll();
    }

    public List<Resource> getByType(String type) {
        return resourceRepository.findByType(Resource.ResourceType.valueOf(type.toUpperCase()));
    }

    public Resource uploadResource(String email, String title, String department,
                                   String type, MultipartFile file) throws IOException {
        User uploader = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Files.createDirectories(Paths.get(UPLOAD_DIR));
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(UPLOAD_DIR + fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        Resource resource = Resource.builder()
                .uploader(uploader)
                .title(title)
                .department(department)
                .type(Resource.ResourceType.valueOf(type.toUpperCase()))
                .filePath(filePath.toString())
                .fileSize(file.getSize() / 1024 + " KB")
                .build();
        return resourceRepository.save(resource);
    }

    public Resource incrementDownload(Long resourceId) {
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new RuntimeException("Resource not found"));
        resource.setDownloadCount(resource.getDownloadCount() + 1);
        return resourceRepository.save(resource);
    }
}