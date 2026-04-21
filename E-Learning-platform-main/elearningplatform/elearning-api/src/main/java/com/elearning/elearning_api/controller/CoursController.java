package com.elearning.elearning_api.controller;

import com.elearning.elearning_api.dto.request.CoursRequest;
import com.elearning.elearning_api.dto.response.CoursResponse;
import com.elearning.elearning_api.enums.EtatCours;
import com.elearning.elearning_api.service.CoursService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/cours")
@RequiredArgsConstructor
public class CoursController {

    private final CoursService coursService;

    @PostMapping
    @PreAuthorize("hasRole('FORMATEUR')")
    public ResponseEntity<CoursResponse> create(@Valid @RequestBody CoursRequest request) {
        return ResponseEntity.ok(coursService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('FORMATEUR')")
    public ResponseEntity<CoursResponse> update(@PathVariable Long id,
                                                @Valid @RequestBody CoursRequest request) {
        return ResponseEntity.ok(coursService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('FORMATEUR') or hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        coursService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CoursResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(coursService.getById(id));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CoursResponse>> getAll() {
        return ResponseEntity.ok(coursService.getAll());
    }

    @GetMapping("/formateur/{formateurId}")
    @PreAuthorize("hasRole('FORMATEUR') or hasRole('ADMIN')")
    public ResponseEntity<List<CoursResponse>> getByFormateur(@PathVariable Long formateurId) {
        return ResponseEntity.ok(coursService.getByFormateur(formateurId));
    }

    @GetMapping("/etat/{etat}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CoursResponse>> getByEtat(@PathVariable EtatCours etat) {
        return ResponseEntity.ok(coursService.getByEtat(etat));
    }

    @GetMapping("/public/published")
    public ResponseEntity<List<CoursResponse>> getPublishedPublic() {
        return ResponseEntity.ok(coursService.getPublishedPublic());
    }

    @GetMapping("/public/published/{id}")
    public ResponseEntity<CoursResponse> getPublishedPublicById(@PathVariable Long id) {
        return ResponseEntity.ok(coursService.getPublishedPublicById(id));
    }

    @PatchMapping("/{id}/etat")
    @PreAuthorize("hasRole('FORMATEUR') or hasRole('ADMIN')")
    public ResponseEntity<CoursResponse> updateEtat(@PathVariable Long id,
                                                    @RequestParam EtatCours etat) {
        return ResponseEntity.ok(coursService.updateEtat(id, etat));
    }
}
