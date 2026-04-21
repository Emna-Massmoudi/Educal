package com.elearning.elearning_api.dto.request;

import com.elearning.elearning_api.enums.TypeRessources;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LeconRessourceRequest {

    @NotBlank
    private String nom;

    @NotNull
    private TypeRessources type;

    @NotBlank
    private String url;
}
