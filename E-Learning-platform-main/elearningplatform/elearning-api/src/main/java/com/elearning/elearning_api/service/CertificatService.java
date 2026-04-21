package com.elearning.elearning_api.service;

import com.elearning.elearning_api.dto.request.CertificatRequest;
import com.elearning.elearning_api.dto.response.CertificatResponse;
import com.elearning.elearning_api.entity.Certificat;
import com.elearning.elearning_api.entity.Cours;
import com.elearning.elearning_api.entity.Etudiant;
import com.elearning.elearning_api.entity.Evaluation;
import com.elearning.elearning_api.entity.Utilisateur;
import com.elearning.elearning_api.enums.StatutInscription;
import com.elearning.elearning_api.exception.ResourceNotFoundException;
import com.elearning.elearning_api.repository.CertificatRepository;
import com.elearning.elearning_api.repository.CoursRepository;
import com.elearning.elearning_api.repository.EtudiantRepository;
import com.elearning.elearning_api.repository.EvaluationRepository;
import com.elearning.elearning_api.repository.InscriptionRepository;
import com.elearning.elearning_api.repository.LeconRepository;
import com.elearning.elearning_api.repository.ProgressionLeconRepository;
import com.elearning.elearning_api.repository.ResultatEvaluationRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CertificatService {

    private final CertificatRepository certificatRepository;
    private final EtudiantRepository etudiantRepository;
    private final CoursRepository coursRepository;
    private final InscriptionRepository inscriptionRepository;
    private final LeconRepository leconRepository;
    private final ProgressionLeconRepository progressionLeconRepository;
    private final EvaluationRepository evaluationRepository;
    private final ResultatEvaluationRepository resultatEvaluationRepository;
    private final QuizAccessService quizAccessService;
    private final CertificatPdfService certificatPdfService;

    public record GeneratedPdf(String fileName, byte[] content) {}

    public CertificatResponse create(CertificatRequest request) {
        Etudiant etudiant = etudiantRepository.findById(request.getEtudiantId())
                .orElseThrow(() -> new ResourceNotFoundException("Etudiant not found: " + request.getEtudiantId()));

        Cours cours = coursRepository.findById(request.getCoursId())
                .orElseThrow(() -> new ResourceNotFoundException("Cours not found: " + request.getCoursId()));

        Certificat certificat = new Certificat();
        certificat.setCode(generateUniqueCode());
        certificat.setUrlPdf(request.getUrlPdf());
        certificat.setEtudiant(etudiant);
        certificat.setCours(cours);

        return toResponse(certificatRepository.save(certificat));
    }

    public void delete(Long id) {
        if (!certificatRepository.existsById(id))
            throw new ResourceNotFoundException("Certificat not found: " + id);
        certificatRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public CertificatResponse getById(Long id) {
        Certificat certificat = certificatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Certificat not found: " + id));
        assertCanAccessCertificate(certificat);
        return toResponse(certificat);
    }

    @Transactional(readOnly = true)
    public CertificatResponse getByCode(String code) {
        Certificat certificat = certificatRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Certificat not found with code: " + code));
        assertCanAccessCertificate(certificat);
        return toResponse(certificat);
    }

    @Transactional(readOnly = true)
    public List<CertificatResponse> getByEtudiant(Long etudiantId) {
        assertCanAccessStudentCertificates(etudiantId);
        return certificatRepository.findByEtudiantId(etudiantId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public GeneratedPdf downloadById(Long certificatId) {
        Utilisateur currentUser = quizAccessService.getCurrentUser();
        Certificat certificat = certificatRepository.findById(certificatId)
                .orElseThrow(() -> new ResourceNotFoundException("Certificat not found: " + certificatId));

        boolean canDownload = quizAccessService.isAdmin(currentUser)
                || (quizAccessService.isEtudiant(currentUser) && certificat.getEtudiant().getId().equals(currentUser.getId()));

        if (!canDownload) {
            throw new AccessDeniedException("Vous ne pouvez pas telecharger ce certificat.");
        }

        return new GeneratedPdf(
                certificatPdfService.buildFileName(certificat),
                certificatPdfService.generate(certificat)
        );
    }

    public Optional<Certificat> ensureAutomaticCertificate(Long etudiantId, Long coursId) {
        Optional<Certificat> existing = certificatRepository.findByEtudiantIdAndCoursId(etudiantId, coursId);
        if (existing.isPresent()) {
            return existing;
        }

        if (!inscriptionRepository.existsByEtudiantIdAndCoursIdAndStatut(etudiantId, coursId, StatutInscription.VALIDE)) {
            return Optional.empty();
        }

        long totalLessons = leconRepository.countByCoursId(coursId);
        if (totalLessons == 0) {
            return Optional.empty();
        }

        long viewedLessons = progressionLeconRepository.countByEtudiantIdAndLeconCoursId(etudiantId, coursId);
        if (viewedLessons < totalLessons) {
            return Optional.empty();
        }

        List<Evaluation> publishedEvaluations = evaluationRepository.findByLeconCoursIdAndPublieTrue(coursId);
        boolean allEvaluationsPassed = publishedEvaluations.stream()
                .allMatch(evaluation -> resultatEvaluationRepository.existsByEvaluationIdAndEtudiantIdAndReussiTrue(evaluation.getId(), etudiantId));

        if (!allEvaluationsPassed) {
            return Optional.empty();
        }

        Etudiant etudiant = etudiantRepository.findById(etudiantId)
                .orElseThrow(() -> new ResourceNotFoundException("Etudiant not found: " + etudiantId));

        Cours cours = coursRepository.findById(coursId)
                .orElseThrow(() -> new ResourceNotFoundException("Cours not found: " + coursId));

        Certificat certificat = new Certificat();
        certificat.setCode(generateUniqueCode());
        certificat.setEtudiant(etudiant);
        certificat.setCours(cours);

        return Optional.of(certificatRepository.save(certificat));
    }

    private CertificatResponse toResponse(Certificat certificat) {
        CertificatResponse response = new CertificatResponse();
        response.setId(certificat.getId());
        response.setCode(certificat.getCode());
        response.setDateObtention(certificat.getDateObtention());
        response.setUrlPdf(certificat.getUrlPdf() != null ? certificat.getUrlPdf() : buildDownloadPath(certificat.getId()));
        response.setEtudiantId(certificat.getEtudiant().getId());
        response.setEtudiantNom(certificat.getEtudiant().getNom());
        response.setCoursId(certificat.getCours().getId());
        response.setCoursTitre(certificat.getCours().getTitre());
        return response;
    }

    private String generateUniqueCode() {
        String code;
        do {
            code = "EDU-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (certificatRepository.findByCode(code).isPresent());
        return code;
    }

    private String buildDownloadPath(Long certificatId) {
        return "/api/certificats/" + certificatId + "/download";
    }

    private void assertCanAccessCertificate(Certificat certificat) {
        Utilisateur currentUser = quizAccessService.getCurrentUser();
        if (quizAccessService.isAdmin(currentUser)) {
            return;
        }
        if (quizAccessService.isEtudiant(currentUser) && certificat.getEtudiant().getId().equals(currentUser.getId())) {
            return;
        }
        throw new AccessDeniedException("Vous ne pouvez pas consulter ce certificat.");
    }

    private void assertCanAccessStudentCertificates(Long etudiantId) {
        Utilisateur currentUser = quizAccessService.getCurrentUser();
        if (quizAccessService.isAdmin(currentUser)) {
            return;
        }
        if (quizAccessService.isEtudiant(currentUser) && etudiantId.equals(currentUser.getId())) {
            return;
        }
        throw new AccessDeniedException("Vous ne pouvez pas consulter les certificats de cet etudiant.");
    }
}
