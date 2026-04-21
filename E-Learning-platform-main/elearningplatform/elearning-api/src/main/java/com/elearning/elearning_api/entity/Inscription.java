package com.elearning.elearning_api.entity;


import com.elearning.elearning_api.enums.StatutInscription;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "inscriptions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Inscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate dateInscription;

    @Enumerated(EnumType.STRING)
    private StatutInscription statut = StatutInscription.EN_ATTENTE;

    @ManyToOne
    @JoinColumn(name = "etudiant_id", nullable = false)
    private Etudiant etudiant;

    @ManyToOne
    @JoinColumn(name = "cours_id", nullable = false)
    private Cours cours;

    @PrePersist
    protected void onCreate() {
        this.dateInscription = LocalDate.now();
    }
}