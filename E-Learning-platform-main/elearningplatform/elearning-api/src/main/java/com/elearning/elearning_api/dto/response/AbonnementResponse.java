package com.elearning.elearning_api.dto.response;

import com.elearning.elearning_api.enums.StatutAbonnement;
import lombok.Data;
import java.time.LocalDate;

@Data
public class AbonnementResponse {
    private Long id;
    private String type;
    private Float prix;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private StatutAbonnement status;
    private Long etudiantId;
    private String etudiantNom;
}