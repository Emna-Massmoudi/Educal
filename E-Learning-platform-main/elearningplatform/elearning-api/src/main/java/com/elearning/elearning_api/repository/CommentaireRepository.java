package com.elearning.elearning_api.repository;


import com.elearning.elearning_api.entity.Commentaire;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommentaireRepository extends JpaRepository<Commentaire, Long> {
    List<Commentaire> findByCoursId(Long coursId);
    List<Commentaire> findByEtudiantId(Long etudiantId);
}