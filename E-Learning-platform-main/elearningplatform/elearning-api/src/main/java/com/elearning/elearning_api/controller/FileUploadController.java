package com.elearning.elearning_api.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/upload")
public class FileUploadController {

    @Value("${upload.dir:uploads}")
    private String uploadDir;

    @PostMapping("/pdf")
    @PreAuthorize("hasRole('FORMATEUR')")
    public ResponseEntity<Map<String, String>> uploadPdf(
            @RequestParam("file") MultipartFile file) throws IOException {
        return uploadWithRules(file, List.of("application/pdf"), 10 * 1024 * 1024, "Seuls les fichiers PDF sont acceptes");
    }

    @PostMapping("/image")
    @PreAuthorize("hasRole('FORMATEUR')")
    public ResponseEntity<Map<String, String>> uploadImage(
            @RequestParam("file") MultipartFile file) throws IOException {

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Seules les images sont acceptees"));
        }

        return saveUploadedFile(file, 8 * 1024 * 1024);
    }

    @PostMapping("/attachment")
    @PreAuthorize("hasRole('FORMATEUR')")
    public ResponseEntity<Map<String, String>> uploadAttachment(
            @RequestParam("file") MultipartFile file) throws IOException {

        List<String> allowedTypes = List.of(
                "application/pdf",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "application/vnd.ms-powerpoint",
                "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                "application/vnd.ms-excel",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "application/zip",
                "application/x-zip-compressed",
                "text/plain"
        );

        return uploadWithRules(file, allowedTypes, 15 * 1024 * 1024, "Type de piece jointe non autorise");
    }

    private ResponseEntity<Map<String, String>> uploadWithRules(
            MultipartFile file,
            List<String> allowedTypes,
            long maxBytes,
            String invalidTypeMessage) throws IOException {

        String contentType = file.getContentType();
        if (contentType == null || !allowedTypes.contains(contentType)) {
            return ResponseEntity.badRequest().body(Map.of("error", invalidTypeMessage));
        }

        return saveUploadedFile(file, maxBytes);
    }

    private ResponseEntity<Map<String, String>> saveUploadedFile(MultipartFile file, long maxBytes) throws IOException {
        if (file.getSize() > maxBytes) {
            long maxMegabytes = maxBytes / (1024 * 1024);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Fichier trop volumineux (max " + maxMegabytes + " MB)"));
        }

        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String originalName = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
        String fileName = UUID.randomUUID() + "_" + originalName.replaceAll("[^a-zA-Z0-9._-]", "_");

        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        String fileUrl = "/uploads/" + fileName;
        return ResponseEntity.ok(Map.of(
                "url", fileUrl,
                "fileName", originalName,
                "size", String.valueOf(file.getSize())
        ));
    }
}
