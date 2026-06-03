package com.mgr.campusbridge.dto.response;

import com.mgr.campusbridge.entity.Resource;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ResourceResponse {
    private Long id;
    private String title;
    private String description;
    private String type;
    private String department;
    private String fileSize;
    private int downloadCount;
    private String uploaderName;
    private boolean saved;
    private LocalDateTime uploadedAt;

    public static ResourceResponse from(Resource r, boolean saved) {
        return ResourceResponse.builder()
                .id(r.getId())
                .title(r.getTitle())
                .description(r.getDescription())
                .type(r.getType().name())
                .department(r.getDepartment())
                .fileSize(r.getFileSize())
                .downloadCount(r.getDownloadCount())
                .uploaderName(r.getUploader() != null ? r.getUploader().getName() : "Unknown")
                .saved(saved)
                .uploadedAt(r.getUploadedAt())
                .build();
    }
}