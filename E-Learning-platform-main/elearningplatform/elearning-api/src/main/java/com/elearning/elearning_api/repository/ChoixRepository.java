package com.elearning.elearning_api.repository;


import com.elearning.elearning_api.entity.Choix;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChoixRepository extends JpaRepository<Choix, Long> {
    List<Choix> findByQuestionId(Long questionId);
    void deleteByQuestionId(Long questionId);
}
