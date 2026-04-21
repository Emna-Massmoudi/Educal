package com.elearning.elearning_api.entity;


import com.elearning.elearning_api.enums.StatutCommentaire;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "commentaires")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Commentaire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    private LocalDateTime dateCreation;

    @Enumerated(EnumType.STRING)
    private StatutCommentaire status = StatutCommentaire.PUBLIE;

    @ManyToOne
    @JoinColumn(name = "etudiant_id", nullable = false)
    private Etudiant etudiant;

    @ManyToOne
    @JoinColumn(name = "cours_id", nullable = false)
    private Cours cours;

    @OneToMany(mappedBy = "commentaire", cascade = CascadeType.ALL)
    private List<ReponseCommentaire> reponses;

    @PrePersist
    protected void onCreate() {
        this.dateCreation = LocalDateTime.now();
    }
}