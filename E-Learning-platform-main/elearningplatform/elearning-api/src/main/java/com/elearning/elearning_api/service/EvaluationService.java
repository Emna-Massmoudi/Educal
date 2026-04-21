package com.elearning.elearning_api.service;

import com.elearning.elearning_api.dto.request.EvaluationRequest;
import com.elearning.elearning_api.dto.response.EvaluationResponse;
import com.elearning.elearning_api.entity.Choix;
import com.elearning.elearning_api.entity.Evaluation;
import com.elearning.elearning_api.entity.Lecon;
import com.elearning.elearning_api.entity.Question;
import com.elearning.elearning_api.entity.ResultatEvaluation;
import com.elearning.elearning_api.entity.Utilisateur;
import com.elearning.elearning_api.exception.ResourceNotFoundException;
import com.elearning.elearning_api.repository.EvaluationRepository;
import com.elearning.elearning_api.repository.LeconRepository;
import com.elearning.elearning_api.repository.QuestionRepository;
import com.elearning.elearning_api.repository.ResultatEvaluationRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class EvaluationService {

    private final EvaluationRepository evaluationRepository;
    private final LeconRepository leconRepository;
    private final QuestionRepository questionRepository;
    private final ResultatEvaluationRepository resultatEvaluationRepository;
    private final QuizAccessService quizAccessService;

    @Transactional
    public EvaluationResponse create(EvaluationRequest request) {
        validateNotes(request);

        Lecon lecon = leconRepository.findById(request.getLeconId())
                .orElseThrow(() -> new ResourceNotFoundException("Lecon not found: " + request.getLeconId()));
        quizAccessService.assertCanManageCourse(lecon.getCours());

        Evaluation evaluation = new Evaluation();
        evaluation.setTitre(request.getTitre().trim());
        evaluation.setType(request.getType());
        evaluation.setNoteMax(request.getNoteMax());
        evaluation.setNoteMin(request.getNoteMin());
        evaluation.setLecon(lecon);
        evaluation.setPublie(false);

        return toResponse(evaluationRepository.save(evaluation));
    }

    @Transactional
    public EvaluationResponse update(Long id, EvaluationRequest request) {
        validateNotes(request);

        Evaluation existing = evaluationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evaluation not found: " + id));
        quizAccessService.assertCanManageCourse(existing.getLecon().getCours());

        Lecon lecon = leconRepository.findById(request.getLeconId())
                .orElseThrow(() -> new ResourceNotFoundException("Lecon not found: " + request.getLeconId()));
        quizAccessService.assertCanManageCourse(lecon.getCours());

        existing.setTitre(request.getTitre().trim());
        existing.setType(request.getType());
        existing.setNoteMax(request.getNoteMax());
        existing.setNoteMin(request.getNoteMin());
        existing.setLecon(lecon);

        return toResponse(evaluationRepository.save(existing));
    }

    @Transactional
    public EvaluationResponse updatePublication(Long id, boolean publie) {
        Evaluation evaluation = evaluationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evaluation not found: " + id));
        quizAccessService.assertCanManageCourse(evaluation.getLecon().getCours());

        if (publie) {
            validateEvaluationCanBePublished(evaluation);
        }

        evaluation.setPublie(publie);
        return toResponse(evaluationRepository.save(evaluation));
    }

    @Transactional
    public void delete(Long id) {
        Evaluation evaluation = evaluationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evaluation not found: " + id));
        quizAccessService.assertCanManageCourse(evaluation.getLecon().getCours());
        evaluationRepository.delete(evaluation);
    }

    @Transactional(readOnly = true)
    public EvaluationResponse getById(Long id) {
        Evaluation evaluation = evaluationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evaluation not found: " + id));
        quizAccessService.assertCanReadEvaluation(evaluation);
        return toResponse(evaluation);
    }

    @Transactional(readOnly = true)
    public List<EvaluationResponse> getByLecon(Long leconId) {
        Lecon lecon = leconRepository.findById(leconId)
                .orElseThrow(() -> new ResourceNotFoundException("Lecon not found: " + leconId));
        quizAccessService.assertCanReadCourseContent(lecon.getCours());

        Utilisateur currentUser = quizAccessService.getCurrentUser();

        return evaluationRepository.findByLeconId(leconId)
                .stream()
                .filter(evaluation -> !quizAccessService.isEtudiant(currentUser) || Boolean.TRUE.equals(evaluation.getPublie()))
                .map(this::toResponse)
                .toList();
    }

    private EvaluationResponse toResponse(Evaluation evaluation) {
        EvaluationResponse response = new EvaluationResponse();
        response.setId(evaluation.getId());
        response.setTitre(evaluation.getTitre());
        response.setType(evaluation.getType());
        response.setNoteMax(evaluation.getNoteMax());
        response.setNoteMin(evaluation.getNoteMin());
        response.setPublie(Boolean.TRUE.equals(evaluation.getPublie()));
        response.setQuestionCount((int) questionRepository.countByEvaluationId(evaluation.getId()));
        response.setDateCreation(evaluation.getDateCreation());
        response.setLeconId(evaluation.getLecon().getId());
        response.setLeconTitre(evaluation.getLecon().getTitre());

        Utilisateur currentUser = quizAccessService.getCurrentUserOrNull();
        if (currentUser != null && quizAccessService.isEtudiant(currentUser)) {
            resultatEvaluationRepository
                    .findTopByEvaluationIdAndEtudiantIdOrderByDateSoumissionDesc(evaluation.getId(), currentUser.getId())
                    .ifPresent(resultat -> applyLatestResult(response, resultat, currentUser.getId()));
        }

        return response;
    }

    private void applyLatestResult(EvaluationResponse response, ResultatEvaluation resultat, Long etudiantId) {
        response.setDerniereNote(resultat.getNoteObtenue());
        response.setDernierScore(resultat.getScore());
        response.setDernierTotalPoints(resultat.getTotalPoints());
        response.setDernierPourcentage(resultat.getPourcentage());
        response.setDerniereReussite(resultat.getReussi());
        response.setDerniereSoumission(resultat.getDateSoumission());
        response.setNombreTentatives((int) resultatEvaluationRepository.countByEvaluationIdAndEtudiantId(response.getId(), etudiantId));
    }

    private void validateNotes(EvaluationRequest request) {
        if (request.getNoteMax() == null || request.getNoteMax() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La note maximale doit etre superieure a zero.");
        }
        if (request.getNoteMin() == null || request.getNoteMin() < 0 || request.getNoteMin() > request.getNoteMax()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La note minimale doit etre comprise entre 0 et la note maximale.");
        }
    }

    private void validateEvaluationCanBePublished(Evaluation evaluation) {
        if (evaluation.getNoteMax() == null || evaluation.getNoteMax() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La note maximale de l'evaluation est invalide.");
        }
        if (evaluation.getNoteMin() == null || evaluation.getNoteMin() < 0 || evaluation.getNoteMin() > evaluation.getNoteMax()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La note minimale doit etre comprise entre 0 et la note maximale.");
        }

        List<Question> questions = questionRepository.findByEvaluationId(evaluation.getId());
        if (questions.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ajoutez au moins une question avant publication.");
        }

        for (Question question : questions) {
            List<Choix> choix = question.getChoix();
            if (choix == null || choix.size() < 2) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Chaque question doit contenir au moins deux choix avant publication.");
            }

            long correctChoices = choix.stream().filter(item -> Boolean.TRUE.equals(item.getEstCorrect())).count();
            if (correctChoices != 1) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Chaque question doit avoir exactement une bonne reponse avant publication.");
            }
        }
    }
}
