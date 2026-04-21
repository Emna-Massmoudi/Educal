package com.elearning.elearning_api.dto.response;

import com.elearning.elearning_api.enums.TypeRessources;
import lombok.Data;

@Data
public class LeconRessourceResponse {
    private Long id;
    private String nom;
    private TypeRessources type;
    private String url;
}
