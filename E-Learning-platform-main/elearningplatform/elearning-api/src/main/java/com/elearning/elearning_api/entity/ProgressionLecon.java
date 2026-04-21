package com.elearning.elearning_api.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "progressions_lecons",
        uniqueConstraints = @UniqueConstraint(columnNames = {"etudiant_id", "lecon_id"})
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProgressionLecon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime datePremiereConsultation;

    private LocalDateTime dateDerniereConsultation;

    @ManyToOne
    @JoinColumn(name = "etudiant_id", nullable = false)
    private Etudiant etudiant;

    @ManyToOne
    @JoinColumn(name = "lecon_id", nullable = false)
    private Lecon lecon;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.datePremiereConsultation = now;
        this.dateDerniereConsultation = now;
    }
}
