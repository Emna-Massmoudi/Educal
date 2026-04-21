package com.elearning.elearning_api.controller;

import com.elearning.elearning_api.dto.request.ReponseCommentaireRequest;
import com.elearning.elearning_api.dto.response.ReponseCommentaireResponse;
import com.elearning.elearning_api.service.ReponseCommentaireService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/reponses-commentaires")
@RequiredArgsConstructor
public class ReponseCommentaireController {

    private final ReponseCommentaireService reponseCommentaireService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReponseCommentaireResponse> create(
            @Valid @RequestBody ReponseCommentaireRequest request) {
        return ResponseEntity.ok(reponseCommentaireService.create(request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reponseCommentaireService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/commentaire/{commentaireId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ReponseCommentaireResponse>> getByCommentaire(
            @PathVariable Long commentaireId) {
        return ResponseEntity.ok(reponseCommentaireService.getByCommentaire(commentaireId));
    }
}