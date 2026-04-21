package com.elearning.elearning_api.dto.response;

import com.elearning.elearning_api.enums.TypeEvaluation;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class EvaluationSubmissionResponse {

    private Long evaluationId;
    private String evaluationTitre;
    private TypeEvaluation type;
    private Integer score;
    private Integer totalPoints;
    private Integer pourcentage;
    private Double noteObtenue;
    private Integer noteMax;
    private Integer noteMin;
    private Boolean reussi;
    private Integer tentativeNumero;
    private LocalDateTime dateSoumission;
}
