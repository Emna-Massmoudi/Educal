package com.elearning.elearning_api.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CertificatRequest {

    @NotNull
    private Long etudiantId;

    @NotNull
    private Long coursId;

    private String urlPdf;
}