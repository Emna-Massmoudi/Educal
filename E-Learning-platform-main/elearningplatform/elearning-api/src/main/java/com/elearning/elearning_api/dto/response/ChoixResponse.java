package com.elearning.elearning_api.dto.response;

import lombok.Data;

@Data
public class ChoixResponse {
    private Long id;
    private String texte;
    private Boolean estCorrect;
    private Long questionId;
}