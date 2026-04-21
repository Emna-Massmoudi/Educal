package com.elearning.elearning_api.repository;


import com.elearning.elearning_api.entity.Abonnement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AbonnementRepository extends JpaRepository<Abonnement, Long> {
    List<Abonnement> findByEtudiantId(Long etudiantId);
}