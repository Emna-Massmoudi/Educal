package com.elearning.elearning_api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "formateurs")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class Formateur extends Utilisateur {

    private String portfolio;

    private String specialite;

    @Column(columnDefinition = "TEXT")
    private String bio;

    private String cvUrl;
    private String diplomeUrl;
    private String certificatUrl;
    private String attestationUrl;

    @Column(columnDefinition = "TEXT")
    private String motivation;

    @Column(columnDefinition = "TEXT")
    private String commentaireAdmin;
}