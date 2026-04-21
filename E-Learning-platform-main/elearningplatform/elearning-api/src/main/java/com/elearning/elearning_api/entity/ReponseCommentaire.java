package com.elearning.elearning_api.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reponses_commentaires")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReponseCommentaire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    private LocalDateTime dateCreation;

    @ManyToOne
    @JoinColumn(name = "commentaire_id", nullable = false)
    private Commentaire commentaire;

    @ManyToOne
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    @PrePersist
    protected void onCreate() {
        this.dateCreation = LocalDateTime.now();
    }
}