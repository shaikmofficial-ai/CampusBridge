package com.mgr.campusbridge.dto.request;

import lombok.Data;

@Data
public class ForumGroupRequest {
    private String name;
    private String description;
    /** Whether the group is private. Defaults to true (a "private forum"). */
    private Boolean isPrivate;
}
