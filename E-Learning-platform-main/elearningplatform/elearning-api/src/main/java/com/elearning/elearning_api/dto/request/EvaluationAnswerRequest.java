package com.elearning.elearning_api.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EvaluationAnswerRequest {

    @NotNull
    private Long questionId;

    @NotNull
    private Long choixId;
}
