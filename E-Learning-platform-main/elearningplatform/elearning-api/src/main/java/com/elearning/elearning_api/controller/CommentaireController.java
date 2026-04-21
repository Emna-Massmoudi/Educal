package com.elearning.elearning_api.controller;

import com.elearning.elearning_api.dto.request.CommentaireRequest;
import com.elearning.elearning_api.dto.response.CommentaireResponse;
import com.elearning.elearning_api.enums.StatutCommentaire;
import com.elearning.elearning_api.service.CommentaireService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/commentaires")
@RequiredArgsConstructor
public class CommentaireController {

    private final CommentaireService commentaireService;

    @PostMapping
    @PreAuthorize("hasRole('ETUDIANT')")
    public ResponseEntity<CommentaireResponse> create(@Valid @RequestBody CommentaireRequest request) {
        return ResponseEntity.ok(commentaireService.create(request));
    }

    @PatchMapping("/{id}/statut")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommentaireResponse> updateStatut(@PathVariable Long id,
                                                            @RequestParam StatutCommentaire statut) {
        return ResponseEntity.ok(commentaireService.updateStatut(id, statut));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        commentaireService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/cours/{coursId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CommentaireResponse>> getByCours(@PathVariable Long coursId) {
        return ResponseEntity.ok(commentaireService.getByCours(coursId));
    }

    @GetMapping("/etudiant/{etudiantId}")
    @PreAuthorize("hasRole('ETUDIANT') or hasRole('ADMIN')")
    public ResponseEntity<List<CommentaireResponse>> getByEtudiant(@PathVariable Long etudiantId) {
        return ResponseEntity.ok(commentaireService.getByEtudiant(etudiantId));
    }
}