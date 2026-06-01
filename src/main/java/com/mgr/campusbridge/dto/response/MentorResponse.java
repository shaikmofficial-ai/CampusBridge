// MentorResponse.java
package com.mgr.campusbridge.dto.response;
import lombok.Data;
import java.util.List;
@Data
public class MentorResponse {
    private Long id;
    private String name;
    private String designation;
    private String company;
    private double rating;
    private int reviewCount;
    private List<String> skills;
    private List<String> domains;
    private String profilePicture;
}