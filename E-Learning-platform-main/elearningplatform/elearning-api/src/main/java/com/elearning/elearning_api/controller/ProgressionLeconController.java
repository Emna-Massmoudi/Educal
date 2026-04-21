package com.elearning.elearning_api.controller;

import com.elearning.elearning_api.dto.response.ProgressionCoursResponse;
import com.elearning.elearning_api.service.ProgressionLeconService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/progressions")
@RequiredArgsConstructor
public class ProgressionLeconController {

    private final ProgressionLeconService progressionLeconService;

    @PostMapping("/lecons/{leconId}/consulter")
    @PreAuthorize("hasRole('ETUDIANT')")
    public ResponseEntity<ProgressionCoursResponse> markLessonViewed(@PathVariable Long leconId) {
        return ResponseEntity.ok(progressionLeconService.markLessonViewed(leconId));
    }

    @GetMapping("/mes-cours")
    @PreAuthorize("hasRole('ETUDIANT')")
    public ResponseEntity<List<ProgressionCoursResponse>> getCurrentStudentCourseProgress() {
        return ResponseEntity.ok(progressionLeconService.getCurrentStudentCourseProgress());
    }
}
