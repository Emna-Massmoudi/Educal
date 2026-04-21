package com.elearning.elearning_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private Long id;
    private String token;
    private String role;
    private String email;
    private String nom;
    private String status;
}