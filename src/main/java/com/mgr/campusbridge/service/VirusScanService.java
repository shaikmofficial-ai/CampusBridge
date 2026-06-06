package com.mgr.campusbridge.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * Structure for scanning attachments for malware via the free VirusTotal API
 * (https://www.virustotal.com/api/v3/).
 *
 * The free tier is asynchronous and rate-limited, so this service is designed
 * to be called best-effort / out of the request hot path:
 *   1. {@link #submit} uploads the bytes and returns an analysis id.
 *   2. {@link #getReport} polls the verdict for that id.
 *
 * If no API key is configured the service is a safe no-op (returns SKIPPED),
 * so the app runs fine without it.
 */
@Service
public class VirusScanService {

    private static final Logger log = LoggerFactory.getLogger(VirusScanService.class);
    private static final String BASE = "https://www.virustotal.com/api/v3";

    @Value("${virustotal.api-key:}")
    private String apiKey;

    @Value("${virustotal.enabled:false}")
    private boolean enabled;

    private final RestClient restClient = RestClient.create();

    public enum Status { SKIPPED, QUEUED, CLEAN, MALICIOUS, ERROR }

    public record ScanResult(Status status, String analysisId, int malicious, String message) {
        public static ScanResult skipped() { return new ScanResult(Status.SKIPPED, null, 0, "Scanning disabled"); }
        public static ScanResult queued(String id) { return new ScanResult(Status.QUEUED, id, 0, "Scan queued"); }
        public static ScanResult error(String msg) { return new ScanResult(Status.ERROR, null, 0, msg); }
    }

    private boolean isConfigured() {
        return enabled && StringUtils.hasText(apiKey);
    }

    /** Upload bytes to VirusTotal and return an analysis id (or a SKIPPED/ERROR result). */
    @SuppressWarnings("unchecked")
    public ScanResult submit(byte[] content, String filename) {
        if (!isConfigured()) return ScanResult.skipped();
        try {
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new ByteArrayResource(content) {
                @Override public String getFilename() { return filename; }
            });

            Map<String, Object> response = restClient.post()
                    .uri(BASE + "/files")
                    .header("x-apikey", apiKey)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            Object data = response != null ? response.get("data") : null;
            if (data instanceof Map<?, ?> m && m.get("id") != null) {
                return ScanResult.queued(m.get("id").toString());
            }
            return ScanResult.error("Unexpected VirusTotal response");
        } catch (Exception e) {
            log.error("VirusTotal submit failed: {}", e.getMessage());
            return ScanResult.error(e.getMessage());
        }
    }

    /** Fetch the verdict for a previously submitted analysis id. */
    @SuppressWarnings("unchecked")
    public ScanResult getReport(String analysisId) {
        if (!isConfigured()) return ScanResult.skipped();
        try {
            Map<String, Object> response = restClient.get()
                    .uri(BASE + "/analyses/" + analysisId)
                    .header("x-apikey", apiKey)
                    .retrieve()
                    .body(Map.class);

            Map<String, Object> data = (Map<String, Object>) (response != null ? response.get("data") : null);
            Map<String, Object> attributes = data != null ? (Map<String, Object>) data.get("attributes") : null;
            if (attributes == null) return ScanResult.error("No analysis attributes");

            String status = String.valueOf(attributes.get("status"));
            if (!"completed".equals(status)) {
                return ScanResult.queued(analysisId);
            }

            Map<String, Object> stats = (Map<String, Object>) attributes.get("stats");
            int malicious = stats != null && stats.get("malicious") != null
                    ? ((Number) stats.get("malicious")).intValue() : 0;

            return malicious > 0
                    ? new ScanResult(Status.MALICIOUS, analysisId, malicious, malicious + " engines flagged this file")
                    : new ScanResult(Status.CLEAN, analysisId, 0, "No threats detected");
        } catch (Exception e) {
            log.error("VirusTotal report fetch failed: {}", e.getMessage());
            return ScanResult.error(e.getMessage());
        }
    }
}
