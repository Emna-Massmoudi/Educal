package com.elearning.elearning_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EtudiantResponse {
    private Long id;
    private String nom;
    private String email;
    private String status;
}