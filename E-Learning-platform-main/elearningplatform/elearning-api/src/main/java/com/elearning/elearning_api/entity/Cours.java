package com.elearning.elearning_api.entity;


import com.elearning.elearning_api.enums.EtatCours;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "cours")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cours {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titre;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private EtatCours etatPublication = EtatCours.BROUILLON;

    private LocalDateTime dateCreation;
    private LocalDateTime datePublication;
    
    private String imageUrl;   
    private String videoUrl;   
    private String pdfUrl;     
    private String duree;      
    private String niveau;

    @Column(precision = 10, scale = 2)
    private BigDecimal prix = BigDecimal.ZERO;
    
    @ManyToOne
    @JoinColumn(name = "formateur_id", nullable = false)
    private Formateur formateur;

    @ManyToOne
    @JoinColumn(name = "sous_categorie_id")
    private SousCategorie sousCategorie;

    @OneToMany(mappedBy = "cours", cascade = CascadeType.ALL)
    private List<Lecon> lecons;

    @OneToMany(mappedBy = "cours", cascade = CascadeType.ALL)
    private List<Inscription> inscriptions;

    @PrePersist
    protected void onCreate() {
        this.dateCreation = LocalDateTime.now();
    }
}
