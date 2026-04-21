package com.elearning.elearning_api.dto.request;

import com.elearning.elearning_api.enums.TypeEvaluation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EvaluationRequest {

    @NotBlank
    private String titre;

    @NotNull
    private TypeEvaluation type;

    @NotNull
    private Integer noteMax;

    @NotNull
    private Integer noteMin;

    @NotNull
    private Long leconId;
}