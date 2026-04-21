package com.elearning.elearning_api.service;

import com.elearning.elearning_api.dto.request.FormateurProfileUpdateRequest;
import com.elearning.elearning_api.dto.request.RefusFormateurRequest;
import com.elearning.elearning_api.dto.response.FormateurResponse;
import com.elearning.elearning_api.entity.Formateur;
import com.elearning.elearning_api.repository.FormateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FormateurService {

    private final FormateurRepository formateurRepository;
    private final String uploadDir = "uploads/";

    // ← AJOUTÉ : retourne tous les formateurs
    public List<FormateurResponse> getAllFormateurs() {
        return formateurRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<FormateurResponse> getFormateursEnAttente() {
        return formateurRepository.findByStatus("EN_ATTENTE")
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public FormateurResponse getFormateurById(Long id) {
        return mapToResponse(formateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Formateur introuvable")));
    }

    public FormateurResponse updateFormateurProfile(Long id, FormateurProfileUpdateRequest request) {
        Formateur formateur = formateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Formateur introuvable"));

        formateur.setNom(request.getNom().trim());
        formateur.setSpecialite(trimToNull(request.getSpecialite()));
        formateur.setBio(trimToNull(request.getBio()));
        formateur.setPortfolio(trimToNull(request.getPortfolio()));

        return mapToResponse(formateurRepository.save(formateur));
    }

    public FormateurResponse accepterFormateur(Long id) {
        Formateur f = formateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Formateur introuvable"));
        f.setStatus("ACTIVE");
        f.setCommentaireAdmin("Candidature acceptée");
        return mapToResponse(formateurRepository.save(f));
    }

    public FormateurResponse refuserFormateur(Long id, RefusFormateurRequest request) {
        Formateur f = formateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Formateur introuvable"));
        f.setStatus("REFUSE");
        f.setCommentaireAdmin(request.getCommentaireAdmin());
        return mapToResponse(formateurRepository.save(f));
    }

    public FormateurResponse completerCandidature(Long id, String specialite, String bio,
            String portfolio, String motivation, MultipartFile cv, MultipartFile diplome,
            MultipartFile certificat, MultipartFile attestation) {
        Formateur f = formateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Formateur introuvable"));
        f.setSpecialite(specialite);
        f.setBio(bio);
        f.setPortfolio(portfolio);
        f.setMotivation(motivation);
        f.setStatus("EN_ATTENTE");
        try {
            if (cv          != null && !cv.isEmpty())          f.setCvUrl(saveFile(cv));
            if (diplome     != null && !diplome.isEmpty())     f.setDiplomeUrl(saveFile(diplome));
            if (certificat  != null && !certificat.isEmpty())  f.setCertificatUrl(saveFile(certificat));
            if (attestation != null && !attestation.isEmpty()) f.setAttestationUrl(saveFile(attestation));
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de l'enregistrement des fichiers.");
        }
        return mapToResponse(formateurRepository.save(f));
    }

    private String saveFile(MultipartFile file) throws IOException {
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || !originalFileName.toLowerCase().endsWith(".pdf"))
            throw new RuntimeException("Seuls les fichiers PDF sont autorisés.");
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);
        String fileName = UUID.randomUUID() + "_" + originalFileName;
        Files.copy(file.getInputStream(), uploadPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
        return "/uploads/" + fileName;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private FormateurResponse mapToResponse(Formateur f) {
        return new FormateurResponse(f.getId(), f.getNom(), f.getEmail(), f.getStatus(),
                f.getPortfolio(), f.getSpecialite(), f.getBio(), f.getCvUrl(),
                f.getDiplomeUrl(), f.getCertificatUrl(), f.getAttestationUrl(),
                f.getMotivation(), f.getCommentaireAdmin());
    }
}
