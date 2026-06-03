package com.mgr.campusbridge.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PlacementStoryRequest {

    @NotBlank(message = "Company name is required")
    private String companyName;

    @NotBlank(message = "Role is required")
    private String role;

    private String packageAmount;

    @NotBlank(message = "Story is required")
    private String story;
}