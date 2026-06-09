package com.mgr.campusbridge.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.Base64;
import java.util.Map;

/**
 * Executes code via a Judge0 instance (https://judge0.com / RapidAPI or a
 * self-hosted CE). Configure with:
 *   judge0.base-url   (default https://judge0-ce.p.rapidapi.com)
 *   judge0.api-key    (RapidAPI key; optional for self-hosted)
 *   judge0.api-host   (RapidAPI host header)
 *
 * Uses the synchronous endpoint (?wait=true) with base64 encoding so source
 * and output survive transport cleanly.
 */
@Service
public class Judge0Service {

    private static final Logger log = LoggerFactory.getLogger(Judge0Service.class);

    @Value("${judge0.base-url:https://judge0-ce.p.rapidapi.com}")
    private String baseUrl;

    @Value("${judge0.api-key:}")
    private String apiKey;

    @Value("${judge0.api-host:judge0-ce.p.rapidapi.com}")
    private String apiHost;

    private final RestClient restClient = RestClient.create();
    private final ObjectMapper mapper = new ObjectMapper();

    @Data
    @Builder
    public static class ExecResult {
        private String stdout;
        private String stderr;
        private String compileOutput;
        private String status;
        private boolean configured;
    }

    public boolean isConfigured() {
        return StringUtils.hasText(apiKey);
    }

    private static String enc(String s) {
        return Base64.getEncoder().encodeToString((s == null ? "" : s).getBytes());
    }

    private static String dec(Object s) {
        if (s == null) return null;
        try {
            return new String(Base64.getDecoder().decode(s.toString()));
        } catch (Exception e) {
            return s.toString();
        }
    }

    /** Submit source for a language and synchronously return the result. */
    @SuppressWarnings("unchecked")
    public ExecResult execute(String sourceCode, int languageId, String stdin) {
        if (!isConfigured()) {
            return ExecResult.builder()
                    .configured(false)
                    .status("Judge0 not configured")
                    .stderr("Code execution is not configured on the server (set judge0.api-key).")
                    .build();
        }
        try {
            Map<String, Object> body = Map.of(
                    "source_code", enc(sourceCode),
                    "language_id", languageId,
                    "stdin", enc(stdin == null ? "" : stdin)
            );

            RestClient.RequestBodySpec req = restClient.post()
                    .uri(baseUrl + "/submissions?base64_encoded=true&wait=true")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-RapidAPI-Key", apiKey)
                    .header("X-RapidAPI-Host", apiHost);

            Map<String, Object> res = req.body(body).retrieve().body(Map.class);
            if (res == null) {
                return ExecResult.builder().configured(true).status("No response").build();
            }

            Object statusObj = res.get("status");
            String statusDesc = statusObj instanceof Map<?, ?> m
                    ? String.valueOf(((Map<String, Object>) m).get("description")) : "Unknown";

            return ExecResult.builder()
                    .configured(true)
                    .stdout(dec(res.get("stdout")))
                    .stderr(dec(res.get("stderr")))
                    .compileOutput(dec(res.get("compile_output")))
                    .status(statusDesc)
                    .build();
        } catch (Exception e) {
            log.error("Judge0 execution failed: {}", e.getMessage());
            return ExecResult.builder()
                    .configured(true)
                    .status("Execution error")
                    .stderr("Could not reach the code execution service: " + e.getMessage())
                    .build();
        }
    }
}
