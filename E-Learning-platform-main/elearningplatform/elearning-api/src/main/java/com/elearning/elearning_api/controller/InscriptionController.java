package com.elearning.elearning_api.controller;

import com.elearning.elearning_api.dto.request.InscriptionRequest;
import com.elearning.elearning_api.dto.response.InscriptionResponse;
import com.elearning.elearning_api.enums.StatutInscription;
import com.elearning.elearning_api.service.InscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/inscriptions")
@RequiredArgsConstructor
public class InscriptionController {

    private final InscriptionService inscriptionService;

    @PostMapping
    @PreAuthorize("hasRole('ETUDIANT')")
    public ResponseEntity<InscriptionResponse> inscrire(@Valid @RequestBody InscriptionRequest request) {
        return ResponseEntity.ok(inscriptionService.inscrire(request));
    }

    // ← MODIFIÉ : FORMATEUR peut aussi accepter/refuser ses inscriptions
    @PatchMapping("/{id}/statut")
    @PreAuthorize("hasRole('ADMIN') or hasRole('FORMATEUR')")
    public ResponseEntity<InscriptionResponse> updateStatut(
            @PathVariable Long id,
            @RequestParam StatutInscription statut) {
        return ResponseEntity.ok(inscriptionService.updateStatut(id, statut));
    }

    @GetMapping("/etudiant/{etudiantId}")
    @PreAuthorize("hasRole('ETUDIANT') or hasRole('ADMIN')")
    public ResponseEntity<List<InscriptionResponse>> getByEtudiant(@PathVariable Long etudiantId) {
        return ResponseEntity.ok(inscriptionService.getByEtudiant(etudiantId));
    }

    @GetMapping("/cours/{coursId}")
    @PreAuthorize("hasRole('FORMATEUR') or hasRole('ADMIN')")
    public ResponseEntity<List<InscriptionResponse>> getByCours(@PathVariable Long coursId) {
        return ResponseEntity.ok(inscriptionService.getByCours(coursId));
    }

    @PatchMapping("/{id}/annuler")
    @PreAuthorize("hasRole('ETUDIANT') or hasRole('ADMIN')")
    public ResponseEntity<Void> annuler(@PathVariable Long id) {
        inscriptionService.annuler(id);
        return ResponseEntity.noContent().build();
    }
}