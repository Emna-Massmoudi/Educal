package com.elearning.elearning_api.repository;

import com.elearning.elearning_api.entity.ResultatEvaluation;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResultatEvaluationRepository extends JpaRepository<ResultatEvaluation, Long> {

    Optional<ResultatEvaluation> findTopByEvaluationIdAndEtudiantIdOrderByDateSoumissionDesc(Long evaluationId, Long etudiantId);

    List<ResultatEvaluation> findByEvaluationIdOrderByDateSoumissionDesc(Long evaluationId);

    long countByEvaluationIdAndEtudiantId(Long evaluationId, Long etudiantId);

    boolean existsByEvaluationIdAndEtudiantIdAndReussiTrue(Long evaluationId, Long etudiantId);
}
