package com.elearning.elearning_api.repository;

import com.elearning.elearning_api.entity.AvisCours;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AvisCoursRepository extends JpaRepository<AvisCours, Long> {

    List<AvisCours> findByCoursIdOrderByDateCreationDesc(Long coursId);

    List<AvisCours> findByEtudiantIdOrderByDateCreationDesc(Long etudiantId);

    Optional<AvisCours> findByEtudiantIdAndCoursId(Long etudiantId, Long coursId);

    long countByCoursId(Long coursId);

    @Query("select coalesce(avg(a.note), 0) from AvisCours a where a.cours.id = :coursId")
    Double findAverageNoteByCoursId(@Param("coursId") Long coursId);
}
