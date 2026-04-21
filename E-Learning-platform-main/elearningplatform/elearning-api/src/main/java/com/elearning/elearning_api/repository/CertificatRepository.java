package com.elearning.elearning_api.repository;


import com.elearning.elearning_api.entity.Certificat;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CertificatRepository extends JpaRepository<Certificat, Long> {
    List<Certificat> findByEtudiantId(Long etudiantId);
    Optional<Certificat> findByEtudiantIdAndCoursId(Long etudiantId, Long coursId);
    Optional<Certificat> findByCode(String code);
}
