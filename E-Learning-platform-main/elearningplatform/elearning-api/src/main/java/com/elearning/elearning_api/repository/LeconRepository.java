package com.elearning.elearning_api.repository;


import com.elearning.elearning_api.entity.Lecon;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LeconRepository extends JpaRepository<Lecon, Long> {
    List<Lecon> findByCoursIdOrderByOrdreAsc(Long coursId);

    long countByCoursId(Long coursId);
}
