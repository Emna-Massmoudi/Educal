package com.elearning.elearning_api.service;

import com.elearning.elearning_api.dto.request.EvaluationAnswerRequest;
import com.elearning.elearning_api.dto.request.EvaluationSubmissionRequest;
import com.elearning.elearning_api.dto.response.EvaluationSubmissionResponse;
import com.elearning.elearning_api.entity.Choix;
import com.elearning.elearning_api.entity.Etudiant;
import com.elearning.elearning_api.entity.Evaluation;
import com.elearning.elearning_api.entity.Question;
import com.elearning.elearning_api.entity.ResultatEvaluation;
import com.elearning.elearning_api.entity.Utilisateur;
import com.elearning.elearning_api.exception.ResourceNotFoundException;
import com.elearning.elearning_api.repository.EvaluationRepository;
import com.elearning.elearning_api.repository.QuestionRepository;
import com.elearning.elearning_api.repository.ResultatEvaluationRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class EvaluationSubmissionService {

    private final EvaluationRepository evaluationRepository;
    private final QuestionRepository questionRepository;
    private final ResultatEvaluationRepository resultatEvaluationRepository;
    private final QuizAccessService quizAccessService;
    private final CertificatService certificatService;

    @Transactional
    public EvaluationSubmissionResponse submit(Long evaluationId, EvaluationSubmissionRequest request) {
        Evaluation evaluation = evaluationRepository.findById(evaluationId)
                .orElseThrow(() -> new ResourceNotFoundException("Evaluation not found: " + evaluationId));
        quizAccessService.assertCanSubmitEvaluation(evaluation);

        Utilisateur utilisateur = quizAccessService.getCurrentUser();
        Etudiant etudiant = (Etudiant) utilisateur;

        List<Question> questions = questionRepository.findByEvaluationId(evaluationId);
        if (questions.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cette evaluation ne contient aucune question.");
        }

        Map<Long, Long> reponses = new HashMap<>();
        for (EvaluationAnswerRequest reponse : request.getReponses()) {
            reponses.put(reponse.getQuestionId(), reponse.getChoixId());
        }

        int score = 0;
        int totalPoints = 0;

        for (Question question : questions) {
            int points = question.getPoint() != null ? question.getPoint() : 1;
            totalPoints += points;

            Long choixSelectionneId = reponses.get(question.getId());
            if (choixSelectionneId == null) {
                continue;
            }

            boolean bonneReponse = question.getChoix() != null && question.getChoix().stream()
                    .filter(choix -> Boolean.TRUE.equals(choix.getEstCorrect()))
                    .anyMatch(choix -> choix.getId().equals(choixSelectionneId));

            if (bonneReponse) {
                score += points;
            }
        }

        int pourcentage = totalPoints > 0 ? (int) Math.round((score * 100.0) / totalPoints) : 0;
        double noteObtenue = totalPoints > 0
                ? Math.round(((score * (double) evaluation.getNoteMax()) / totalPoints) * 100.0) / 100.0
                : 0.0;
        boolean reussi = noteObtenue >= evaluation.getNoteMin();
        int tentativeNumero = (int) resultatEvaluationRepository.countByEvaluationIdAndEtudiantId(evaluationId, etudiant.getId()) + 1;

        ResultatEvaluation resultat = new ResultatEvaluation();
        resultat.setEvaluation(evaluation);
        resultat.setEtudiant(etudiant);
        resultat.setScore(score);
        resultat.setTotalPoints(totalPoints);
        resultat.setPourcentage(pourcentage);
        resultat.setNoteObtenue(noteObtenue);
        resultat.setReussi(reussi);
        resultat.setTentativeNumero(tentativeNumero);

        ResultatEvaluation saved = resultatEvaluationRepository.save(resultat);
        certificatService.ensureAutomaticCertificate(etudiant.getId(), evaluation.getLecon().getCours().getId());
        return toResponse(saved);
    }

    public EvaluationSubmissionResponse toResponse(ResultatEvaluation resultat) {
        EvaluationSubmissionResponse response = new EvaluationSubmissionResponse();
        response.setEvaluationId(resultat.getEvaluation().getId());
        response.setEvaluationTitre(resultat.getEvaluation().getTitre());
        response.setType(resultat.getEvaluation().getType());
        response.setScore(resultat.getScore());
        response.setTotalPoints(resultat.getTotalPoints());
        response.setPourcentage(resultat.getPourcentage());
        response.setNoteObtenue(resultat.getNoteObtenue());
        response.setNoteMax(resultat.getEvaluation().getNoteMax());
        response.setNoteMin(resultat.getEvaluation().getNoteMin());
        response.setReussi(resultat.getReussi());
        response.setTentativeNumero(resultat.getTentativeNumero());
        response.setDateSoumission(resultat.getDateSoumission());
        return response;
    }
}
