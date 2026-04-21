package com.elearning.elearning_api.controller;

import com.elearning.elearning_api.dto.request.AbonnementRequest;
import com.elearning.elearning_api.dto.response.AbonnementResponse;
import com.elearning.elearning_api.enums.StatutAbonnement;
import com.elearning.elearning_api.service.AbonnementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/abonnements")
@RequiredArgsConstructor
public class AbonnementController {

    private final AbonnementService abonnementService;

    @PostMapping
    @PreAuthorize("hasRole('ETUDIANT')")
    public ResponseEntity<AbonnementResponse> create(@Valid @RequestBody AbonnementRequest request) {
        return ResponseEntity.ok(abonnementService.create(request));
    }

    @PatchMapping("/{id}/statut")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AbonnementResponse> updateStatut(@PathVariable Long id,
                                                           @RequestParam StatutAbonnement statut) {
        return ResponseEntity.ok(abonnementService.updateStatut(id, statut));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        abonnementService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ETUDIANT') or hasRole('ADMIN')")
    public ResponseEntity<AbonnementResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(abonnementService.getById(id));
    }

    @GetMapping("/etudiant/{etudiantId}")
    @PreAuthorize("hasRole('ETUDIANT') or hasRole('ADMIN')")
    public ResponseEntity<List<AbonnementResponse>> getByEtudiant(@PathVariable Long etudiantId) {
        return ResponseEntity.ok(abonnementService.getByEtudiant(etudiantId));
    }
}