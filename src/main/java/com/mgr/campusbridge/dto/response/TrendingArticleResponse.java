package com.mgr.campusbridge.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/** A trending technical article sourced from the free Dev.to API. */
@Data
@Builder
public class TrendingArticleResponse {
    private Long id;
    private String title;
    private String description;
    private String url;
    private String coverImage;
    private String authorName;
    private String authorAvatar;
    private Integer readingTimeMinutes;
    private Integer reactionsCount;
    private String publishedAt;
    private List<String> tags;
}
