package com.elearning.elearning_api.dto.response;

import lombok.Data;

@Data
public class SousCategorieResponse {
    private Long id;
    private String nom;
    private String description;
    private Long categorieId;
    private String categorieNom;
}