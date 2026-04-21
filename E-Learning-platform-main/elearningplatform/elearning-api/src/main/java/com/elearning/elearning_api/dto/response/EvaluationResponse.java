package com.elearning.elearning_api.dto.response;

import com.elearning.elearning_api.enums.TypeEvaluation;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EvaluationResponse {
    private Long id;
    private String titre;
    private TypeEvaluation type;
    private Integer noteMax;
    private Integer noteMin;
    private Boolean publie;
    private Integer questionCount;
    private LocalDateTime dateCreation;
    private Long leconId;
    private String leconTitre;
    private Double derniereNote;
    private Integer dernierScore;
    private Integer dernierTotalPoints;
    private Integer dernierPourcentage;
    private Boolean derniereReussite;
    private Integer nombreTentatives;
    private LocalDateTime derniereSoumission;
}
