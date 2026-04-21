package com.elearning.elearning_api.controller;

import com.elearning.elearning_api.dto.request.SousCategorieRequest;
import com.elearning.elearning_api.dto.response.SousCategorieResponse;
import com.elearning.elearning_api.service.SousCategorieService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/sous-categories")
@RequiredArgsConstructor
public class SousCategorieController {

    private final SousCategorieService sousCategorieService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SousCategorieResponse> create(@Valid @RequestBody SousCategorieRequest request) {
        return ResponseEntity.ok(sousCategorieService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SousCategorieResponse> update(@PathVariable Long id,
                                                        @Valid @RequestBody SousCategorieRequest request) {
        return ResponseEntity.ok(sousCategorieService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        sousCategorieService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SousCategorieResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(sousCategorieService.getById(id));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<SousCategorieResponse>> getAll() {
        return ResponseEntity.ok(sousCategorieService.getAll());
    }

    @GetMapping("/categorie/{categorieId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<SousCategorieResponse>> getByCategorie(@PathVariable Long categorieId) {
        return ResponseEntity.ok(sousCategorieService.getByCategorie(categorieId));
    }
}