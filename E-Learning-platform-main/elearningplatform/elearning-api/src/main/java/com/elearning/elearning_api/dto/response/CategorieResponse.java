package com.elearning.elearning_api.dto.response;

import lombok.Data;

@Data
public class CategorieResponse {
    private Long id;
    private String nom;
    private String description;
    private Integer sousCategorieCount;
}
