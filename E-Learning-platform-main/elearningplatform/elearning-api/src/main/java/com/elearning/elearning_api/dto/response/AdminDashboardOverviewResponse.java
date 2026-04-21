package com.elearning.elearning_api.dto.response;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class AdminDashboardOverviewResponse {
    private int totalCours;
    private int coursPublies;
    private int coursEnAttenteValidation;
    private int coursBrouillons;
    private int coursSupprimes;
    private int totalFormateurs;
    private int formateursActifs;
    private int formateursEnAttente;
    private int totalEtudiants;
    private int etudiantsActifs;
    private int paiementsEnAttente;
    private int paiementsApprouves;
    private long certificatsGeneres;
    private BigDecimal revenusPlateforme;
    private BigDecimal revenusFormateurs;
}
