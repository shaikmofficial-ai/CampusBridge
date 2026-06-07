package com.mgr.campusbridge.dto.request;

import lombok.Data;
import java.util.List;

/** Used by a mentor to create/update their mentor profile details. */
@Data
public class MentorProfileRequest {
    private String designation;
    private String company;
    private String currentCompany;
    private String currentRole;
    private List<String> skills;
    private List<String> domains;
    private Boolean available;
}
