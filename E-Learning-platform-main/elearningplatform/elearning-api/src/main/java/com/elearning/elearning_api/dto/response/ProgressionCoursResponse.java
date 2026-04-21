package com.elearning.elearning_api.dto.response;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ProgressionCoursResponse {
    private Long coursId;
    private Integer totalLecons;
    private Integer leconsConsultees;
    private Integer pourcentage;
    private Boolean termine;
    private Long derniereLeconId;
    private String derniereLeconTitre;
    private LocalDateTime dateDerniereConsultation;
    private Long prochaineLeconId;
    private String prochaineLeconTitre;
    private Integer evaluationsPubliees;
    private Integer evaluationsValidees;
    private Boolean certificatDisponible;
    private Long certificatId;
    private String certificatCode;
    private LocalDateTime certificatDateObtention;
}
