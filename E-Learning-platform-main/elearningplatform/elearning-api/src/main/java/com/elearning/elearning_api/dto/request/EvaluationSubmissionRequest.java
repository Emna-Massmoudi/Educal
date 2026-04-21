package com.elearning.elearning_api.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

@Data
public class EvaluationSubmissionRequest {

    @Valid
    @NotNull
    private List<EvaluationAnswerRequest> reponses;
}
