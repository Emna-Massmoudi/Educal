package com.elearning.elearning_api.repository;


import com.elearning.elearning_api.entity.SousCategorie;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SousCategorieRepository extends JpaRepository<SousCategorie, Long> {
    List<SousCategorie> findByCategorieId(Long categorieId);
}