package com.elearning.elearning_api.entity;
import com.elearning.elearning_api.entity.Categorie;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "sous_categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SousCategorie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    private String description;

    @ManyToOne
    @JoinColumn(name = "categorie_id", nullable = false)
    private Categorie categorie;

    @OneToMany(mappedBy = "sousCategorie", cascade = CascadeType.ALL)
    private List<Cours> cours;
}