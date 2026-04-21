package com.elearning.elearning_api.entity;



import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "etudiants")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class Etudiant extends Utilisateur {

}