package com.elearning.elearning_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FormateurProfileUpdateRequest {

    @NotBlank
    private String nom;

    private String specialite;

    private String bio;

    private String portfolio;
}
