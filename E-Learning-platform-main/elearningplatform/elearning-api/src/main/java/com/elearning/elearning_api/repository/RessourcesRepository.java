package com.elearning.elearning_api.repository;


import com.elearning.elearning_api.entity.Ressources;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RessourcesRepository extends JpaRepository<Ressources, Long> {
    List<Ressources> findByLeconId(Long leconId);
}
