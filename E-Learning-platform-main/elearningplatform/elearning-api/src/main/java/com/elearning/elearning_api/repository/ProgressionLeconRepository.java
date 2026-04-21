package com.elearning.elearning_api.repository;

import com.elearning.elearning_api.entity.ProgressionLecon;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProgressionLeconRepository extends JpaRepository<ProgressionLecon, Long> {

    Optional<ProgressionLecon> findByEtudiantIdAndLeconId(Long etudiantId, Long leconId);

    List<ProgressionLecon> findByEtudiantIdAndLeconCoursId(Long etudiantId, Long coursId);

    long countByEtudiantIdAndLeconCoursId(Long etudiantId, Long coursId);

    Optional<ProgressionLecon> findTopByEtudiantIdAndLeconCoursIdOrderByDateDerniereConsultationDesc(Long etudiantId, Long coursId);
}
