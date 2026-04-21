package com.elearning.elearning_api.repository;

import com.elearning.elearning_api.entity.Etudiant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EtudiantRepository extends JpaRepository<Etudiant, Long> {

    // ← Ajouter cette méthode à votre EtudiantRepository existant
    List<Etudiant> findByStatus(String status);
}