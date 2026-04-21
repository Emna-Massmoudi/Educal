package com.elearning.elearning_api.entity;


import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "lecons")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Lecon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer ordre;

    @Column(nullable = false)
    private String titre;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "LONGTEXT")
    private String contenuHtml;

    @ManyToOne
    @JoinColumn(name = "cours_id", nullable = false)
    private Cours cours;

    @OneToMany(mappedBy = "lecon", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Ressources> ressources;

    @OneToMany(mappedBy = "lecon", cascade = CascadeType.ALL)
    private List<Evaluation> evaluations;
}
