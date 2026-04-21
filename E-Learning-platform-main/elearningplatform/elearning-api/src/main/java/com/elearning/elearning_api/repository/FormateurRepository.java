package com.elearning.elearning_api.repository;

import com.elearning.elearning_api.entity.Formateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FormateurRepository extends JpaRepository<Formateur, Long> {

    // ← Ajouter cette méthode à votre FormateurRepository existant
    List<Formateur> findByStatus(String status);
}