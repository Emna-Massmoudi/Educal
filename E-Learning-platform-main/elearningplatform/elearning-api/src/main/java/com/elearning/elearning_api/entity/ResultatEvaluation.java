package com.elearning.elearning_api.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "resultats_evaluations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResultatEvaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer score;
    private Integer totalPoints;
    private Integer pourcentage;
    private Double noteObtenue;
    private Boolean reussi;
    private Integer tentativeNumero;
    private LocalDateTime dateSoumission;

    @ManyToOne
    @JoinColumn(name = "evaluation_id", nullable = false)
    private Evaluation evaluation;

    @ManyToOne
    @JoinColumn(name = "etudiant_id", nullable = false)
    private Etudiant etudiant;

    @PrePersist
    protected void onCreate() {
        this.dateSoumission = LocalDateTime.now();
    }
}
