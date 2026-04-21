package com.elearning.elearning_api.service;

import com.elearning.elearning_api.entity.Cours;
import com.elearning.elearning_api.entity.Evaluation;
import com.elearning.elearning_api.entity.Utilisateur;
import com.elearning.elearning_api.enums.Role;
import com.elearning.elearning_api.enums.StatutInscription;
import com.elearning.elearning_api.exception.ResourceNotFoundException;
import com.elearning.elearning_api.repository.InscriptionRepository;
import com.elearning.elearning_api.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QuizAccessService {

    private final UtilisateurRepository utilisateurRepository;
    private final InscriptionRepository inscriptionRepository;

    public Utilisateur getCurrentUser() {
        String email = getCurrentUserEmail();
        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur not found: " + email));
    }

    public Utilisateur getCurrentUserOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        String email = authentication.getName();
        if (email == null || email.isBlank() || "anonymousUser".equals(email)) {
            return null;
        }

        return utilisateurRepository.findByEmail(email).orElse(null);
    }

    public boolean isAdmin(Utilisateur utilisateur) {
        return utilisateur != null && utilisateur.getRole() == Role.ADMIN;
    }

    public boolean isFormateur(Utilisateur utilisateur) {
        return utilisateur != null && utilisateur.getRole() == Role.FORMATEUR;
    }

    public boolean isEtudiant(Utilisateur utilisateur) {
        return utilisateur != null && utilisateur.getRole() == Role.ETUDIANT;
    }

    public void assertCanManageCourse(Cours cours) {
        Utilisateur utilisateur = getCurrentUser();
        if (isAdmin(utilisateur)) {
            return;
        }
        if (isFormateur(utilisateur) && cours.getFormateur() != null && cours.getFormateur().getId().equals(utilisateur.getId())) {
            return;
        }
        throw new AccessDeniedException("Vous ne pouvez pas modifier ce contenu.");
    }

    public void assertCanReadCourseContent(Cours cours) {
        Utilisateur utilisateur = getCurrentUser();
        if (isAdmin(utilisateur)) {
            return;
        }
        if (isFormateur(utilisateur) && cours.getFormateur() != null && cours.getFormateur().getId().equals(utilisateur.getId())) {
            return;
        }
        if (isEtudiant(utilisateur) && hasValidEnrollment(utilisateur.getId(), cours.getId())) {
            return;
        }
        throw new AccessDeniedException("Vous n'avez pas acces a ce cours.");
    }

    public void assertCanReadEvaluation(Evaluation evaluation) {
        Utilisateur utilisateur = getCurrentUser();
        if (isAdmin(utilisateur)) {
            return;
        }
        if (isFormateur(utilisateur)
                && evaluation.getLecon() != null
                && evaluation.getLecon().getCours() != null
                && evaluation.getLecon().getCours().getFormateur() != null
                && evaluation.getLecon().getCours().getFormateur().getId().equals(utilisateur.getId())) {
            return;
        }
        if (isEtudiant(utilisateur)
                && Boolean.TRUE.equals(evaluation.getPublie())
                && hasValidEnrollment(utilisateur.getId(), evaluation.getLecon().getCours().getId())) {
            return;
        }
        throw new AccessDeniedException("Vous n'avez pas acces a cette evaluation.");
    }

    public void assertCanSubmitEvaluation(Evaluation evaluation) {
        Utilisateur utilisateur = getCurrentUser();
        if (!isEtudiant(utilisateur)) {
            throw new AccessDeniedException("Seuls les etudiants inscrits peuvent soumettre cette evaluation.");
        }
        if (!Boolean.TRUE.equals(evaluation.getPublie())) {
            throw new AccessDeniedException("Cette evaluation n'est pas encore publiee.");
        }
        if (!hasValidEnrollment(utilisateur.getId(), evaluation.getLecon().getCours().getId())) {
            throw new AccessDeniedException("Vous devez etre inscrit et valide pour passer cette evaluation.");
        }
    }

    private boolean hasValidEnrollment(Long etudiantId, Long coursId) {
        return inscriptionRepository.existsByEtudiantIdAndCoursIdAndStatut(etudiantId, coursId, StatutInscription.VALIDE);
    }

    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Utilisateur non authentifie.");
        }

        String email = authentication.getName();
        if (email == null || email.isBlank() || "anonymousUser".equals(email)) {
            throw new AccessDeniedException("Utilisateur non authentifie.");
        }
        return email;
    }
}
