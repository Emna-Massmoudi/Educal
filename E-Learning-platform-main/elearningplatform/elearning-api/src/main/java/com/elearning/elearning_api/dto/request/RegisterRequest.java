package com.elearning.elearning_api.dto.request;

import com.elearning.elearning_api.enums.Role;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank
    private String nom;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 6)
    private String motDePasse;

    private Role role;
    private String portfolio;
}