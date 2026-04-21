package com.elearning.elearning_api.controller;

import com.elearning.elearning_api.dto.request.PaiementCodeRequest;
import com.elearning.elearning_api.dto.response.PaiementCoursResponse;
import com.elearning.elearning_api.dto.response.WalletFormateurResponse;
import com.elearning.elearning_api.service.PaiementCoursService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/paiements")
@RequiredArgsConstructor
public class PaiementCoursController {

    private final PaiementCoursService paiementCoursService;

    @PostMapping("/code")
    @PreAuthorize("hasRole('ETUDIANT')")
    public ResponseEntity<PaiementCoursResponse> soumettreCode(@Valid @RequestBody PaiementCodeRequest request) {
        return ResponseEntity.ok(paiementCoursService.soumettreCode(request));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PaiementCoursResponse>> getAll() {
        return ResponseEntity.ok(paiementCoursService.getAll());
    }

    @GetMapping("/admin/en-attente")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PaiementCoursResponse>> getEnAttente() {
        return ResponseEntity.ok(paiementCoursService.getEnAttente());
    }

    @PatchMapping("/{id}/approuver")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaiementCoursResponse> approuver(@PathVariable Long id) {
        return ResponseEntity.ok(paiementCoursService.approuver(id));
    }

    @PatchMapping("/{id}/refuser")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaiementCoursResponse> refuser(@PathVariable Long id) {
        return ResponseEntity.ok(paiementCoursService.refuser(id));
    }

    @GetMapping("/formateur/{formateurId}/wallet")
    @PreAuthorize("hasRole('FORMATEUR') or hasRole('ADMIN')")
    public ResponseEntity<WalletFormateurResponse> getWalletFormateur(@PathVariable Long formateurId) {
        return ResponseEntity.ok(paiementCoursService.getWalletFormateur(formateurId));
    }
}
