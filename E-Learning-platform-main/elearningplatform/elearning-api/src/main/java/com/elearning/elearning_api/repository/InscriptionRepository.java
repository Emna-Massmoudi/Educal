package com.elearning.elearning_api.repository;

import com.elearning.elearning_api.entity.Inscription;
import com.elearning.elearning_api.enums.StatutInscription;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InscriptionRepository extends JpaRepository<Inscription, Long> {

    List<Inscription> findByEtudiantId(Long etudiantId);

    List<Inscription> findByCoursId(Long coursId);

    Optional<Inscription> findByEtudiantIdAndCoursId(Long etudiantId, Long coursId);

    Optional<Inscription> findByEtudiantIdAndCoursIdAndStatut(Long etudiantId, Long coursId, StatutInscription statut);

    boolean existsByEtudiantIdAndCoursId(Long etudiantId, Long coursId);

    boolean existsByEtudiantIdAndCoursIdAndStatut(Long etudiantId, Long coursId, StatutInscription statut);

    long countByCoursIdAndStatut(Long coursId, StatutInscription statut);
}
