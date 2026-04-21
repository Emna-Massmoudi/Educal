package com.elearning.elearning_api.service;

import com.elearning.elearning_api.dto.request.QuestionChoixRequest;
import com.elearning.elearning_api.dto.request.QuestionRequest;
import com.elearning.elearning_api.dto.response.ChoixResponse;
import com.elearning.elearning_api.dto.response.QuestionResponse;
import com.elearning.elearning_api.entity.Choix;
import com.elearning.elearning_api.entity.Evaluation;
import com.elearning.elearning_api.entity.Question;
import com.elearning.elearning_api.entity.Utilisateur;
import com.elearning.elearning_api.exception.ResourceNotFoundException;
import com.elearning.elearning_api.repository.ChoixRepository;
import com.elearning.elearning_api.repository.EvaluationRepository;
import com.elearning.elearning_api.repository.QuestionRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final EvaluationRepository evaluationRepository;
    private final ChoixRepository choixRepository;
    private final QuizAccessService quizAccessService;

    @Transactional
    public QuestionResponse create(QuestionRequest request) {
        validateChoices(request.getChoix());

        Evaluation evaluation = evaluationRepository.findById(request.getEvaluationId())
                .orElseThrow(() -> new ResourceNotFoundException("Evaluation not found: " + request.getEvaluationId()));
        quizAccessService.assertCanManageCourse(evaluation.getLecon().getCours());

        Question question = new Question();
        question.setEnonce(request.getEnonce().trim());
        question.setPoint(request.getPoint());
        question.setEvaluation(evaluation);

        Question saved = questionRepository.save(question);
        saveChoices(saved, request.getChoix());
        return getById(saved.getId());
    }

    @Transactional
    public QuestionResponse update(Long id, QuestionRequest request) {
        validateChoices(request.getChoix());

        Question existing = questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found: " + id));
        quizAccessService.assertCanManageCourse(existing.getEvaluation().getLecon().getCours());

        existing.setEnonce(request.getEnonce().trim());
        existing.setPoint(request.getPoint());
        questionRepository.save(existing);

        choixRepository.deleteByQuestionId(existing.getId());
        saveChoices(existing, request.getChoix());

        return getById(existing.getId());
    }

    @Transactional
    public void delete(Long id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found: " + id));
        quizAccessService.assertCanManageCourse(question.getEvaluation().getLecon().getCours());

        Evaluation evaluation = question.getEvaluation();
        questionRepository.delete(question);

        if (Boolean.TRUE.equals(evaluation.getPublie())
                && questionRepository.countByEvaluationId(evaluation.getId()) == 0) {
            evaluation.setPublie(false);
            evaluationRepository.save(evaluation);
        }
    }

    @Transactional(readOnly = true)
    public QuestionResponse getById(Long id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found: " + id));
        quizAccessService.assertCanReadEvaluation(question.getEvaluation());
        return toResponse(question);
    }

    @Transactional(readOnly = true)
    public List<QuestionResponse> getByEvaluation(Long evaluationId) {
        Evaluation evaluation = evaluationRepository.findById(evaluationId)
                .orElseThrow(() -> new ResourceNotFoundException("Evaluation not found: " + evaluationId));
        quizAccessService.assertCanReadEvaluation(evaluation);

        return questionRepository.findByEvaluationId(evaluationId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private void saveChoices(Question question, List<QuestionChoixRequest> choixRequests) {
        List<Choix> choix = new ArrayList<>();
        for (QuestionChoixRequest request : choixRequests) {
            Choix option = new Choix();
            option.setTexte(request.getTexte().trim());
            option.setEstCorrect(request.getEstCorrect());
            option.setQuestion(question);
            choix.add(option);
        }
        question.setChoix(choixRepository.saveAll(choix));
    }

    private QuestionResponse toResponse(Question question) {
        Utilisateur currentUser = quizAccessService.getCurrentUserOrNull();
        boolean hideCorrectAnswers = currentUser != null && quizAccessService.isEtudiant(currentUser);

        QuestionResponse response = new QuestionResponse();
        response.setId(question.getId());
        response.setEnonce(question.getEnonce());
        response.setPoint(question.getPoint());
        response.setEvaluationId(question.getEvaluation().getId());
        response.setEvaluationTitre(question.getEvaluation().getTitre());

        List<ChoixResponse> choixResponses = new ArrayList<>();
        if (question.getChoix() != null) {
            for (Choix choix : question.getChoix()) {
                ChoixResponse choixResponse = new ChoixResponse();
                choixResponse.setId(choix.getId());
                choixResponse.setTexte(choix.getTexte());
                choixResponse.setQuestionId(question.getId());
                choixResponse.setEstCorrect(hideCorrectAnswers ? null : choix.getEstCorrect());
                choixResponses.add(choixResponse);
            }
        }
        response.setChoix(choixResponses);

        return response;
    }

    private void validateChoices(List<QuestionChoixRequest> choix) {
        if (choix == null || choix.size() < 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chaque question doit contenir au moins deux choix.");
        }

        long validChoices = choix.stream().filter(item -> item.getTexte() != null && !item.getTexte().isBlank()).count();
        if (validChoices < 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chaque question doit contenir au moins deux choix valides.");
        }

        long correctChoices = choix.stream().filter(item -> Boolean.TRUE.equals(item.getEstCorrect())).count();
        if (correctChoices != 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Une question doit avoir exactement une bonne reponse.");
        }
    }
}
