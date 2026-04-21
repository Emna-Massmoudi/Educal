package com.elearning.elearning_api.controller;

import com.elearning.elearning_api.dto.request.CertificatRequest;
import com.elearning.elearning_api.dto.response.CertificatResponse;
import com.elearning.elearning_api.service.CertificatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/certificats")
@RequiredArgsConstructor
public class CertificatController {

    private final CertificatService certificatService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CertificatResponse> create(@Valid @RequestBody CertificatRequest request) {
        return ResponseEntity.ok(certificatService.create(request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        certificatService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ETUDIANT') or hasRole('ADMIN')")
    public ResponseEntity<CertificatResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(certificatService.getById(id));
    }

    @GetMapping("/{id}/download")
    @PreAuthorize("hasRole('ETUDIANT') or hasRole('ADMIN')")
    public ResponseEntity<byte[]> downloadById(@PathVariable Long id) {
        CertificatService.GeneratedPdf pdf = certificatService.downloadById(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + pdf.fileName() + "\"")
                .body(pdf.content());
    }

    @GetMapping("/code/{code}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CertificatResponse> getByCode(@PathVariable String code) {
        return ResponseEntity.ok(certificatService.getByCode(code));
    }

    @GetMapping("/etudiant/{etudiantId}")
    @PreAuthorize("hasRole('ETUDIANT') or hasRole('ADMIN')")
    public ResponseEntity<List<CertificatResponse>> getByEtudiant(@PathVariable Long etudiantId) {
        return ResponseEntity.ok(certificatService.getByEtudiant(etudiantId));
    }
}
