package com.mgr.campusbridge.service;

import com.mgr.campusbridge.dto.response.TrendingArticleResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Streams trending technical tutorials from the free, no-auth Dev.to API
 * (https://dev.to/api/articles?tag={skill}&per_page=10).
 */
@Service
@RequiredArgsConstructor
public class EducationalResourceService {

    private static final Logger log = LoggerFactory.getLogger(EducationalResourceService.class);
    private static final String DEVTO_API = "https://dev.to/api/articles";

    private final RestClient restClient = RestClient.create();

    @SuppressWarnings("unchecked")
    public List<TrendingArticleResponse> getTrending(String tag, int perPage) {
        int size = Math.min(Math.max(perPage, 1), 30);
        String normalizedTag = StringUtils.hasText(tag)
                ? tag.trim().toLowerCase().replace("#", "").replace(" ", "")
                : null;

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(DEVTO_API)
                .queryParam("per_page", size);
        if (normalizedTag != null) builder.queryParam("tag", normalizedTag);

        try {
            List<Map<String, Object>> articles = restClient.get()
                    .uri(builder.build().toUriString())
                    .retrieve()
                    .body(List.class);

            if (articles == null) return List.of();

            List<TrendingArticleResponse> out = new ArrayList<>();
            for (Map<String, Object> a : articles) {
                out.add(mapArticle(a));
            }
            return out;
        } catch (Exception e) {
            log.error("Dev.to fetch failed for tag '{}': {}", normalizedTag, e.getMessage());
            return List.of();
        }
    }

    @SuppressWarnings("unchecked")
    private TrendingArticleResponse mapArticle(Map<String, Object> a) {
        Map<String, Object> user = (Map<String, Object>) a.getOrDefault("user", Map.of());
        Object tagList = a.get("tag_list");
        List<String> tags = (tagList instanceof List) ? (List<String>) tagList : List.of();

        return TrendingArticleResponse.builder()
                .id(asLong(a.get("id")))
                .title(asString(a.get("title")))
                .description(asString(a.get("description")))
                .url(asString(a.get("url")))
                .coverImage(asString(a.get("cover_image")))
                .authorName(asString(user.get("name")))
                .authorAvatar(asString(user.get("profile_image_90")))
                .readingTimeMinutes(asInt(a.get("reading_time_minutes")))
                .reactionsCount(asInt(a.get("public_reactions_count")))
                .publishedAt(asString(a.get("published_at")))
                .tags(tags)
                .build();
    }

    private String asString(Object o) { return o != null ? o.toString() : null; }
    private Long asLong(Object o) { return o instanceof Number n ? n.longValue() : null; }
    private Integer asInt(Object o) { return o instanceof Number n ? n.intValue() : null; }
}
