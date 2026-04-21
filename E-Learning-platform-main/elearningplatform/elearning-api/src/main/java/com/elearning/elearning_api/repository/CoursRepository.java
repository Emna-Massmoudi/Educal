package com.elearning.elearning_api.repository;


import com.elearning.elearning_api.entity.Cours;
import com.elearning.elearning_api.enums.EtatCours;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CoursRepository extends JpaRepository<Cours, Long> {
    List<Cours> findByFormateurId(Long formateurId);
    List<Cours> findByEtatPublication(EtatCours etat);
    List<Cours> findBySousCategorieId(Long sousCategorieId);
    Optional<Cours> findByIdAndEtatPublication(Long id, EtatCours etat);
}
