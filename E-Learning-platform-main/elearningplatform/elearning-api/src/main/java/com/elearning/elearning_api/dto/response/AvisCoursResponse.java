package com.elearning.elearning_api.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AvisCoursResponse {
    private Long id;
    private Long etudiantId;
    private String etudiantNom;
    private Long coursId;
    private String coursTitre;
    private Integer note;
    private String commentaire;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
}
