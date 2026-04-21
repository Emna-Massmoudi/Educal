package com.elearning.elearning_api.controller;

import com.elearning.elearning_api.dto.request.ChoixRequest;
import com.elearning.elearning_api.dto.response.ChoixResponse;
import com.elearning.elearning_api.service.ChoixService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/choix")
@RequiredArgsConstructor
public class ChoixController {

    private final ChoixService choixService;

    @PostMapping
    @PreAuthorize("hasRole('FORMATEUR')")
    public ResponseEntity<ChoixResponse> create(@Valid @RequestBody ChoixRequest request) {
        return ResponseEntity.ok(choixService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('FORMATEUR')")
    public ResponseEntity<ChoixResponse> update(@PathVariable Long id,
                                                @Valid @RequestBody ChoixRequest request) {
        return ResponseEntity.ok(choixService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('FORMATEUR') or hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        choixService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/question/{questionId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ChoixResponse>> getByQuestion(@PathVariable Long questionId) {
        return ResponseEntity.ok(choixService.getByQuestion(questionId));
    }
}