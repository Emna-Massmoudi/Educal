package com.elearning.elearning_api.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReponseCommentaireResponse {
    private Long id;
    private String description;
    private LocalDateTime dateCreation;
    private Long commentaireId;
    private Long utilisateurId;
    private String utilisateurNom;
}