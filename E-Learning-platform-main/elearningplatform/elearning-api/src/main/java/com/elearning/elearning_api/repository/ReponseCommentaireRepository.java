package com.elearning.elearning_api.repository;


import com.elearning.elearning_api.entity.ReponseCommentaire;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReponseCommentaireRepository extends JpaRepository<ReponseCommentaire, Long> {
    List<ReponseCommentaire> findByCommentaireId(Long commentaireId);
}