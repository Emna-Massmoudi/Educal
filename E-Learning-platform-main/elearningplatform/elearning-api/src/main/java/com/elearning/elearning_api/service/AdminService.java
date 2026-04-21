package com.elearning.elearning_api.service;

import com.elearning.elearning_api.dto.response.AdminDashboardOverviewResponse;
import com.elearning.elearning_api.dto.response.AdminDashboardResponse;
import com.elearning.elearning_api.dto.response.CoursResponse;
import com.elearning.elearning_api.dto.response.EtudiantResponse;
import com.elearning.elearning_api.dto.response.FormateurResponse;
import com.elearning.elearning_api.dto.response.PaiementCoursResponse;
import com.elearning.elearning_api.entity.Etudiant;
import com.elearning.elearning_api.entity.Formateur;
import com.elearning.elearning_api.enums.EtatCours;
import com.elearning.elearning_api.enums.StatutPaiement;
import com.elearning.elearning_api.repository.CertificatRepository;
import com.elearning.elearning_api.repository.EtudiantRepository;
import com.elearning.elearning_api.repository.FormateurRepository;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final EtudiantRepository etudiantRepository;
    private final FormateurRepository formateurRepository;
    private final CertificatRepository certificatRepository;
    private final CoursService coursService;
    private final FormateurService formateurService;
    private final PaiementCoursService paiementCoursService;

    public List<EtudiantResponse> getAllEtudiants() {
        return etudiantRepository.findAll()
                .stream()
                .map(e -> new EtudiantResponse(
                        e.getId(),
                        e.getNom(),
                        e.getEmail(),
                        e.getStatus()
                ))
                .toList();
    }

    public List<EtudiantResponse> getEtudiantsBloques() {
        return etudiantRepository.findByStatus("BLOQUE")
                .stream()
                .map(e -> new EtudiantResponse(
                        e.getId(),
                        e.getNom(),
                        e.getEmail(),
                        e.getStatus()
                ))
                .toList();
    }

    public EtudiantResponse bloquerEtudiant(Long id) {
        Etudiant etudiant = etudiantRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Etudiant introuvable : " + id));
        etudiant.setStatus("BLOQUE");
        etudiantRepository.save(etudiant);
        return new EtudiantResponse(etudiant.getId(), etudiant.getNom(), etudiant.getEmail(), etudiant.getStatus());
    }

    public EtudiantResponse debloquerEtudiant(Long id) {
        Etudiant etudiant = etudiantRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Etudiant introuvable : " + id));
        etudiant.setStatus("ACTIF");
        etudiantRepository.save(etudiant);
        return new EtudiantResponse(etudiant.getId(), etudiant.getNom(), etudiant.getEmail(), etudiant.getStatus());
    }

    public List<FormateurResponse> getFormateursBloques() {
        return formateurRepository.findByStatus("BLOQUE")
                .stream()
                .map(this::mapBlockedFormateur)
                .toList();
    }

    public FormateurResponse bloquerFormateur(Long id) {
        Formateur formateur = formateurRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Formateur introuvable : " + id));
        formateur.setStatus("BLOQUE");
        formateurRepository.save(formateur);
        return mapBlockedFormateur(formateur);
    }

    public FormateurResponse debloquerFormateur(Long id) {
        Formateur formateur = formateurRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Formateur introuvable : " + id));
        formateur.setStatus("ACTIF");
        formateurRepository.save(formateur);
        return mapBlockedFormateur(formateur);
    }

    @Transactional(readOnly = true)
    public AdminDashboardResponse getDashboard() {
        List<CoursResponse> allCourses = coursService.getAll();
        List<FormateurResponse> allFormateurs = formateurService.getAllFormateurs();
        List<EtudiantResponse> allEtudiants = getAllEtudiants();
        List<PaiementCoursResponse> allPayments = paiementCoursService.getAll();

        List<CoursResponse> recentCourses = allCourses.stream()
                .sorted((left, right) -> compareDates(right.getDateCreation(), left.getDateCreation()))
                .limit(5)
                .toList();

        List<CoursResponse> topCourses = allCourses.stream()
                .filter(course -> course.getEtatPublication() == EtatCours.PUBLIE)
                .sorted((left, right) -> {
                    int byEnrollments = Integer.compare(nullSafeInt(right.getNombreInscrits()), nullSafeInt(left.getNombreInscrits()));
                    if (byEnrollments != 0) {
                        return byEnrollments;
                    }
                    int byRating = Double.compare(nullSafeDouble(right.getNoteMoyenne()), nullSafeDouble(left.getNoteMoyenne()));
                    if (byRating != 0) {
                        return byRating;
                    }
                    return Long.compare(nullSafeLong(right.getNombreAvis()), nullSafeLong(left.getNombreAvis()));
                })
                .limit(5)
                .toList();

        List<FormateurResponse> pendingFormateurs = allFormateurs.stream()
                .filter(formateur -> isPendingStatus(formateur.getStatus()))
                .sorted((left, right) -> Long.compare(nullSafeLong(right.getId()), nullSafeLong(left.getId())))
                .limit(5)
                .toList();

        List<PaiementCoursResponse> pendingPayments = allPayments.stream()
                .filter(payment -> payment.getStatut() == StatutPaiement.EN_ATTENTE)
                .limit(5)
                .toList();

        AdminDashboardOverviewResponse overview = new AdminDashboardOverviewResponse();
        overview.setTotalCours(allCourses.size());
        overview.setCoursPublies((int) allCourses.stream().filter(course -> course.getEtatPublication() == EtatCours.PUBLIE).count());
        overview.setCoursEnAttenteValidation((int) allCourses.stream().filter(course -> course.getEtatPublication() == EtatCours.EN_ATTENTE_VALIDATION).count());
        overview.setCoursBrouillons((int) allCourses.stream().filter(course -> course.getEtatPublication() == EtatCours.BROUILLON).count());
        overview.setCoursSupprimes((int) allCourses.stream().filter(course -> course.getEtatPublication() == EtatCours.SUPPRIME).count());
        overview.setTotalFormateurs(allFormateurs.size());
        overview.setFormateursActifs((int) allFormateurs.stream().filter(formateur -> isActiveStatus(formateur.getStatus())).count());
        overview.setFormateursEnAttente((int) allFormateurs.stream().filter(formateur -> isPendingStatus(formateur.getStatus())).count());
        overview.setTotalEtudiants(allEtudiants.size());
        overview.setEtudiantsActifs((int) allEtudiants.stream().filter(etudiant -> isActiveStatus(etudiant.getStatus())).count());
        overview.setPaiementsEnAttente((int) allPayments.stream().filter(payment -> payment.getStatut() == StatutPaiement.EN_ATTENTE).count());
        overview.setPaiementsApprouves((int) allPayments.stream().filter(payment -> payment.getStatut() == StatutPaiement.APPROUVE).count());
        overview.setCertificatsGeneres(certificatRepository.count());
        overview.setRevenusPlateforme(sumPayments(allPayments, true));
        overview.setRevenusFormateurs(sumPayments(allPayments, false));

        AdminDashboardResponse response = new AdminDashboardResponse();
        response.setOverview(overview);
        response.setRecentCourses(recentCourses);
        response.setTopCourses(topCourses);
        response.setPendingFormateurs(pendingFormateurs);
        response.setPendingPayments(pendingPayments);
        return response;
    }

    private FormateurResponse mapBlockedFormateur(Formateur formateur) {
        return new FormateurResponse(
                formateur.getId(),
                formateur.getNom(),
                formateur.getEmail(),
                formateur.getStatus(),
                formateur.getPortfolio(),
                formateur.getSpecialite(),
                formateur.getBio(),
                formateur.getCvUrl(),
                formateur.getDiplomeUrl(),
                formateur.getCertificatUrl(),
                formateur.getAttestationUrl(),
                formateur.getMotivation(),
                null
        );
    }

    private BigDecimal sumPayments(List<PaiementCoursResponse> payments, boolean platformShare) {
        return payments.stream()
                .filter(payment -> payment.getStatut() == StatutPaiement.APPROUVE)
                .map(payment -> platformShare ? payment.getCommissionPlateforme() : payment.getMontantFormateur())
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private boolean isActiveStatus(String status) {
        String normalized = normalizeStatus(status);
        return "ACTIVE".equals(normalized) || "ACTIF".equals(normalized);
    }

    private boolean isPendingStatus(String status) {
        return "EN_ATTENTE".equals(normalizeStatus(status));
    }

    private String normalizeStatus(String status) {
        return status == null ? "" : status.trim().toUpperCase(Locale.ROOT);
    }

    private int compareDates(LocalDateTime left, LocalDateTime right) {
        if (left == null && right == null) {
            return 0;
        }
        if (left == null) {
            return -1;
        }
        if (right == null) {
            return 1;
        }
        return left.compareTo(right);
    }

    private int nullSafeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private double nullSafeDouble(Double value) {
        return value == null ? 0.0 : value;
    }

    private long nullSafeLong(Long value) {
        return value == null ? 0L : value;
    }
}
