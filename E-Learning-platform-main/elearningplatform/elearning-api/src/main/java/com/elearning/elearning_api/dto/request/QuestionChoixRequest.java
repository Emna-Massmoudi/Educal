package com.elearning.elearning_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class QuestionChoixRequest {

    @NotBlank
    private String texte;

    @NotNull
    private Boolean estCorrect;
}
