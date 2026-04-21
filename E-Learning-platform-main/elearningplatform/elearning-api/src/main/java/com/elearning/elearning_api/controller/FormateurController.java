package com.elearning.elearning_api.controller;

import com.elearning.elearning_api.dto.request.FormateurProfileUpdateRequest;
import com.elearning.elearning_api.dto.request.RefusFormateurRequest;
import com.elearning.elearning_api.dto.response.FormateurResponse;
import com.elearning.elearning_api.service.FormateurService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/formateurs")
@RequiredArgsConstructor
public class FormateurController {

    private final FormateurService formateurService;

    // ← AJOUTÉ
    @GetMapping
    public List<FormateurResponse> getAllFormateurs() {
        return formateurService.getAllFormateurs();
    }

    @GetMapping("/en-attente")
    public List<FormateurResponse> getFormateursEnAttente() {
        return formateurService.getFormateursEnAttente();
    }

    @GetMapping("/{id}")
    public FormateurResponse getFormateurById(@PathVariable Long id) {
        return formateurService.getFormateurById(id);
    }

    @PutMapping("/{id}/profil")
    public FormateurResponse updateFormateurProfile(@PathVariable Long id,
            @Valid @RequestBody FormateurProfileUpdateRequest request) {
        return formateurService.updateFormateurProfile(id, request);
    }

    @PatchMapping("/{id}/accepter")
    public FormateurResponse accepterFormateur(@PathVariable Long id) {
        return formateurService.accepterFormateur(id);
    }

    @PatchMapping("/{id}/refuser")
    public FormateurResponse refuserFormateur(@PathVariable Long id,
            @RequestBody RefusFormateurRequest request) {
        return formateurService.refuserFormateur(id, request);
    }

    @PutMapping(value = "/{id}/candidature", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public FormateurResponse completerCandidature(@PathVariable Long id,
            @RequestParam String specialite, @RequestParam String bio,
            @RequestParam String portfolio, @RequestParam String motivation,
            @RequestParam(required = false) MultipartFile cv,
            @RequestParam(required = false) MultipartFile diplome,
            @RequestParam(required = false) MultipartFile certificat,
            @RequestParam(required = false) MultipartFile attestation) {
        return formateurService.completerCandidature(id, specialite, bio, portfolio,
                motivation, cv, diplome, certificat, attestation);
    }
}
