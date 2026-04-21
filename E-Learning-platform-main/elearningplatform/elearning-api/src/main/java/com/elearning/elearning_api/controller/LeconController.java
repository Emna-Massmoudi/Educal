package com.elearning.elearning_api.controller;

import com.elearning.elearning_api.dto.request.LeconRequest;
import com.elearning.elearning_api.dto.response.LeconPreviewResponse;
import com.elearning.elearning_api.dto.response.LeconResponse;
import com.elearning.elearning_api.service.LeconService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/lecons")
@RequiredArgsConstructor
public class LeconController {

    private final LeconService leconService;

    @PostMapping
    @PreAuthorize("hasRole('FORMATEUR')")
    public ResponseEntity<LeconResponse> create(@Valid @RequestBody LeconRequest request) {
        return ResponseEntity.ok(leconService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('FORMATEUR')")
    public ResponseEntity<LeconResponse> update(@PathVariable Long id,
                                                @Valid @RequestBody LeconRequest request) {
        return ResponseEntity.ok(leconService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('FORMATEUR') or hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        leconService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LeconResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(leconService.getById(id));
    }

    @GetMapping("/cours/{coursId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<LeconResponse>> getByCours(@PathVariable Long coursId) {
        return ResponseEntity.ok(leconService.getByCours(coursId));
    }

    @GetMapping("/public/cours/{coursId}")
    public ResponseEntity<List<LeconPreviewResponse>> getPublicPreviewByCours(@PathVariable Long coursId) {
        return ResponseEntity.ok(leconService.getPublicPreviewByCours(coursId));
    }
}
