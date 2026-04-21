package com.elearning.elearning_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SousCategorieRequest {

    @NotBlank
    private String nom;

    private String description;

    @NotNull
    private Long categorieId;
}