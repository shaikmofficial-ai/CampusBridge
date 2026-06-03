package com.mgr.campusbridge.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class PlacementDriveRequest {

    @NotBlank(message = "Company name is required")
    private String companyName;

    @NotBlank(message = "Role is required")
    private String role;

    private String packageAmount;
    private String location;
    private String eligibilityCriteria;

    @NotNull(message = "Application deadline is required")
    private LocalDate applicationDeadline;

    private String applicationLink;
    private String description;
}