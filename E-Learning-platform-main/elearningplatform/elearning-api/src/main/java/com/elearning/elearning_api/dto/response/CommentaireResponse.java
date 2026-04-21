package com.elearning.elearning_api.dto.response;

import com.elearning.elearning_api.enums.StatutCommentaire;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CommentaireResponse {
    private Long id;
    private String description;
    private LocalDateTime dateCreation;
    private StatutCommentaire status;
    private Long etudiantId;
    private String etudiantNom;
    private Long coursId;
    private String coursTitre;
}