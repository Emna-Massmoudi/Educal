package com.elearning.elearning_api.service;

import com.elearning.elearning_api.dto.request.CommentaireRequest;
import com.elearning.elearning_api.dto.response.CommentaireResponse;
import com.elearning.elearning_api.entity.Commentaire;
import com.elearning.elearning_api.entity.Cours;
import com.elearning.elearning_api.entity.Etudiant;
import com.elearning.elearning_api.enums.StatutCommentaire;
import com.elearning.elearning_api.exception.ResourceNotFoundException;
import com.elearning.elearning_api.repository.CommentaireRepository;
import com.elearning.elearning_api.repository.CoursRepository;
import com.elearning.elearning_api.repository.EtudiantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentaireService {

    private final CommentaireRepository commentaireRepository;
    private final EtudiantRepository etudiantRepository;
    private final CoursRepository coursRepository;

    public CommentaireResponse create(CommentaireRequest request) {
        Etudiant etudiant = etudiantRepository.findById(request.getEtudiantId())
                .orElseThrow(() -> new ResourceNotFoundException("Etudiant not found: " + request.getEtudiantId()));

        Cours cours = coursRepository.findById(request.getCoursId())
                .orElseThrow(() -> new ResourceNotFoundException("Cours not found: " + request.getCoursId()));

        Commentaire commentaire = new Commentaire();
        commentaire.setDescription(request.getDescription());
        commentaire.setEtudiant(etudiant);
        commentaire.setCours(cours);
        commentaire.setStatus(StatutCommentaire.PUBLIE);

        return toResponse(commentaireRepository.save(commentaire));
    }

    public CommentaireResponse updateStatut(Long id, StatutCommentaire statut) {
        Commentaire existing = commentaireRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commentaire not found: " + id));
        existing.setStatus(statut);
        return toResponse(commentaireRepository.save(existing));
    }

    public void delete(Long id) {
        if (!commentaireRepository.existsById(id))
            throw new ResourceNotFoundException("Commentaire not found: " + id);
        commentaireRepository.deleteById(id);
    }

    public CommentaireResponse getById(Long id) {
        return commentaireRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Commentaire not found: " + id));
    }

    public List<CommentaireResponse> getByCours(Long coursId) {
        return commentaireRepository.findByCoursId(coursId)
                .stream().map(this::toResponse).toList();
    }

    public List<CommentaireResponse> getByEtudiant(Long etudiantId) {
        return commentaireRepository.findByEtudiantId(etudiantId)
                .stream().map(this::toResponse).toList();
    }

    private CommentaireResponse toResponse(Commentaire commentaire) {
        CommentaireResponse response = new CommentaireResponse();
        response.setId(commentaire.getId());
        response.setDescription(commentaire.getDescription());
        response.setDateCreation(commentaire.getDateCreation());
        response.setStatus(commentaire.getStatus());
        response.setEtudiantNom(commentaire.getEtudiant().getNom());
        response.setCoursTitre(commentaire.getCours().getTitre());
        return response;
    }
}