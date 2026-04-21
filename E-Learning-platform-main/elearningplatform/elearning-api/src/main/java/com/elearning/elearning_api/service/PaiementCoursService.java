package com.elearning.elearning_api.service;

import com.elearning.elearning_api.dto.request.PaiementCodeRequest;
import com.elearning.elearning_api.dto.response.PaiementCoursResponse;
import com.elearning.elearning_api.dto.response.WalletFormateurResponse;
import com.elearning.elearning_api.entity.Cours;
import com.elearning.elearning_api.entity.Etudiant;
import com.elearning.elearning_api.entity.Inscription;
import com.elearning.elearning_api.entity.PaiementCours;
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
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaiementCoursService {

    private static final BigDecimal PLATFORM_RATE = new BigDecimal("0.20");

    private final PaiementCoursRepository paiementCoursRepository;
    private final InscriptionRepository inscriptionRepository;
    private final EtudiantRepository etudiantRepository;
    private final CoursRepository coursRepository;

    public PaiementCoursResponse soumettreCode(PaiementCodeRequest request) {
        Etudiant etudiant = etudiantRepository.findById(request.getEtudiantId())
                .orElseThrow(() -> new ResourceNotFoundException("Etudiant not found: " + request.getEtudiantId()));
        Cours cours = coursRepository.findById(request.getCoursId())
                .orElseThrow(() -> new ResourceNotFoundException("Cours not found: " + request.getCoursId()));

        BigDecimal prix = normalizeMoney(cours.getPrix());
        if (prix.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Cette formation est gratuite. Utilisez l'inscription classique.");
        }

        String code = normalizeCode(request.getCodePaiement());
        if (code.isBlank()) {
            throw new BadRequestException("Le code de paiement est obligatoire.");
        }
        if (paiementCoursRepository.existsByCodePaiement(code)) {
            throw new AlreadyExistsException("Ce code de paiement est deja utilise.");
        }
        if (paiementCoursRepository.existsByEtudiantIdAndCoursIdAndStatut(etudiant.getId(), cours.getId(), StatutPaiement.EN_ATTENTE)) {
            throw new AlreadyExistsException("Un paiement est deja en attente pour cette formation.");
        }
        if (paiementCoursRepository.existsByEtudiantIdAndCoursIdAndStatut(etudiant.getId(), cours.getId(), StatutPaiement.APPROUVE)) {
            throw new AlreadyExistsException("Cette formation est deja payee.");
        }

        Inscription inscription = getOrCreatePendingInscription(etudiant, cours);

        PaiementCours paiement = new PaiementCours();
        paiement.setCodePaiement(code);
        paiement.setMontant(prix);
        paiement.setCommissionPlateforme(calculateCommission(prix));
        paiement.setMontantFormateur(prix.subtract(paiement.getCommissionPlateforme()).setScale(2, RoundingMode.HALF_UP));
        paiement.setStatut(StatutPaiement.EN_ATTENTE);
        paiement.setEtudiant(etudiant);
        paiement.setCours(cours);
        paiement.setInscription(inscription);

        return toResponse(paiementCoursRepository.save(paiement));
    }

    public List<PaiementCoursResponse> getAll() {
        return paiementCoursRepository.findAllByOrderByDateCreationDesc()
                .stream().map(this::toResponse).toList();
    }

    public List<PaiementCoursResponse> getEnAttente() {
        return paiementCoursRepository.findByStatutOrderByDateCreationDesc(StatutPaiement.EN_ATTENTE)
                .stream().map(this::toResponse).toList();
    }

    public PaiementCoursResponse approuver(Long id) {
        PaiementCours paiement = paiementCoursRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paiement not found: " + id));
        if (paiement.getStatut() == StatutPaiement.APPROUVE) {
            return toResponse(paiement);
        }

        paiement.setStatut(StatutPaiement.APPROUVE);
        paiement.setDateDecision(LocalDateTime.now());
        paiement.getInscription().setStatut(StatutInscription.VALIDE);
        inscriptionRepository.save(paiement.getInscription());
        return toResponse(paiementCoursRepository.save(paiement));
    }

    public PaiementCoursResponse refuser(Long id) {
        PaiementCours paiement = paiementCoursRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paiement not found: " + id));
        if (paiement.getStatut() == StatutPaiement.APPROUVE) {
            throw new BadRequestException("Un paiement deja approuve ne peut pas etre refuse.");
        }

        paiement.setStatut(StatutPaiement.REFUSE);
        paiement.setDateDecision(LocalDateTime.now());
        paiement.getInscription().setStatut(StatutInscription.REFUSE);
        inscriptionRepository.save(paiement.getInscription());
        return toResponse(paiementCoursRepository.save(paiement));
    }

    public WalletFormateurResponse getWalletFormateur(Long formateurId) {
        List<PaiementCours> paiements = paiementCoursRepository
                .findByCoursFormateurIdAndStatutOrderByDateCreationDesc(formateurId, StatutPaiement.APPROUVE);

        BigDecimal totalGagne = paiements.stream()
                .map(PaiementCours::getMontantFormateur)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalCommission = paiements.stream()
                .map(PaiementCours::getCommissionPlateforme)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        return new WalletFormateurResponse(formateurId, totalGagne, totalCommission, paiements.size());
    }

    private Inscription getOrCreatePendingInscription(Etudiant etudiant, Cours cours) {
        return inscriptionRepository.findByEtudiantIdAndCoursId(etudiant.getId(), cours.getId())
                .map(existing -> {
                    if (existing.getStatut() == StatutInscription.VALIDE) {
                        throw new AlreadyExistsException("Etudiant already inscribed in this cours");
                    }
                    if (existing.getStatut() == StatutInscription.ANNULE || existing.getStatut() == StatutInscription.REFUSE) {
                        existing.setStatut(StatutInscription.EN_ATTENTE);
                        return inscriptionRepository.save(existing);
                    }
                    return existing;
                })
                .orElseGet(() -> {
                    Inscription inscription = new Inscription();
                    inscription.setEtudiant(etudiant);
                    inscription.setCours(cours);
                    inscription.setStatut(StatutInscription.EN_ATTENTE);
                    return inscriptionRepository.save(inscription);
                });
    }

    private PaiementCoursResponse toResponse(PaiementCours paiement) {
        PaiementCoursResponse response = new PaiementCoursResponse();
        response.setId(paiement.getId());
        response.setCodePaiement(paiement.getCodePaiement());
        response.setMontant(paiement.getMontant());
        response.setCommissionPlateforme(paiement.getCommissionPlateforme());
        response.setMontantFormateur(paiement.getMontantFormateur());
        response.setStatut(paiement.getStatut());
        response.setDateCreation(paiement.getDateCreation());
        response.setDateDecision(paiement.getDateDecision());
        response.setEtudiantId(paiement.getEtudiant().getId());
        response.setEtudiantNom(paiement.getEtudiant().getNom());
        response.setCoursId(paiement.getCours().getId());
        response.setCoursTitre(paiement.getCours().getTitre());
        response.setInscriptionId(paiement.getInscription().getId());
        if (paiement.getCours().getFormateur() != null) {
            response.setFormateurId(paiement.getCours().getFormateur().getId());
            response.setFormateurNom(paiement.getCours().getFormateur().getNom());
        }
        return response;
    }

    private BigDecimal normalizeMoney(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateCommission(BigDecimal prix) {
        return prix.multiply(PLATFORM_RATE).setScale(2, RoundingMode.HALF_UP);
    }

    private String normalizeCode(String code) {
        return code == null ? "" : code.trim().toUpperCase();
    }
}
