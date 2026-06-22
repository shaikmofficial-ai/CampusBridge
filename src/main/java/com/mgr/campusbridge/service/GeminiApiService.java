package com.mgr.campusbridge.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * Live integration with the Google Gemini generative API.
 *
 * Config (env vars — never hard-code the key):
 *   GEMINI_API_KEY   — your Google AI Studio key
 *   GEMINI_BASE_URL  — generateContent endpoint (defaults to gemini-1.5-flash)
 *
 * If the key is missing or the network call fails, {@link #generate} returns
 * null so callers can serve a safe pre-written fallback.
 */
@Service
public class GeminiApiService {

    private static final Logger log = LoggerFactory.getLogger(GeminiApiService.class);

    @Value("${gemini.api.key:}")
    private String apiKey;

    @Value("${gemini.base.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent}")
    private String baseUrl;

    private final RestClient restClient = RestClient.create();

    public boolean isConfigured() {
        return StringUtils.hasText(apiKey);
    }

    /**
     * Send a prompt to Gemini and return the extracted text, or null on
     * failure / when unconfigured (so the caller can fall back gracefully).
     */
    @SuppressWarnings("unchecked")
    public String generate(String prompt) {
        if (!isConfigured()) {
            log.warn("Gemini API key not configured; returning null for graceful fallback.");
            return null;
        }
        try {
            // Google payload shape: {"contents":[{"parts":[{"text": prompt}]}]}
            Map<String, Object> body = Map.of(
                    "contents", List.of(Map.of(
                            "parts", List.of(Map.of("text", prompt))
                    ))
            );

            Map<String, Object> res = restClient.post()
                    .uri(baseUrl + "?key=" + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    // Surface Google's actual error body (invalid key, model not
                    // found, quota, safety block...) so it shows in our console
                    // instead of being hidden behind a generic fallback.
                    .onStatus(status -> status.isError(), (request, response) -> {
                        String errorBody = new String(response.getBody().readAllBytes());
                        log.error("Gemini HTTP {} error. Response body: {}",
                                response.getStatusCode(), errorBody);
                    })
                    .body(Map.class);

            if (res == null) {
                log.error("Gemini returned an empty response body.");
                return null;
            }

            // Log a possible safety/promptFeedback block (no candidates returned).
            if (res.get("candidates") == null && res.get("promptFeedback") != null) {
                log.error("Gemini returned no candidates. promptFeedback={}", res.get("promptFeedback"));
            }

            String text = extractText(res);
            if (text == null) {
                log.error("Gemini parsing yielded no text. Raw response: {}", res);
            }
            return text;
        } catch (Exception e) {
            // Full stack trace so the real cause flows to the console window.
            log.error("Gemini call failed with an exception", e);
            return null;
        }
    }

    /** Safely walk candidates[0].content.parts[0].text. */
    @SuppressWarnings("unchecked")
    private String extractText(Map<String, Object> res) {
        if (res == null) return null;
        try {
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) res.get("candidates");
            if (candidates == null || candidates.isEmpty()) return null;
            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
            if (content == null) return null;
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
            if (parts == null || parts.isEmpty()) return null;
            Object text = parts.get(0).get("text");
            return text != null ? text.toString() : null;
        } catch (Exception e) {
            // Log the full structure + stack so parsing issues are debuggable.
            log.error("Failed to parse Gemini response. Raw: {}", res, e);
            return null;
        }
    }
}
