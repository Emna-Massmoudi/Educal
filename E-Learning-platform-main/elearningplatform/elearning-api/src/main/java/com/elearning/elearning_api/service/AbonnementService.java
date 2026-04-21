package com.elearning.elearning_api.service;

import com.elearning.elearning_api.dto.request.AbonnementRequest;
import com.elearning.elearning_api.dto.response.AbonnementResponse;
import com.elearning.elearning_api.entity.Abonnement;
import com.elearning.elearning_api.entity.Etudiant;
import com.elearning.elearning_api.enums.StatutAbonnement;
import com.elearning.elearning_api.exception.ResourceNotFoundException;
import com.elearning.elearning_api.repository.AbonnementRepository;
import com.elearning.elearning_api.repository.EtudiantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AbonnementService {

    private final AbonnementRepository abonnementRepository;
    private final EtudiantRepository etudiantRepository;

    public AbonnementResponse create(AbonnementRequest request) {
        Etudiant etudiant = etudiantRepository.findById(request.getEtudiantId())
                .orElseThrow(() -> new ResourceNotFoundException("Etudiant not found: " + request.getEtudiantId()));

        Abonnement abonnement = new Abonnement();
        abonnement.setType(request.getType());
        abonnement.setPrix(request.getPrix());
        abonnement.setDateDebut(request.getDateDebut());
        abonnement.setDateFin(request.getDateFin());
        abonnement.setEtudiant(etudiant);
        abonnement.setStatus(StatutAbonnement.EN_ATTENTE);

        return toResponse(abonnementRepository.save(abonnement));
    }

    public AbonnementResponse updateStatut(Long id, StatutAbonnement statut) {
        Abonnement existing = abonnementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Abonnement not found: " + id));
        existing.setStatus(statut);
        return toResponse(abonnementRepository.save(existing));
    }

    public void delete(Long id) {
        if (!abonnementRepository.existsById(id))
            throw new ResourceNotFoundException("Abonnement not found: " + id);
        abonnementRepository.deleteById(id);
    }

    public AbonnementResponse getById(Long id) {
        return abonnementRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Abonnement not found: " + id));
    }

    public List<AbonnementResponse> getByEtudiant(Long etudiantId) {
        return abonnementRepository.findByEtudiantId(etudiantId)
                .stream().map(this::toResponse).toList();
    }

    private AbonnementResponse toResponse(Abonnement abonnement) {
        AbonnementResponse response = new AbonnementResponse();
        response.setId(abonnement.getId());
        response.setType(abonnement.getType());
        response.setPrix(abonnement.getPrix());
        response.setDateDebut(abonnement.getDateDebut());
        response.setDateFin(abonnement.getDateFin());
        response.setStatus(abonnement.getStatus());
        response.setEtudiantNom(abonnement.getEtudiant().getNom());
        return response;
    }
}