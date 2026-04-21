package com.elearning.elearning_api.dto.response;

import com.elearning.elearning_api.enums.StatutInscription;
import lombok.Data;
import java.time.LocalDate;

@Data
public class InscriptionResponse {
    private Long id;
    private LocalDate dateInscription;
    private StatutInscription statut;
    private Long etudiantId;
    private String etudiantNom;
    private Long coursId;
    private String coursTitre;
}