package com.elearning.elearning_api.service;

import com.elearning.elearning_api.dto.request.ChoixRequest;
import com.elearning.elearning_api.dto.response.ChoixResponse;
import com.elearning.elearning_api.entity.Choix;
import com.elearning.elearning_api.entity.Question;
import com.elearning.elearning_api.entity.Utilisateur;
import com.elearning.elearning_api.exception.ResourceNotFoundException;
import com.elearning.elearning_api.repository.ChoixRepository;
import com.elearning.elearning_api.repository.QuestionRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChoixService {

    private final ChoixRepository choixRepository;
    private final QuestionRepository questionRepository;
    private final QuizAccessService quizAccessService;

    @Transactional
    public ChoixResponse create(ChoixRequest request) {
        Question question = questionRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new ResourceNotFoundException("Question not found: " + request.getQuestionId()));
        quizAccessService.assertCanManageCourse(question.getEvaluation().getLecon().getCours());

        Choix choix = new Choix();
        choix.setTexte(request.getTexte());
        choix.setEstCorrect(request.getEstCorrect());
        choix.setQuestion(question);

        return toResponse(choixRepository.save(choix));
    }

    @Transactional
    public ChoixResponse update(Long id, ChoixRequest request) {
        Choix existing = choixRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Choix not found: " + id));
        quizAccessService.assertCanManageCourse(existing.getQuestion().getEvaluation().getLecon().getCours());

        existing.setTexte(request.getTexte());
        existing.setEstCorrect(request.getEstCorrect());

        return toResponse(choixRepository.save(existing));
    }

    @Transactional
    public void delete(Long id) {
        Choix choix = choixRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Choix not found: " + id));
        quizAccessService.assertCanManageCourse(choix.getQuestion().getEvaluation().getLecon().getCours());
        choixRepository.delete(choix);
    }

    @Transactional(readOnly = true)
    public List<ChoixResponse> getByQuestion(Long questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found: " + questionId));
        quizAccessService.assertCanReadEvaluation(question.getEvaluation());

        return choixRepository.findByQuestionId(questionId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private ChoixResponse toResponse(Choix choix) {
        Utilisateur currentUser = quizAccessService.getCurrentUserOrNull();
        boolean hideCorrectAnswer = currentUser != null && quizAccessService.isEtudiant(currentUser);

        ChoixResponse response = new ChoixResponse();
        response.setId(choix.getId());
        response.setTexte(choix.getTexte());
        response.setEstCorrect(hideCorrectAnswer ? null : choix.getEstCorrect());
        response.setQuestionId(choix.getQuestion().getId());
        return response;
    }
}
