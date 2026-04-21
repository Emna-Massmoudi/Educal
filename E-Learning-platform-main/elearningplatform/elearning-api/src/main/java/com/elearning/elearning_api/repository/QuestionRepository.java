package com.elearning.elearning_api.repository;


import com.elearning.elearning_api.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByEvaluationId(Long evaluationId);
    long countByEvaluationId(Long evaluationId);
}
