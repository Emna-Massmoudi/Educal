package com.elearning.elearning_api.repository;

import com.elearning.elearning_api.entity.PaiementCours;
import com.elearning.elearning_api.enums.StatutPaiement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaiementCoursRepository extends JpaRepository<PaiementCours, Long> {
    List<PaiementCours> findAllByOrderByDateCreationDesc();

    List<PaiementCours> findByStatutOrderByDateCreationDesc(StatutPaiement statut);

    List<PaiementCours> findByCoursFormateurIdAndStatutOrderByDateCreationDesc(Long formateurId, StatutPaiement statut);

    boolean existsByCodePaiement(String codePaiement);

    boolean existsByInscriptionIdAndStatut(Long inscriptionId, StatutPaiement statut);

    boolean existsByEtudiantIdAndCoursIdAndStatut(Long etudiantId, Long coursId, StatutPaiement statut);
}
