package com.elearning.elearning_api.controller;

import com.elearning.elearning_api.dto.request.AvisCoursRequest;
import com.elearning.elearning_api.dto.response.AvisCoursResponse;
import com.elearning.elearning_api.service.AvisCoursService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/avis-cours")
@RequiredArgsConstructor
public class AvisCoursController {

    private final AvisCoursService avisCoursService;

    @PostMapping
    @PreAuthorize("hasRole('ETUDIANT')")
    public ResponseEntity<AvisCoursResponse> save(@Valid @RequestBody AvisCoursRequest request) {
        return ResponseEntity.ok(avisCoursService.save(request));
    }

    @GetMapping("/cours/{coursId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AvisCoursResponse>> getByCours(@PathVariable Long coursId) {
        return ResponseEntity.ok(avisCoursService.getByCours(coursId));
    }

    @GetMapping("/public/cours/{coursId}")
    public ResponseEntity<List<AvisCoursResponse>> getPublicByCours(@PathVariable Long coursId) {
        return ResponseEntity.ok(avisCoursService.getPublicByCours(coursId));
    }

    @GetMapping("/etudiant/{etudiantId}")
    @PreAuthorize("hasRole('ETUDIANT') or hasRole('ADMIN')")
    public ResponseEntity<List<AvisCoursResponse>> getByEtudiant(@PathVariable Long etudiantId) {
        return ResponseEntity.ok(avisCoursService.getByEtudiant(etudiantId));
    }
}
