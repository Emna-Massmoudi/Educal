package com.elearning.elearning_api.service;

import com.elearning.elearning_api.dto.response.ProgressionCoursResponse;
import com.elearning.elearning_api.entity.Certificat;
import com.elearning.elearning_api.entity.Cours;
import com.elearning.elearning_api.entity.Etudiant;
import com.elearning.elearning_api.entity.Inscription;
import com.elearning.elearning_api.entity.Lecon;
import com.elearning.elearning_api.entity.ProgressionLecon;
import com.elearning.elearning_api.entity.Utilisateur;
import com.elearning.elearning_api.enums.StatutInscription;
import com.elearning.elearning_api.exception.ResourceNotFoundException;
import com.elearning.elearning_api.repository.EvaluationRepository;
import com.elearning.elearning_api.repository.EtudiantRepository;
import com.elearning.elearning_api.repository.InscriptionRepository;
import com.elearning.elearning_api.repository.LeconRepository;
import com.elearning.elearning_api.repository.ProgressionLeconRepository;
import com.elearning.elearning_api.repository.ResultatEvaluationRepository;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProgressionLeconService {

    private final ProgressionLeconRepository progressionLeconRepository;
    private final LeconRepository leconRepository;
    private final InscriptionRepository inscriptionRepository;
    private final EtudiantRepository etudiantRepository;
    private final EvaluationRepository evaluationRepository;
    private final ResultatEvaluationRepository resultatEvaluationRepository;
    private final CertificatService certificatService;
    private final QuizAccessService quizAccessService;

    @Transactional
    public ProgressionCoursResponse markLessonViewed(Long leconId) {
        Etudiant etudiant = getCurrentEtudiant();

        Lecon lecon = leconRepository.findById(leconId)
                .orElseThrow(() -> new ResourceNotFoundException("Lecon not found: " + leconId));

        assertHasValidEnrollment(etudiant.getId(), lecon.getCours().getId());

        ProgressionLecon progression = progressionLeconRepository
                .findByEtudiantIdAndLeconId(etudiant.getId(), leconId)
                .orElseGet(ProgressionLecon::new);

        progression.setEtudiant(etudiant);
        progression.setLecon(lecon);
        if (progression.getDatePremiereConsultation() == null) {
            progression.setDatePremiereConsultation(LocalDateTime.now());
        }
        progression.setDateDerniereConsultation(LocalDateTime.now());

        progressionLeconRepository.save(progression);
        return buildCourseProgress(etudiant.getId(), lecon.getCours());
    }

    @Transactional
    public List<ProgressionCoursResponse> getCurrentStudentCourseProgress() {
        Etudiant etudiant = getCurrentEtudiant();

        return inscriptionRepository.findByEtudiantId(etudiant.getId())
                .stream()
                .filter(inscription -> inscription.getStatut() == StatutInscription.VALIDE)
                .map(Inscription::getCours)
                .map(cours -> buildCourseProgress(etudiant.getId(), cours))
                .toList();
    }

    private ProgressionCoursResponse buildCourseProgress(Long etudiantId, Cours cours) {
        List<Lecon> lessons = leconRepository.findByCoursIdOrderByOrdreAsc(cours.getId());
        List<ProgressionLecon> progressions = progressionLeconRepository.findByEtudiantIdAndLeconCoursId(etudiantId, cours.getId());
        ProgressionLecon latestProgress = progressionLeconRepository
                .findTopByEtudiantIdAndLeconCoursIdOrderByDateDerniereConsultationDesc(etudiantId, cours.getId())
                .orElse(null);

        Set<Long> viewedLessonIds = new HashSet<>();
        progressions.forEach(item -> viewedLessonIds.add(item.getLecon().getId()));

        int totalLessons = lessons.size();
        int viewedLessons = viewedLessonIds.size();
        int percentage = totalLessons > 0 ? (int) Math.round((viewedLessons * 100.0) / totalLessons) : 0;
        List<Long> publishedEvaluationIds = evaluationRepository.findByLeconCoursIdAndPublieTrue(cours.getId())
                .stream()
                .map(evaluation -> evaluation.getId())
                .toList();
        int validatedEvaluations = (int) publishedEvaluationIds.stream()
                .filter(evaluationId -> resultatEvaluationRepository.existsByEvaluationIdAndEtudiantIdAndReussiTrue(evaluationId, etudiantId))
                .count();
        boolean lessonsCompleted = totalLessons > 0 && viewedLessons >= totalLessons;
        boolean allEvaluationsPassed = validatedEvaluations >= publishedEvaluationIds.size();
        boolean completed = lessonsCompleted && allEvaluationsPassed;
        Optional<Certificat> certificate = certificatService.ensureAutomaticCertificate(etudiantId, cours.getId());

        Lecon nextLesson = lessons.stream()
                .filter(lesson -> !viewedLessonIds.contains(lesson.getId()))
                .findFirst()
                .orElse(latestProgress != null ? latestProgress.getLecon() : null);

        ProgressionCoursResponse response = new ProgressionCoursResponse();
        response.setCoursId(cours.getId());
        response.setTotalLecons(totalLessons);
        response.setLeconsConsultees(viewedLessons);
        response.setPourcentage(percentage);
        response.setTermine(completed);
        response.setEvaluationsPubliees(publishedEvaluationIds.size());
        response.setEvaluationsValidees(validatedEvaluations);
        response.setCertificatDisponible(certificate.isPresent());

        if (latestProgress != null) {
            response.setDerniereLeconId(latestProgress.getLecon().getId());
            response.setDerniereLeconTitre(latestProgress.getLecon().getTitre());
            response.setDateDerniereConsultation(latestProgress.getDateDerniereConsultation());
        }

        if (nextLesson != null) {
            response.setProchaineLeconId(nextLesson.getId());
            response.setProchaineLeconTitre(nextLesson.getTitre());
        }

        certificate.ifPresent(certificat -> {
            response.setCertificatId(certificat.getId());
            response.setCertificatCode(certificat.getCode());
            response.setCertificatDateObtention(certificat.getDateObtention());
        });

        return response;
    }

    private Etudiant getCurrentEtudiant() {
        Utilisateur currentUser = quizAccessService.getCurrentUser();
        if (!quizAccessService.isEtudiant(currentUser)) {
            throw new AccessDeniedException("Seuls les etudiants peuvent suivre leur progression.");
        }

        return etudiantRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Etudiant not found: " + currentUser.getId()));
    }

    private void assertHasValidEnrollment(Long etudiantId, Long coursId) {
        if (!inscriptionRepository.existsByEtudiantIdAndCoursIdAndStatut(etudiantId, coursId, StatutInscription.VALIDE)) {
            throw new AccessDeniedException("Vous devez etre inscrit et valide pour suivre cette lecon.");
        }
    }
}
