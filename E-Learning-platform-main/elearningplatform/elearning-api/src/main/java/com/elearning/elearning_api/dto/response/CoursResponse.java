package com.elearning.elearning_api.dto.response;

import com.elearning.elearning_api.enums.EtatCours;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CoursResponse {
    private Long          id;
    private String        titre;
    private String        description;
    private EtatCours     etatPublication;
    private LocalDateTime dateCreation;
    private LocalDateTime datePublication;

    // Formateur
    private Long   formateurId;
    private String formateurNom;
    private String formateurEmail;

    // Catégorie ← obligatoire pour le formulaire de modification
    private Long   categorieId;
    private String categorieNom;

    // Sous-catégorie
    private Long   sousCategorieId;
    private String sousCategorieNom;

    // Infos supplémentaires
    private String  duree;
    private String  niveau;
    private String  imageUrl;
    private String  videoUrl;
    private String  pdfUrl;
    private BigDecimal prix;
    private Integer nombreInscrits;
    private Double noteMoyenne;
    private Long nombreAvis;
}
