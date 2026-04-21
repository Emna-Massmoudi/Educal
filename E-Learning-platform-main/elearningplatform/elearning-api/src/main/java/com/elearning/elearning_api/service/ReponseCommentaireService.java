package com.elearning.elearning_api.service;

import com.elearning.elearning_api.dto.request.ReponseCommentaireRequest;
import com.elearning.elearning_api.dto.response.ReponseCommentaireResponse;
import com.elearning.elearning_api.entity.Commentaire;
import com.elearning.elearning_api.entity.ReponseCommentaire;
import com.elearning.elearning_api.entity.Utilisateur;
import com.elearning.elearning_api.exception.ResourceNotFoundException;
import com.elearning.elearning_api.repository.CommentaireRepository;
import com.elearning.elearning_api.repository.ReponseCommentaireRepository;
import com.elearning.elearning_api.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReponseCommentaireService {

    private final ReponseCommentaireRepository reponseCommentaireRepository;
    private final CommentaireRepository commentaireRepository;
    private final UtilisateurRepository utilisateurRepository;

    public ReponseCommentaireResponse create(ReponseCommentaireRequest request) {
        Commentaire commentaire = commentaireRepository.findById(request.getCommentaireId())
                .orElseThrow(() -> new ResourceNotFoundException("Commentaire not found: " + request.getCommentaireId()));

        Utilisateur utilisateur = utilisateurRepository.findById(request.getUtilisateurId())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur not found: " + request.getUtilisateurId()));

        ReponseCommentaire reponse = new ReponseCommentaire();
        reponse.setDescription(request.getDescription());
        reponse.setCommentaire(commentaire);
        reponse.setUtilisateur(utilisateur);

        return toResponse(reponseCommentaireRepository.save(reponse));
    }

    public void delete(Long id) {
        if (!reponseCommentaireRepository.existsById(id))
            throw new ResourceNotFoundException("ReponseCommentaire not found: " + id);
        reponseCommentaireRepository.deleteById(id);
    }

    public List<ReponseCommentaireResponse> getByCommentaire(Long commentaireId) {
        return reponseCommentaireRepository.findByCommentaireId(commentaireId)
                .stream().map(this::toResponse).toList();
    }

    private ReponseCommentaireResponse toResponse(ReponseCommentaire reponse) {
        ReponseCommentaireResponse response = new ReponseCommentaireResponse();
        response.setId(reponse.getId());
        response.setDescription(reponse.getDescription());
        response.setDateCreation(reponse.getDateCreation());
        response.setUtilisateurNom(reponse.getUtilisateur().getNom());
        return response;
    }
}