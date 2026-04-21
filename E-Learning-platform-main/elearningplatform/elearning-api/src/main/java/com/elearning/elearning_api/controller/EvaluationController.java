package com.elearning.elearning_api.controller;

import com.elearning.elearning_api.dto.request.EvaluationRequest;
import com.elearning.elearning_api.dto.request.EvaluationSubmissionRequest;
import com.elearning.elearning_api.dto.response.EvaluationResponse;
import com.elearning.elearning_api.dto.response.EvaluationSubmissionResponse;
import com.elearning.elearning_api.service.EvaluationService;
import com.elearning.elearning_api.service.EvaluationSubmissionService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/evaluations")
@RequiredArgsConstructor
public class EvaluationController {

    private final EvaluationService evaluationService;
    private final EvaluationSubmissionService evaluationSubmissionService;

    @PostMapping
    @PreAuthorize("hasRole('FORMATEUR')")
    public ResponseEntity<EvaluationResponse> create(@Valid @RequestBody EvaluationRequest request) {
        return ResponseEntity.ok(evaluationService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('FORMATEUR')")
    public ResponseEntity<EvaluationResponse> update(@PathVariable Long id,
                                                     @Valid @RequestBody EvaluationRequest request) {
        return ResponseEntity.ok(evaluationService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('FORMATEUR') or hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        evaluationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EvaluationResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(evaluationService.getById(id));
    }

    @GetMapping("/lecon/{leconId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<EvaluationResponse>> getByLecon(@PathVariable Long leconId) {
        return ResponseEntity.ok(evaluationService.getByLecon(leconId));
    }

    @PatchMapping("/{id}/publication")
    @PreAuthorize("hasRole('FORMATEUR') or hasRole('ADMIN')")
    public ResponseEntity<EvaluationResponse> updatePublication(@PathVariable Long id,
                                                                @RequestParam boolean publie) {
        return ResponseEntity.ok(evaluationService.updatePublication(id, publie));
    }

    @PostMapping("/{id}/soumissions")
    @PreAuthorize("hasRole('ETUDIANT')")
    public ResponseEntity<EvaluationSubmissionResponse> submit(@PathVariable Long id,
                                                               @Valid @RequestBody EvaluationSubmissionRequest request) {
        return ResponseEntity.ok(evaluationSubmissionService.submit(id, request));
    }
}
