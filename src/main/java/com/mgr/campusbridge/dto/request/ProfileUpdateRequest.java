// ProfileUpdateRequest.java
package com.mgr.campusbridge.dto.request;
import lombok.Data;
import java.util.List;
@Data
public class ProfileUpdateRequest {
    private String bio;
    private String location;
    private String linkedinUrl;
    private String githubUrl;
    private List<String> skills;
    private List<String> achievements;
}