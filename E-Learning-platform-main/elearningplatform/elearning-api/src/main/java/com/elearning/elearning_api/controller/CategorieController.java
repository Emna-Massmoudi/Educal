package com.elearning.elearning_api.controller;

import com.elearning.elearning_api.dto.request.CategorieRequest;
import com.elearning.elearning_api.dto.response.CategorieResponse;
import com.elearning.elearning_api.service.CategorieService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategorieController {

    private final CategorieService categorieService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategorieResponse> create(@Valid @RequestBody CategorieRequest request) {
        return ResponseEntity.ok(categorieService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategorieResponse> update(@PathVariable Long id,
                                                    @Valid @RequestBody CategorieRequest request) {
        return ResponseEntity.ok(categorieService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        categorieService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CategorieResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(categorieService.getById(id));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CategorieResponse>> getAll() {
        return ResponseEntity.ok(categorieService.getAll());
    }

    @GetMapping("/public")
    public ResponseEntity<List<CategorieResponse>> getPublicCategories() {
        return ResponseEntity.ok(categorieService.getPublicCategories());
    }
}
