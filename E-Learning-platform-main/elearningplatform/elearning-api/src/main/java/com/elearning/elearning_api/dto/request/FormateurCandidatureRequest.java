package com.elearning.elearning_api.dto.request;

import lombok.Data;

@Data
public class FormateurCandidatureRequest {
    private String specialite;
    private String bio;
    private String portfolio;
    private String cvUrl;
    private String diplomeUrl;
    private String certificatUrl;
    private String attestationUrl;
    private String motivation;
}