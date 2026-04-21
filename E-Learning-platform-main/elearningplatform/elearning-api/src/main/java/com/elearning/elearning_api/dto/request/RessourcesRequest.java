package com.elearning.elearning_api.dto.request;

import com.elearning.elearning_api.enums.TypeRessources;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RessourcesRequest {

    @NotBlank
    private String nom;

    @NotNull
    private TypeRessources type;

    private String url;

    @NotNull
    private Long leconId;
}