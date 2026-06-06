package com.mgr.campusbridge.dto.response;

import com.mgr.campusbridge.entity.ExternalJob;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ExternalJobResponse {
    private Long id;
    private String title;
    private String company;
    private String location;
    private String category;
    private Double salaryMin;
    private Double salaryMax;
    private String contractTime;
    private String redirectUrl;
    private String description;
    private String source;
    private LocalDateTime postedAt;

    public static ExternalJobResponse from(ExternalJob job) {
        return ExternalJobResponse.builder()
                .id(job.getId())
                .title(job.getTitle())
                .company(job.getCompany())
                .location(job.getLocation())
                .category(job.getCategory())
                .salaryMin(job.getSalaryMin())
                .salaryMax(job.getSalaryMax())
                .contractTime(job.getContractTime())
                .redirectUrl(job.getRedirectUrl())
                .description(job.getDescription())
                .source(job.getSource())
                .postedAt(job.getPostedAt())
                .build();
    }
}
