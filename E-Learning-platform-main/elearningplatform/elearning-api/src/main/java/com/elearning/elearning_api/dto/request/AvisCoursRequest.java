package com.elearning.elearning_api.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AvisCoursRequest {

    @NotNull
    private Long etudiantId;

    @NotNull
    private Long coursId;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer note;

    @Size(max = 1000)
    private String commentaire;
}
