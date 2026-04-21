package com.elearning.elearning_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class LeconRequest {

    @NotNull
    private Integer ordre;

    @NotBlank
    private String titre;

    private String description;
    private String contenuHtml;
    private String pdfUrl;
    private List<LeconRessourceRequest> ressources = new ArrayList<>();

    @NotNull
    private Long coursId;
}
