package com.mgr.campusbridge.service;

import com.mgr.campusbridge.exception.UnauthorizedException;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * Validates uploaded files by inspecting their actual content (via Apache Tika)
 * rather than trusting the filename extension. This blocks "fake extension"
 * attacks (e.g. malware.exe renamed to notes.pdf).
 */
@Service
public class FileValidationService {

    private static final Logger log = LoggerFactory.getLogger(FileValidationService.class);
    private final Tika tika = new Tika();

    /** Detected MIME types we permit for forum/resource attachments. */
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "text/plain",
            "text/csv",
            "application/zip",
            "image/png",
            "image/jpeg",
            "image/gif",
            "image/webp"
    );

    /** Extension -> the MIME type(s) we expect its real content to be. */
    private static final Map<String, Set<String>> EXTENSION_MIME = Map.ofEntries(
            Map.entry("pdf", Set.of("application/pdf")),
            Map.entry("doc", Set.of("application/msword", "application/x-tika-msoffice")),
            Map.entry("docx", Set.of("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "application/x-tika-ooxml", "application/zip")),
            Map.entry("ppt", Set.of("application/vnd.ms-powerpoint", "application/x-tika-msoffice")),
            Map.entry("pptx", Set.of("application/vnd.openxmlformats-officedocument.presentationml.presentation", "application/x-tika-ooxml", "application/zip")),
            Map.entry("xls", Set.of("application/vnd.ms-excel", "application/x-tika-msoffice")),
            Map.entry("xlsx", Set.of("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "application/x-tika-ooxml", "application/zip")),
            Map.entry("txt", Set.of("text/plain")),
            Map.entry("csv", Set.of("text/csv", "text/plain")),
            Map.entry("zip", Set.of("application/zip")),
            Map.entry("png", Set.of("image/png")),
            Map.entry("jpg", Set.of("image/jpeg")),
            Map.entry("jpeg", Set.of("image/jpeg")),
            Map.entry("gif", Set.of("image/gif")),
            Map.entry("webp", Set.of("image/webp"))
    );

    /**
     * Detect the real content type and verify it is allowed and that it matches
     * the declared file extension. Throws {@link UnauthorizedException} (-> 403)
     * with a clear message on any violation.
     *
     * @return the detected MIME type (useful for storage metadata).
     */
    public String validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new UnauthorizedException("No file provided.");
        }

        final String detectedType;
        try {
            detectedType = tika.detect(file.getInputStream(), file.getOriginalFilename());
        } catch (IOException e) {
            log.error("Tika failed to read uploaded file: {}", e.getMessage());
            throw new UnauthorizedException("Could not read the uploaded file.");
        }

        if (!ALLOWED_MIME_TYPES.contains(detectedType)) {
            throw new UnauthorizedException(
                    "File type not allowed. Detected content type: " + detectedType);
        }

        String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
        if (ext != null) {
            ext = ext.toLowerCase();
            Set<String> expected = EXTENSION_MIME.get(ext);
            if (expected != null && !expected.contains(detectedType)) {
                throw new UnauthorizedException(
                        "The file's real content (" + detectedType + ") does not match its ." + ext
                                + " extension. Upload blocked.");
            }
        }

        return detectedType;
    }
}
