package com.elearning.elearning_api.repository;

import com.elearning.elearning_api.entity.Evaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {
    List<Evaluation> findByLeconId(Long leconId);

    List<Evaluation> findByLeconCoursIdAndPublieTrue(Long coursId);
}
