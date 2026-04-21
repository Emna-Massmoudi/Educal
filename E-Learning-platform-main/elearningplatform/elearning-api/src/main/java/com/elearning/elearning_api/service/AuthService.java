package com.elearning.elearning_api.service;

import com.elearning.elearning_api.dto.request.LoginRequest;
import com.elearning.elearning_api.dto.request.RegisterRequest;
import com.elearning.elearning_api.dto.response.AuthResponse;
import com.elearning.elearning_api.entity.Etudiant;
import com.elearning.elearning_api.entity.Formateur;
import com.elearning.elearning_api.entity.Utilisateur;
import com.elearning.elearning_api.enums.Role;
import com.elearning.elearning_api.exception.AlreadyExistsException;
import com.elearning.elearning_api.repository.UtilisateurRepository;
import com.elearning.elearning_api.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        if (utilisateurRepository.existsByEmail(request.getEmail())) {
            throw new AlreadyExistsException("Email already in use: " + request.getEmail());
        }

        Utilisateur utilisateur;

        if (request.getRole() == Role.FORMATEUR) {
            Formateur formateur = new Formateur();
            formateur.setPortfolio(request.getPortfolio());
            utilisateur = formateur;
        } else if (request.getRole() == Role.ETUDIANT) {
            utilisateur = new Etudiant();
        } else {
            utilisateur = new Utilisateur();
        }

        utilisateur.setNom(request.getNom());
        utilisateur.setEmail(request.getEmail());
        utilisateur.setMotDePasse(passwordEncoder.encode(request.getMotDePasse()));
        utilisateur.setRole(request.getRole());

        if (request.getRole() == Role.FORMATEUR) {
            utilisateur.setStatus("EN_ATTENTE");
        } else {
            utilisateur.setStatus("ACTIVE");
        }

        utilisateurRepository.save(utilisateur);

        String token = jwtUtil.generateToken(utilisateur.getEmail(), utilisateur.getRole().name());

        return new AuthResponse(
                utilisateur.getId(),
                token,
                utilisateur.getRole().name(),
                utilisateur.getEmail(),
                utilisateur.getNom(),
                utilisateur.getStatus()
        );
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getMotDePasse()
                )
        );

        Utilisateur utilisateur = utilisateurRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtUtil.generateToken(utilisateur.getEmail(), utilisateur.getRole().name());

        return new AuthResponse(
                utilisateur.getId(),
                token,
                utilisateur.getRole().name(),
                utilisateur.getEmail(),
                utilisateur.getNom(),
                utilisateur.getStatus()
        );
    }
}