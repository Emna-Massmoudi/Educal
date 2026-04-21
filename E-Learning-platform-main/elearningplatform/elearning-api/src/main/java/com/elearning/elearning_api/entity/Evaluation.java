package com.elearning.elearning_api.entity;


import com.elearning.elearning_api.enums.TypeEvaluation;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "evaluations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Evaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titre;

    @Enumerated(EnumType.STRING)
    private TypeEvaluation type;

    @Column(nullable = false)
    private Boolean publie = false;

    private Integer noteMax;
    private Integer noteMin;
    private LocalDateTime dateCreation;

    @ManyToOne
    @JoinColumn(name = "lecon_id", nullable = false)
    private Lecon lecon;

    @OneToMany(mappedBy = "evaluation", cascade = CascadeType.ALL)
    private List<Question> questions;

    @OneToMany(mappedBy = "evaluation", cascade = CascadeType.ALL)
    private List<ResultatEvaluation> resultats;

    @PrePersist
    protected void onCreate() {
        this.dateCreation = LocalDateTime.now();
    }
}
