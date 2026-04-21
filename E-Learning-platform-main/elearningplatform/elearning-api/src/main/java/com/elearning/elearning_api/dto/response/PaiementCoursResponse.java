package com.elearning.elearning_api.dto.response;

import com.elearning.elearning_api.enums.StatutPaiement;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaiementCoursResponse {
    private Long id;
    private String codePaiement;
    private BigDecimal montant;
    private BigDecimal commissionPlateforme;
    private BigDecimal montantFormateur;
    private StatutPaiement statut;
    private LocalDateTime dateCreation;
    private LocalDateTime dateDecision;
    private Long etudiantId;
    private String etudiantNom;
    private Long coursId;
    private String coursTitre;
    private Long formateurId;
    private String formateurNom;
    private Long inscriptionId;
}
