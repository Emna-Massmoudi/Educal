package com.elearning.elearning_api.service;

import com.elearning.elearning_api.dto.request.AvisCoursRequest;
import com.elearning.elearning_api.dto.response.AvisCoursResponse;
import com.elearning.elearning_api.entity.AvisCours;
import com.elearning.elearning_api.entity.Cours;
import com.elearning.elearning_api.entity.Etudiant;
import com.elearning.elearning_api.enums.EtatCours;
import com.elearning.elearning_api.enums.StatutInscription;
import com.elearning.elearning_api.exception.BadRequestException;
import com.elearning.elearning_api.exception.ResourceNotFoundException;
import com.elearning.elearning_api.repository.AvisCoursRepository;
import com.elearning.elearning_api.repository.CoursRepository;
import com.elearning.elearning_api.repository.EtudiantRepository;
import com.elearning.elearning_api.repository.InscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AvisCoursService {

    private final AvisCoursRepository avisCoursRepository;
    private final EtudiantRepository etudiantRepository;
    private final CoursRepository coursRepository;
    private final InscriptionRepository inscriptionRepository;

    public AvisCoursResponse save(AvisCoursRequest request) {
        boolean hasValidInscription = inscriptionRepository.existsByEtudiantIdAndCoursIdAndStatut(
                request.getEtudiantId(),
                request.getCoursId(),
                StatutInscription.VALIDE
        );

        if (!hasValidInscription) {
            throw new BadRequestException("Seul un etudiant inscrit et accepte peut donner un avis sur ce cours.");
        }

        Etudiant etudiant = etudiantRepository.findById(request.getEtudiantId())
                .orElseThrow(() -> new ResourceNotFoundException("Etudiant not found: " + request.getEtudiantId()));
        Cours cours = coursRepository.findById(request.getCoursId())
                .orElseThrow(() -> new ResourceNotFoundException("Cours not found: " + request.getCoursId()));

        AvisCours avis = avisCoursRepository
                .findByEtudiantIdAndCoursId(request.getEtudiantId(), request.getCoursId())
                .orElseGet(AvisCours::new);

        avis.setEtudiant(etudiant);
        avis.setCours(cours);
        avis.setNote(request.getNote());
        avis.setCommentaire(cleanComment(request.getCommentaire()));

        return toResponse(avisCoursRepository.save(avis));
    }

    public List<AvisCoursResponse> getByCours(Long coursId) {
        return avisCoursRepository.findByCoursIdOrderByDateCreationDesc(coursId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<AvisCoursResponse> getPublicByCours(Long coursId) {
        coursRepository.findByIdAndEtatPublication(coursId, EtatCours.PUBLIE)
                .orElseThrow(() -> new ResourceNotFoundException("Published course not found: " + coursId));

        return getByCours(coursId);
    }

    public List<AvisCoursResponse> getByEtudiant(Long etudiantId) {
        return avisCoursRepository.findByEtudiantIdOrderByDateCreationDesc(etudiantId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private String cleanComment(String commentaire) {
        if (commentaire == null) {
            return null;
        }
        String trimmed = commentaire.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private AvisCoursResponse toResponse(AvisCours avis) {
        AvisCoursResponse response = new AvisCoursResponse();
        response.setId(avis.getId());
        response.setEtudiantId(avis.getEtudiant().getId());
        response.setEtudiantNom(avis.getEtudiant().getNom());
        response.setCoursId(avis.getCours().getId());
        response.setCoursTitre(avis.getCours().getTitre());
        response.setNote(avis.getNote());
        response.setCommentaire(avis.getCommentaire());
        response.setDateCreation(avis.getDateCreation());
        response.setDateModification(avis.getDateModification());
        return response;
    }
}
