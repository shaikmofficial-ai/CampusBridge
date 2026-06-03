
package com.mgr.campusbridge.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;

@Data
public class ProfileUpdateRequest {

    @Size(max = 500, message = "Bio must be under 500 characters")
    private String bio;

    private String linkedinUrl;
    private String githubUrl;
    private String portfolioUrl;

    @Size(max = 20, message = "Max 20 skills allowed")
    private List<String> skills;

    @Size(max = 10, message = "Max 10 achievements allowed")
    private List<String> achievements;
}