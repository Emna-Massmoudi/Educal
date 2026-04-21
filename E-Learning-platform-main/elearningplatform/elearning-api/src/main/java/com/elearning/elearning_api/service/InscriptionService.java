package com.elearning.elearning_api.service;

import com.elearning.elearning_api.dto.request.InscriptionRequest;
import com.elearning.elearning_api.dto.response.InscriptionResponse;
import com.elearning.elearning_api.entity.Cours;
import com.elearning.elearning_api.entity.Etudiant;
import com.elearning.elearning_api.entity.Inscription;
import com.elearning.elearning_api.enums.StatutInscription;
import com.elearning.elearning_api.enums.StatutPaiement;
import com.elearning.elearning_api.exception.AlreadyExistsException;
import com.elearning.elearning_api.exception.BadRequestException;
import com.elearning.elearning_api.exception.ResourceNotFoundException;
import com.elearning.elearning_api.repository.CoursRepository;
import com.elearning.elearning_api.repository.EtudiantRepository;
import com.elearning.elearning_api.repository.InscriptionRepository;
import com.elearning.elearning_api.repository.PaiementCoursRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InscriptionService {

    private final InscriptionRepository inscriptionRepository;
    private final EtudiantRepository    etudiantRepository;
    private final CoursRepository       coursRepository;
    private final PaiementCoursRepository paiementCoursRepository;

    public InscriptionResponse inscrire(InscriptionRequest request) {

        // Chercher une inscription existante (peu importe le statut)
        Optional<Inscription> existing = inscriptionRepository
                .findByEtudiantIdAndCoursId(request.getEtudiantId(), request.getCoursId());

        if (existing.isPresent()) {
            Inscription insc = existing.get();

            // Si annulée ou refusée → réactiver en EN_ATTENTE
            if (insc.getStatut() == StatutInscription.ANNULE
             || insc.getStatut() == StatutInscription.REFUSE) {
                insc.setStatut(StatutInscription.EN_ATTENTE);
                return toResponse(inscriptionRepository.save(insc));
            }

            // Sinon déjà active → refuser
            throw new AlreadyExistsException("Etudiant already inscribed in this cours");
        }

        // Pas d'inscription existante → créer
        Etudiant etudiant = etudiantRepository.findById(request.getEtudiantId())
                .orElseThrow(() -> new ResourceNotFoundException("Etudiant not found: " + request.getEtudiantId()));
        Cours cours = coursRepository.findById(request.getCoursId())
                .orElseThrow(() -> new ResourceNotFoundException("Cours not found: " + request.getCoursId()));

        Inscription inscription = new Inscription();
        inscription.setEtudiant(etudiant);
        inscription.setCours(cours);
        inscription.setStatut(StatutInscription.EN_ATTENTE);
        return toResponse(inscriptionRepository.save(inscription));
    }

    public InscriptionResponse updateStatut(Long id, StatutInscription statut) {
        Inscription inscription = inscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inscription not found: " + id));
        if (statut == StatutInscription.VALIDE
                && isCoursPayant(inscription.getCours())
                && !paiementCoursRepository.existsByInscriptionIdAndStatut(inscription.getId(), StatutPaiement.APPROUVE)) {
            throw new BadRequestException("Cette inscription payante doit etre validee depuis le paiement approuve.");
        }
        inscription.setStatut(statut);
        return toResponse(inscriptionRepository.save(inscription));
    }

    public List<InscriptionResponse> getByEtudiant(Long etudiantId) {
        return inscriptionRepository.findByEtudiantId(etudiantId)
                .stream().map(this::toResponse).toList();
    }

    public List<InscriptionResponse> getByCours(Long coursId) {
        return inscriptionRepository.findByCoursId(coursId)
                .stream().map(this::toResponse).toList();
    }

    public void annuler(Long id) {
        updateStatut(id, StatutInscription.ANNULE);
    }

    private InscriptionResponse toResponse(Inscription inscription) {
        InscriptionResponse response = new InscriptionResponse();
        response.setId(inscription.getId());
        response.setDateInscription(inscription.getDateInscription());
        response.setStatut(inscription.getStatut());
        response.setEtudiantId(inscription.getEtudiant().getId());
        response.setEtudiantNom(inscription.getEtudiant().getNom());
        response.setCoursId(inscription.getCours().getId());
        response.setCoursTitre(inscription.getCours().getTitre());
        return response;
    }

    private boolean isCoursPayant(Cours cours) {
        return cours.getPrix() != null && cours.getPrix().compareTo(BigDecimal.ZERO) > 0;
    }
}
