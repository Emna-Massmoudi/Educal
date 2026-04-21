package com.elearning.elearning_api.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "choix")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Choix {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String texte;

    private Boolean estCorrect = false;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;
}