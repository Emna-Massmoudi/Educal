package com.elearning.elearning_api.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CertificatResponse {
    private Long id;
    private String code;
    private LocalDateTime dateObtention;
    private String urlPdf;
    private Long etudiantId;
    private String etudiantNom;
    private Long coursId;
    private String coursTitre;
}