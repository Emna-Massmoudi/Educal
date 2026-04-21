package com.elearning.elearning_api.entity;

import com.elearning.elearning_api.enums.StatutPaiement;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "paiements_cours")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaiementCours {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String codePaiement;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal montant;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal commissionPlateforme;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal montantFormateur;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutPaiement statut = StatutPaiement.EN_ATTENTE;

    private LocalDateTime dateCreation;
    private LocalDateTime dateDecision;

    @ManyToOne(optional = false)
    @JoinColumn(name = "etudiant_id", nullable = false)
    private Etudiant etudiant;

    @ManyToOne(optional = false)
    @JoinColumn(name = "cours_id", nullable = false)
    private Cours cours;

    @ManyToOne(optional = false)
    @JoinColumn(name = "inscription_id", nullable = false)
    private Inscription inscription;

    @PrePersist
    protected void onCreate() {
        this.dateCreation = LocalDateTime.now();
    }
}
