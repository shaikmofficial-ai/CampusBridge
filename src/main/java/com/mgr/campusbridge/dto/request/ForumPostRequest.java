// ForumPostRequest.java
package com.mgr.campusbridge.dto.request;
import lombok.Data;
@Data
public class ForumPostRequest {
    private String title;
    private String content;
    private String category;
    private boolean isPublic;
    private Long groupId;
}