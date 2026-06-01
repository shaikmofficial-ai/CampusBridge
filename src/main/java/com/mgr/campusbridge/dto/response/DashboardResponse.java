// DashboardResponse.java
package com.mgr.campusbridge.dto.response;
import lombok.Data;
@Data
public class DashboardResponse {
    private String userName;
    private long mentorsConnected;
    private long resourcesSaved;
    private long forumInteractions;
    private int communityPoints;
}